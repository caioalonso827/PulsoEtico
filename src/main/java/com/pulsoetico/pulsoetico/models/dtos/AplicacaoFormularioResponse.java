package com.pulsoetico.pulsoetico.models.dtos;

import java.time.Instant;
import java.util.List;

import com.pulsoetico.pulsoetico.models.StatusAplicacaoFormulario;
import com.pulsoetico.pulsoetico.models.TipoFormularioPsicossocial;

public record AplicacaoFormularioResponse(
        Long id,
        Long empresaId,
        Long formularioId,
        TipoFormularioPsicossocial tipo,
        String titulo,
        String descricao,
        List<Long> setorIds,
        Instant inicioEm,
        Instant fimEm,
        Instant canceladoEm,
        Instant encerradoEm,
        Integer minimoRespostas,
        StatusAplicacaoFormulario status,
        List<PerguntaFormularioResponse> perguntas
) {
}