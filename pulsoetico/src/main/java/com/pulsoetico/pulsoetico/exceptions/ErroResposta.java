package com.pulsoetico.pulsoetico.exceptions;

import java.time.Instant;
import java.util.Map;

public record ErroResposta(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        Map<String, String> camposInvalidos
) {
    public static ErroResposta de(int status, String erro, String mensagem) {
        return new ErroResposta(Instant.now(), status, erro, mensagem, null);
    }

    public static ErroResposta deValidacao(int status, String erro, Map<String, String> camposInvalidos) {
        return new ErroResposta(Instant.now(), status, erro, "Um ou mais campos são inválidos", camposInvalidos);
    }
}
