package com.pulsoetico.pulsoetico.controllers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.dtos.JornadaResumoResponse;
import com.pulsoetico.pulsoetico.models.dtos.RegistroDiaResponse;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.JornadaAnalyticsService;

@RestController
@RequestMapping("/api/painel/empresas/{empresaId}/jornada")
public class JornadaController {

    private static final ZoneId ZONA =
            ZoneId.of("America/Sao_Paulo");

    private final JornadaAnalyticsService jornadaAnalyticsService;

    public JornadaController(
            JornadaAnalyticsService jornadaAnalyticsService
    ) {
        this.jornadaAnalyticsService = jornadaAnalyticsService;
    }

    @GetMapping("/resumo")
    public JornadaResumoResponse resumo(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return jornadaAnalyticsService.resumoDoMes(
                empresaId,
                principal.getFuncionario()
        );
    }

    @GetMapping("/dia")
    public List<RegistroDiaResponse> registrosDoDia(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data
    ) {
        LocalDate dia = data != null
                ? data
                : LocalDate.now(ZONA);

        return jornadaAnalyticsService.registrosDoDia(
                empresaId,
                principal.getFuncionario(),
                dia
        );
    }
}
