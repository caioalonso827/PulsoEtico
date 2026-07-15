package com.pulsoetico.pulsoetico.services;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.MembroEmpresa;
import com.pulsoetico.pulsoetico.models.Permissoes;
import com.pulsoetico.pulsoetico.models.RegistroPonto;
import com.pulsoetico.pulsoetico.models.dtos.JornadaResumoResponse;
import com.pulsoetico.pulsoetico.models.dtos.RegistroDiaResponse;
import com.pulsoetico.pulsoetico.repositories.AvaliacaoRiscoRepository;
import com.pulsoetico.pulsoetico.repositories.MembroEmpresaRepository;
import com.pulsoetico.pulsoetico.repositories.RegistroPontoRepository;

@Service
public class JornadaAnalyticsService {

    private static final ZoneId ZONA =
            ZoneId.of("America/Sao_Paulo");

    private static final double LIMITE_LEGAL_HORAS_DIA =
            10.0;

    private final RegistroPontoRepository registroPontoRepository;
    private final MembroEmpresaRepository membroRepository;
    private final AvaliacaoRiscoRepository avaliacaoRiscoRepository;
    private final HorasExtrasCalculatorService horasExtrasCalculatorService;
    private final DenunciaService denunciaService;
    private final AutorizacaoEmpresaService autorizacao;

    public JornadaAnalyticsService(
            RegistroPontoRepository registroPontoRepository,
            MembroEmpresaRepository membroRepository,
            AvaliacaoRiscoRepository avaliacaoRiscoRepository,
            HorasExtrasCalculatorService horasExtrasCalculatorService,
            DenunciaService denunciaService,
            AutorizacaoEmpresaService autorizacao
    ) {
        this.registroPontoRepository =
                registroPontoRepository;

        this.membroRepository = membroRepository;

        this.avaliacaoRiscoRepository =
                avaliacaoRiscoRepository;

        this.horasExtrasCalculatorService =
                horasExtrasCalculatorService;

        this.denunciaService = denunciaService;
        this.autorizacao = autorizacao;
    }

    @Transactional(readOnly = true)
    public JornadaResumoResponse resumoDoMes(
            Long empresaId,
            Funcionario usuario
    ) {
        exigirAcesso(empresaId, usuario);

        Instant inicioDoMes = LocalDate.now(ZONA)
                .withDayOfMonth(1)
                .atStartOfDay(ZONA)
                .toInstant();

        Instant agora = Instant.now();

        List<RegistroPonto> registrosDoMes =
                registroPontoRepository
                        .findByEmpresaIdAndHorarioBetweenOrderByHorarioAsc(
                                empresaId,
                                inicioDoMes,
                                agora
                        );

        Map<Long, List<RegistroPonto>>
        registrosPorFuncionario =
                agruparPorFuncionario(registrosDoMes);

        List<MembroEmpresa> membrosAtivos =
                membroRepository
                        .findAllByEmpresaIdAndAtivoTrueOrderByFuncionarioNomeCompletoAsc(
                                empresaId
                        );

        double horasExtrasMes =
                registrosPorFuncionario.values()
                        .stream()
                        .mapToDouble(
                                horasExtrasCalculatorService
                                        ::calcularHorasExtrasFuncionario
                        )
                        .sum();

        long faltasMes = calcularFaltas(
                inicioDoMes,
                agora,
                registrosPorFuncionario,
                membrosAtivos
        );

        String jornadaMediaFormatada =
                calcularJornadaMediaFormatada(
                        registrosDoMes
                );

        double compliance = calcularComplianceNR1(
                empresaId,
                registrosPorFuncionario
        );

        List<JornadaResumoResponse.HorasPorDia> grafico =
                calcularMediaPorDiaDaSemana(
                        registrosDoMes
                );

        return new JornadaResumoResponse(
                arredondar(horasExtrasMes),
                faltasMes,
                jornadaMediaFormatada,
                arredondar(compliance),
                grafico
        );
    }

