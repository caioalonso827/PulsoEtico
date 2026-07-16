package com.pulsoetico.pulsoetico.models;

import java.time.LocalDate;

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

/**
 * Registra somente que um usuário já realizou o check-in de humor em uma
 * empresa em determinada data.
 *
 * O nível de humor não é armazenado nesta tabela e não existe relacionamento
 * com CheckinHumor. Dessa forma, a resposta continua anônima, mas a API
 * consegue impedir mais de um envio por usuário no mesmo dia.
 */
@Entity
@Table(
        name = "controles_checkin_humor_diario",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_checkin_humor_empresa_funcionario_data",
                columnNames = {
                        "empresa_id",
                        "funcionario_id",
                        "data_checkin"
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControleCheckinHumorDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false, updatable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false, updatable = false)
    private Funcionario funcionario;

    @Column(name = "data_checkin", nullable = false, updatable = false)
    private LocalDate dataCheckin;
}
