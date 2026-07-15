package com.pulsoetico.pulsoetico.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.Denuncia;
import com.pulsoetico.pulsoetico.models.dtos.DenunciaRequest;
import com.pulsoetico.pulsoetico.models.dtos.DenunciaResponse;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.DenunciaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/app/empresas/{empresaId}/denuncias")
public class DenunciaAppController {

    private final DenunciaService denunciaService;

    public DenunciaAppController(DenunciaService denunciaService) {
        this.denunciaService = denunciaService;
    }

    @PostMapping
    public ResponseEntity<DenunciaResponse> registrar(
            @PathVariable Long empresaId,
            @Valid @RequestBody DenunciaRequest request,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        Denuncia denuncia = denunciaService.registrarAnonimamente(
                request,
                principal.getFuncionario(),
                empresaId
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DenunciaResponse.from(denuncia));
    }
}