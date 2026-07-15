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

import com.pulsoetico.pulsoetico.models.dtos.RegistroPontoRequest;
import com.pulsoetico.pulsoetico.models.dtos.RegistroPontoResponse;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.RegistroPontoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/app/empresas/{empresaId}/ponto")
public class RegistroPontoController {

    private final RegistroPontoService registroPontoService;

    public RegistroPontoController(
            RegistroPontoService registroPontoService
    ) {
        this.registroPontoService = registroPontoService;
    }

    @PostMapping
    public ResponseEntity<RegistroPontoResponse> bater(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal,
            @Valid @RequestBody RegistroPontoRequest request
    ) {
        var registro = registroPontoService.registrar(
                empresaId,
                principal.getFuncionario(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RegistroPontoResponse.from(registro));
    }

    @GetMapping("/hoje")
    public List<RegistroPontoResponse> espelhoDeHoje(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return registroPontoService
                .buscarRegistrosDeHoje(
                        empresaId,
                        principal.getFuncionario()
                )
                .stream()
                .map(RegistroPontoResponse::from)
                .toList();
    }
}
