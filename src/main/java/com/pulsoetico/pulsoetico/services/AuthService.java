package com.pulsoetico.pulsoetico.services;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.dtos.CadastroRequest;
import com.pulsoetico.pulsoetico.models.dtos.LoginRequest;
import com.pulsoetico.pulsoetico.models.dtos.LoginResponse;
import com.pulsoetico.pulsoetico.repositories.FuncionarioRepository;
import com.pulsoetico.pulsoetico.security.JwtService;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final FuncionarioRepository funcionarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            FuncionarioRepository funcionarioRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.funcionarioRepository = funcionarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {

    String email = request.email()
            .trim()
            .toLowerCase(Locale.ROOT);

    try {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.senha()
                )
        );
    } catch (org.springframework.security.core.AuthenticationException ex) {
        throw new BadCredentialsException(
                "Email ou senha inválidos"
        );
    }

    Funcionario funcionario = funcionarioRepository
            .findByEmailWithSetor(email)
            .orElseThrow(() ->
                    new BadCredentialsException(
                            "Email ou senha inválidos"
                    )
            );

    String token = jwtService.gerarToken(funcionario);

    return LoginResponse.de(
            token,
            funcionario
    );
}

        @Transactional
        public LoginResponse cadastrar(CadastroRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase();

        if (funcionarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Já existe uma conta com esse email"
            );
        }

        Funcionario funcionario = Funcionario.builder()
                .nomeCompleto(request.nomeCompleto().trim())
                .email(email)
                .senha(passwordEncoder.encode(request.senha()))
                .papel(Funcionario.Papel.USUARIO)
                .build();

        funcionarioRepository.save(funcionario);

        String token = jwtService.gerarToken(funcionario);

        return LoginResponse.de(token, funcionario);
    }
}
