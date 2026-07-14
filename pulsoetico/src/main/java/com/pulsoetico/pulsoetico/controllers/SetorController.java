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

import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.models.dtos.IndicadoresManuaisRequest;
import com.pulsoetico.pulsoetico.models.dtos.SetorRequest;
import com.pulsoetico.pulsoetico.models.dtos.SetorResponse;
import com.pulsoetico.pulsoetico.services.SetorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/painel/setores")
public class SetorController {

    private final SetorService setorService;

    public SetorController(SetorService setorService) {
        this.setorService = setorService;
    }

    @PostMapping
    public ResponseEntity<SetorResponse> criar(@Valid @RequestBody SetorRequest request) {
        Setor setor = setorService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SetorResponse.from(setor));
    }

    @GetMapping
    public List<SetorResponse> listarTodos() {
        return setorService.listarTodos().stream()
                .map(SetorResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public SetorResponse buscarPorId(@PathVariable Long id) {
        return SetorResponse.from(setorService.buscarPorId(id));
    }

    /** RH atualiza rotatividade/denúncias periodicamente (ex: 1x por mês). */
    @org.springframework.web.bind.annotation.PatchMapping("/{id}/indicadores")
    public SetorResponse atualizarIndicadores(
            @PathVariable Long id,
            @Valid @RequestBody IndicadoresManuaisRequest request
    ) {
        return SetorResponse.from(setorService.atualizarIndicadoresManuais(id, request));
    }
}