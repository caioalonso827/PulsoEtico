package com.pulsoetico.pulsoetico.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DenunciaRequest(

        @NotBlank(message = "A categoria é obrigatória")
        @Size(max = 80, message = "A categoria deve ter no máximo 80 caracteres")
        String tipo,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String descricao
) {
}
