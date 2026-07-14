package com.pulsoetico.pulsoetico.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.Denuncia;
import com.pulsoetico.pulsoetico.models.Recomendacao;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.models.dtos.AlertaResponse;
import com.pulsoetico.pulsoetico.models.dtos.DashboardResumoResponse;
import com.pulsoetico.pulsoetico.models.dtos.SetorStatusResponse;
import com.pulsoetico.pulsoetico.repositories.AvaliacaoRiscoRepository;
import com.pulsoetico.pulsoetico.repositories.FuncionarioRepository;
import com.pulsoetico.pulsoetico.repositories.RecomendacaoRepository;
import com.pulsoetico.pulsoetico.repositories.SetorRepository;

/**
 * Agrega dados de vários lugares (setores, funcionários, avaliações de risco,
 * denúncias, recomendações) pras telas de dashboard do gestor. Não guarda
 * estado próprio — só combina o que os outros services/repositories já calculam.
 */
@Service
public class DashboardService {

    private static final int PONTOS_TENDENCIA = 10;

    private final SetorRepository setorRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final AvaliacaoRiscoRepository avaliacaoRiscoRepository;
    private final RecomendacaoRepository recomendacaoRepository;
    private final DenunciaService denunciaService;

    public DashboardService(
            SetorRepository setorRepository,
            FuncionarioRepository funcionarioRepository,
            AvaliacaoRiscoRepository avaliacaoRiscoRepository,
            RecomendacaoRepository recomendacaoRepository,
            DenunciaService denunciaService
    ) {
        this.setorRepository = setorRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.avaliacaoRiscoRepository = avaliacaoRiscoRepository;
        this.recomendacaoRepository = recomendacaoRepository;
        this.denunciaService = denunciaService;
    }

    public DashboardResumoResponse resumoGeral() {
        List<AvaliacaoRisco> ultimasAvaliacoes = avaliacaoRiscoRepository.buscarUltimaAvaliacaoDeCadaSetor();

        Double scoreMedioBemEstar = ultimasAvaliacoes.isEmpty()
                ? null
                : ultimasAvaliacoes.stream()
                    .mapToDouble(a -> 100.0 - a.getIndiceRisco())
                    .average()
                    .orElse(0.0);

        return new DashboardResumoResponse(
                funcionarioRepository.countByAtivoTrue(),
                scoreMedioBemEstar != null ? arredondar(scoreMedioBemEstar) : null
        );
    }

    /** Status de cada setor (score de bem-estar, tendência recente, rótulo) pro card "Status geral dos setores". */
    public List<SetorStatusResponse> statusPorSetor() {
        List<Setor> setores = setorRepository.findAll();
        List<SetorStatusResponse> resultado = new ArrayList<>();

        for (Setor setor : setores) {
            List<AvaliacaoRisco> historico = avaliacaoRiscoRepository.findBySetorOrderByCalculadoEmDesc(setor);

            if (historico.isEmpty()) {
                resultado.add(new SetorStatusResponse(
                        setor.getId(), setor.getNome(), setor.getQuantidadeColaboradores(),
                        null, "SEM_DADOS", List.of()
                ));
                continue;
            }

            AvaliacaoRisco maisRecente = historico.get(0);
            double scoreBemEstar = arredondar(100.0 - maisRecente.getIndiceRisco());
            String statusLabel = mapearStatusLabel(maisRecente.getNivelRisco());

            // Tendência em ordem cronológica (mais antigo -> mais recente), últimos N pontos.
            List<Double> tendencia = historico.stream()
                    .limit(PONTOS_TENDENCIA)
                    .sorted(Comparator.comparing(AvaliacaoRisco::getCalculadoEm))
                    .map(a -> arredondar(100.0 - a.getIndiceRisco()))
                    .toList();

            resultado.add(new SetorStatusResponse(
                    setor.getId(), setor.getNome(), setor.getQuantidadeColaboradores(),
                    scoreBemEstar, statusLabel, tendencia
            ));
        }

        return resultado;
    }

    /** Feed combinado: denúncias reais + recomendações automáticas, mais recentes primeiro. */
    public List<AlertaResponse> alertasRecentes(int limite) {
        List<AlertaResponse> alertas = new ArrayList<>();

        for (Denuncia denuncia : denunciaService.listarRecentes()) {
            String mensagem = denuncia.getDescricao() != null && !denuncia.getDescricao().isBlank()
                    ? denuncia.getTipo() + " — " + denuncia.getDescricao()
                    : denuncia.getTipo();

            alertas.add(new AlertaResponse(
                    denuncia.getId(), AlertaResponse.Origem.DENUNCIA,
                    denuncia.getSetor().getNome(), mensagem, denuncia.getCriadoEm()
            ));
        }

        for (Recomendacao recomendacao : recomendacaoRepository.findTop20ByOrderByCriadoEmDesc()) {
            alertas.add(new AlertaResponse(
                    recomendacao.getId(), AlertaResponse.Origem.RECOMENDACAO,
                    recomendacao.getAvaliacaoRisco().getSetor().getNome(),
                    recomendacao.getMensagem(), recomendacao.getCriadoEm()
            ));
        }

        return alertas.stream()
                .sorted(Comparator.comparing(AlertaResponse::criadoEm).reversed())
                .limit(limite)
                .toList();
    }

    private String mapearStatusLabel(AvaliacaoRisco.NivelRisco nivelRisco) {
        return switch (nivelRisco) {
            case BAIXO -> "ESTAVEL";
            case ATENCAO -> "ATENCAO";
            case ALTO -> "CRITICO";
        };
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}