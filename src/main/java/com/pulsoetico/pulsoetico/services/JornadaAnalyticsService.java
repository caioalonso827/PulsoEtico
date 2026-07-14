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
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.RegistroPonto;
import com.pulsoetico.pulsoetico.models.dtos.JornadaResumoResponse;
import com.pulsoetico.pulsoetico.models.dtos.RegistroDiaResponse;
import com.pulsoetico.pulsoetico.repositories.AvaliacaoRiscoRepository;
import com.pulsoetico.pulsoetico.repositories.FuncionarioRepository;
import com.pulsoetico.pulsoetico.repositories.RegistroPontoRepository;

/**
 * Indicadores agregados da empresa TODA (não por setor) pra tela de
 * Ponto & Jornada do gestor: horas extras do mês, faltas, jornada média,
 * o gráfico semanal e o "Compliance NR-1".
 */
@Service
public class JornadaAnalyticsService {

    private static final ZoneId ZONA = ZoneId.systemDefault();
    private static final double HORAS_DIARIAS_PADRAO = 8.0;
    /** Limite legal de jornada diária (CLT: 8h + até 2h extras = 10h). Usado no Compliance NR-1. */
    private static final double LIMITE_LEGAL_HORAS_DIA = 10.0;

    private final RegistroPontoRepository registroPontoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final AvaliacaoRiscoRepository avaliacaoRiscoRepository;
    private final HorasExtrasCalculatorService horasExtrasCalculatorService;
    private final DenunciaService denunciaService;

    public JornadaAnalyticsService(
            RegistroPontoRepository registroPontoRepository,
            FuncionarioRepository funcionarioRepository,
            AvaliacaoRiscoRepository avaliacaoRiscoRepository,
            HorasExtrasCalculatorService horasExtrasCalculatorService,
            DenunciaService denunciaService
    ) {
        this.registroPontoRepository = registroPontoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.avaliacaoRiscoRepository = avaliacaoRiscoRepository;
        this.horasExtrasCalculatorService = horasExtrasCalculatorService;
        this.denunciaService = denunciaService;
    }

    public JornadaResumoResponse resumoDoMes() {
        Instant inicioDoMes = LocalDate.now(ZONA).withDayOfMonth(1).atStartOfDay(ZONA).toInstant();
        Instant agora = Instant.now();

        List<RegistroPonto> registrosDoMes = registroPontoRepository.findByHorarioBetween(inicioDoMes, agora);
        Map<Long, List<RegistroPonto>> registrosPorFuncionario = registrosDoMes.stream()
                .collect(Collectors.groupingBy(r -> r.getFuncionario().getId()));

        double horasExtrasMes = registrosPorFuncionario.values().stream()
                .mapToDouble(horasExtrasCalculatorService::calcularHorasExtrasFuncionario)
                .sum();

        long faltasMes = calcularFaltas(inicioDoMes, agora, registrosPorFuncionario);
        String jornadaMediaFormatada = calcularJornadaMediaFormatada(registrosDoMes);
        double compliance = calcularComplianceNR1(registrosPorFuncionario);
        List<JornadaResumoResponse.HorasPorDia> grafico = calcularMediaPorDiaDaSemana(registrosDoMes);

        return new JornadaResumoResponse(
                arredondar(horasExtrasMes), faltasMes, jornadaMediaFormatada, arredondar(compliance), grafico
        );
    }

