package com.pulsoetico.pulsoetico.models.dtos;

import com.pulsoetico.pulsoetico.models.Funcionario;

public record FuncionarioResponse(
        Long id,
        String nomeCompleto,
        String email,
        String matricula,
        Funcionario.Papel papel,
        Long setorId,
        String setorNome,
        boolean ativo
) {
    public static FuncionarioResponse from(Funcionario funcionario) {
        return new FuncionarioResponse(
                funcionario.getId(),
                funcionario.getNomeCompleto(),
                funcionario.getEmail(),
                funcionario.getMatricula(),
                funcionario.getPapel(),
                funcionario.getSetor().getId(),
                funcionario.getSetor().getNome(),
                funcionario.isAtivo()
        );
    }
}
