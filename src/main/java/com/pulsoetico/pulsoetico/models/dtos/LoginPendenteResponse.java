package com.pulsoetico.pulsoetico.models.dtos;

public record LoginPendenteResponse(
                boolean requerVerificacao,
                String email,
                String mensagem) {
}