package com.pulsoetico.pulsoetico.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.dtos.AlertaResponse;
import com.pulsoetico.pulsoetico.models.dtos.DashboardResumoResponse;
import com.pulsoetico.pulsoetico.models.dtos.SetorStatusResponse;
import com.pulsoetico.pulsoetico.services.DashboardService;

@RestController
@RequestMapping("/api/painel/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** Os 4 cards do topo: colaboradores ativos, score de bem-estar, setores críticos, denúncias abertas. */
    @GetMapping("/resumo")
    public DashboardResumoResponse resumo() {
        return dashboardService.resumoGeral();
    }

    /** Lista "Status geral dos setores" (score + tendência + rótulo por setor). */
    @GetMapping("/setores")
    public List<SetorStatusResponse> setores() {
        return dashboardService.statusPorSetor();
    }

    /** Feed "Denúncias & alertas" (mistura denúncias reais com recomendações automáticas). */
    @GetMapping("/alertas")
    public List<AlertaResponse> alertas(@RequestParam(defaultValue = "10") int limite) {
        return dashboardService.alertasRecentes(limite);
    }
}
