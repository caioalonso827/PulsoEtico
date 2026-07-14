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
 * Um registro de ponto (ENTRADA ou SAIDA), com foto de comprovação.
 *
 * A foto fica aqui só como prova/auditoria manual — não há verificação facial
 * automática no MVP. Esses registros também alimentam o cálculo de horas extras
 * por setor no RiskCalculationService (substituindo, no futuro, a entrada manual
 * que o RH faz hoje no CalculoRiscoRequest).
 */
@Entity
@Table(name = "registros_ponto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRegistro tipo;

    @Column(nullable = false)
    private Instant horario;

    /**
     * Selfie tirada no momento do registro, em base64.
     * Simplificação para o hack — em produção isso iria para um storage de
     * objetos (S3, Cloudinary etc), guardando só a URL aqui.
     */
    @Column(name = "foto_base64", columnDefinition = "TEXT", nullable = false)
    private String fotoBase64;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void aoCriar() {
        this.criadoEm = Instant.now();
        if (this.horario == null) {
            this.horario = this.criadoEm;
        }
    }

    public enum TipoRegistro {
        ENTRADA,
        SAIDA
    }
}