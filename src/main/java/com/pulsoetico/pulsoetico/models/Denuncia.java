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
 * Denúncia anônima vinculada somente ao setor.
 * Não existe vínculo com funcionário, email, matrícula ou usuário autenticado —
 * e isso é definitivo: mesmo tendo um status (aberta/respondida) para o gestor
 * acompanhar, a denúncia NUNCA guarda quem a fez.
 */
@Entity
@Table(name = "denuncias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @Column(nullable = false, length = 80)
    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusDenuncia status = StatusDenuncia.ABERTA;

    @Column(name = "respondida_em")
    private Instant respondidaEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void aoCriar() {
        this.criadoEm = Instant.now();
        if (this.status == null) {
            this.status = StatusDenuncia.ABERTA;
        }
    }

    public enum StatusDenuncia {
        ABERTA,
        RESPONDIDA
    }
}
