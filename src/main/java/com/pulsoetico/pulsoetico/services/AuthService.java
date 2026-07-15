package com.pulsoetico.pulsoetico.services;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pulsoetico.pulsoetico.models.CodigoVerificacao;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.dtos.CadastroRequest;
import com.pulsoetico.pulsoetico.models.dtos.LoginPendenteResponse;
import com.pulsoetico.pulsoetico.models.dtos.LoginRequest;
import com.pulsoetico.pulsoetico.models.dtos.LoginResponse;
import com.pulsoetico.pulsoetico.models.dtos.VerificarCodigoRequest;
import com.pulsoetico.pulsoetico.repositories.FuncionarioRepository;
import com.pulsoetico.pulsoetico.repositories.MembroEmpresaRepository;
import com.pulsoetico.pulsoetico.security.JwtService;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final FuncionarioRepository funcionarioRepository;
    private final MembroEmpresaRepository membroEmpresaRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CodigoVerificacaoService codigoVerificacaoService;

    public AuthService(
            AuthenticationManager authenticationManager,
            FuncionarioRepository funcionarioRepository,
            MembroEmpresaRepository membroEmpresaRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            CodigoVerificacaoService codigoVerificacaoService
    ) {
        this.authenticationManager = authenticationManager;
        this.funcionarioRepository = funcionarioRepository;
        this.membroEmpresaRepository = membroEmpresaRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.codigoVerificacaoService = codigoVerificacaoService;
    }

    public LoginPendenteResponse login(LoginRequest request) {
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

        funcionarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Email ou senha inválidos"
                        )
                );

        codigoVerificacaoService.gerarEEnviarCodigo(email);

        return new LoginPendenteResponse(
                true,
                email,
                "Código enviado para o email"
        );
    }

    public LoginResponse verificarCodigo(
            VerificarCodigoRequest request
    ) {
        CodigoVerificacao codigoVerificacao =
                codigoVerificacaoService.validarCodigo(
                        request.codigo()
                );

        Funcionario funcionario = funcionarioRepository
                .findByEmail(codigoVerificacao.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Usuário não encontrado"
                        )
                );

        String token = membroEmpresaRepository
                .findFirstByFuncionarioIdAndAtivoTrueOrderByEntrouEmAsc(
                        funcionario.getId()
                )
                .map(membro ->
                        jwtService.gerarToken(
                                funcionario,
                                membro
                        )
                )
                .orElseGet(() ->
                        jwtService.gerarToken(funcionario)
                );

        return LoginResponse.de(token, funcionario);
    }

    @Transactional
    public LoginResponse cadastrar(CadastroRequest request) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (funcionarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Já existe uma conta com esse email"
            );
        }

        Funcionario funcionario = Funcionario.builder()
                .nomeCompleto(request.nomeCompleto().trim())
                .email(email)
                .senha(passwordEncoder.encode(request.senha()))
                .build();

        funcionarioRepository.save(funcionario);

        String token = jwtService.gerarToken(funcionario);

        return LoginResponse.de(token, funcionario);
    }
}
