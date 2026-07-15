package com.pulsoetico.pulsoetico.models.dtos;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import com.pulsoetico.pulsoetico.models.dtos.*;

public record ResponderFormularioRequest(
        @NotEmpty
        List<RespostaItemRequest> respostas
) {
}