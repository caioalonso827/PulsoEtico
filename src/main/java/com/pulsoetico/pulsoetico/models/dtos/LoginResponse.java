package com.pulsoetico.pulsoetico.models.dtos;

import com.pulsoetico.pulsoetico.models.Funcionario;

public record LoginResponse(
        /** Sempre false aqui — existe só pra o front checar esse campo em qualquer resposta de login. */
        boolean requerVerificacao,
        String token,
        Long funcionarioId,
        String nome,
        String email,
        /**
         * Preenchido só quando um dispositivo confiável NOVO foi criado nessa
         * chamada (ex: acabou de verificar o código). O cliente deve guardar
         * esse valor e mandar em LoginRequest.dispositivoToken nos próximos
         * logins, pra pular a verificação. Quando o login já veio de um
         * dispositivo confiável existente, esse campo vem null (não precisa
         * trocar o token que o cliente já tinha). Após a verificação, a API
         * também envia o mesmo valor em cookie HttpOnly e no cabeçalho
         * X-Dispositivo-Token.
         */
        String dispositivoToken
) {
    public static LoginResponse de(String token, Funcionario funcionario, String dispositivoToken) {
        return new LoginResponse(
                false,
                token,
                funcionario.getId(),
                funcionario.getNomeCompleto(),
                funcionario.getEmail(),
                dispositivoToken
        );
    }
}
