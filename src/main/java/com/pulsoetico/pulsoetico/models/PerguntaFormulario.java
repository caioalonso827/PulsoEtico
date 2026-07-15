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
        name = "perguntas_formulario",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pergunta_formulario_ordem",
                columnNames = {
                        "formulario_id",
                        "ordem"
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerguntaFormulario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "formulario_id",
            nullable = false
    )
    private FormularioModelo formulario;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(nullable = false)
    private Integer ordem;
}