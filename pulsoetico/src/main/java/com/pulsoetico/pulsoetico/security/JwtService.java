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

/**
 * Gera e valida os tokens JWT. O token carrega o email (subject), o papel
 * (TRABALHADOR/GESTOR) e o setorId — assim o resto da API sabe quem é o
 * usuário sem precisar consultar o banco a cada requisição.
 *
 * NOTA: isso substitui o services/JwtService.java antigo. Apague o arquivo
 * antigo em services/ pra não ter duas classes JwtService no projeto.
 */
@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtService(
            @Value("${jwt.secret}") String segredo,
            @Value("${jwt.expiration-ms}") long expiracaoMs
    ) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = expiracaoMs;
    }

    public String gerarToken(Funcionario funcionario) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expiracaoMs);

        return Jwts.builder()
                .subject(funcionario.getEmail())
                .claim("funcionarioId", funcionario.getId())
                .claim("papel", funcionario.getPapel().name())
                .claim("setorId", funcionario.getSetor().getId())
                .claim("nome", funcionario.getNomeCompleto())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(chave)
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public boolean tokenValido(String token, String emailEsperado) {
        String email = extrairEmail(token);
        return email.equals(emailEsperado) && !tokenExpirado(token);
    }

    private boolean tokenExpirado(String token) {
        return extrairClaims(token).getExpiration().before(new Date());
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
