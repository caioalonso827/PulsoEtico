package com.pulsoetico.pulsoetico.models.dtos;

import com.pulsoetico.pulsoetico.models.RegistroPonto;

import java.time.Instant;

public record RegistroPontoResponse(
        Long id,
        RegistroPonto.TipoRegistro tipo,
        Instant horario
) {
    public static RegistroPontoResponse from(RegistroPonto registro) {
        return new RegistroPontoResponse(registro.getId(), registro.getTipo(), registro.getHorario());
    }
}
