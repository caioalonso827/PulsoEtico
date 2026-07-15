package com.pulsoetico.pulsoetico.services;

import com.pulsoetico.pulsoetico.repositories.*;
import com.pulsoetico.pulsoetico.models.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import com.pulsoetico.pulsoetico.models.dtos.*;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FormularioService {

    private final AutorizacaoEmpresaService autorizacao;
    private final FormularioModeloRepository formularioRepository;
    private final AplicacaoFormularioRepository aplicacaoRepository;
    private final SetorRepository setorRepository;
    private final RespostaFormularioRepository respostaRepository;

    public FormularioService(
            AutorizacaoEmpresaService autorizacao,
            FormularioModeloRepository formularioRepository,
            AplicacaoFormularioRepository aplicacaoRepository,
            SetorRepository setorRepository,
            RespostaFormularioRepository respostaRepository
    ) {
        this.autorizacao = autorizacao;
        this.formularioRepository = formularioRepository;
        this.aplicacaoRepository = aplicacaoRepository;
        this.setorRepository = setorRepository;
        this.respostaRepository = respostaRepository;
    }

    @Transactional
    public AplicacaoFormulario liberar(
            Long empresaId,
            LiberarFormularioRequest request,
            Funcionario usuario
    ) {
        MembroEmpresa administrador =
                autorizacao.exigirPermissao(
                        empresaId,
                        usuario,
                        Permissoes.GERENCIAR_PESQUISAS
                );

        FormularioModelo formulario =
                formularioRepository
                        .findByTipoAndAtivoTrue(
                                request.tipo()
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Modelo de formulário não encontrado"
                                )
                        );

        List<Setor> setores =
                setorRepository
                        .findAllByEmpresaIdAndIdIn(
                                empresaId,
                                request.setorIds()
                        );

        if (setores.size()
                != request.setorIds().size()) {

            throw new IllegalArgumentException(
                    "Um ou mais setores não pertencem à empresa"
            );
        }

        Instant inicio = request.inicioEm() == null
                ? Instant.now()
                : request.inicioEm();

        Instant fim = inicio.plus(
                request.duracaoHoras(),
                ChronoUnit.HOURS
        );

        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException(
                    "A duração do formulário é inválida"
            );
        }

        AplicacaoFormulario aplicacao =
                AplicacaoFormulario.builder()
                        .empresa(administrador.getEmpresa())
                        .formulario(formulario)
                        .liberadoPor(administrador)
                        .setores(new HashSet<>(setores))
                        .inicioEm(inicio)
                        .fimEm(fim)
                        .minimoRespostas(
                                request.minimoRespostas() == null
                                        ? 5
                                        : request.minimoRespostas()
                        )
                        .build();

        return aplicacaoRepository.save(aplicacao);
    }

@Transactional
public void cancelar(
        Long empresaId,
        Long aplicacaoId,
        Funcionario usuario
) {
    autorizacao.exigirPermissao(
            empresaId,
            usuario,
            Permissoes.GERENCIAR_PESQUISAS
    );

    AplicacaoFormulario aplicacao =
            aplicacaoRepository
                    .findByIdAndEmpresaId(
                            aplicacaoId,
                            empresaId
                    )
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Aplicação não encontrada"
                            )
                    );

    if (aplicacao.getStatusAtual()
            == StatusAplicacaoFormulario.ENCERRADO) {

        throw new IllegalArgumentException(
                "O formulário já foi encerrado"
        );
    }

    aplicacao.setCanceladoEm(Instant.now());
    aplicacaoRepository.save(aplicacao);
}
@Transactional(readOnly = true)
public List<AplicacaoFormulario> listarDisponiveis(
        Long empresaId,
        Funcionario usuario
) {
    MembroEmpresa membro =
            autorizacao.exigirPermissao(
                    empresaId,
                    usuario,
                    Permissoes.RESPONDER_PESQUISAS
            );

    if (membro.getSetor() == null) {
        throw new IllegalArgumentException(
                "Você ainda não possui setor nesta empresa"
        );
    }

    return aplicacaoRepository
            .encontrarAtivasParaSetor(
                    empresaId,
                    membro.getSetor().getId(),
                    Instant.now()
            )
            .stream()
            .filter(aplicacao ->
                    !respostaRepository
                            .existsByAplicacaoIdAndMembroId(
                                    aplicacao.getId(),
                                    membro.getId()
                            )
            )
            .toList();
}
}