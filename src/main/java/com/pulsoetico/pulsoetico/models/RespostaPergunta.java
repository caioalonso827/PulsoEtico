package com.pulsoetico.pulsoetico.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "respostas_pergunta",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resposta_pergunta",
                columnNames = {
                        "resposta_formulario_id",
                        "pergunta_id"
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespostaPergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "resposta_formulario_id",
            nullable = false
    )
    private RespostaFormulario respostaFormulario;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "pergunta_id",
            nullable = false
    )
    private PerguntaFormulario pergunta;

    @Column(nullable = false)
    private Integer valor;
}