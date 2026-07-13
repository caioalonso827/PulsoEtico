package com.pulsoetico.pulsoetico.models;


import java.time.Instant;

import jakarta.persistence.Column;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Snapshot do Índice de Risco Psicossocial calculado para um setor em um dado momento.
 * Guardamos o histórico (em vez de sobrescrever) para permitir o gráfico de tendência
 * ("horas extras subindo, humor caindo nos últimos 20 dias") mencionado na ideia original.
 */
@Entity
@Table(name = "avaliacoes_risco")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoRisco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    /** Índice de 0 a 100. Quanto maior, maior o risco psicossocial do setor. */
    @Column(name = "indice_risco", nullable = false)
    private Double indiceRisco;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_risco", nullable = false)
    private NivelRisco nivelRisco;

    /** Componentes que entraram no cálculo, guardados para explicabilidade (útil na banca). */
    @Column(name = "media_horas_extras")
    private Double mediaHorasExtras;

    @Column(name = "media_severidade_humor")
    private Double mediaSeveridadeHumor;

    @Column(name = "taxa_rotatividade")
    private Double taxaRotatividade;

    @Column(name = "quantidade_denuncias_anonimas")
    private Integer quantidadeDenunciasAnonimas;

    @Column(name = "calculado_em", nullable = false, updatable = false)
    private Instant calculadoEm;

    @PrePersist
    protected void aoCriar() {
        this.calculadoEm = Instant.now();
    }

    public enum NivelRisco {
        BAIXO,   // 🟢
        ATENCAO, // 🟡
        ALTO     // 🔴
    }
}