package com.pulsoetico.pulsoetico.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.pulsoetico.pulsoetico.models.Funcionario;

/**
 * Adapta Funcionario + o CONTEXTO DA EMPRESA ATIVA (vindo do token JWT) para
 * o contrato que o Spring Security espera.
 *
 * IMPORTANTE (correção de bug): empresaIdAtual, setorIdAtual e papelAtual vêm
 * das claims do JWT (resolvidas no momento do login/entrada na empresa a
 * partir do Cargo/Permissoes daquele MembroEmpresa) — NUNCA de
 * funcionario.getPapel()/getSetor(), que são campos globais e ficam
 * desatualizados assim que a pessoa participa de mais de uma empresa.
 */
public class FuncionarioUserDetails implements UserDetails {

    private final Funcionario funcionario;
    private final Long empresaIdAtual;
    private final Long setorIdAtual;
    private final Funcionario.Papel papelAtual;

    /** Construtor usado só no passo de validação de senha (login), sem contexto de empresa ainda. */
    public FuncionarioUserDetails(Funcionario funcionario) {
        this(funcionario, null, null, Funcionario.Papel.USUARIO);
    }

    /** Construtor usado pelo JwtAuthenticationFilter, com o contexto extraído do token. */
    public FuncionarioUserDetails(
            Funcionario funcionario,
            Long empresaIdAtual,
            Long setorIdAtual,
            Funcionario.Papel papelAtual
    ) {
        this.funcionario = funcionario;
        this.empresaIdAtual = empresaIdAtual;
        this.setorIdAtual = setorIdAtual;
        this.papelAtual = papelAtual != null ? papelAtual : Funcionario.Papel.USUARIO;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    /** Empresa em que o usuário está atuando NESSA sessão/token. Pode ser null (sem empresa ativa). */
    public Long getEmpresaIdAtual() {
        return empresaIdAtual;
    }

    /** Setor do usuário NAQUELA empresa (vindo de MembroEmpresa.setor, não de Funcionario.setor). */
    public Long getSetorIdAtual() {
        return setorIdAtual;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + papelAtual.name()));
    }

    @Override
    public String getPassword() {
        return funcionario.getSenha();
    }

    @Override
    public String getUsername() {
        return funcionario.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return funcionario.isAtivo();
    }
}