    @Transactional(readOnly = true)
    public List<RegistroDiaResponse> registrosDoDia(
            Long empresaId,
            Funcionario usuario,
            LocalDate dia
    ) {
        exigirAcesso(empresaId, usuario);

        Instant inicio = dia
                .atStartOfDay(ZONA)
                .toInstant();

        Instant fim = inicio.plus(
                1,
                ChronoUnit.DAYS
        );

        List<RegistroPonto> registros =
                registroPontoRepository
                        .findByEmpresaIdAndHorarioBetweenOrderByHorarioAsc(
                                empresaId,
                                inicio,
                                fim
                        );

        Map<Long, List<RegistroPonto>> porFuncionario =
                agruparPorFuncionario(registros);

        List<MembroEmpresa> membrosAtivos =
                membroRepository
                        .findAllByEmpresaIdAndAtivoTrueOrderByFuncionarioNomeCompletoAsc(
                                empresaId
                        );

        Map<Long, MembroEmpresa> membroPorFuncionario =
                membrosAtivos.stream()
                        .collect(
                                Collectors.toMap(
                                        membro ->
                                                membro
                                                        .getFuncionario()
                                                        .getId(),
                                        Function.identity()
                                )
                        );

        List<RegistroDiaResponse> resultado =
                new ArrayList<>();

        for (List<RegistroPonto> registrosDoFuncionario :
                porFuncionario.values()) {

            Funcionario funcionario =
                    registrosDoFuncionario
                            .get(0)
                            .getFuncionario();

            List<RegistroPonto> ordenados =
                    registrosDoFuncionario.stream()
                            .sorted(
                                    Comparator.comparing(
                                            RegistroPonto
                                                    ::getHorario
                                    )
                            )
                            .toList();

            Instant entrada = ordenados.stream()
                    .filter(
                            registro ->
                                    registro.getTipo()
                                            == RegistroPonto
                                                    .TipoRegistro
                                                    .ENTRADA
                    )
                    .map(RegistroPonto::getHorario)
                    .findFirst()
                    .orElse(null);

            Instant saida = ordenados.stream()
                    .filter(
                            registro ->
                                    registro.getTipo()
                                            == RegistroPonto
                                                    .TipoRegistro
                                                    .SAIDA
                    )
                    .map(RegistroPonto::getHorario)
                    .reduce(
                            (primeiro, ultimo) -> ultimo
                    )
                    .orElse(null);

            double horasExtras =
                    horasExtrasCalculatorService
                            .calcularHorasExtrasFuncionario(
                                    registrosDoFuncionario
                            );

            String status =
                    entrada != null && saida != null
                            ? "COMPLETO"
                            : "INCOMPLETO";

            String setorNome =
                    registrosDoFuncionario.get(0)
                            .getSetor() != null
                            ? registrosDoFuncionario
                                    .get(0)
                                    .getSetor()
                                    .getNome()
                            : obterSetorAtual(
                                    membroPorFuncionario.get(
                                            funcionario.getId()
                                    )
                            );

            resultado.add(
                    new RegistroDiaResponse(
                            funcionario.getNomeCompleto(),
                            setorNome,
                            entrada,
                            saida,
                            arredondar(horasExtras),
                            status
                    )
            );
        }

        if (ehDiaUtil(dia)) {
            for (MembroEmpresa membro : membrosAtivos) {
                Long funcionarioId =
                        membro.getFuncionario().getId();

                boolean bateuPonto =
                        porFuncionario.containsKey(
                                funcionarioId
                        );

                boolean jaEraMembro =
                        membro.getEntrouEm()
                                .isBefore(fim);

                if (!bateuPonto && jaEraMembro) {
                    resultado.add(
                            new RegistroDiaResponse(
                                    membro
                                            .getFuncionario()
                                            .getNomeCompleto(),
                                    obterSetorAtual(membro),
                                    null,
                                    null,
                                    null,
                                    "FALTA"
                            )
                    );
                }
            }
        }

        return resultado;
    }

