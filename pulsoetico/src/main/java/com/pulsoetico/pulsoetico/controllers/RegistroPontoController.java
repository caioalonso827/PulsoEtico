package com.pulsoetico.pulsoetico.controllers;

import com.pulsoetico.pulsoetico.models.dtos.RegistroPontoRequest;
import com.pulsoetico.pulsoetico.models.dtos.RegistroPontoResponse;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.RegistroPontoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/app/ponto")
public class RegistroPontoController {

    private final RegistroPontoService registroPontoService;

    public RegistroPontoController(RegistroPontoService registroPontoService) {
        this.registroPontoService = registroPontoService;
    }

    @PostMapping
    public ResponseEntity<RegistroPontoResponse> bater(
            @AuthenticationPrincipal FuncionarioUserDetails usuario,
            @Valid @RequestBody RegistroPontoRequest request
    ) {
        var registro = registroPontoService.registrar(usuario.getFuncionario(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RegistroPontoResponse.from(registro));
    }

    @GetMapping("/hoje")
    public List<RegistroPontoResponse> espelhoDeHoje(@AuthenticationPrincipal FuncionarioUserDetails usuario) {
        return registroPontoService.buscarRegistrosDeHoje(usuario.getFuncionario()).stream()
                .map(RegistroPontoResponse::from)
                .toList();
    }
}
