package com.pulsoetico.pulsoetico.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.dtos.LoginRequest;
import com.pulsoetico.pulsoetico.models.dtos.LoginResponse;
import com.pulsoetico.pulsoetico.repositories.FuncionarioRepository;
import com.pulsoetico.pulsoetico.security.JwtService;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final FuncionarioRepository funcionarioRepository;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            FuncionarioRepository funcionarioRepository,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.funcionarioRepository = funcionarioRepository;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Email ou senha inválidos");
        }

        Funcionario funcionario = funcionarioRepository.findByEmailWithSetor(request.email())
                .orElseThrow(() -> new BadCredentialsException("Email ou senha inválidos"));

        String token = jwtService.gerarToken(funcionario);
        return LoginResponse.de(token, funcionario);
    }
}
