package com.pulsoetico.pulsoetico.models;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "cargos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cargo_empresa_nome",
                columnNames = {"empresa_id", "nome"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(length = 300)
    private String descricao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "cargo_permissoes",
            joinColumns = @JoinColumn(name = "cargo_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permissao", nullable = false)
    @Builder.Default
    private Set<Permissoes> permissoes = new HashSet<>();

    /*
     * Administrador e Colaborador são cargos automáticos.
     */
    @Column(nullable = false)
    private boolean sistema;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void aoCriar() {
        criadoEm = Instant.now();
    }
}
