package com.pulsoetico.pulsoetico.models;


import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    /*
     * Quando estiver null, a empresa não possui código de entrada ativo.
     */
    @Column(name = "codigo_convite", unique = true, length = 8)
    private String codigoConvite;

    @Column(name = "codigo_gerado_em")
    private Instant codigoGeradoEm;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "criado_por_id", nullable = false)
    private Funcionario criadoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void aoCriar() {
        criadoEm = Instant.now();
    }
}