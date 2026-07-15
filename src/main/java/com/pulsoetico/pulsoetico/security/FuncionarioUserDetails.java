package com.pulsoetico.pulsoetico.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.pulsoetico.pulsoetico.models.Funcionario;

/**
 * Não cria ROLE_GESTOR ou ROLE_TRABALHADOR.
 *
 * Cada ação consulta o vínculo MembroEmpresa e as permissões do cargo
 * dentro da empresa informada na rota.
 */
public class FuncionarioUserDetails implements UserDetails {

    private final Funcionario funcionario;
    private final Long empresaIdAtual;

    public FuncionarioUserDetails(Funcionario funcionario) {
        this(funcionario, null);
    }

    public FuncionarioUserDetails(
            Funcionario funcionario,
            Long empresaIdAtual
    ) {
        this.funcionario = funcionario;
        this.empresaIdAtual = empresaIdAtual;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public Long getEmpresaIdAtual() {
        return empresaIdAtual;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
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