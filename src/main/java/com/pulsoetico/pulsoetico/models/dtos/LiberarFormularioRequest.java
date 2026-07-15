package com.pulsoetico.pulsoetico.models.dtos;

import java.time.Instant;
import java.util.Set;

import com.pulsoetico.pulsoetico.models.TipoFormularioPsicossocial;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record LiberarFormularioRequest(

        @NotNull
        TipoFormularioPsicossocial tipo,

        @NotEmpty
        Set<Long> setorIds,

        Instant inicioEm,

        @NotNull
        @Min(1)
        @Max(720)
        Integer duracaoHoras,

        @Min(5)
        Integer minimoRespostas

) {
}