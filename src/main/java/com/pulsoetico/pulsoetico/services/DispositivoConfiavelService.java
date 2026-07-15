package com.pulsoetico.pulsoetico.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.DispositivoConfiavel;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.repositories.DispositivoConfiavelRepository;

/**
 * Controla quais dispositivos já passaram pela verificação em 2 etapas e
 * por isso podem pular essa etapa nos próximos logins (até expirar).
 *
 * O token em si (o que o app/navegador guarda) só existe em texto puro na
 * hora de ser gerado — no banco fica só o hash SHA-256 dele, igual senha.
 */
@Service
public class DispositivoConfiavelService {

    private static final int DIAS_DE_CONFIANCA = 90;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final DispositivoConfiavelRepository dispositivoConfiavelRepository;

    public DispositivoConfiavelService(DispositivoConfiavelRepository dispositivoConfiavelRepository) {
        this.dispositivoConfiavelRepository = dispositivoConfiavelRepository;
    }

    /**
     * Gera um novo token de dispositivo confiável pro funcionário e salva só o hash.
     * Retorna o token EM TEXTO PURO — essa é a única vez que ele existe assim;
     * o cliente (app/navegador) precisa guardar esse valor pra usar nos próximos logins.
     */
    @Transactional
    public String gerarNovoDispositivo(Funcionario funcionario, String descricao) {
        String tokenBruto = gerarTokenAleatorio();
        Instant agora = Instant.now();

        DispositivoConfiavel dispositivo = DispositivoConfiavel.builder()
                .funcionario(funcionario)
                .tokenHash(hash(tokenBruto))
                .descricao(descricao)
                .expiraEm(agora.plus(DIAS_DE_CONFIANCA, ChronoUnit.DAYS))
                .ultimoUsoEm(agora)
                .build();

        dispositivoConfiavelRepository.save(dispositivo);

        return tokenBruto;
    }

    /**
     * Verifica se o token de dispositivo enviado é válido (existe, não expirou,
     * pertence a esse funcionário). Se for válido, atualiza o "último uso".
     */
    @Transactional
    public boolean validarDispositivo(Funcionario funcionario, String tokenBruto) {
        if (tokenBruto == null || tokenBruto.isBlank()) {
            return false;
        }

        return dispositivoConfiavelRepository
                .findByTokenHashAndFuncionarioIdAndExpiraEmAfter(
                        hash(tokenBruto), funcionario.getId(), Instant.now())
                .map(dispositivo -> {
                    dispositivo.setUltimoUsoEm(Instant.now());
                    dispositivoConfiavelRepository.save(dispositivo);
                    return true;
                })
                .orElse(false);
    }

    private String gerarTokenAleatorio() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }
}
