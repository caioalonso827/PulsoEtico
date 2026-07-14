package com.pulsoetico.pulsoetico.models.dtos;


import jakarta.validation.constraints.NotBlank;

public record SetorRequest(

        @NotBlank(message = "O nome do setor é obrigatório")
        String nome,

        Integer quantidadeColaboradores
) {
}