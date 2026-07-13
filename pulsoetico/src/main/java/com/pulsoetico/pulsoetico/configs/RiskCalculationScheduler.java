package com.pulsoetico.pulsoetico.configs;

import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.repositories.SetorRepository;
import com.pulsoetico.pulsoetico.services.RiskCalculationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Roda o cálculo de risco automaticamente para TODOS os setores, sem
 * intervenção do gestor. É isso que torna a "IA" de fato automática, e não
 * só uma calculadora que alguém precisa acionar.
 *
 * Frequência: a cada 24h (cron abaixo). Pra demo/apresentação, dá pra trocar
 * por um intervalo curto (ex: a cada 5 minutos) só pra mostrar funcionando
 * ao vivo — tem uma sugestão comentada logo abaixo.
 */
@Component
public class RiskCalculationScheduler {

    private static final int DIAS_JANELA_PADRAO = 20;

    private final SetorRepository setorRepository;
    private final RiskCalculationService riskCalculationService;

    public RiskCalculationScheduler(SetorRepository setorRepository, RiskCalculationService riskCalculationService) {
        this.setorRepository = setorRepository;
        this.riskCalculationService = riskCalculationService;
    }

    // Roda 1x por dia, às 3h da manhã (fora do horário comercial).
    @Scheduled(cron = "0 0 3 * * *")
    // Pra demonstrar ao vivo na apresentação, troque a linha acima por:
    // @Scheduled(fixedRate = 5 * 60 * 1000) // a cada 5 minutos
    public void recalcularRiscoDeTodosOsSetores() {
        List<Setor> setores = setorRepository.findAll();

        for (Setor setor : setores) {
            try {
                riskCalculationService.calcularRiscoDoSetor(setor, DIAS_JANELA_PADRAO);
            } catch (Exception ex) {
                // Um setor com erro não pode travar o cálculo dos outros.
                System.err.println("Falha ao calcular risco do setor " + setor.getNome() + ": " + ex.getMessage());
            }
        }

        System.out.println(">>> Cálculo automático de risco concluído para " + setores.size() + " setor(es).");
    }
}
