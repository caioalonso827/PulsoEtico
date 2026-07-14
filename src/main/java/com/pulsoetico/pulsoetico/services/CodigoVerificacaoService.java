package com.pulsoetico.pulsoetico.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsoetico.pulsoetico.models.CodigoVerificacao;
import com.pulsoetico.pulsoetico.repositories.CodigoVerificacaoRepository;

@Service
public class CodigoVerificacaoService {

    private static final long TEMPO_EXPIRACAO_SEGUNDOS = 5 * 60;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private final CodigoVerificacaoRepository codigoRepository;
    private final SecureRandom secureRandom;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final String brevoApiKey;
    private final String remetenteEmail;
    private final String remetenteNome;

    public CodigoVerificacaoService(
            CodigoVerificacaoRepository codigoRepository,
            ObjectMapper objectMapper,
            @Value("${brevo.api-key}") String brevoApiKey,
            @Value("${brevo.sender-email}") String remetenteEmail,
            @Value("${brevo.sender-name}") String remetenteNome) {
        this.codigoRepository = codigoRepository;
        this.objectMapper = objectMapper;
        this.brevoApiKey = brevoApiKey;
        this.remetenteEmail = remetenteEmail;
        this.remetenteNome = remetenteNome;

        this.secureRandom = new SecureRandom();

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
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
        String html = """
                <div style="font-family: Arial, sans-serif;
                            max-width: 500px;
                            margin: auto;
                            padding: 24px;">

                    <h2 style="margin-bottom: 20px;">
                        Pulso Ético
                    </h2>

                    <p>
                        Use o código abaixo para concluir seu login:
                    </p>

                    <div style="
                            font-size: 34px;
                            font-weight: bold;
                            letter-spacing: 8px;
                            margin: 28px 0;
                            padding: 18px;
                            text-align: center;
                            background: #f3f3f3;
                            border-radius: 8px;">
                        %s
                    </div>

                    <p>
                        O código expira em 5 minutos.
                    </p>

                    <p style="color: #666;">
                        Caso você não tenha tentado entrar,
                        ignore esta mensagem.
                    </p>
                </div>
                """.formatted(codigo);

        Map<String, Object> corpo = Map.of(
                "sender", Map.of(
                        "name", remetenteNome,
                        "email", remetenteEmail),
                "to", List.of(
                        Map.of(
                                "email", emailDestino)),
                "subject", "Código de verificação - Pulso Ético",
                "htmlContent", html);

        String json;

        try {
            json = objectMapper.writeValueAsString(corpo);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Não foi possível preparar o e-mail",
                    ex);
        }

        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(BREVO_URL))
                .timeout(Duration.ofSeconds(15))
                .header("accept", "application/json")
                .header("api-key", brevoApiKey)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> resposta = httpClient.send(
                    requisicao,
                    HttpResponse.BodyHandlers.ofString());

            if (resposta.statusCode() < 200
                    || resposta.statusCode() >= 300) {

                throw new IllegalStateException(
                        "A Brevo recusou o envio do e-mail. Status: "
                                + resposta.statusCode()
                                + ". Resposta: "
                                + resposta.body());
            }

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "O envio do e-mail foi interrompido",
                    ex);

        } catch (java.io.IOException ex) {
            throw new IllegalStateException(
                    "Não foi possível conectar ao serviço de e-mail",
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