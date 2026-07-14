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

    @Column(name = "codigo_expira_em")
    private Instant codigoExpiraEm;

    @PrePersist
    protected void aoCriar() {
        criadoEm = Instant.now();
    }
}