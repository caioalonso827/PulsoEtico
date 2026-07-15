package com.pulsoetico.pulsoetico.models.dtos;

import com.pulsoetico.pulsoetico.models.Funcionario;

public record LoginResponse(
        String token,
        Long funcionarioId,
        String nome,
        String email
) {
    public static LoginResponse de(
            String token,
            Funcionario funcionario
    ) {
        return new LoginResponse(
                token,
                funcionario.getId(),
                funcionario.getNomeCompleto(),
                funcionario.getEmail()
        );
    }
}
