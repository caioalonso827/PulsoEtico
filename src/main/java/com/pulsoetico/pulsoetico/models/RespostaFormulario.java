package com.pulsoetico.pulsoetico.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
        name = "respostas_formulario",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resposta_aplicacao_membro",
                columnNames = {
                        "aplicacao_id",
                        "membro_empresa_id"
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespostaFormulario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "aplicacao_id",
            nullable = false
    )
    private AplicacaoFormulario aplicacao;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "membro_empresa_id",
            nullable = false
    )
    private MembroEmpresa membro;

    @OneToMany(
            mappedBy = "respostaFormulario",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<RespostaPergunta> respostas =
            new ArrayList<>();

    @Column(
            name = "respondido_em",
            nullable = false,
            updatable = false
    )
    private Instant respondidoEm;

    @PrePersist
    protected void aoResponder() {
        respondidoEm = Instant.now();
    }
}