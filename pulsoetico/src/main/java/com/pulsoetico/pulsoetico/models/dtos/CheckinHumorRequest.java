package com.pulsoetico.pulsoetico.models.dtos;

import com.pulsoetico.pulsoetico.models.CheckinHumor;
import jakarta.validation.constraints.NotNull;

/**
 * Note que NÃO tem setorId aqui — o setor é deduzido do funcionário
 * autenticado (token JWT), no controller/service. Evita que o cliente
 * escolha/falsifique o setor manualmente.
 */
public record CheckinHumorRequest(

        @NotNull(message = "O nível de humor é obrigatório")
        CheckinHumor.NivelHumor nivelHumor
) {
}
