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
 * Check-in de humor anônimo (a pesquisa de 30 segundos).
 * IMPORTANTE: propositalmente não existe nenhuma coluna de colaborador aqui.
 * O registro só se liga ao setor, nunca à pessoa. Isso sustenta o discurso
 * de "monitoramos o ambiente, não as pessoas" na apresentação.
 */
@Entity
@Table(name = "checkins_humor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckinHumor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    /** Escala fechada, sem texto livre, para não vazar identidade por estilo de escrita. */
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_humor", nullable = false)
    private NivelHumor nivelHumor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void aoCriar() {
        this.criadoEm = Instant.now();
    }

    public enum NivelHumor {
        MUITO_BEM(1),
        BEM(2),
        CANSADO(3),
        SOBRECARREGADO(4),
        PRECISA_AJUDA(5);

        private final int severidade;

        NivelHumor(int severidade) {
            this.severidade = severidade;
        }

        public int getSeveridade() {
            return severidade;
        }
    }
}
