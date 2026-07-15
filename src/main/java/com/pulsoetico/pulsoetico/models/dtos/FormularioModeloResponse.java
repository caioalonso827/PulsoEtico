package com.pulsoetico.pulsoetico.models.dtos;

import java.util.List;

import com.pulsoetico.pulsoetico.models.TipoFormularioPsicossocial;

public record FormularioModeloResponse(
        Long id,
        TipoFormularioPsicossocial tipo,
        String titulo,
        String descricao,
        boolean ativo,
        int quantidadePerguntas,
        List<PerguntaFormularioResponse> perguntas
) {
}
