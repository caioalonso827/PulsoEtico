package com.pulsoetico.pulsoetico.services;

import java.security.SecureRandom;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.CodigoVerificacao;
import com.pulsoetico.pulsoetico.repositories.CodigoVerificacaoRepository;

@Service
public class CodigoVerificacaoService {

    private static final long TEMPO_EXPIRACAO_SEGUNDOS = 5 * 60;

    private final CodigoVerificacaoRepository codigoRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom;

    @Value("${spring.mail.username}")
    private String emailRemetente;

    public CodigoVerificacaoService(
            CodigoVerificacaoRepository codigoRepository,
            JavaMailSender mailSender) {
        this.codigoRepository = codigoRepository;
        this.mailSender = mailSender;
        this.secureRandom = new SecureRandom();
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
    public void validarCodigo(String email, String codigoInformado) {
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

        if (!codigoVerificacao.getCodigo().equals(codigoNormalizado)) {
            throw new IllegalArgumentException(
                    "Código de verificação inválido");
        }

        codigoVerificacao.setUtilizado(true);
        codigoRepository.save(codigoVerificacao);
    }

    private String gerarCodigo() {
        int numero = secureRandom.nextInt(1_000_000);
        return String.format("%06d", numero);
    }

    private void enviarEmail(String emailDestino, String codigo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setFrom(emailRemetente);
        mensagem.setTo(emailDestino);
        mensagem.setSubject("Código de verificação - Pulso Ético");
        mensagem.setText(
                "Seu código de verificação é: " + codigo
                        + "\n\nO código expira em 5 minutos."
                        + "\n\nSe você não tentou entrar, ignore esta mensagem.");

        mailSender.send(mensagem);
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "O email é obrigatório");
        }

        return email.trim().toLowerCase();
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null || !codigo.trim().matches("\\d{6}")) {
            throw new IllegalArgumentException(
                    "O código deve conter 6 dígitos");
        }

        return codigo.trim();
    }
}