package com.pulsoetico.pulsoetico.models.dtos;

import java.util.List;

public record JornadaResumoResponse(
        double horasExtrasMes,
        long faltasMes,
        String jornadaMediaFormatada,
        double complianceNR1Percentual,
        List<HorasPorDia> horasTrabalhadasPorDiaDaSemana
) {
    /** Um ponto do gráfico "Horas trabalhadas na semana" (Seg a Dom). */
    public record HorasPorDia(String diaAbreviado, double mediaHoras) {
    }
}
