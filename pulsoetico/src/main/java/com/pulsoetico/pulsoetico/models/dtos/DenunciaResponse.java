package com.pulsoetico.pulsoetico.models.dtos;

import java.time.Instant;

import com.pulsoetico.pulsoetico.models.Denuncia;

/**
 * Nunca inclui nenhum identificador de pessoa — só setor, categoria,
 * descrição (o que a pessoa escreveu, não quem escreveu) e status.
 */
public record DenunciaResponse(
        Long id,
        Long setorId,
        String setorNome,
        String tipo,
        String descricao,
        Denuncia.StatusDenuncia status,
        Instant criadoEm
) {
    public static DenunciaResponse from(Denuncia denuncia) {
        return new DenunciaResponse(
                denuncia.getId(),
                denuncia.getSetor().getId(),
<<<<<<< HEAD
                denuncia.getDescricao(),
=======
                denuncia.getSetor().getNome(),
                denuncia.getTipo(),
                denuncia.getDescricao(),
                denuncia.getStatus(),
>>>>>>> 3c99e66 (Update Brabo)
                denuncia.getCriadoEm()
        );
    }
}
