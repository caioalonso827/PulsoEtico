package com.pulsoetico.pulsoetico.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.MembroEmpresa;
import com.pulsoetico.pulsoetico.models.Permissoes;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Gera e valida os tokens JWT.
 *
 * IMPORTANTE (correção de bug): o token agora carrega, além do email, o
 * contexto da EMPRESA ATIVA (empresaId, setorId e o papel calculado a partir
 * do Cargo/Permissoes daquele MembroEmpresa especificamente). Antes, o papel
 * vinha de Funcionario.papel, um campo GLOBAL — o que quebrava assim que uma
 * pessoa participasse de mais de uma empresa (ex: GESTOR na empresa A e
 * TRABALHADOR na empresa B não podem ser representados por um único campo).
 *
 * Existem dois cenários de emissão:
 *  - gerarToken(funcionario): sem empresa ativa (ex: logo após o cadastro,
 *    antes de criar/entrar em qualquer empresa). Papel efetivo = USUARIO.
 *  - gerarToken(funcionario, membro): com uma empresa ativa específica.
 *    O papel é derivado do Cargo/Permissoes daquele MembroEmpresa, nunca do
 *    Funcionario.papel.
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

    /** Token sem empresa ativa (ex: acabou de se cadastrar, ainda não escolheu/criou uma empresa). */
    public String gerarToken(Funcionario funcionario) {
        return construirToken(funcionario, null, null, Funcionario.Papel.USUARIO);
    }

    /**
     * Token com uma empresa ativa específica. O papel é SEMPRE recalculado a
     * partir do Cargo/Permissoes desse MembroEmpresa — nunca lido de
     * Funcionario.papel, que é um campo legado e não confiável para autorização.
     */
    public String gerarToken(Funcionario funcionario, MembroEmpresa membro) {
        Funcionario.Papel papelEfetivo = calcularPapelEfetivo(membro);
        Long setorId = membro.getSetor() != null ? membro.getSetor().getId() : null;

        return construirToken(funcionario, membro.getEmpresa().getId(), setorId, papelEfetivo);
    }

    private String construirToken(
            Funcionario funcionario,
            Long empresaId,
            Long setorId,
            Funcionario.Papel papelEfetivo
    ) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expiracaoMs);

        var tokenBuilder = Jwts.builder()
                .subject(funcionario.getEmail())
                .claim("funcionarioId", funcionario.getId())
                .claim("papel", papelEfetivo.name())
                .claim("nome", funcionario.getNomeCompleto())
                .issuedAt(agora)
                .expiration(expiracao);

        if (empresaId != null) {
            tokenBuilder.claim("empresaId", empresaId);
        }
        if (setorId != null) {
            tokenBuilder.claim("setorId", setorId);
        }

        return tokenBuilder.signWith(chave).compact();
    }

    /**
     * GESTOR = proprietário da empresa OU tem a permissão de ver o dashboard.
     * TRABALHADOR = qualquer outro membro ativo.
     * Isso substitui o antigo Funcionario.papel fixo.
     */
    private Funcionario.Papel calcularPapelEfetivo(MembroEmpresa membro) {
        boolean atuaComoGestor = membro.isProprietario()
                || membro.possuiPermissao(Permissoes.VISUALIZAR_DASHBOARD);

        return atuaComoGestor ? Funcionario.Papel.GESTOR : Funcionario.Papel.TRABALHADOR;
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public Long extrairEmpresaId(String token) {
        Object valor = extrairClaims(token).get("empresaId");
        return valor != null ? Long.valueOf(valor.toString()) : null;
    }

    public Long extrairSetorId(String token) {
        Object valor = extrairClaims(token).get("setorId");
        return valor != null ? Long.valueOf(valor.toString()) : null;
    }

    public String extrairPapel(String token) {
        return extrairClaims(token).get("papel", String.class);
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