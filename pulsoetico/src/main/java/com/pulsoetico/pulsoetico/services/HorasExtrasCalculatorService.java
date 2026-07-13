package com.pulsoetico.pulsoetico.services;

import com.pulsoetico.pulsoetico.models.RegistroPonto;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.repositories.RegistroPontoRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calcula a média de horas extras semanais de um setor, direto a partir dos
 * registros de ponto (RegistroPonto) — sem nenhuma digitação manual do RH.
 *
 * Lógica: soma o tempo trabalhado de cada funcionário no período (pares
 * ENTRADA→INICIO_INTERVALO e FIM_INTERVALO→SAIDA), compara com a jornada
 * padrão esperada (44h/semana, CLT) e calcula o excedente (hora extra).
 * A média do setor é a média desse excedente entre todos os funcionários.
 */
@Service
public class HorasExtrasCalculatorService {

    /** Jornada semanal padrão CLT. Ajustável se o setor tiver regime diferente. */
    private static final double HORAS_SEMANAIS_PADRAO = 44.0;

    private final RegistroPontoRepository registroPontoRepository;

    public HorasExtrasCalculatorService(RegistroPontoRepository registroPontoRepository) {
        this.registroPontoRepository = registroPontoRepository;
    }

    /** Retorna a média de horas extras POR SEMANA do setor, no período informado. */
    public double calcularMediaHorasExtrasSemana(Setor setor, Instant inicio, Instant fim) {
        List<RegistroPonto> registros = registroPontoRepository
                .findByFuncionario_SetorAndHorarioBetween(setor, inicio, fim);

        if (registros.isEmpty()) {
            return 0.0;
        }

        double semanasNoPeriodo = Duration.between(inicio, fim).toHours() / (24.0 * 7.0);
        if (semanasNoPeriodo <= 0) {
            semanasNoPeriodo = 1.0;
        }

        Map<Long, List<RegistroPonto>> registrosPorFuncionario = registros.stream()
                .collect(Collectors.groupingBy(r -> r.getFuncionario().getId()));

        double somaExcedentePorFuncionario = 0.0;
        int quantidadeFuncionarios = registrosPorFuncionario.size();

        for (List<RegistroPonto> registrosDoFuncionario : registrosPorFuncionario.values()) {
            double horasTrabalhadas = calcularHorasTrabalhadas(registrosDoFuncionario);
            double horasEsperadasNoPeriodo = HORAS_SEMANAIS_PADRAO * semanasNoPeriodo;
            double excedente = Math.max(0, horasTrabalhadas - horasEsperadasNoPeriodo);
            somaExcedentePorFuncionario += excedente;
        }

        double mediaExcedenteNoPeriodo = somaExcedentePorFuncionario / quantidadeFuncionarios;

        // Normaliza para "por semana", já que o índice de risco trabalha em horas extras/semana.
        return mediaExcedenteNoPeriodo / semanasNoPeriodo;
    }

    /**
     * Soma o tempo trabalhado de um funcionário, pareando ENTRADA→INICIO_INTERVALO
     * (primeira parte do turno) e FIM_INTERVALO→SAIDA (segunda parte). Registros
     * incompletos (ex: esqueceu de bater um dos dois) são ignorados nesse trecho.
     */
    private double calcularHorasTrabalhadas(List<RegistroPonto> registros) {
        List<RegistroPonto> ordenados = registros.stream()
                .sorted(Comparator.comparing(RegistroPonto::getHorario))
                .toList();

        double totalHoras = 0.0;
        Instant aberturaTurno = null;

        for (RegistroPonto registro : ordenados) {
            switch (registro.getTipo()) {
                case ENTRADA, FIM_INTERVALO -> aberturaTurno = registro.getHorario();
                case INICIO_INTERVALO, SAIDA -> {
                    if (aberturaTurno != null) {
                        totalHoras += Duration.between(aberturaTurno, registro.getHorario()).toMinutes() / 60.0;
                        aberturaTurno = null;
                    }
                }
            }
        }

        return totalHoras;
    }
}