    /** Registros de ponto de TODOS os funcionários num dia específico — a tabela "Registros do dia". */
    public List<RegistroDiaResponse> registrosDoDia(LocalDate dia) {
        Instant inicio = dia.atStartOfDay(ZONA).toInstant();
        Instant fim = inicio.plus(1, ChronoUnit.DAYS);

        List<RegistroPonto> registros = registroPontoRepository.findByHorarioBetween(inicio, fim);
        Map<Long, List<RegistroPonto>> porFuncionario = registros.stream()
                .collect(Collectors.groupingBy(r -> r.getFuncionario().getId()));

        List<RegistroDiaResponse> resultado = new ArrayList<>();

        for (List<RegistroPonto> registrosDoFuncionario : porFuncionario.values()) {
            Funcionario funcionario = registrosDoFuncionario.get(0).getFuncionario();

            List<RegistroPonto> ordenados = registrosDoFuncionario.stream()
                    .sorted(Comparator.comparing(RegistroPonto::getHorario))
                    .toList();

            Instant entrada = ordenados.stream()
                    .filter(r -> r.getTipo() == RegistroPonto.TipoRegistro.ENTRADA)
                    .map(RegistroPonto::getHorario).findFirst().orElse(null);
            Instant saida = ordenados.stream()
                    .filter(r -> r.getTipo() == RegistroPonto.TipoRegistro.SAIDA)
                    .map(RegistroPonto::getHorario).reduce((primeiro, ultimo) -> ultimo).orElse(null);

            double horasExtras = horasExtrasCalculatorService.calcularHorasExtrasFuncionario(registrosDoFuncionario);
            String status = (entrada != null && saida != null) ? "COMPLETO" : "INCOMPLETO";

            resultado.add(new RegistroDiaResponse(
                    funcionario.getNomeCompleto(),
                    funcionario.getSetor() != null ? funcionario.getSetor().getNome() : null,
                    entrada, saida, arredondar(horasExtras), status
            ));
        }

        // Funcionários ativos que não bateram ponto nenhum nesse dia = FALTA (só considera dias úteis seg-sex).
        if (dia.getDayOfWeek() != DayOfWeek.SATURDAY && dia.getDayOfWeek() != DayOfWeek.SUNDAY) {
            for (Funcionario funcionario : funcionarioRepository.findByAtivoTrue()) {
                boolean bateuPonto = porFuncionario.containsKey(funcionario.getId());
                boolean jaExistiaNesseDia = funcionario.getCriadoEm().isBefore(fim);
                if (!bateuPonto && jaExistiaNesseDia) {
                    resultado.add(new RegistroDiaResponse(
                            funcionario.getNomeCompleto(),
                            funcionario.getSetor() != null ? funcionario.getSetor().getNome() : null,
                            null, null, null, "FALTA"
                    ));
                }
            }
        }

        return resultado;
    }

    /** Conta dias úteis (seg-sex) em que um funcionário ativo não bateu nenhum ponto no período. */
    private long calcularFaltas(Instant inicio, Instant fim, Map<Long, List<RegistroPonto>> registrosPorFuncionario) {
        long totalFaltas = 0;
        List<Funcionario> ativos = funcionarioRepository.findByAtivoTrue();

        LocalDate diaInicio = LocalDate.ofInstant(inicio, ZONA);
        LocalDate diaFim = LocalDate.ofInstant(fim, ZONA);

        for (Funcionario funcionario : ativos) {
            Map<LocalDate, Double> horasPorDia = registrosPorFuncionario.containsKey(funcionario.getId())
                    ? horasExtrasCalculatorService.calcularHorasTrabalhadasPorDia(registrosPorFuncionario.get(funcionario.getId()))
                    : Map.of();

            LocalDate dataAdmissao = LocalDate.ofInstant(funcionario.getCriadoEm(), ZONA);

            for (LocalDate dia = diaInicio; !dia.isAfter(diaFim); dia = dia.plusDays(1)) {
                boolean diaUtil = dia.getDayOfWeek() != DayOfWeek.SATURDAY && dia.getDayOfWeek() != DayOfWeek.SUNDAY;
                boolean jaAdmitido = !dia.isBefore(dataAdmissao);
                boolean naoTrabalhouNesseDia = !horasPorDia.containsKey(dia);

                if (diaUtil && jaAdmitido && naoTrabalhouNesseDia) {
                    totalFaltas++;
                }
            }
        }

        return totalFaltas;
    }

