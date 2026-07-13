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
 * Colaborador com login no sistema. Diferente do CheckinHumor (que é anônimo),
 * aqui a identificação é necessária: bater ponto é uma exigência legal (CLT) e
 * o gestor precisa de login para acessar o dashboard.
 *
 * Importante para a apresentação: o Funcionario se liga ao Setor (pra saber de
 * quem são as horas extras, por exemplo), mas isso NUNCA aparece junto dos dados
 * de humor/risco nos relatórios — só é usado para o registro de ponto em si.
 */
@Entity
@Table(name = "funcionarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Column(nullable = false, unique = true)
    private String email;

    /** Hash da senha (BCrypt) — nunca a senha em texto puro. */
    @Column(nullable = false)
    private String senha;

    @Column(unique = true)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Papel papel;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    /**
     * Foto de referência cadastrada no onboarding, para uma futura comparação
     * facial automática (fora do escopo do MVP do hack). Por enquanto, cada
     * RegistroPonto guarda sua própria foto como prova/auditoria manual.
     */
    @Column(name = "foto_referencia_base64", columnDefinition = "TEXT")
    private String fotoReferenciaBase64;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void aoCriar() {
        this.criadoEm = Instant.now();
        this.ativo = true;
    }

    public enum Papel {
        TRABALHADOR,
        GESTOR
    }
}