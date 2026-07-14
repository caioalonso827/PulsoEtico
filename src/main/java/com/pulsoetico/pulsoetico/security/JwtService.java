package com.pulsoetico.pulsoetico.security;

import com.pulsoetico.pulsoetico.models.Funcionario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

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
        Date agora = new Date();
        Date expiracao = new Date(
                agora.getTime() + expiracaoMs
        );

        var tokenBuilder = Jwts.builder()
                .subject(funcionario.getEmail())
                .claim("funcionarioId", funcionario.getId())
                .claim("papel", funcionario.getPapel().name())
                .claim("nome", funcionario.getNomeCompleto())
                .issuedAt(agora)
                .expiration(expiracao);

        /*
         * Usuários recém-cadastrados podem ainda não possuir setor.
         */
        if (funcionario.getSetor() != null) {
            tokenBuilder.claim(
                    "setorId",
                    funcionario.getSetor().getId()
            );
        }

        return tokenBuilder
                .signWith(chave)
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public boolean tokenValido(
            String token,
            String emailEsperado
    ) {
        String email = extrairEmail(token);

        return email.equals(emailEsperado)
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