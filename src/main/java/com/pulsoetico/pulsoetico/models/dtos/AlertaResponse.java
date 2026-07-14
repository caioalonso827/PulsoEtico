package com.pulsoetico.pulsoetico.models.dtos;

import java.time.Instant;

/**
 * Feed unificado de alertas do dashboard: mistura denúncias reais (anônimas)
 * com recomendações automáticas geradas pela avaliação de risco. Nenhuma das
 * duas fontes carrega identificador de pessoa.
 */
public record AlertaResponse(
        Long id,
        Origem origem,
        String setorNome,
        String mensagem,
        Instant criadoEm
) {
    public enum Origem {
        DENUNCIA, RECOMENDACAO
    }
}
