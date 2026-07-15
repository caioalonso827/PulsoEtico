package com.pulsoetico.pulsoetico.configs;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pulsoetico.pulsoetico.models.Empresa;
import com.pulsoetico.pulsoetico.models.MembroEmpresa;
import com.pulsoetico.pulsoetico.models.Setor;
import com.pulsoetico.pulsoetico.repositories.EmpresaRepository;
import com.pulsoetico.pulsoetico.repositories.MembroEmpresaRepository;
import com.pulsoetico.pulsoetico.services.SetorPadraoService;

@Component
@Order(0)
public class SetorPadraoSeeder implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final MembroEmpresaRepository membroRepository;
    private final SetorPadraoService setorPadraoService;

    public SetorPadraoSeeder(
            EmpresaRepository empresaRepository,
            MembroEmpresaRepository membroRepository,
            SetorPadraoService setorPadraoService
    ) {
        this.empresaRepository = empresaRepository;
        this.membroRepository = membroRepository;
        this.setorPadraoService = setorPadraoService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (Empresa empresa : empresaRepository.findAll()) {
            Setor setorNaoAlocado =
                    setorPadraoService.obterOuCriar(empresa);

            List<MembroEmpresa> membrosSemSetor =
                    membroRepository.findAllByEmpresaIdAndSetorIsNull(
                            empresa.getId()
                    );

            if (membrosSemSetor.isEmpty()) {
                continue;
            }

            membrosSemSetor.forEach(
                    membro -> membro.setSetor(setorNaoAlocado)
            );

            membroRepository.saveAll(membrosSemSetor);
        }
    }
}
