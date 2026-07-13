package com.pulsoetico.pulsoetico.models.dtos;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;

public record PrevisaoRiscoResponse(
        Long setorId,
        String setorNome,
        boolean dadosSuficientes,
        Double indiceAtual,
        Tendencia tendencia,
        Double indiceProjetadoEm45Dias,
        Integer diasEstimadosAteAltoRisco,
        String mensagem
) {
    public enum Tendencia {
        SUBINDO, ESTAVEL, CAINDO
    }

    public static PrevisaoRiscoResponse dadosInsuficientes(Long setorId, String setorNome) {
        return new PrevisaoRiscoResponse(
                setorId, setorNome, false, null, null, null, null,
                "Ainda não há avaliações suficientes desse setor para calcular uma tendência. " +
                        "São necessárias pelo menos 2 avaliações de risco no histórico."
        );
    }

    public static PrevisaoRiscoResponse from(
            Long setorId,
            String setorNome,
            double indiceAtual,
            Tendencia tendencia,
            double indiceProjetado,
            Integer diasAteAlto
    ) {
        String mensagem = switch (tendencia) {
            case SUBINDO -> diasAteAlto != null
                    ? "Tendência de alta. Nesse ritmo, o setor pode atingir risco ALTO em aproximadamente "
                        + diasAteAlto + " dias."
                    : "Tendência de alta, mas ainda distante do nível de risco ALTO.";
            case CAINDO -> "Tendência de queda. O setor está melhorando no período analisado.";
            case ESTAVEL -> "Tendência estável, sem sinais de piora ou melhora significativa.";
        };

        return new PrevisaoRiscoResponse(
                setorId, setorNome, true, indiceAtual, tendencia, indiceProjetado, diasAteAlto, mensagem
        );
    }
}
