package com.pulsoetico.pulsoetico.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerificarCodigoRequest(

                @NotBlank(message = "O código é obrigatório") @Pattern(regexp = "\\d{6}", message = "O código deve conter 6 dígitos") String codigo

) {
}