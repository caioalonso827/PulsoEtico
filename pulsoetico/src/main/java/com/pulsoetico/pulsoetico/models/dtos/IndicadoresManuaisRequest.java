package com.pulsoetico.pulsoetico.models.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Indicadores que o RH atualiza periodicamente (ex: 1x por mês), porque ainda
 * não vêm de nenhuma integração automática (folha de pagamento, sistema de
 * desligamento). O job automático de risco lê o último valor salvo aqui.
 */
public record IndicadoresManuaisRequest(

        @NotNull(message = "A taxa de rotatividade é obrigatória")
        @Min(value = 0, message = "Rotatividade não pode ser negativa")
        Double taxaRotatividadeMensal,

        @NotNull(message = "A quantidade de denúncias é obrigatória")
        @Min(value = 0, message = "Quantidade de denúncias não pode ser negativa")
        Integer quantidadeDenunciasAnonimasMensal
) {
}
