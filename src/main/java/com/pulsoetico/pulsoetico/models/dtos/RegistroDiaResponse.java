package com.pulsoetico.pulsoetico.models.dtos;

import java.time.Instant;

public record RegistroDiaResponse(
        String colaborador,
        String setorNome,
        Instant entrada,
        Instant saida,
        Double horasExtras,
        String status
) {
    public enum Status {
        COMPLETO, INCOMPLETO, FALTA
    }
}
