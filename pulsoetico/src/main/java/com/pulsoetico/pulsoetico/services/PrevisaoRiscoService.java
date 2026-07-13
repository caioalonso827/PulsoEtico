package com.pulsoetico.pulsoetico.services;

import com.pulsoetico.pulsoetico.models.AvaliacaoRisco;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.models.dtos.PrevisaoRiscoResponse;
import com.pulsoetico.pulsoetico.repositories.AvaliacaoRiscoRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Projeta a tendência de risco de um setor usando regressão linear simples
 * sobre o histórico de AvaliacaoRisco. Não é machine learning "de verdade"
 * (não há treino/aprendizado), mas é genuinamente preditivo: usa a taxa de
 * variação real do setor pra estimar o índice futuro — é a mesma ideia por
 * trás do "82% de chance de burnout em 45 dias" do pitch original.
 */
@Service
public class PrevisaoRiscoService {

    private static final double LIMITE_ALTO = 70.0;
    private static final int HORIZONTE_PROJECAO_DIAS = 45;
    private static final double MARGEM_TENDENCIA_ESTAVEL = 0.05; // variação por dia abaixo disso = "estável"

    private final AvaliacaoRiscoRepository avaliacaoRiscoRepository;

    public PrevisaoRiscoService(AvaliacaoRiscoRepository avaliacaoRiscoRepository) {
        this.avaliacaoRiscoRepository = avaliacaoRiscoRepository;
    }

    public PrevisaoRiscoResponse preverTendencia(Setor setor) {
        List<AvaliacaoRisco> historico = avaliacaoRiscoRepository.findBySetorOrderByCalculadoEmDesc(setor);
        Collections.reverse(historico); // queremos do mais antigo pro mais recente

        if (historico.size() < 2) {
            return PrevisaoRiscoResponse.dadosInsuficientes(setor.getId(), setor.getNome());
        }

        Instant primeiraData = historico.get(0).getCalculadoEm();

        double[] x = new double[historico.size()];
        double[] y = new double[historico.size()];
        for (int i = 0; i < historico.size(); i++) {
            x[i] = diasEntre(primeiraData, historico.get(i).getCalculadoEm());
            y[i] = historico.get(i).getIndiceRisco();
        }

        RegressaoLinear regressao = calcularRegressao(x, y);

        double ultimoX = x[x.length - 1];
        double indiceAtual = y[y.length - 1];
        double indiceProjetado = clamp(regressao.projetar(ultimoX + HORIZONTE_PROJECAO_DIAS));

        PrevisaoRiscoResponse.Tendencia tendencia = classificarTendencia(regressao.inclinacao);

        Integer diasAteAlto = null;
        if (tendencia == PrevisaoRiscoResponse.Tendencia.SUBINDO && indiceAtual < LIMITE_ALTO) {
            double xParaAtingirAlto = (LIMITE_ALTO - regressao.intercepto) / regressao.inclinacao;
            double diasRestantes = xParaAtingirAlto - ultimoX;
            if (diasRestantes > 0) {
                diasAteAlto = (int) Math.round(diasRestantes);
            }
        }

        return PrevisaoRiscoResponse.from(
                setor.getId(), setor.getNome(), arredondar(indiceAtual), tendencia,
                arredondar(indiceProjetado), diasAteAlto
        );
    }

    private PrevisaoRiscoResponse.Tendencia classificarTendencia(double inclinacao) {
        if (inclinacao > MARGEM_TENDENCIA_ESTAVEL) {
            return PrevisaoRiscoResponse.Tendencia.SUBINDO;
        }
        if (inclinacao < -MARGEM_TENDENCIA_ESTAVEL) {
            return PrevisaoRiscoResponse.Tendencia.CAINDO;
        }
        return PrevisaoRiscoResponse.Tendencia.ESTAVEL;
    }

    /** Regressão linear simples (mínimos quadrados): y = intercepto + inclinacao * x. */
    private RegressaoLinear calcularRegressao(double[] x, double[] y) {
        int n = x.length;
        double mediaX = media(x);
        double mediaY = media(y);

        double numerador = 0;
        double denominador = 0;
        for (int i = 0; i < n; i++) {
            numerador += (x[i] - mediaX) * (y[i] - mediaY);
            denominador += (x[i] - mediaX) * (x[i] - mediaX);
        }

        double inclinacao = denominador == 0 ? 0 : numerador / denominador;
        double intercepto = mediaY - inclinacao * mediaX;

        return new RegressaoLinear(inclinacao, intercepto);
    }

    private double media(double[] valores) {
        double soma = 0;
        for (double v : valores) {
            soma += v;
        }
        return soma / valores.length;
    }

    private double diasEntre(Instant inicio, Instant fim) {
        return (fim.toEpochMilli() - inicio.toEpochMilli()) / (1000.0 * 60 * 60 * 24);
    }

    private double clamp(double valor) {
        return Math.max(0, Math.min(100, valor));
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private record RegressaoLinear(double inclinacao, double intercepto) {
        double projetar(double x) {
            return intercepto + inclinacao * x;
        }
    }
}
