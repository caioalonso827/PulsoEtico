package com.pulsoetico.pulsoetico.services;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.CodigoVerificacao;
import com.pulsoetico.pulsoetico.repositories.CodigoVerificacaoRepository;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;

@Service
public class CodigoVerificacaoService {

    private static final long TEMPO_EXPIRACAO_SEGUNDOS = 5 * 60;

    private final CodigoVerificacaoRepository codigoRepository;
    private final SecureRandom secureRandom;
    private final Resend resend;
    private final String emailRemetente;

    public CodigoVerificacaoService(
            CodigoVerificacaoRepository codigoRepository,
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from}") String emailRemetente) {
        this.codigoRepository = codigoRepository;
        this.secureRandom = new SecureRandom();
        this.resend = new Resend(apiKey);
        this.emailRemetente = emailRemetente;
    }

    @Transactional
    public void gerarEEnviarCodigo(String email) {
        String emailNormalizado = normalizarEmail(email);
        String codigo = gerarCodigo();

        CodigoVerificacao codigoVerificacao = codigoRepository
                .findTopByEmailOrderByExpiraEmDesc(emailNormalizado)
                .orElseGet(CodigoVerificacao::new);

        codigoVerificacao.setEmail(emailNormalizado);
        codigoVerificacao.setCodigo(codigo);
        codigoVerificacao.setExpiraEm(
                Instant.now().plusSeconds(TEMPO_EXPIRACAO_SEGUNDOS));
        codigoVerificacao.setUtilizado(false);

        codigoRepository.save(codigoVerificacao);

        enviarEmail(emailNormalizado, codigo);
    }

    @Transactional
    public void validarCodigo(
            String email,
            String codigoInformado) {
        String emailNormalizado = normalizarEmail(email);
        String codigoNormalizado = normalizarCodigo(codigoInformado);

        CodigoVerificacao codigoVerificacao = codigoRepository
                .findTopByEmailOrderByExpiraEmDesc(emailNormalizado)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nenhum código de verificação foi solicitado"));

        if (codigoVerificacao.isUtilizado()) {
            throw new IllegalArgumentException(
                    "Este código já foi utilizado");
        }

        if (Instant.now().isAfter(codigoVerificacao.getExpiraEm())) {
            throw new IllegalArgumentException(
                    "Código expirado. Solicite um novo código");
        }

        if (!codigoVerificacao
                .getCodigo()
                .equals(codigoNormalizado)) {

            throw new IllegalArgumentException(
                    "Código de verificação inválido");
        }

        codigoVerificacao.setUtilizado(true);
        codigoRepository.save(codigoVerificacao);
    }

    private void enviarEmail(
            String emailDestino,
            String codigo) {
        CreateEmailOptions parametros = CreateEmailOptions.builder()
                .from(emailRemetente)
                .to(emailDestino)
                .subject("Código de verificação - Pulso Ético")
                .html(
                        """
                                <div style="font-family: Arial, sans-serif;">
                                    <h2>Pulso Ético</h2>

                                    <p>Seu código de verificação é:</p>

                                    <div style="
                                        font-size: 32px;
                                        font-weight: bold;
                                        letter-spacing: 8px;
                                        margin: 24px 0;
                                    ">
                                        %s
                                    </div>

                                    <p>Este código expira em 5 minutos.</p>

                                    <p>
                                        Se você não tentou entrar,
                                        ignore esta mensagem.
                                    </p>
                                </div>
                                """.formatted(codigo))
                .build();

        try {
            resend.emails().send(parametros);
        } catch (ResendException ex) {
            throw new IllegalStateException(
                    "Não foi possível enviar o código por e-mail",
                    ex);
        }
    }

    private String gerarCodigo() {
        int numero = secureRandom.nextInt(1_000_000);
        return String.format("%06d", numero);
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "O email é obrigatório");
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null
                || !codigo.trim().matches("\\d{6}")) {

            throw new IllegalArgumentException(
                    "O código deve conter 6 dígitos");
        }

        return codigo.trim();
    }
}