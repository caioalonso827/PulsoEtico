package com.pulsoetico.pulsoetico.models;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "membros_empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembroEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    /*
     * Um usuário possui somente um cargo dentro da mesma empresa.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cargo_id", nullable = false)
    private Cargo cargo;

    /*
     * Um usuário pertence a somente um setor dentro da mesma empresa.
     * Pode ficar null até o administrador escolher o setor.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "setor_id")
    private Setor setor;

    /*
     * Somente o criador da empresa recebe proprietario = true.
     */
    @Column(nullable = false)
    private boolean proprietario;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @Column(name = "entrou_em", nullable = false, updatable = false)
    private Instant entrouEm;

    @Column(name = "saiu_em")
    private Instant saiuEm;

    @PrePersist
    protected void aoEntrar() {
        entrouEm = Instant.now();
        ativo = true;
    }

    public boolean possuiPermissao(Permissoes permissao) {
        return proprietario || cargo.getPermissoes().contains(permissao);
    }
}