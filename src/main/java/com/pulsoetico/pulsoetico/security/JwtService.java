package com.pulsoetico.pulsoetico.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.MembroEmpresa;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * O token identifica o funcionário e pode guardar a empresa selecionada.
 *
 * Ele não guarda ROLE_GESTOR, ROLE_TRABALHADOR, cargo ou permissões.
 */
@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtService(
            @Value("${jwt.secret}") String segredo,
            @Value("${jwt.expiration-ms}") long expiracaoMs
    ) {
        this.chave = Keys.hmacShaKeyFor(
                segredo.getBytes(StandardCharsets.UTF_8)
        );

        this.expiracaoMs = expiracaoMs;
    }

    public String gerarToken(Funcionario funcionario) {
        return construirToken(funcionario, null);
    }

    public String gerarToken(
            Funcionario funcionario,
            MembroEmpresa membro
    ) {
        Long empresaId = membro == null
                ? null
                : membro.getEmpresa().getId();

        return construirToken(funcionario, empresaId);
    }

    private String construirToken(
            Funcionario funcionario,
            Long empresaId
    ) {
        Date agora = new Date();

        Date expiracao = new Date(
                agora.getTime() + expiracaoMs
        );

        var tokenBuilder = Jwts.builder()
                .subject(funcionario.getEmail())
                .claim("funcionarioId", funcionario.getId())
                .claim("nome", funcionario.getNomeCompleto())
                .issuedAt(agora)
                .expiration(expiracao);

        if (empresaId != null) {
            tokenBuilder.claim("empresaId", empresaId);
        }

        return tokenBuilder
                .signWith(chave)
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public Long extrairEmpresaId(String token) {
        Object valor = extrairClaims(token).get("empresaId");

        return valor == null
                ? null
                : Long.valueOf(valor.toString());
    }

    public boolean tokenValido(
            String token,
            String emailEsperado
    ) {
        String email = extrairEmail(token);

        return email != null
                && email.equalsIgnoreCase(emailEsperado)
                && !tokenExpirado(token);
    }

    private boolean tokenExpirado(String token) {
        return extrairClaims(token)
                .getExpiration()
                .before(new Date());
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}