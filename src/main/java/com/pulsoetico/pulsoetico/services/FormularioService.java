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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class FormularioService {

    private final AutorizacaoEmpresaService autorizacao;
    private final FormularioModeloRepository formularioRepository;
    private final AplicacaoFormularioRepository aplicacaoFormularioRepository;
    private final SetorRepository setorRepository;
    private final RespostaFormularioRepository respostaRepository;
    private final MembroEmpresaRepository membroEmpresaRepository;

    public FormularioService(
            AutorizacaoEmpresaService autorizacao,
            FormularioModeloRepository formularioRepository,
            AplicacaoFormularioRepository aplicacaoFormularioRepository,
            SetorRepository setorRepository,
            RespostaFormularioRepository respostaRepository,
            MembroEmpresaRepository membroEmpresaRepository
    ) {
        this.autorizacao = autorizacao;
        this.formularioRepository = formularioRepository;
        this.aplicacaoFormularioRepository = aplicacaoFormularioRepository;
        this.setorRepository = setorRepository;
        this.respostaRepository = respostaRepository;
        this.membroEmpresaRepository = membroEmpresaRepository;
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

        return aplicacaoFormularioRepository.save(aplicacao);
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
            aplicacaoFormularioRepository
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
    aplicacaoFormularioRepository.save(aplicacao);
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

    return aplicacaoFormularioRepository
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

@Transactional
public void responderFormulario(
        Long empresaId,
        Long aplicacaoId,
        ResponderFormularioRequest request,
        Funcionario usuario
) {
    MembroEmpresa membro =
            autorizacao.exigirPermissao(
                    empresaId,
                    usuario,
                    Permissoes.RESPONDER_PESQUISAS
            );

    AplicacaoFormulario aplicacao =
            aplicacaoFormularioRepository
                    .findByIdAndEmpresa_Id(
                            aplicacaoId,
                            empresaId
                    )
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Aplicação de formulário não encontrada"
                            )
                    );

    // Impede resposta em formulário cancelado,
    // encerrado, agendado ou expirado.
    validarAplicacaoAtiva(aplicacao);

    if (membro.getSetor() == null) {
        throw new IllegalArgumentException(
                "Você ainda não possui setor nesta empresa"
        );
    }

    boolean formularioLiberadoParaSetor =
            aplicacao.getSetores()
                    .stream()
                    .anyMatch(setor ->
                            setor.getId().equals(
                                    membro.getSetor().getId()
                            )
                    );

    if (!formularioLiberadoParaSetor) {
        throw new IllegalArgumentException(
                "Este formulário não foi liberado para o seu setor"
        );
    }

    if (respostaRepository
            .existsByAplicacaoIdAndMembroId(
                    aplicacaoId,
                    membro.getId()
            )) {

        throw new IllegalArgumentException(
                "Você já respondeu este formulário"
        );
    }

    if (request.respostas() == null
            || request.respostas().isEmpty()) {

        throw new IllegalArgumentException(
                "Nenhuma resposta foi enviada"
        );
    }

    Map<Long, Integer> valoresPorPergunta =
            new HashMap<>();

    for (RespostaPerguntaRequest respostaRequest
            : request.respostas()) {

        if (respostaRequest.perguntaId() == null) {
            throw new IllegalArgumentException(
                    "O ID da pergunta é obrigatório"
            );
        }

        if (respostaRequest.valor() == null
                || respostaRequest.valor() < 1
                || respostaRequest.valor() > 5) {

            throw new IllegalArgumentException(
                    "Cada resposta deve possuir um valor entre 1 e 5"
            );
        }

        if (valoresPorPergunta.put(
                respostaRequest.perguntaId(),
                respostaRequest.valor()
        ) != null) {

            throw new IllegalArgumentException(
                    "Uma mesma pergunta não pode ser respondida duas vezes"
            );
        }
    }

    List<PerguntaFormulario> perguntas =
            aplicacao.getFormulario().getPerguntas();

    if (valoresPorPergunta.size() != perguntas.size()) {
        throw new IllegalArgumentException(
                "Todas as perguntas devem ser respondidas"
        );
    }

    for (PerguntaFormulario pergunta : perguntas) {
        if (!valoresPorPergunta.containsKey(
                pergunta.getId()
        )) {
            throw new IllegalArgumentException(
                    "A pergunta " + pergunta.getId()
                            + " não foi respondida"
            );
        }
    }

    RespostaFormulario respostaFormulario =
            RespostaFormulario.builder()
                    .aplicacao(aplicacao)
                    .membro(membro)
                    .respondidoEm(Instant.now())
                    .respostas(new ArrayList<>())
                    .build();

    for (PerguntaFormulario pergunta : perguntas) {
        Integer valor =
                valoresPorPergunta.get(
                        pergunta.getId()
                );

        RespostaPergunta respostaPergunta =
                RespostaPergunta.builder()
                        .respostaFormulario(
                                respostaFormulario
                        )
                        .pergunta(pergunta)
                        .valor(valor)
                        .build();

        respostaFormulario
                .getRespostas()
                .add(respostaPergunta);
    }

    respostaRepository.save(
            respostaFormulario
    );
}

@Transactional
public void encerrarFormulario(
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
            aplicacaoFormularioRepository
                    .findByIdAndEmpresa_Id(
                            aplicacaoId,
                            empresaId
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Aplicação de formulário não encontrada"
                            )
                    );

    Instant agora = Instant.now();

    if (aplicacao.getCanceladoEm() != null) {
        throw new IllegalStateException(
                "Não é possível encerrar um formulário cancelado"
        );
    }

    if (aplicacao.getEncerradoEm() != null) {
        throw new IllegalStateException(
                "Este formulário já foi encerrado"
        );
    }

    if (!agora.isBefore(aplicacao.getFimEm())) {
        throw new IllegalStateException(
                "Este formulário já terminou pelo prazo definido"
        );
    }

    if (agora.isBefore(aplicacao.getInicioEm())) {
        throw new IllegalStateException(
                "O formulário ainda não foi iniciado. Cancele o agendamento em vez de encerrá-lo"
        );
    }

    MembroEmpresa membroAdministrador =
            membroEmpresaRepository
                    .findByEmpresa_IdAndFuncionario_Id(
                            empresaId,
                            usuario.getId()
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Membro da empresa não encontrado"
                            )
                    );

    aplicacao.setEncerradoEm(agora);
    aplicacao.setEncerradoPor(membroAdministrador);

    aplicacaoFormularioRepository.save(aplicacao);
}

private void validarAplicacaoAtiva(
        AplicacaoFormulario aplicacao
) {
    Instant agora = Instant.now();

    if (aplicacao.getCanceladoEm() != null) {
        throw new IllegalStateException(
                "Este formulário foi cancelado"
        );
    }

    if (aplicacao.getEncerradoEm() != null) {
        throw new IllegalStateException(
                "Este formulário foi encerrado e não aceita mais respostas"
        );
    }

    if (aplicacao.getInicioEm() == null
            || aplicacao.getFimEm() == null) {

        throw new IllegalStateException(
                "O período do formulário não foi configurado corretamente"
        );
    }

    if (agora.isBefore(
            aplicacao.getInicioEm()
    )) {
        throw new IllegalStateException(
                "Este formulário ainda não está disponível"
        );
    }

    if (!agora.isBefore(
            aplicacao.getFimEm()
    )) {
        throw new IllegalStateException(
                "O prazo para responder este formulário terminou"
        );
    }
}

}