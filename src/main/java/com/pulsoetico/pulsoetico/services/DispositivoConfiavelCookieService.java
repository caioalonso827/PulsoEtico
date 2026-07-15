package com.pulsoetico.pulsoetico.services;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Faz a integração do dispositivo confiável com clientes HTTP.
 *
 * A API continua aceitando o token no JSON para aplicativos móveis, mas
 * também grava um cookie HttpOnly para que navegadores reconheçam o mesmo
 * dispositivo automaticamente nos próximos logins.
 */
@Service
public class DispositivoConfiavelCookieService {

    public static final String HEADER_DISPOSITIVO_TOKEN =
            "X-Dispositivo-Token";

    private final String nomeCookie;
    private final boolean cookieSeguro;
    private final String sameSite;
    private final Duration duracao;

    public DispositivoConfiavelCookieService(
            @Value("${auth.dispositivo.cookie-name:pulso_dispositivo}")
            String nomeCookie,
            @Value("${auth.dispositivo.cookie-secure:false}")
            boolean cookieSeguro,
            @Value("${auth.dispositivo.cookie-same-site:Lax}")
            String sameSite,
            @Value("${auth.dispositivo.dias-confianca:90}")
            long diasConfianca
    ) {
        if (diasConfianca <= 0) {
            throw new IllegalArgumentException(
                    "auth.dispositivo.dias-confianca deve ser maior que zero"
            );
        }

        this.nomeCookie = nomeCookie;
        this.cookieSeguro = cookieSeguro;
        this.sameSite = normalizarSameSite(sameSite);
        this.duracao = Duration.ofDays(diasConfianca);
    }

    /**
     * Prioridade: token enviado no JSON, depois cabeçalho e, por último,
     * cookie persistente do navegador.
     */
    public String resolverToken(
            String tokenDoCorpo,
            String tokenDoCabecalho,
            Cookie[] cookies
    ) {
        String token = normalizarToken(tokenDoCorpo);

        if (token != null) {
            return token;
        }

        token = normalizarToken(tokenDoCabecalho);

        if (token != null) {
            return token;
        }

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (nomeCookie.equals(cookie.getName())) {
                return normalizarToken(cookie.getValue());
            }
        }

        return null;
    }

    public void gravarCookie(
            HttpServletResponse response,
            String dispositivoToken
    ) {
        String token = normalizarToken(dispositivoToken);

        if (token == null) {
            return;
        }

        ResponseCookie cookie = ResponseCookie
                .from(nomeCookie, token)
                .httpOnly(true)
                .secure(cookieSeguro)
                .sameSite(sameSite)
                .path("/api/auth")
                .maxAge(duracao)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );

        // Mantém compatibilidade com clientes móveis que preferem ler header.
        response.setHeader(
                HEADER_DISPOSITIVO_TOKEN,
                token
        );
    }

    public void apagarCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from(nomeCookie, "")
                .httpOnly(true)
                .secure(cookieSeguro)
                .sameSite(sameSite)
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }

    private String normalizarToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        return token.trim();
    }

    private String normalizarSameSite(String valor) {
        if (valor == null) {
            return "Lax";
        }

        return switch (valor.trim().toLowerCase()) {
            case "strict" -> "Strict";
            case "none" -> "None";
            default -> "Lax";
        };
    }
}
