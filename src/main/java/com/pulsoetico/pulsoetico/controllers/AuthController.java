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

    @PostMapping("/login")
    public ResponseEntity<LoginPendenteResponse> login(
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

    @PostMapping("/cadastro")
    public ResponseEntity<LoginResponse> cadastrar(
            @Valid @RequestBody CadastroRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.cadastrar(request));
    }
}