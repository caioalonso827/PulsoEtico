package com.pulsoetico.pulsoetico.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.repositories.FuncionarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Valida o JWT e autentica o funcionário.
 *
 * O filtro não transforma cargo em ROLE e não decide se o usuário
 * pode acessar uma funcionalidade.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final FuncionarioRepository funcionarioRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            FuncionarioRepository funcionarioRepository
    ) {
        this.jwtService = jwtService;
        this.funcionarioRepository = funcionarioRepository;
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

            if (email != null
                    && SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                Funcionario funcionario = funcionarioRepository
                        .findByEmail(email)
                        .orElseThrow();

                if (jwtService.tokenValido(
                        token,
                        funcionario.getEmail()
                )) {
                    Long empresaId =
                            jwtService.extrairEmpresaId(token);

                    FuncionarioUserDetails userDetails =
                            new FuncionarioUserDetails(
                                    funcionario,
                                    empresaId
                            );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}