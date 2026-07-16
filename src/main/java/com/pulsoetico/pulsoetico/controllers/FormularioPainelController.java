package com.pulsoetico.pulsoetico.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.dtos.AplicacaoFormularioResponse;
import com.pulsoetico.pulsoetico.models.dtos.FormularioModeloResponse;
import com.pulsoetico.pulsoetico.models.dtos.FormularioResultadoResponse;
import com.pulsoetico.pulsoetico.models.dtos.LiberarFormularioRequest;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.FormularioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(
        "/api/painel/empresas/{empresaId}/formularios"
)
public class FormularioPainelController {

    private final FormularioService formularioService;

    public FormularioPainelController(
            FormularioService formularioService
    ) {
        this.formularioService = formularioService;
    }

    @GetMapping("/modelos")
    public List<FormularioModeloResponse> listarModelos(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal
            FuncionarioUserDetails principal
    ) {
        return formularioService.listarModelos(
                empresaId,
                principal.getFuncionario()
        );
    }

    @GetMapping("/aplicacoes")
    public List<AplicacaoFormularioResponse> listarAplicacoes(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal
            FuncionarioUserDetails principal
    ) {
        return formularioService.listarAplicacoes(
                empresaId,
                principal.getFuncionario()
        );
    }

    @GetMapping("/aplicacoes/{aplicacaoId}/resultados")
    public FormularioResultadoResponse buscarResultados(
            @PathVariable Long empresaId,
            @PathVariable Long aplicacaoId,
            @AuthenticationPrincipal
            FuncionarioUserDetails principal
    ) {
        return formularioService.buscarResultados(
                empresaId,
                aplicacaoId,
                principal.getFuncionario()
        );
    }

    @PostMapping("/aplicacoes")
    public ResponseEntity<AplicacaoFormularioResponse> liberar(
            @PathVariable Long empresaId,
            @Valid @RequestBody
            LiberarFormularioRequest request,
            @AuthenticationPrincipal
            FuncionarioUserDetails principal
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        formularioService.liberar(
                                empresaId,
                                request,
                                principal.getFuncionario()
                        )
                );
    }

    @PatchMapping("/aplicacoes/{aplicacaoId}/encerrar")
    public ResponseEntity<Void> encerrarFormulario(
            @PathVariable Long empresaId,
            @PathVariable Long aplicacaoId,
            @AuthenticationPrincipal
            FuncionarioUserDetails principal
    ) {
        formularioService.encerrarFormulario(
                empresaId,
                aplicacaoId,
                principal.getFuncionario()
        );

        return ResponseEntity.noContent().build();
    }
}
