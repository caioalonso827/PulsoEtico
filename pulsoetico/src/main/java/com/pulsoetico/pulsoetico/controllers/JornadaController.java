package com.pulsoetico.pulsoetico.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.dtos.JornadaResumoResponse;
import com.pulsoetico.pulsoetico.models.dtos.RegistroDiaResponse;
import com.pulsoetico.pulsoetico.services.JornadaAnalyticsService;

@RestController
@RequestMapping("/api/painel/jornada")
public class JornadaController {

    private final JornadaAnalyticsService jornadaAnalyticsService;

    public JornadaController(JornadaAnalyticsService jornadaAnalyticsService) {
        this.jornadaAnalyticsService = jornadaAnalyticsService;
    }

    /** Os cards + gráfico do topo da tela: horas extras (mês), faltas (mês), jornada média, compliance NR-1. */
    @GetMapping("/resumo")
    public JornadaResumoResponse resumo() {
        return jornadaAnalyticsService.resumoDoMes();
    }

    /** A tabela "Registros do dia" — todos os colaboradores, um dia específico (padrão: hoje). */
    @GetMapping("/dia")
    public List<RegistroDiaResponse> registrosDoDia(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        LocalDate dia = data != null ? data : LocalDate.now();
        return jornadaAnalyticsService.registrosDoDia(dia);
    }
}
