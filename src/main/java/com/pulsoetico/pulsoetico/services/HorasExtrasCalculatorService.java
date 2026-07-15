package com.pulsoetico.pulsoetico.services;

import com.pulsoetico.pulsoetico.models.RegistroPonto;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.repositories.RegistroPontoRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calcula a média de horas extras semanais de um setor, direto a partir dos
 * registros de ponto (RegistroPonto) — sem nenhuma digitação manual do RH.
 *
 * Lógica: para cada funcionário, o tempo trabalhado é apurado DIA A DIA
 * (pares ENTRADA→SAIDA agrupados pelo dia da ENTRADA), e a hora extra de
 * cada dia é calculada segundo o dia da semana:
 * - Segunda a sexta: jornada padrão de 8h/dia; o que exceder 8h é hora extra.
 * - Sábado e domingo: não há jornada padrão; tudo o que for trabalhado é hora extra.
 * A média do setor é a média do total de horas extras (no período) entre
 * todos os funcionários, normalizada para "por semana".
 */
@Service
public class HorasExtrasCalculatorService {

    /** Jornada diária padrão CLT para dias úteis (segunda a sexta). */
    private static final double HORAS_DIARIAS_PADRAO = 8.0;

    private static final ZoneId ZONA = ZoneId.systemDefault();

    private final RegistroPontoRepository registroPontoRepository;

    public HorasExtrasCalculatorService(RegistroPontoRepository registroPontoRepository) {
        this.registroPontoRepository = registroPontoRepository;
    }

    /** Retorna a média de horas extras POR SEMANA do setor, no período informado. */
    public double calcularMediaHorasExtrasSemana(Setor setor, Instant inicio, Instant fim) {
        List<RegistroPonto> registros = registroPontoRepository
                .findBySetorAndHorarioBetween(setor, inicio, fim);

        if (registros.isEmpty()) {
            return 0.0;
        }

        double semanasNoPeriodo = Duration.between(inicio, fim).toHours() / (24.0 * 7.0);
        if (semanasNoPeriodo <= 0) {
            semanasNoPeriodo = 1.0;
        }

        Map<Long, List<RegistroPonto>> registrosPorFuncionario = registros.stream()
                .collect(Collectors.groupingBy(r -> r.getFuncionario().getId()));

        double somaHorasExtrasPorFuncionario = 0.0;
        int quantidadeFuncionarios = registrosPorFuncionario.size();

        for (List<RegistroPonto> registrosDoFuncionario : registrosPorFuncionario.values()) {
            somaHorasExtrasPorFuncionario += calcularHorasExtrasFuncionario(registrosDoFuncionario);
        }

        double mediaExtrasNoPeriodo = somaHorasExtrasPorFuncionario / quantidadeFuncionarios;

        // Normaliza para "por semana", já que o índice de risco trabalha em horas extras/semana.
        return mediaExtrasNoPeriodo / semanasNoPeriodo;
    }

    /**
     * Soma as horas extras de um funcionário, dia a dia.
     * Primeiro pareia ENTRADA→SAIDA (registros incompletos são ignorados),
     * depois agrupa as horas trabalhadas por dia (data da ENTRADA) e aplica
     * a regra de excedente correspondente ao dia da semana.
     *
     * Não é mais privado: reaproveitado pelo JornadaAnalyticsService pra
     * somar horas extras da empresa toda no mês (em vez de só por setor).
     */
    double calcularHorasExtrasFuncionario(List<RegistroPonto> registros) {
        Map<LocalDate, Double> horasTrabalhadasPorDia = calcularHorasTrabalhadasPorDia(registros);

        double totalHorasExtras = 0.0;

        for (Map.Entry<LocalDate, Double> entrada : horasTrabalhadasPorDia.entrySet()) {
            LocalDate dia = entrada.getKey();
            double horasTrabalhadasNoDia = entrada.getValue();
            DayOfWeek diaDaSemana = dia.getDayOfWeek();

            double horasExtrasNoDia;
            if (diaDaSemana == DayOfWeek.SATURDAY || diaDaSemana == DayOfWeek.SUNDAY) {
                // Sábado e domingo: tudo o que for trabalhado é hora extra.
                horasExtrasNoDia = horasTrabalhadasNoDia;
            } else {
                // Segunda a sexta: excedente sobre a jornada padrão de 8h.
                horasExtrasNoDia = Math.max(0, horasTrabalhadasNoDia - HORAS_DIARIAS_PADRAO);
            }

            totalHorasExtras += horasExtrasNoDia;
        }

        return totalHorasExtras;
    }

    /**
     * Pareia ENTRADA→SAIDA em ordem cronológica e agrupa o tempo trabalhado
     * por dia, usando a data da ENTRADA como referência do dia (ex: um turno
     * que vira a noite conta inteiro no dia em que começou).
     *
     * Não é mais privado: o JornadaAnalyticsService reaproveita essa mesma
     * lógica de pareamento pros indicadores da tela de Ponto & Jornada,
     * pra não duplicar essa regra em dois lugares.
     */
    Map<LocalDate, Double> calcularHorasTrabalhadasPorDia(List<RegistroPonto> registros) {
        List<RegistroPonto> ordenados = registros.stream()
                .sorted(Comparator.comparing(RegistroPonto::getHorario))
                .toList();

        Map<LocalDate, Double> horasPorDia = new java.util.HashMap<>();
        Instant aberturaTurno = null;

        for (RegistroPonto registro : ordenados) {
            switch (registro.getTipo()) {
                case ENTRADA -> aberturaTurno = registro.getHorario();
                case SAIDA -> {
                    if (aberturaTurno != null) {
                        double horas = Duration.between(aberturaTurno, registro.getHorario()).toMinutes() / 60.0;
                        LocalDate diaDoTurno = LocalDate.ofInstant(aberturaTurno, ZONA);
                        horasPorDia.merge(diaDoTurno, horas, Double::sum);
                        aberturaTurno = null;
                    }
                }
            }
        }

        return horasPorDia;
    }
}