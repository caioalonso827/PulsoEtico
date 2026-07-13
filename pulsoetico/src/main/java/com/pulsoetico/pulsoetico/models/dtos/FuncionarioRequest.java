package com.pulsoetico.pulsoetico.models.dtos;

import com.pulsoetico.pulsoetico.models.Funcionario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FuncionarioRequest(

        @NotBlank(message = "O nome é obrigatório")
        String nomeCompleto,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha precisa ter pelo menos 6 caracteres")
        String senha,

        String matricula,

        @NotNull(message = "O papel é obrigatório")
        Funcionario.Papel papel,

        @NotNull(message = "O setorId é obrigatório")
        Long setorId
) {
}