    /** Duração média dos turnos completos (ENTRADA→SAIDA), formatada como "8h42". */
    private String calcularJornadaMediaFormatada(List<RegistroPonto> registros) {
        Map<Long, List<RegistroPonto>> porFuncionario = registros.stream()
                .collect(Collectors.groupingBy(r -> r.getFuncionario().getId()));

        List<Double> duracoesDosTurnos = new ArrayList<>();
        for (List<RegistroPonto> registrosDoFuncionario : porFuncionario.values()) {
            duracoesDosTurnos.addAll(
                    horasExtrasCalculatorService.calcularHorasTrabalhadasPorDia(registrosDoFuncionario).values());
        }

        if (duracoesDosTurnos.isEmpty()) {
            return "0h00";
        }

        double mediaHoras = duracoesDosTurnos.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        int horasInteiras = (int) mediaHoras;
        int minutos = (int) Math.round((mediaHoras - horasInteiras) * 60);

        return horasInteiras + "h" + String.format("%02d", minutos);
    }

    /**
     * Compliance NR-1: média simples de 3 sinais (0-100%):
     *  1) % de setores fora do estado crítico (nível ALTO)
     *  2) % de denúncias respondidas dentro do prazo de 48h
     *  3) % de dias trabalhados dentro do limite legal diário (10h, CLT)
     * É um indicador de saúde geral de conformidade, não uma métrica oficial da norma.
     */
    private double calcularComplianceNR1(Map<Long, List<RegistroPonto>> registrosPorFuncionario) {
        List<AvaliacaoRisco> ultimasAvaliacoes = avaliacaoRiscoRepository.buscarUltimaAvaliacaoDeCadaSetor();
        double percentualSetoresOk = ultimasAvaliacoes.isEmpty()
                ? 100.0
                : 100.0 * ultimasAvaliacoes.stream()
                    .filter(a -> a.getNivelRisco() != AvaliacaoRisco.NivelRisco.ALTO)
                    .count() / ultimasAvaliacoes.size();

        long denunciasAbertas = denunciaService.contarAbertas();
        long denunciasVencidas = denunciaService.contarSemRespostaAlemDoLimite();
        double percentualDenunciasOk = denunciasAbertas == 0
                ? 100.0
                : 100.0 * (denunciasAbertas - denunciasVencidas) / denunciasAbertas;

        long totalDias = 0;
        long diasDentroDoLimite = 0;
        for (List<RegistroPonto> registrosDoFuncionario : registrosPorFuncionario.values()) {
            for (double horasNoDia : horasExtrasCalculatorService.calcularHorasTrabalhadasPorDia(registrosDoFuncionario).values()) {
                totalDias++;
                if (horasNoDia <= LIMITE_LEGAL_HORAS_DIA) {
                    diasDentroDoLimite++;
                }
            }
        }
        double percentualJornadaOk = totalDias == 0 ? 100.0 : 100.0 * diasDentroDoLimite / totalDias;

        return (percentualSetoresOk + percentualDenunciasOk + percentualJornadaOk) / 3.0;
    }

    /** Média de horas trabalhadas por dia da semana (Seg a Dom), pro gráfico de barras. */
    private List<JornadaResumoResponse.HorasPorDia> calcularMediaPorDiaDaSemana(List<RegistroPonto> registros) {
        Map<Long, List<RegistroPonto>> porFuncionario = registros.stream()
                .collect(Collectors.groupingBy(r -> r.getFuncionario().getId()));

        Map<DayOfWeek, List<Double>> horasPorDiaDaSemana = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek dia : DayOfWeek.values()) {
            horasPorDiaDaSemana.put(dia, new ArrayList<>());
        }

        for (List<RegistroPonto> registrosDoFuncionario : porFuncionario.values()) {
            Map<LocalDate, Double> horasPorDia =
                    horasExtrasCalculatorService.calcularHorasTrabalhadasPorDia(registrosDoFuncionario);

            horasPorDia.forEach((dia, horas) -> horasPorDiaDaSemana.get(dia.getDayOfWeek()).add(horas));
        }

        List<JornadaResumoResponse.HorasPorDia> resultado = new ArrayList<>();
        DayOfWeek[] ordemExibicao = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };

        for (DayOfWeek dia : ordemExibicao) {
            List<Double> valores = horasPorDiaDaSemana.get(dia);
            double media = valores.isEmpty() ? 0.0 : valores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            resultado.add(new JornadaResumoResponse.HorasPorDia(abreviar(dia), arredondar(media)));
        }

        return resultado;
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
