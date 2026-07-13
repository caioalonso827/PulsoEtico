package com.pulsoetico.pulsoetico.models.dtos;

import jakarta.validation.constraints.NotNull;

/**
 * Só precisa do setor — todo o resto (humor, horas extras) é calculado
 * automaticamente a partir dos dados já registrados (check-ins e ponto).
 * Rotatividade e denúncias vêm do Setor (indicadores manuais do RH).
 *
 * Esse endpoint existe pra permitir um "recalcular agora" manual; o cálculo
 * também roda sozinho, agendado, via RiskCalculationScheduler.
 */
public record CalculoRiscoRequest(

        @NotNull(message = "O setorId é obrigatório")
        Long setorId,

        /** Janela de dias pra olhar check-ins e ponto. Padrão sugerido: 20. */
        Integer diasJanela
) {
    public int diasJanelaOuPadrao() {
        return diasJanela != null ? diasJanela : 20;
    }
}