    private long calcularFaltas(
            Instant inicio,
            Instant fim,
            Map<Long, List<RegistroPonto>>
                    registrosPorFuncionario,
            List<MembroEmpresa> membrosAtivos
    ) {
        long totalFaltas = 0;

        LocalDate diaInicio =
                LocalDate.ofInstant(inicio, ZONA);

        LocalDate diaFim =
                LocalDate.ofInstant(fim, ZONA);

        for (MembroEmpresa membro : membrosAtivos) {
            Long funcionarioId =
                    membro.getFuncionario().getId();

            Map<LocalDate, Double> horasPorDia =
                    registrosPorFuncionario
                            .containsKey(funcionarioId)
                            ? horasExtrasCalculatorService
                                    .calcularHorasTrabalhadasPorDia(
                                            registrosPorFuncionario
                                                    .get(
                                                            funcionarioId
                                                    )
                                    )
                            : Map.of();

            LocalDate dataEntradaNaEmpresa =
                    LocalDate.ofInstant(
                            membro.getEntrouEm(),
                            ZONA
                    );

            for (
                    LocalDate dia = diaInicio;
                    !dia.isAfter(diaFim);
                    dia = dia.plusDays(1)
            ) {
                boolean jaEraMembro =
                        !dia.isBefore(
                                dataEntradaNaEmpresa
                        );

                boolean naoTrabalhouNesseDia =
                        !horasPorDia.containsKey(dia);

                if (ehDiaUtil(dia)
                        && jaEraMembro
                        && naoTrabalhouNesseDia) {
                    totalFaltas++;
                }
            }
        }

        return totalFaltas;
    }

    private String calcularJornadaMediaFormatada(
            List<RegistroPonto> registros
    ) {
        Map<Long, List<RegistroPonto>> porFuncionario =
                agruparPorFuncionario(registros);

        List<Double> duracoesDosTurnos =
                new ArrayList<>();

        for (List<RegistroPonto> registrosDoFuncionario :
                porFuncionario.values()) {

            duracoesDosTurnos.addAll(
                    horasExtrasCalculatorService
                            .calcularHorasTrabalhadasPorDia(
                                    registrosDoFuncionario
                            )
                            .values()
            );
        }

        if (duracoesDosTurnos.isEmpty()) {
            return "0h00";
        }

        double mediaHoras =
                duracoesDosTurnos.stream()
                        .mapToDouble(valor -> valor)
                        .average()
                        .orElse(0.0);

        int horasInteiras = (int) mediaHoras;

        int minutos = (int) Math.round(
                (mediaHoras - horasInteiras) * 60
        );

        return horasInteiras
                + "h"
                + String.format("%02d", minutos);
    }

