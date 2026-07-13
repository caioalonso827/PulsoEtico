package com.pulsoetico.pulsoetico.models.dtos;

import java.time.Instant;

/** Data opcional; quando omitida, o desligamento ocorre no momento da requisição. */
public record DesligamentoFuncionarioRequest(Instant desligadoEm) {
}
