package com.pulsoetico.pulsoetico.controllers;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.dtos.DenunciaResponse;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.DenunciaService;

@RestController
@RequestMapping("/api/painel/empresas/{empresaId}/denuncias")
public class DenunciaPainelController {

    private final DenunciaService denunciaService;

    public DenunciaPainelController(
            DenunciaService denunciaService
    ) {
        this.denunciaService = denunciaService;
    }

    @GetMapping
    public List<DenunciaResponse> listarRecentes(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return denunciaService
                .listarRecentesDaEmpresa(
                        empresaId,
                        principal.getFuncionario()
                )
                .stream()
                .map(DenunciaResponse::from)
                .toList();
    }

    @PatchMapping("/{denunciaId}/responder")
    public DenunciaResponse responder(
            @PathVariable Long empresaId,
            @PathVariable Long denunciaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return DenunciaResponse.from(
                denunciaService.marcarComoRespondida(
                        empresaId,
                        denunciaId,
                        principal.getFuncionario()
                )
        );
    }
}