package com.pulsoetico.pulsoetico.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.Denuncia;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.Permissoes;
import com.pulsoetico.pulsoetico.models.Recomendacao;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.models.dtos.AlertaResponse;
import com.pulsoetico.pulsoetico.models.dtos.DashboardResumoResponse;
import com.pulsoetico.pulsoetico.models.dtos.SetorStatusResponse;
import com.pulsoetico.pulsoetico.repositories.AvaliacaoRiscoRepository;
import com.pulsoetico.pulsoetico.repositories.MembroEmpresaRepository;
import com.pulsoetico.pulsoetico.repositories.RecomendacaoRepository;
import com.pulsoetico.pulsoetico.repositories.SetorRepository;

@Service
public class DashboardService {

    private static final int PONTOS_TENDENCIA = 10;

    private final SetorRepository setorRepository;
    private final MembroEmpresaRepository membroRepository;
    private final AvaliacaoRiscoRepository avaliacaoRiscoRepository;
    private final RecomendacaoRepository recomendacaoRepository;
    private final DenunciaService denunciaService;
    private final AutorizacaoEmpresaService autorizacao;

    public DashboardService(
            SetorRepository setorRepository,
            MembroEmpresaRepository membroRepository,
            AvaliacaoRiscoRepository avaliacaoRiscoRepository,
            RecomendacaoRepository recomendacaoRepository,
            DenunciaService denunciaService,
            AutorizacaoEmpresaService autorizacao
    ) {
        this.setorRepository = setorRepository;
        this.membroRepository = membroRepository;
        this.avaliacaoRiscoRepository = avaliacaoRiscoRepository;
        this.recomendacaoRepository = recomendacaoRepository;
        this.denunciaService = denunciaService;
        this.autorizacao = autorizacao;
    }

    @Transactional(readOnly = true)
    public DashboardResumoResponse resumoGeral(
            Long empresaId,
            Funcionario usuario
    ) {
        exigirAcessoAoDashboard(empresaId, usuario);

        List<AvaliacaoRisco> ultimasAvaliacoes =
                avaliacaoRiscoRepository
                        .buscarUltimaAvaliacaoDeCadaSetorDaEmpresa(
                                empresaId
                        );

        Double scoreMedioBemEstar = ultimasAvaliacoes.isEmpty()
                ? null
                : ultimasAvaliacoes.stream()
                        .mapToDouble(
                                avaliacao ->
                                        100.0
                                                - avaliacao
                                                        .getIndiceRisco()
                        )
                        .average()
                        .orElse(0.0);

        return new DashboardResumoResponse(
                membroRepository.countByEmpresaIdAndAtivoTrue(
                        empresaId
                ),
                scoreMedioBemEstar != null
                        ? arredondar(scoreMedioBemEstar)
                        : null
        );
    }

    @Transactional(readOnly = true)
    public List<SetorStatusResponse> statusPorSetor(
            Long empresaId,
            Funcionario usuario
    ) {
        exigirAcessoAoDashboard(empresaId, usuario);

        List<Setor> setores = setorRepository
                .findAllByEmpresaIdOrderByNomeAsc(empresaId);

        List<SetorStatusResponse> resultado = new ArrayList<>();

        for (Setor setor : setores) {
            List<AvaliacaoRisco> historico =
                    avaliacaoRiscoRepository
                            .findBySetorOrderByCalculadoEmDesc(setor);

            if (historico.isEmpty()) {
                resultado.add(
                        new SetorStatusResponse(
                                setor.getId(),
                                setor.getNome(),
                                setor.getQuantidadeColaboradores(),
                                null,
                                "SEM_DADOS",
                                List.of()
                        )
                );

                continue;
            }

            AvaliacaoRisco maisRecente = historico.get(0);

            double scoreBemEstar = arredondar(
                    100.0 - maisRecente.getIndiceRisco()
            );

            List<Double> tendencia = historico.stream()
                    .limit(PONTOS_TENDENCIA)
                    .sorted(
                            Comparator.comparing(
                                    AvaliacaoRisco::getCalculadoEm
                            )
                    )
                    .map(
                            avaliacao -> arredondar(
                                    100.0
                                            - avaliacao
                                                    .getIndiceRisco()
                            )
                    )
                    .toList();

            resultado.add(
                    new SetorStatusResponse(
                            setor.getId(),
                            setor.getNome(),
                            setor.getQuantidadeColaboradores(),
                            scoreBemEstar,
                            mapearStatusLabel(
                                    maisRecente.getNivelRisco()
                            ),
                            tendencia
                    )
            );
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> alertasRecentes(
            Long empresaId,
            Funcionario usuario,
            int limite
    ) {
        exigirAcessoAoDashboard(empresaId, usuario);

        List<AlertaResponse> alertas = new ArrayList<>();

        for (Denuncia denuncia :
                denunciaService.listarRecentesDaEmpresa(empresaId)) {

            String mensagem = denuncia.getDescricao() != null
                    && !denuncia.getDescricao().isBlank()
                    ? denuncia.getTipo()
                            + " — "
                            + denuncia.getDescricao()
                    : denuncia.getTipo();

            alertas.add(
                    new AlertaResponse(
                            denuncia.getId(),
                            AlertaResponse.Origem.DENUNCIA,
                            denuncia.getSetor().getNome(),
                            mensagem,
                            denuncia.getCriadoEm()
                    )
            );
        }

        for (Recomendacao recomendacao :
                recomendacaoRepository
                        .findTop20ByAvaliacaoRisco_Setor_Empresa_IdOrderByCriadoEmDesc(
                                empresaId
                        )) {

            alertas.add(
                    new AlertaResponse(
                            recomendacao.getId(),
                            AlertaResponse.Origem.RECOMENDACAO,
                            recomendacao
                                    .getAvaliacaoRisco()
                                    .getSetor()
                                    .getNome(),
                            recomendacao.getMensagem(),
                            recomendacao.getCriadoEm()
                    )
            );
        }

        return alertas.stream()
                .sorted(
                        Comparator.comparing(
                                AlertaResponse::criadoEm
                        ).reversed()
                )
                .limit(limite)
                .toList();
    }

    private void exigirAcessoAoDashboard(
            Long empresaId,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.VISUALIZAR_DASHBOARD
        );
    }

    private String mapearStatusLabel(
            AvaliacaoRisco.NivelRisco nivelRisco
    ) {
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