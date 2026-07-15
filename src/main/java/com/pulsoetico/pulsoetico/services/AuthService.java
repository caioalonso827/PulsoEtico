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

/**
 * Verificação em 2 etapas (código por email) só acontece em 2 situações:
 *   1) No cadastro (cadastrar() não emite token direto — manda pro fluxo de código)
 *   2) No login de um dispositivo desconhecido (sem dispositivoToken válido)
 *
 * Um dispositivo já verificado antes pula a etapa do código nos próximos
 * logins, até o token de confiança expirar (90 dias) — ver DispositivoConfiavelService.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final FuncionarioRepository funcionarioRepository;
    private final MembroEmpresaRepository membroEmpresaRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CodigoVerificacaoService codigoVerificacaoService;
    private final DispositivoConfiavelService dispositivoConfiavelService;

    public AuthService(
            AuthenticationManager authenticationManager,
            FuncionarioRepository funcionarioRepository,
            MembroEmpresaRepository membroEmpresaRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            CodigoVerificacaoService codigoVerificacaoService,
            DispositivoConfiavelService dispositivoConfiavelService
    ) {
        this.authenticationManager = authenticationManager;
        this.funcionarioRepository = funcionarioRepository;
        this.membroEmpresaRepository = membroEmpresaRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.codigoVerificacaoService = codigoVerificacaoService;
        this.dispositivoConfiavelService = dispositivoConfiavelService;
    }

    /**
     * Retorna LoginResponse (login concluído, dispositivo já confiável) ou
     * LoginPendenteResponse (dispositivo novo/desconhecido, precisa do código).
     * O front distingue os dois pelo campo "requerVerificacao" em ambos.
     */
    public Object login(LoginRequest request) {
        return login(request, request.dispositivoToken());
    }

    /**
     * Versão usada pelo controller quando o token foi recuperado de cookie ou
     * cabeçalho. O cadastro não chama este método e, portanto, continua sempre
     * exigindo código.
     */
    public Object login(
            LoginRequest request,
            String dispositivoToken
    ) {
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

        Funcionario funcionario = funcionarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Email ou senha inválidos"
                        )
                );

        boolean dispositivoConfiavel =
                dispositivoConfiavelService.validarDispositivo(
                        funcionario,
                        dispositivoToken
                );

        if (dispositivoConfiavel) {
            // Já verificado nesse dispositivo antes (e ainda dentro dos 90 dias) — pula o código.
            String token = gerarTokenComEmpresaAtiva(funcionario);
            return LoginResponse.de(token, funcionario, null);
        }

        // Dispositivo novo/desconhecido (ou token de dispositivo ausente/expirado) — pede código.
        codigoVerificacaoService.gerarEEnviarCodigo(email);

        return new LoginPendenteResponse(
                true,
                email,
                "Código enviado para o email"
        );
    }

    /** Confirma o código (de um login pendente OU de um cadastro recém-feito) e emite o token. */
    public LoginResponse verificarCodigo(VerificarCodigoRequest request) {
        return verificarCodigo(request, null);
    }

    public LoginResponse verificarCodigo(
            VerificarCodigoRequest request,
            String descricaoDispositivo
    ) {
        CodigoVerificacao codigoVerificacao =
                codigoVerificacaoService.validarCodigo(request.codigo());

        Funcionario funcionario = funcionarioRepository
                .findByEmail(codigoVerificacao.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException("Usuário não encontrado")
                );

        String token = gerarTokenComEmpresaAtiva(funcionario);

        // Verificação concluída com sucesso = dispositivo passa a ser confiável por 90 dias.
        String dispositivoToken =
                dispositivoConfiavelService.gerarNovoDispositivo(
                        funcionario,
                        descricaoDispositivo
                );

        return LoginResponse.de(token, funcionario, dispositivoToken);
    }

    /**
     * Cria a conta mas NÃO emite token — sempre manda pro fluxo de código
     * (verificação obrigatória no cadastro), reaproveitando o mesmo
     * verificarCodigo() de cima pra concluir e já sair com um dispositivo confiável.
     */
    @Transactional
    public LoginPendenteResponse cadastrar(CadastroRequest request) {
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

        codigoVerificacaoService.gerarEEnviarCodigo(email);

        return new LoginPendenteResponse(
                true,
                email,
                "Conta criada! Enviamos um código de verificação para o seu email."
        );
    }

    private String gerarTokenComEmpresaAtiva(Funcionario funcionario) {
        return membroEmpresaRepository
                .findFirstByFuncionarioIdAndAtivoTrueOrderByEntrouEmAsc(funcionario.getId())
                .map(membro -> jwtService.gerarToken(funcionario, membro))
                .orElseGet(() -> jwtService.gerarToken(funcionario));
    }
}
