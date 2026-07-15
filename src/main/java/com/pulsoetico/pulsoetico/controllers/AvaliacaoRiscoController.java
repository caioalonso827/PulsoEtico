package com.pulsoetico.pulsoetico.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.Permissoes;
import com.pulsoetico.pulsoetico.models.dtos.AvaliacaoRiscoResponse;
import com.pulsoetico.pulsoetico.models.dtos.CalculoRiscoRequest;
import com.pulsoetico.pulsoetico.models.dtos.PrevisaoRiscoResponse;
import com.pulsoetico.pulsoetico.repositories.AvaliacaoRiscoRepository;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.AutorizacaoEmpresaService;
import com.pulsoetico.pulsoetico.services.PrevisaoRiscoService;
import com.pulsoetico.pulsoetico.services.RiskCalculationService;
import com.pulsoetico.pulsoetico.services.SetorService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/painel/empresas/{empresaId}/avaliacoes-risco")
public class AvaliacaoRiscoController {

    private final RiskCalculationService riskCalculationService;
    private final AvaliacaoRiscoRepository avaliacaoRiscoRepository;
    private final SetorService setorService;
    private final PrevisaoRiscoService previsaoRiscoService;
    private final AutorizacaoEmpresaService autorizacao;

    public AvaliacaoRiscoController(
            RiskCalculationService riskCalculationService,
            AvaliacaoRiscoRepository avaliacaoRiscoRepository,
            SetorService setorService,
            PrevisaoRiscoService previsaoRiscoService,
            AutorizacaoEmpresaService autorizacao
    ) {
        this.riskCalculationService = riskCalculationService;
        this.avaliacaoRiscoRepository = avaliacaoRiscoRepository;
        this.setorService = setorService;
        this.previsaoRiscoService = previsaoRiscoService;
        this.autorizacao = autorizacao;
    }

    @PostMapping
    public ResponseEntity<AvaliacaoRiscoResponse> calcular(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal,
            @Valid @RequestBody CalculoRiscoRequest request
    ) {
        AvaliacaoRisco avaliacao =
                riskCalculationService.calcularRisco(
                        empresaId,
                        principal.getFuncionario(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AvaliacaoRiscoResponse.from(avaliacao));
    }

    @GetMapping("/setor/{setorId}")
    public AvaliacaoRiscoResponse buscarAtualPorSetor(
            @PathVariable Long empresaId,
            @PathVariable Long setorId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        exigirVisualizacao(empresaId, principal);

        var setor = setorService.buscarPorIdDaEmpresa(
                empresaId,
                setorId
        );

        return avaliacaoRiscoRepository
                .findTopBySetorOrderByCalculadoEmDesc(setor)
                .map(AvaliacaoRiscoResponse::from)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Ainda não há avaliação para este setor"
                        )
                );
    }

    @GetMapping("/setor/{setorId}/historico")
    public List<AvaliacaoRiscoResponse> buscarHistoricoPorSetor(
            @PathVariable Long empresaId,
            @PathVariable Long setorId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        exigirVisualizacao(empresaId, principal);

        var setor = setorService.buscarPorIdDaEmpresa(
                empresaId,
                setorId
        );

        return avaliacaoRiscoRepository
                .findBySetorOrderByCalculadoEmDesc(setor)
                .stream()
                .map(AvaliacaoRiscoResponse::from)
                .toList();
    }

    @GetMapping("/mapa")
    public List<AvaliacaoRiscoResponse> buscarMapaDeRiscoGeral(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        exigirVisualizacao(empresaId, principal);

        return avaliacaoRiscoRepository
                .buscarUltimaAvaliacaoDeCadaSetorDaEmpresa(
                        empresaId
                )
                .stream()
                .map(AvaliacaoRiscoResponse::from)
                .toList();
    }

    @GetMapping("/setor/{setorId}/previsao")
    public PrevisaoRiscoResponse preverTendencia(
            @PathVariable Long empresaId,
            @PathVariable Long setorId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        exigirVisualizacao(empresaId, principal);

        var setor = setorService.buscarPorIdDaEmpresa(
                empresaId,
                setorId
        );

        return previsaoRiscoService.preverTendencia(setor);
    }

    private void exigirVisualizacao(
            Long empresaId,
            FuncionarioUserDetails principal
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                principal.getFuncionario(),
                Permissoes.VISUALIZAR_DASHBOARD
        );
    }
}
