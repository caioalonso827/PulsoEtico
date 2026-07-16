package com.pulsoetico.pulsoetico.models.dtos;

import java.time.Instant;
import java.time.LocalDate;

public record CheckinHumorStatusResponse(
        boolean podeResponder,
        boolean respondidoHoje,
        LocalDate data,
        Instant proximoCheckinEm
) {
}
