package com.pulsoetico.pulsoetico.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
<<<<<<< HEAD
=======
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
>>>>>>> 3c99e66 (Update Brabo)
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

<<<<<<< HEAD
    @PostMapping("/api/app/denuncias")
=======
    /** Lado do TRABALHADOR: registra uma denúncia anônima. */
    @SecurityRequirements
    @PostMapping("/api/publico/denuncias")
>>>>>>> 3c99e66 (Update Brabo)
    public ResponseEntity<DenunciaResponse> registrar(
            @Valid @RequestBody DenunciaRequest request,
            Authentication authentication) {

        var denuncia = denunciaService.registrarAnonimamente(request, authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DenunciaResponse.from(denuncia));
<<<<<<< HEAD

        
=======
    }

    /** Lado do GESTOR: lista as denúncias mais recentes (qualquer setor). */
    @GetMapping("/api/painel/denuncias")
    public List<DenunciaResponse> listarRecentes() {
        return denunciaService.listarRecentes().stream()
                .map(DenunciaResponse::from)
                .toList();
    }

    /** Lado do GESTOR: marca uma denúncia como respondida/tratada. */
    @PatchMapping("/api/painel/denuncias/{id}/responder")
    public DenunciaResponse responder(@PathVariable Long id) {
        return DenunciaResponse.from(denunciaService.marcarComoRespondida(id));
>>>>>>> 3c99e66 (Update Brabo)
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