package com.pulsoetico.pulsoetico.models;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Representa um dispositivo já verificado por código (email), que por isso
 * pode pular a verificação em 2 etapas nos próximos logins, até expirar.
 *
 * Nunca guardamos o token em texto puro — só o hash (SHA-256). O valor cru
 * só existe uma vez, na resposta pro cliente guardar localmente (igual senha).
 */
@Entity
@Table(name = "dispositivos_confiaveis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispositivoConfiavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    /** Rótulo opcional (ex: "Chrome - Windows"), pra uma futura tela de "meus dispositivos". */
    @Column(length = 150)
    private String descricao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "ultimo_uso_em")
    private Instant ultimoUsoEm;

    @PrePersist
    protected void aoCriar() {
        this.criadoEm = Instant.now();
    }
}
