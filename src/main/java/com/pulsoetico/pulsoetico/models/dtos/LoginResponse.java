package com.pulsoetico.pulsoetico.models.dtos;

import com.pulsoetico.pulsoetico.models.Funcionario;

public record LoginResponse(
        String token,
        String nome,
        String email,
        Funcionario.Papel papel,
        Long setorId,
        String setorNome
) {
    public static LoginResponse de(String token, Funcionario funcionario) {
        return new LoginResponse(
                token,
                funcionario.getNomeCompleto(),
                funcionario.getEmail(),
                funcionario.getPapel(),
                funcionario.getSetor() != null
                ? funcionario.getSetor().getId()
                : null,
        
        funcionario.getSetor() != null
                ? funcionario.getSetor().getNome()
                : null
        );
    }
}
