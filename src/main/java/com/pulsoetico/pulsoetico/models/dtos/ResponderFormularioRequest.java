package com.pulsoetico.pulsoetico.models.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record ResponderFormularioRequest(

        @NotEmpty(
                message = "As respostas são obrigatórias"
        )
        List<@Valid RespostaPerguntaRequest> respostas

){
}

 