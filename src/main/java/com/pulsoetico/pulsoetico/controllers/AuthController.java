package com.pulsoetico.pulsoetico.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.dtos.CadastroRequest;
import com.pulsoetico.pulsoetico.models.dtos.LoginPendenteResponse;
import com.pulsoetico.pulsoetico.models.dtos.LoginRequest;
import com.pulsoetico.pulsoetico.models.dtos.LoginResponse;
import com.pulsoetico.pulsoetico.models.dtos.VerificarCodigoRequest;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Retorna 200 com um de dois formatos (o front distingue pelo campo
     * "requerVerificacao" presente nos dois):
     *  - LoginResponse (requerVerificacao: false) — dispositivo já confiável, login concluído.
     *  - LoginPendenteResponse (requerVerificacao: true) — dispositivo novo, precisa do código.
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal FuncionarioUserDetails principal) {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verificar-codigo")
    public ResponseEntity<LoginResponse> verificarCodigo(
            @Valid @RequestBody VerificarCodigoRequest request) {
        return ResponseEntity.ok(
                authService.verificarCodigo(request));
    }

    /** Sempre pede código de verificação (nunca emite token direto) — completa com POST /verificar-codigo. */
    @PostMapping("/cadastro")
    public ResponseEntity<LoginPendenteResponse> cadastrar(
            @Valid @RequestBody CadastroRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.cadastrar(request));
    }
}