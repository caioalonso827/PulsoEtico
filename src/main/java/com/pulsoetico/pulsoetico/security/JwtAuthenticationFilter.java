package com.pulsoetico.pulsoetico.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lê o header "Authorization: Bearer <token>", valida o JWT e, se for válido,
 * autentica a requisição no contexto do Spring Security.
 *
 * IMPORTANTE: qualquer erro ao processar o token (expirado, mal formado,
 * assinatura inválida, ou de um usuário que não existe mais) é capturado e
 * IGNORADO silenciosamente — a requisição segue como não-autenticada, e quem
 * decide se isso é problema é a regra de autorização do SecurityConfig
 * (permitAll, hasRole etc), não esse filtro. Sem isso, um token velho/inválido
 * quebra até rotas públicas como o login.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final FuncionarioDetailsService funcionarioDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, FuncionarioDetailsService funcionarioDetailsService) {
        this.jwtService = jwtService;
        this.funcionarioDetailsService = funcionarioDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extrairEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = funcionarioDetailsService.loadUserByUsername(email);

                if (jwtService.tokenValido(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // Token inválido/expirado/de usuário inexistente: segue sem autenticar.
            // As regras do SecurityConfig decidem se a rota exige login ou não.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}