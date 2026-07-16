package com.pulsoetico.pulsoetico.services;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.AplicacaoFormulario;
import com.pulsoetico.pulsoetico.models.FormularioModelo;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.MembroEmpresa;
import com.pulsoetico.pulsoetico.models.PerguntaFormulario;
import com.pulsoetico.pulsoetico.models.Permissoes;
import com.pulsoetico.pulsoetico.models.RespostaFormulario;
import com.pulsoetico.pulsoetico.models.RespostaPergunta;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.models.StatusAplicacaoFormulario;
import com.pulsoetico.pulsoetico.models.dtos.AlternativaFormularioResponse;
import com.pulsoetico.pulsoetico.models.dtos.AplicacaoFormularioResponse;
import com.pulsoetico.pulsoetico.models.dtos.DistribuicaoRespostaResponse;
import com.pulsoetico.pulsoetico.models.dtos.FormularioModeloResponse;
import com.pulsoetico.pulsoetico.models.dtos.FormularioResultadoResponse;
import com.pulsoetico.pulsoetico.models.dtos.LiberarFormularioRequest;
import com.pulsoetico.pulsoetico.models.dtos.PerguntaFormularioResponse;
import com.pulsoetico.pulsoetico.models.dtos.PerguntaResultadoResponse;
import com.pulsoetico.pulsoetico.models.dtos.ResponderFormularioRequest;
import com.pulsoetico.pulsoetico.models.dtos.RespostaPerguntaRequest;
import com.pulsoetico.pulsoetico.repositories.AplicacaoFormularioRepository;
import com.pulsoetico.pulsoetico.repositories.FormularioModeloRepository;
import com.pulsoetico.pulsoetico.repositories.MembroEmpresaRepository;
import com.pulsoetico.pulsoetico.repositories.RespostaFormularioRepository;
import com.pulsoetico.pulsoetico.repositories.SetorRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class FormularioService {

    private static final long DIAS_VISIBILIDADE_APOS_ENCERRAMENTO = 3;

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

    @Transactional(readOnly = true)
    public List<FormularioModeloResponse> listarModelos(
            Long empresaId,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_PESQUISAS
        );

        return formularioRepository
                .findAllByAtivoTrueOrderByTituloAsc()
                .stream()
                .map(this::converterModeloParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AplicacaoFormularioResponse> listarAplicacoes(
            Long empresaId,
            Funcionario usuario
    ) {
        autorizacao.exigirPermissao(
                empresaId,
                usuario,
                Permissoes.GERENCIAR_PESQUISAS
        );

        Instant limiteDeVisibilidade = Instant.now().minus(
                DIAS_VISIBILIDADE_APOS_ENCERRAMENTO,
                ChronoUnit.DAYS
        );

        return aplicacaoFormularioRepository
                .findVisiveisParaGestor(
                        empresaId,
                        limiteDeVisibilidade
                )
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FormularioResultadoResponse buscarResultados(
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
                                new EntityNotFoundException(
                                        "Aplicação de formulário não encontrada"
                                )
                        );

        long totalRespostas = respostaRepository
                .countByAplicacaoId(aplicacaoId);

        int minimoRespostas = aplicacao.getMinimoRespostas() == null
                ? 5
                : aplicacao.getMinimoRespostas();

        long respostasFaltantes = Math.max(
                0L,
                minimoRespostas - totalRespostas
        );

        if (totalRespostas < minimoRespostas) {
            return criarResultadoResponse(
                    aplicacao,
                    totalRespostas,
                    respostasFaltantes,
                    false,
                    "São necessárias pelo menos "
                            + minimoRespostas
                            + " respostas para preservar o anonimato. "
                            + "Ainda "
                            + (respostasFaltantes == 1
                                    ? "falta 1 resposta."
                                    : "faltam "
                                            + respostasFaltantes
                                            + " respostas."),
                    null,
                    List.of()
            );
        }

        List<RespostaFormulario> respostas = respostaRepository
                .findAllComItensByAplicacaoId(aplicacaoId);

        Map<Long, List<Integer>> valoresPorPergunta =
                new HashMap<>();

        for (RespostaFormulario resposta : respostas) {
            for (RespostaPergunta item : resposta.getRespostas()) {
                if (item.getPergunta() == null
                        || item.getPergunta().getId() == null
                        || item.getValor() == null
                        || item.getValor() < 1
                        || item.getValor() > 5) {
                    continue;
                }

                valoresPorPergunta
                        .computeIfAbsent(
                                item.getPergunta().getId(),
                                chave -> new ArrayList<>()
                        )
                        .add(item.getValor());
            }
        }

        List<PerguntaFormulario> perguntasOrdenadas = aplicacao
                .getFormulario()
                .getPerguntas()
                .stream()
                .sorted(java.util.Comparator.comparing(
                        PerguntaFormulario::getOrdem,
                        java.util.Comparator.nullsLast(
                                java.util.Comparator.naturalOrder()
                        )
                ))
                .toList();

        List<PerguntaResultadoResponse> resultadosPerguntas =
                new ArrayList<>();

        long somaGeral = 0L;
        long quantidadeValoresGeral = 0L;

        for (PerguntaFormulario pergunta : perguntasOrdenadas) {
            List<Integer> valores = valoresPorPergunta
                    .getOrDefault(
                            pergunta.getId(),
                            List.of()
                    );

            long quantidadeRespostasPergunta = valores.size();
            long somaPergunta = valores
                    .stream()
                    .mapToLong(Integer::longValue)
                    .sum();

            double mediaPergunta = quantidadeRespostasPergunta == 0
                    ? 0.0
                    : arredondarDuasCasas(
                            (double) somaPergunta
                                    / quantidadeRespostasPergunta
                    );

            List<DistribuicaoRespostaResponse> distribuicao =
                    new ArrayList<>();

            for (int valor = 1; valor <= 5; valor++) {
                int alternativaAtual = valor;

                long quantidade = valores
                        .stream()
                        .filter(item -> item == alternativaAtual)
                        .count();

                double percentual = quantidadeRespostasPergunta == 0
                        ? 0.0
                        : arredondarDuasCasas(
                                100.0
                                        * quantidade
                                        / quantidadeRespostasPergunta
                        );

                distribuicao.add(
                        new DistribuicaoRespostaResponse(
                                valor,
                                textoAlternativa(valor),
                                quantidade,
                                percentual
                        )
                );
            }

            resultadosPerguntas.add(
                    new PerguntaResultadoResponse(
                            pergunta.getId(),
                            pergunta.getTexto(),
                            pergunta.getOrdem(),
                            quantidadeRespostasPergunta,
                            mediaPergunta,
                            distribuicao
                    )
            );

            somaGeral += somaPergunta;
            quantidadeValoresGeral += quantidadeRespostasPergunta;
        }

        Double mediaGeral = quantidadeValoresGeral == 0
                ? 0.0
                : arredondarDuasCasas(
                        (double) somaGeral
                                / quantidadeValoresGeral
                );

        return criarResultadoResponse(
                aplicacao,
                totalRespostas,
                0L,
                true,
                "Resultados agregados disponíveis.",
                mediaGeral,
                resultadosPerguntas
        );
    }

    @Transactional
    public AplicacaoFormularioResponse liberar(
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

        AplicacaoFormulario salva =
                aplicacaoFormularioRepository.save(aplicacao);

        return converterParaResponse(salva);
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
public List<AplicacaoFormularioResponse> listarDisponiveis(
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
            .map(this::converterParaResponse)
            .toList();
}

@Transactional(readOnly = true)
public AplicacaoFormularioResponse buscarDisponivel(
        Long empresaId,
        Long aplicacaoId,
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

    validarAplicacaoAtiva(aplicacao);

    boolean liberadoParaSetor =
            aplicacao.getSetores()
                    .stream()
                    .anyMatch(setor ->
                            setor.getId().equals(
                                    membro.getSetor().getId()
                            )
                    );

    if (!liberadoParaSetor) {
        throw new IllegalArgumentException(
                "Este formulário não foi liberado para o seu setor"
        );
    }

    if (respostaRepository.existsByAplicacaoIdAndMembroId(
            aplicacaoId,
            membro.getId()
    )) {
        throw new IllegalArgumentException(
                "Você já respondeu este formulário"
        );
    }

    return converterParaResponse(aplicacao);
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
    private FormularioResultadoResponse criarResultadoResponse(
            AplicacaoFormulario aplicacao,
            long totalRespostas,
            long respostasFaltantes,
            boolean resultadoDisponivel,
            String mensagem,
            Double mediaGeral,
            List<PerguntaResultadoResponse> perguntas
    ) {
        List<Long> setorIds = aplicacao.getSetores()
                .stream()
                .map(Setor::getId)
                .sorted()
                .toList();

        FormularioModelo formulario = aplicacao.getFormulario();

        return new FormularioResultadoResponse(
                aplicacao.getId(),
                formulario.getId(),
                formulario.getTipo(),
                formulario.getTitulo(),
                formulario.getDescricao(),
                aplicacao.getStatusAtual(),
                setorIds,
                aplicacao.getInicioEm(),
                aplicacao.getFimEm(),
                aplicacao.getCanceladoEm(),
                aplicacao.getEncerradoEm(),
                aplicacao.getMinimoRespostas() == null
                        ? 5
                        : aplicacao.getMinimoRespostas(),
                totalRespostas,
                respostasFaltantes,
                resultadoDisponivel,
                mensagem,
                mediaGeral,
                perguntas
        );
    }

    private double arredondarDuasCasas(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private String textoAlternativa(int valor) {
        return switch (valor) {
            case 1 -> "Nunca";
            case 2 -> "Raramente";
            case 3 -> "Às vezes";
            case 4 -> "Frequentemente";
            case 5 -> "Sempre";
            default -> throw new IllegalArgumentException(
                    "Valor de alternativa inválido"
            );
        };
    }

    private List<AlternativaFormularioResponse> criarAlternativas() {
        return List.of(
                new AlternativaFormularioResponse(1, textoAlternativa(1)),
                new AlternativaFormularioResponse(2, textoAlternativa(2)),
                new AlternativaFormularioResponse(3, textoAlternativa(3)),
                new AlternativaFormularioResponse(4, textoAlternativa(4)),
                new AlternativaFormularioResponse(5, textoAlternativa(5))
        );
    }

    private FormularioModeloResponse converterModeloParaResponse(
            FormularioModelo formulario
    ) {
        List<PerguntaFormularioResponse> perguntas =
                formulario.getPerguntas()
                        .stream()
                        .sorted(
                                java.util.Comparator.comparing(
                                        PerguntaFormulario::getOrdem,
                                        java.util.Comparator.nullsLast(
                                                java.util.Comparator.naturalOrder()
                                        )
                                )
                        )
                        .map(pergunta ->
                                new PerguntaFormularioResponse(
                                        pergunta.getId(),
                                        pergunta.getTexto(),
                                        pergunta.getOrdem(),
                                        criarAlternativas()
                                )
                        )
                        .toList();

        return new FormularioModeloResponse(
                formulario.getId(),
                formulario.getTipo(),
                formulario.getTitulo(),
                formulario.getDescricao(),
                formulario.isAtivo(),
                perguntas.size(),
                perguntas
        );
    }

    private AplicacaoFormularioResponse converterParaResponse(
        AplicacaoFormulario aplicacao
) {
    FormularioModelo formulario =
            aplicacao.getFormulario();

    List<PerguntaFormularioResponse> perguntas =
            formulario.getPerguntas()
                    .stream()
                    .sorted(
                            java.util.Comparator.comparing(
                                    PerguntaFormulario::getOrdem,
                                    java.util.Comparator.nullsLast(
                                            java.util.Comparator.naturalOrder()
                                    )
                            )
                    )
                    .map(pergunta ->
                            new PerguntaFormularioResponse(
                                    pergunta.getId(),
                                    pergunta.getTexto(),
                                    pergunta.getOrdem(),
                                    criarAlternativas()
                            )
                    )
                    .toList();

    List<Long> setorIds =
            aplicacao.getSetores()
                    .stream()
                    .map(Setor::getId)
                    .sorted()
                    .toList();

    return new AplicacaoFormularioResponse(
            aplicacao.getId(),
            aplicacao.getEmpresa().getId(),
            formulario.getId(),
            formulario.getTipo(),
            formulario.getTitulo(),
            formulario.getDescricao(),
            setorIds,
            aplicacao.getInicioEm(),
            aplicacao.getFimEm(),
            aplicacao.getCanceladoEm(),
            aplicacao.getEncerradoEm(),
            aplicacao.getMinimoRespostas(),
            aplicacao.getStatusAtual(),
            perguntas
    );
}

}