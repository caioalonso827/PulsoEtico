package com.pulsoetico.pulsoetico.controllers;


import com.pulsoetico.pulsoetico.models.dtos.RecomendacaoResponse;
import com.pulsoetico.pulsoetico.services.RecomendacaoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/painel/recomendacoes")
public class RecomendacaoController {

    private final RecomendacaoService recommendationService;

    public RecomendacaoController(RecomendacaoService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /** Recomendações pendentes (ainda não vistas) de um setor — o que aparece no painel do gestor. */
    @GetMapping("/setor/{setorId}/pendentes")
    public List<RecomendacaoResponse> listarPendentes(@PathVariable Long setorId) {
        return recommendationService.listarPendentesPorSetor(setorId).stream()
                .map(RecomendacaoResponse::from)
                .toList();
    }

    /** Gestor marca a recomendação como vista/tratada. */
    @PatchMapping("/{id}/reconhecer")
    public RecomendacaoResponse reconhecer(@PathVariable Long id) {
        return RecomendacaoResponse.from(recommendationService.reconhecer(id));
    }
}