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
 * Recomendação automática gerada quando uma AvaliacaoRisco indica risco
 * em nível ATENCAO ou ALTO. É o que aparece no painel do gestor/RH.
 */
@Entity
@Table(name = "recomendacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recomendacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "avaliacao_risco_id", nullable = false)
    private AvaliacaoRisco avaliacaoRisco;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRecomendacao tipo;

    @Column(nullable = false)
    private boolean reconhecida;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void aoCriar() {
        this.criadoEm = Instant.now();
        this.reconhecida = false;
    }

    public enum TipoRecomendacao {
        REDUZIR_HORAS_EXTRAS,
        REDISTRIBUIR_TAREFAS,
        CONVERSA_COM_EQUIPE,
        APOIO_PSICOLOGICO,
        TREINAMENTO_LIDERANCA
    }
}