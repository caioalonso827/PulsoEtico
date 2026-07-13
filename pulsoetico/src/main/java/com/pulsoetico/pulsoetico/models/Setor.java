package com.pulsoetico.pulsoetico.models;


import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa um setor/equipe da empresa (ex: Financeiro, RH, Logística).
 * Nunca referenciamos colaboradores individuais aqui — apenas o setor,
 * para garantir que toda a análise de risco seja feita de forma agregada e anônima.
 */
@Entity
@Table(name = "setores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    /** Número aproximado de colaboradores no setor (usado para normalizar índices). */
    @Column(name = "quantidade_colaboradores")
    private Integer quantidadeColaboradores;

    /**
     * Indicadores "lentos" que o RH atualiza periodicamente (mensal, por ex),
     * já que ainda não vêm de nenhum sistema automatizado (folha de pagamento,
     * sistema de desligamento etc). O cálculo de risco lê esses valores direto
     * daqui — RH não precisa digitar isso toda vez que o índice é recalculado.
     */
    @Column(name = "taxa_rotatividade_mensal")
    @Builder.Default
    private Double taxaRotatividadeMensal = 0.0;

    @Column(name = "quantidade_denuncias_anonimas_mensal")
    @Builder.Default
    private Integer quantidadeDenunciasAnonimasMensal = 0;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void aoCriar() {
        this.criadoEm = Instant.now();
    }
}
