package com.pulsoetico.pulsoetico.models.dtos;

import java.time.Instant;
import java.util.List;

import com.pulsoetico.pulsoetico.models.StatusAplicacaoFormulario;
import com.pulsoetico.pulsoetico.models.TipoFormularioPsicossocial;

public record FormularioResultadoResponse(
        Long aplicacaoId,
        Long formularioId,
        TipoFormularioPsicossocial tipo,
        String titulo,
        String descricao,
        StatusAplicacaoFormulario status,
        List<Long> setorIds,
        Instant inicioEm,
        Instant fimEm,
        Instant canceladoEm,
        Instant encerradoEm,
        Integer minimoRespostas,
        long totalRespostas,
        long respostasFaltantes,
        boolean resultadoDisponivel,
        String mensagem,
        Double mediaGeral,
        List<PerguntaResultadoResponse> perguntas
) {
}
