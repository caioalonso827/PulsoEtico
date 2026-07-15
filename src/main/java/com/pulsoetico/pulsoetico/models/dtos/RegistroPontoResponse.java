package com.pulsoetico.pulsoetico.models.dtos;

import java.time.Instant;

import com.pulsoetico.pulsoetico.models.RegistroPonto;

public record RegistroPontoResponse(
        Long id,
        Long empresaId,
        String empresaNome,
        Long setorId,
        String setorNome,
        RegistroPonto.TipoRegistro tipo,
        Instant horario
) {

    public static RegistroPontoResponse from(
            RegistroPonto registro
    ) {
        return new RegistroPontoResponse(
                registro.getId(),
                registro.getEmpresa() != null
                        ? registro.getEmpresa().getId()
                        : null,
                registro.getEmpresa() != null
                        ? registro.getEmpresa().getNome()
                        : null,
                registro.getSetor() != null
                        ? registro.getSetor().getId()
                        : null,
                registro.getSetor() != null
                        ? registro.getSetor().getNome()
                        : null,
                registro.getTipo(),
                registro.getHorario()
        );
    }
}