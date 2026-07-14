package com.pulsoetico.pulsoetico.controllers;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.dtos.AvaliacaoRiscoResponse;
import com.pulsoetico.pulsoetico.models.dtos.CalculoRiscoRequest;
import com.pulsoetico.pulsoetico.models.dtos.PrevisaoRiscoResponse;
import com.pulsoetico.pulsoetico.repositories.AvaliacaoRiscoRepository;
import com.pulsoetico.pulsoetico.services.PrevisaoRiscoService;
import com.pulsoetico.pulsoetico.services.RiskCalculationService;
import com.pulsoetico.pulsoetico.services.SetorService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/painel/avaliacoes-risco")
public class AvaliacaoRiscoController {

    private final RiskCalculationService riskCalculationService;
    private final AvaliacaoRiscoRepository avaliacaoRiscoRepository;
    private final SetorService setorService;
    private final PrevisaoRiscoService previsaoRiscoService;

    public AvaliacaoRiscoController(
            RiskCalculationService riskCalculationService,
            AvaliacaoRiscoRepository avaliacaoRiscoRepository,
            SetorService setorService,
            PrevisaoRiscoService previsaoRiscoService
    ) {
        this.riskCalculationService = riskCalculationService;
        this.avaliacaoRiscoRepository = avaliacaoRiscoRepository;
        this.setorService = setorService;
        this.previsaoRiscoService = previsaoRiscoService;
    }

    /** Dispara o cálculo do índice de risco para um setor (o "core" da IA). */
    @PostMapping
    public ResponseEntity<AvaliacaoRiscoResponse> calcular(@Valid @RequestBody CalculoRiscoRequest request) {
        AvaliacaoRisco avaliacao = riskCalculationService.calcularRisco(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(AvaliacaoRiscoResponse.from(avaliacao));
    }

    /** Última avaliação de um setor específico — o "estado atual" no painel. */
    @GetMapping("/setor/{setorId}")
    public AvaliacaoRiscoResponse buscarAtualPorSetor(@PathVariable Long setorId) {
        var setor = setorService.buscarPorId(setorId);
        return avaliacaoRiscoRepository.findTopBySetorOrderByCalculadoEmDesc(setor)
                .map(AvaliacaoRiscoResponse::from)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ainda não há avaliação de risco calculada para o setor: " + setorId));
    }

    /** Histórico completo do setor — usado no gráfico de tendência. */
    @GetMapping("/setor/{setorId}/historico")
    public List<AvaliacaoRiscoResponse> buscarHistoricoPorSetor(@PathVariable Long setorId) {
        var setor = setorService.buscarPorId(setorId);
        return avaliacaoRiscoRepository.findBySetorOrderByCalculadoEmDesc(setor).stream()
                .map(AvaliacaoRiscoResponse::from)
                .toList();
    }

    /** Última avaliação de CADA setor — o "mapa da empresa" (🟢🟡🔴) do dashboard geral. */
    @GetMapping("/mapa")
    public List<AvaliacaoRiscoResponse> buscarMapaDeRiscoGeral() {
        return avaliacaoRiscoRepository.buscarUltimaAvaliacaoDeCadaSetor().stream()
                .map(AvaliacaoRiscoResponse::from)
                .toList();
    }

    /** Previsão por tendência: projeta se/quando o setor deve atingir risco ALTO. */
    @GetMapping("/setor/{setorId}/previsao")
    public PrevisaoRiscoResponse preverTendencia(@PathVariable Long setorId) {
        var setor = setorService.buscarPorId(setorId);
        return previsaoRiscoService.preverTendencia(setor);
    }
}