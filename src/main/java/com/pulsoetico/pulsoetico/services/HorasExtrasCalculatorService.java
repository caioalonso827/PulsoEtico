package com.pulsoetico.pulsoetico.services;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pulsoetico.pulsoetico.models.RegistroPonto;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.repositories.RegistroPontoRepository;

@Service
public class HorasExtrasCalculatorService {

    private static final double HORAS_DIARIAS_PADRAO = 8.0;
    private static final ZoneId ZONA = ZoneId.systemDefault();

    private final RegistroPontoRepository registroPontoRepository;

    public HorasExtrasCalculatorService(
            RegistroPontoRepository registroPontoRepository
    ) {
        this.registroPontoRepository = registroPontoRepository;
    }

    public double calcularMediaHorasExtrasSemana(
            Setor setor,
            Instant inicio,
            Instant fim
    ) {
        List<RegistroPonto> registros =
                registroPontoRepository
                        .findBySetorAndHorarioBetween(
                                setor,
                                inicio,
                                fim
                        );

        if (registros.isEmpty()) {
            return 0.0;
        }

        double semanasNoPeriodo =
                Duration.between(inicio, fim).toHours()
                        / (24.0 * 7.0);

        if (semanasNoPeriodo <= 0) {
            semanasNoPeriodo = 1.0;
        }

        Map<Long, List<RegistroPonto>>
        registrosPorFuncionario = registros.stream()
                .collect(
                        Collectors.groupingBy(
                                registro ->
                                        registro
                                                .getFuncionario()
                                                .getId()
                        )
                );

        double somaHorasExtrasPorFuncionario = 0.0;

        int quantidadeFuncionarios =
                registrosPorFuncionario.size();

        for (List<RegistroPonto> registrosDoFuncionario :
                registrosPorFuncionario.values()) {

            somaHorasExtrasPorFuncionario +=
                    calcularHorasExtrasFuncionario(
                            registrosDoFuncionario
                    );
        }

        double mediaExtrasNoPeriodo =
                somaHorasExtrasPorFuncionario
                        / quantidadeFuncionarios;

        return mediaExtrasNoPeriodo / semanasNoPeriodo;
    }

    double calcularHorasExtrasFuncionario(
            List<RegistroPonto> registros
    ) {
        Map<LocalDate, Double> horasTrabalhadasPorDia =
                calcularHorasTrabalhadasPorDia(registros);

        double totalHorasExtras = 0.0;

        for (Map.Entry<LocalDate, Double> entrada :
                horasTrabalhadasPorDia.entrySet()) {

            LocalDate dia = entrada.getKey();
            double horasTrabalhadasNoDia = entrada.getValue();

            DayOfWeek diaDaSemana = dia.getDayOfWeek();

            double horasExtrasNoDia;

            if (diaDaSemana == DayOfWeek.SATURDAY
                    || diaDaSemana == DayOfWeek.SUNDAY) {

                horasExtrasNoDia = horasTrabalhadasNoDia;
            } else {
                horasExtrasNoDia = Math.max(
                        0,
                        horasTrabalhadasNoDia
                                - HORAS_DIARIAS_PADRAO
                );
            }

            totalHorasExtras += horasExtrasNoDia;
        }

        return totalHorasExtras;
    }

    Map<LocalDate, Double> calcularHorasTrabalhadasPorDia(
            List<RegistroPonto> registros
    ) {
        List<RegistroPonto> ordenados = registros.stream()
                .sorted(
                        Comparator.comparing(
                                RegistroPonto::getHorario
                        )
                )
                .toList();

        Map<LocalDate, Double> horasPorDia =
                new java.util.HashMap<>();

        Instant aberturaTurno = null;

        for (RegistroPonto registro : ordenados) {
            switch (registro.getTipo()) {
                case ENTRADA ->
                        aberturaTurno = registro.getHorario();

                case SAIDA -> {
                    if (aberturaTurno != null) {
                        double horas =
                                Duration.between(
                                        aberturaTurno,
                                        registro.getHorario()
                                ).toMinutes() / 60.0;

                        LocalDate diaDoTurno =
                                LocalDate.ofInstant(
                                        aberturaTurno,
                                        ZONA
                                );

                        horasPorDia.merge(
                                diaDoTurno,
                                horas,
                                Double::sum
                        );

                        aberturaTurno = null;
                    }
                }
            }
        }

        return horasPorDia;
    }
}