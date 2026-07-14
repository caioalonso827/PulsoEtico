package com.pulsoetico.pulsoetico.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.pulsoetico.pulsoetico.models.Funcionario;
import com.pulsoetico.pulsoetico.models.MembroEmpresa;
import com.pulsoetico.pulsoetico.models.Permissoes;
import com.pulsoetico.pulsoetico.repositories.MembroEmpresaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AutorizacaoEmpresaService {

    private final MembroEmpresaRepository membroRepository;

    public AutorizacaoEmpresaService(
            MembroEmpresaRepository membroRepository
    ) {
        this.membroRepository = membroRepository;
    }

    public MembroEmpresa exigirMembro(
            Long empresaId,
            Funcionario usuario
    ) {
        return membroRepository
                .findByEmpresaIdAndFuncionarioIdAndAtivoTrue(
                        empresaId,
                        usuario.getId()
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Empresa não encontrada"
                        )
                );
    }

    public MembroEmpresa exigirPermissao(
            Long empresaId,
            Funcionario usuario,
            Permissoes permissao
    ) {
        MembroEmpresa membro = exigirMembro(empresaId, usuario);

        if (!membro.possuiPermissao(permissao)) {
            throw new AccessDeniedException(
                    "Você não possui a permissão " +
                    permissao +
                    " nesta empresa"
            );
        }

        return membro;
    }
}