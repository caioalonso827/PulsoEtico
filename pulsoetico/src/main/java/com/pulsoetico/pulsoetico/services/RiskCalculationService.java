package com.pulsoetico.pulsoetico.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.CheckinHumor;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.models.dtos.CalculoRiscoRequest;
import com.pulsoetico.pulsoetico.repositories.AvaliacaoRiscoRepository;
import com.pulsoetico.pulsoetico.repositories.CheckinHumorRepository;
import com.pulsoetico.pulsoetico.repositories.SetorRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Calcula o Índice de Risco Psicossocial (0 a 100) de um setor — 100% automático.
 *
 * Fontes de cada componente:
 *   - Humor médio ................ CheckinHumor (check-ins dos últimos N dias)
 *   - Horas extras médias/semana .. RegistroPonto (calculado por HorasExtrasCalculatorService)
 *   - Rotatividade mensal ......... Setor.taxaRotatividadeMensal (indicador manual do RH)
 *   - Denúncias anônimas .......... Setor.quantidadeDenunciasAnonimasMensal (indicador manual do RH)
 *
 * Pesos (somam 100%): Humor 40% / Horas extras 30% / Rotatividade 20% / Denúncias 10%.
 */
@Service
public class RiskCalculationService {

    private static final double PESO_HUMOR = 0.40;
    private static final double PESO_HORAS_EXTRAS = 0.30;
    private static final double PESO_ROTATIVIDADE = 0.20;
    private static final double PESO_DENUNCIAS = 0.10;

    private static final double TETO_HORAS_EXTRAS_SEMANA = 20.0;
    private static final double TETO_ROTATIVIDADE_PERCENTUAL = 30.0;
    private static final int TETO_DENUNCIAS = 5;

    private static final double LIMITE_ATENCAO = 40.0;
    private static final double LIMITE_ALTO = 70.0;

    private final SetorRepository setorRepository;
    private final CheckinHumorRepository checkinHumorRepository;
    private final AvaliacaoRiscoRepository avaliacaoRiscoRepository;
    private final RecomendacaoService recommendationService;
    private final HorasExtrasCalculatorService horasExtrasCalculatorService;

    public RiskCalculationService(
            SetorRepository setorRepository,
            CheckinHumorRepository checkinHumorRepository,
            AvaliacaoRiscoRepository avaliacaoRiscoRepository,
            RecomendacaoService recommendationService,
            HorasExtrasCalculatorService horasExtrasCalculatorService
    ) {
        this.setorRepository = setorRepository;
        this.checkinHumorRepository = checkinHumorRepository;
        this.avaliacaoRiscoRepository = avaliacaoRiscoRepository;
        this.recommendationService = recommendationService;
        this.horasExtrasCalculatorService = horasExtrasCalculatorService;
    }

    /** Dispara manualmente ("recalcular agora"), usado pelo endpoint POST /avaliacoes-risco. */
    @Transactional
    public AvaliacaoRisco calcularRisco(CalculoRiscoRequest request) {
        Setor setor = setorRepository.findById(request.setorId())
                .orElseThrow(() -> new EntityNotFoundException("Setor não encontrado: " + request.setorId()));

        return calcularRiscoDoSetor(setor, request.diasJanelaOuPadrao());
    }

    /** Usado pelo job agendado (RiskCalculationScheduler), que roda pra todos os setores sozinho. */
    @Transactional
    public AvaliacaoRisco calcularRiscoDoSetor(Setor setor, int diasJanela) {
        Instant desde = Instant.now().minus(diasJanela, ChronoUnit.DAYS);
        Instant agora = Instant.now();

        List<CheckinHumor> checkins = checkinHumorRepository.findBySetorAndCriadoEmAfter(setor, desde);
        double mediaSeveridadeHumor = calcularMediaSeveridadeHumor(checkins);

        double horasExtrasMediasSemana =
                horasExtrasCalculatorService.calcularMediaHorasExtrasSemana(setor, desde, agora);

        double taxaRotatividadeMensal = setor.getTaxaRotatividadeMensal() != null
                ? setor.getTaxaRotatividadeMensal() : 0.0;
        int quantidadeDenuncias = setor.getQuantidadeDenunciasAnonimasMensal() != null
                ? setor.getQuantidadeDenunciasAnonimasMensal() : 0;

        double notaHumor = normalizarSeveridadeHumor(mediaSeveridadeHumor);
        double notaHorasExtras = normalizar(horasExtrasMediasSemana, TETO_HORAS_EXTRAS_SEMANA);
        double notaRotatividade = normalizar(taxaRotatividadeMensal, TETO_ROTATIVIDADE_PERCENTUAL);
        double notaDenuncias = normalizar(quantidadeDenuncias, TETO_DENUNCIAS);

        double indiceRisco =
                (notaHumor * PESO_HUMOR)
                        + (notaHorasExtras * PESO_HORAS_EXTRAS)
                        + (notaRotatividade * PESO_ROTATIVIDADE)
                        + (notaDenuncias * PESO_DENUNCIAS);

        AvaliacaoRisco avaliacao = AvaliacaoRisco.builder()
                .setor(setor)
                .indiceRisco(arredondar(indiceRisco))
                .nivelRisco(classificarNivelRisco(indiceRisco))
                .mediaHorasExtras(arredondar(horasExtrasMediasSemana))
                .mediaSeveridadeHumor(arredondar(mediaSeveridadeHumor))
                .taxaRotatividade(taxaRotatividadeMensal)
                .quantidadeDenunciasAnonimas(quantidadeDenuncias)
                .build();

        AvaliacaoRisco salva = avaliacaoRiscoRepository.save(avaliacao);

        if (salva.getNivelRisco() != AvaliacaoRisco.NivelRisco.BAIXO) {
            recommendationService.gerarRecomendacoes(salva);
        }

        return salva;
    }

    private double calcularMediaSeveridadeHumor(List<CheckinHumor> checkins) {
        if (checkins.isEmpty()) {
            return 2.5;
        }
        return checkins.stream()
                .mapToInt(c -> c.getNivelHumor().getSeveridade())
                .average()
                .orElse(2.5);
    }

    private double normalizarSeveridadeHumor(double severidadeMedia) {
        double notaBruta = ((severidadeMedia - 1) / 4.0) * 100;
        return limitarEntre0e100(notaBruta);
    }

    private double normalizar(double valor, double teto) {
        double notaBruta = (valor / teto) * 100;
        return limitarEntre0e100(notaBruta);
    }

    private double limitarEntre0e100(double valor) {
        return Math.max(0, Math.min(100, valor));
    }

    private AvaliacaoRisco.NivelRisco classificarNivelRisco(double indiceRisco) {
        if (indiceRisco >= LIMITE_ALTO) {
            return AvaliacaoRisco.NivelRisco.ALTO;
        }
        if (indiceRisco >= LIMITE_ATENCAO) {
            return AvaliacaoRisco.NivelRisco.ATENCAO;
        }
        return AvaliacaoRisco.NivelRisco.BAIXO;
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
