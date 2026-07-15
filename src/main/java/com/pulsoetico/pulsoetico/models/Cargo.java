package com.pulsoetico.pulsoetico.models;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "cargos",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cargo_empresa_nome",
                columnNames = {
                        "empresa_id",
                        "nome"
                }
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

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "empresa_id",
            nullable = false
    )
    private Empresa empresa;

    @Column(
            nullable = false,
            length = 80
    )
    private String nome;

    @Column(length = 300)
    private String descricao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "cargo_permissoes",
            joinColumns = @JoinColumn(
                    name = "cargo_id"
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(
            name = "permissao",
            nullable = false
    )
    @Builder.Default
    private Set<Permissoes> permissoes =
            new HashSet<>();

    @Column(nullable = false)
    private boolean sistema;

    @Column(
            name = "criado_em",
            nullable = false,
            updatable = false
    )
    private Instant criadoEm;

    @PrePersist
    protected void aoCriar() {
        criadoEm = Instant.now();
    }
}