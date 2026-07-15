package com.pulsoetico.pulsoetico.controllers;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.dtos.RecomendacaoResponse;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.RecomendacaoService;

@RestController
@RequestMapping("/api/painel/empresas/{empresaId}/recomendacoes")
public class RecomendacaoController {

    private final RecomendacaoService recomendacaoService;

    public RecomendacaoController(
            RecomendacaoService recomendacaoService
    ) {
        this.recomendacaoService = recomendacaoService;
    }

    @GetMapping("/setor/{setorId}/pendentes")
    public List<RecomendacaoResponse> listarPendentes(
            @PathVariable Long empresaId,
            @PathVariable Long setorId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return recomendacaoService
                .listarPendentesPorSetor(
                        empresaId,
                        setorId,
                        principal.getFuncionario()
                )
                .stream()
                .map(RecomendacaoResponse::from)
                .toList();
    }

    @PatchMapping("/{recomendacaoId}/reconhecer")
    public RecomendacaoResponse reconhecer(
            @PathVariable Long empresaId,
            @PathVariable Long recomendacaoId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return RecomendacaoResponse.from(
                recomendacaoService.reconhecer(
                        empresaId,
                        recomendacaoId,
                        principal.getFuncionario()
                )
        );
    }
}