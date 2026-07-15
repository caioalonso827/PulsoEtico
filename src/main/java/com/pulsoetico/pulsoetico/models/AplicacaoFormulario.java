package com.pulsoetico.pulsoetico.models;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aplicacoes_formulario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AplicacaoFormulario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "empresa_id",
            nullable = false
    )
    private Empresa empresa;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "formulario_id",
            nullable = false
    )
    private FormularioModelo formulario;

    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "liberado_por_id",
            nullable = false
    )
    private MembroEmpresa liberadoPor;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "aplicacao_formulario_setores",
            joinColumns = @JoinColumn(
                    name = "aplicacao_id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "setor_id"
            )
    )
    @Builder.Default
    private Set<Setor> setores = new HashSet<>();

    @Column(
            name = "inicio_em",
            nullable = false
    )
    private Instant inicioEm;

    @Column(
            name = "fim_em",
            nullable = false
    )
    private Instant fimEm;

    @Column(
            name = "cancelado_em"
    )
    private Instant canceladoEm;

    @Column(
            name = "minimo_respostas",
            nullable = false
    )
    @Builder.Default
    private Integer minimoRespostas = 5;

    @Column(
            name = "criado_em",
            nullable = false,
            updatable = false
    )
    private Instant criadoEm;

    @PrePersist
    protected void aoCriar() {
        criadoEm = Instant.now();
    }


    public boolean estaAtivo() {
        return getStatusAtual()
                == StatusAplicacaoFormulario.ATIVO;
    }

        @Column(name = "encerrado_em")
        private Instant encerradoEm;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "encerrado_por_id")
        private MembroEmpresa encerradoPor;


        @Transient
        public StatusAplicacaoFormulario getStatusAtual() {
        Instant agora = Instant.now();

        if (canceladoEm != null) {
                return StatusAplicacaoFormulario.CANCELADO;
        }

        if (encerradoEm != null) {
                return StatusAplicacaoFormulario.ENCERRADO;
        }

        if (agora.isBefore(inicioEm)) {
                return StatusAplicacaoFormulario.AGENDADO;
        }

        if (!agora.isBefore(fimEm)) {
                return StatusAplicacaoFormulario.ENCERRADO;
        }

        return StatusAplicacaoFormulario.ATIVO;
        }

}