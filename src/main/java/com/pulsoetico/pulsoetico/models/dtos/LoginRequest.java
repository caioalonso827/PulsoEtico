package com.pulsoetico.pulsoetico.models.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "O email é obrigatório")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha,

        /**
         * Token de dispositivo confiável, salvo localmente numa verificação
         * anterior. Se for válido e não tiver expirado, pula a etapa do
         * código. Opcional — se não mandar (ou mandar inválido), pede código
         * normalmente (dispositivo novo/desconhecido).
         */
        String dispositivoToken
) {
}