    private double calcularComplianceNR1(
            Long empresaId,
            Map<Long, List<RegistroPonto>>
                    registrosPorFuncionario
    ) {
        List<AvaliacaoRisco> ultimasAvaliacoes =
                avaliacaoRiscoRepository
                        .buscarUltimaAvaliacaoDeCadaSetorDaEmpresa(
                                empresaId
                        );

        double percentualSetoresOk =
                ultimasAvaliacoes.isEmpty()
                        ? 100.0
                        : 100.0
                                * ultimasAvaliacoes.stream()
                                        .filter(
                                                avaliacao ->
                                                        avaliacao
                                                                .getNivelRisco()
                                                                != AvaliacaoRisco
                                                                        .NivelRisco
                                                                        .ALTO
                                        )
                                        .count()
                                / ultimasAvaliacoes.size();

        long denunciasAbertas =
                denunciaService.contarAbertasDaEmpresa(
                        empresaId
                );

        long denunciasVencidas =
                denunciaService
                        .contarSemRespostaAlemDoLimiteDaEmpresa(
                                empresaId
                        );

        double percentualDenunciasOk =
                denunciasAbertas == 0
                        ? 100.0
                        : 100.0
                                * (
                                    denunciasAbertas
                                            - denunciasVencidas
                                )
                                / denunciasAbertas;

        long totalDias = 0;
        long diasDentroDoLimite = 0;

        for (List<RegistroPonto> registrosDoFuncionario :
                registrosPorFuncionario.values()) {

            for (double horasNoDia :
                    horasExtrasCalculatorService
                            .calcularHorasTrabalhadasPorDia(
                                    registrosDoFuncionario
                            )
                            .values()) {

                totalDias++;

                if (horasNoDia
                        <= LIMITE_LEGAL_HORAS_DIA) {
                    diasDentroDoLimite++;
                }
            }
        }

        double percentualJornadaOk =
                totalDias == 0
                        ? 100.0
                        : 100.0
                                * diasDentroDoLimite
                                / totalDias;

        return (
                percentualSetoresOk
                        + percentualDenunciasOk
                        + percentualJornadaOk
        ) / 3.0;
    }

    private List<JornadaResumoResponse.HorasPorDia>
    calcularMediaPorDiaDaSemana(
            List<RegistroPonto> registros
    ) {
        Map<Long, List<RegistroPonto>> porFuncionario =
                agruparPorFuncionario(registros);

        Map<DayOfWeek, List<Double>>
        horasPorDiaDaSemana =
                new EnumMap<>(DayOfWeek.class);

        for (DayOfWeek dia : DayOfWeek.values()) {
            horasPorDiaDaSemana.put(
                    dia,
                    new ArrayList<>()
            );
        }

        for (List<RegistroPonto> registrosDoFuncionario :
                porFuncionario.values()) {

            Map<LocalDate, Double> horasPorDia =
                    horasExtrasCalculatorService
                            .calcularHorasTrabalhadasPorDia(
                                    registrosDoFuncionario
                            );

            horasPorDia.forEach(
                    (dia, horas) ->
                            horasPorDiaDaSemana
                                    .get(dia.getDayOfWeek())
                                    .add(horas)
            );
        }

        List<JornadaResumoResponse.HorasPorDia>
        resultado = new ArrayList<>();

        DayOfWeek[] ordemExibicao = {
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
        };

        for (DayOfWeek dia : ordemExibicao) {
            List<Double> valores =
                    horasPorDiaDaSemana.get(dia);

            double media = valores.isEmpty()
                    ? 0.0
                    : valores.stream()
                            .mapToDouble(valor -> valor)
                            .average()
                            .orElse(0.0);

            resultado.add(
                    new JornadaResumoResponse.HorasPorDia(
                            abreviar(dia),
                            arredondar(media)
                    )
            );
        }

        return resultado;
    }

    private Map<Long, List<RegistroPonto>>
    agruparPorFuncionario(
            List<RegistroPonto> registros
    ) {
        return registros.stream()
                .collect(
                        Collectors.groupingBy(
                                registro ->
                                        registro
                                                .getFuncionario()
                                                .getId()
                        )
                );
    }

    private String obterSetorAtual(
            MembroEmpresa membro
    ) {
        return membro != null
                && membro.getSetor() != null
                ? membro.getSetor().getNome()
                : null;
    }

    private boolean ehDiaUtil(LocalDate dia) {
        return dia.getDayOfWeek()
                != DayOfWeek.SATURDAY
                && dia.getDayOfWeek()
                != DayOfWeek.SUNDAY;
    }

    private void exigirAcesso(
            Long empresaId,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_JORNADAS
        );
    }

    private String abreviar(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> "Seg";
            case TUESDAY -> "Ter";
            case WEDNESDAY -> "Qua";
            case THURSDAY -> "Qui";
            case FRIDAY -> "Sex";
            case SATURDAY -> "Sáb";
            case SUNDAY -> "Dom";
        };
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}