package com.pulsoetico.pulsoetico.controllers;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.dtos.AlertaResponse;
import com.pulsoetico.pulsoetico.models.dtos.DashboardResumoResponse;
import com.pulsoetico.pulsoetico.models.dtos.SetorStatusResponse;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.DashboardService;

@RestController
@RequestMapping("/api/painel/empresas/{empresaId}/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumo")
    public DashboardResumoResponse resumo(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return dashboardService.resumoGeral(
                empresaId,
                principal.getFuncionario()
        );
    }

    @GetMapping("/setores")
    public List<SetorStatusResponse> setores(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return dashboardService.statusPorSetor(
                empresaId,
                principal.getFuncionario()
        );
    }

    @GetMapping("/alertas")
    public List<AlertaResponse> alertas(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal,
            @RequestParam(defaultValue = "10") int limite
    ) {
        return dashboardService.alertasRecentes(
                empresaId,
                principal.getFuncionario(),
                limite
        );
    }
}