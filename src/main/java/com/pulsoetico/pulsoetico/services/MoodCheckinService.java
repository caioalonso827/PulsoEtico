package com.pulsoetico.pulsoetico.services;

import com.pulsoetico.pulsoetico.models.CheckinHumor;
import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.dtos.CheckinHumorRequest;
import com.pulsoetico.pulsoetico.repositories.CheckinHumorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra o check-in de humor. Recebe o Funcionario só para pegar o setor
 * dele — o registro salvo (CheckinHumor) não guarda nenhum identificador
 * de pessoa, só o setor.
 */
@Service
public class MoodCheckinService {

    private final CheckinHumorRepository checkinHumorRepository;

    public MoodCheckinService(CheckinHumorRepository checkinHumorRepository) {
        this.checkinHumorRepository = checkinHumorRepository;
    }

    @Transactional
    public CheckinHumor registrar(Funcionario funcionarioLogado, CheckinHumorRequest request) {
        CheckinHumor checkin = CheckinHumor.builder()
                .setor(funcionarioLogado.getSetor())
                .nivelHumor(request.nivelHumor())
                .build();

        return checkinHumorRepository.save(checkin);
    }
}
