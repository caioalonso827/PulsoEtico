package com.pulsoetico.pulsoetico.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.dtos.DenunciaRequest;
import com.pulsoetico.pulsoetico.models.dtos.DenunciaResponse;
import com.pulsoetico.pulsoetico.services.DenunciaService;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;

@RestController
public class DenunciaController {

    private final DenunciaService denunciaService;

    public DenunciaController(DenunciaService denunciaService) {
        this.denunciaService = denunciaService;
    }

    @PostMapping("/api/app/denuncias")
    public ResponseEntity<DenunciaResponse> registrar(
            @Valid @RequestBody DenunciaRequest request,
            Authentication authentication) {

        var denuncia = denunciaService.registrarAnonimamente(request, authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DenunciaResponse.from(denuncia));

        
    }


       @GetMapping("/api/painel/denuncias")
    public ResponseEntity<List<DenunciaResponse>> listar() {

        return ResponseEntity.ok(
                denunciaService.listar()
                        .stream()
                        .map(DenunciaResponse::from)
                        .toList()
        );
}
}