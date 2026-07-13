package com.pulsoetico.pulsoetico.models.dtos;

import java.time.Instant;

import com.pulsoetico.pulsoetico.models.Denuncia;

public record DenunciaResponse(
        Long id,
        Long setorId,
        String mensagem,
        Instant criadoEm
) {
    public static DenunciaResponse from(Denuncia denuncia) {
        return new DenunciaResponse(
                denuncia.getId(),
                denuncia.getSetor().getId(),
                "Denúncia registrada anonimamente",
                denuncia.getCriadoEm()
        );
    }
}
