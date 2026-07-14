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
 * Lê o header "Authorization: Bearer <token>", valida o JWT e, se for válido,
 * autentica a requisição no contexto do Spring Security.
 *
 * IMPORTANTE (correção de bug): o papel/empresa/setor usados pra montar as
 * autoridades (ROLE_GESTOR/ROLE_TRABALHADOR) e o contexto de empresa ativa
 * vêm DIRETO DAS CLAIMS DO TOKEN — não são recalculados a partir do
 * Funcionario salvo no banco. Antes, esse filtro chamava
 * FuncionarioDetailsService.loadUserByUsername(email), que montava as
 * autoridades a partir de Funcionario.papel (um campo global), ignorando
 * completamente qual empresa o token representava.
 *
 * Qualquer erro ao processar o token (expirado, mal formado, assinatura
 * inválida, ou de um usuário que não existe mais) é capturado e IGNORADO
 * silenciosamente — a requisição segue como não-autenticada.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final FuncionarioRepository funcionarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, FuncionarioRepository funcionarioRepository) {
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

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Funcionario funcionario = funcionarioRepository.findByEmailWithSetor(email)
                        .orElseThrow();

                if (jwtService.tokenValido(token, funcionario.getEmail())) {
                    Long empresaId = jwtService.extrairEmpresaId(token);
                    Long setorId = jwtService.extrairSetorId(token);
                    String papelClaim = jwtService.extrairPapel(token);
                    Funcionario.Papel papel = papelClaim != null
                            ? Funcionario.Papel.valueOf(papelClaim)
                            : Funcionario.Papel.USUARIO;

                    FuncionarioUserDetails userDetails =
                            new FuncionarioUserDetails(funcionario, empresaId, setorId, papel);

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