package com.pulsoetico.pulsoetico.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import com.pulsoetico.pulsoetico.models.AplicacaoFormulario;
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

    @PostMapping("/aplicacoes")
    public ResponseEntity<AplicacaoFormulario> liberar(
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
                @AuthenticationPrincipal FuncionarioUserDetails userDetails
        ) {
        formularioService.encerrarFormulario(
                empresaId,
                aplicacaoId,
                userDetails.getFuncionario()
        );

        return ResponseEntity.noContent().build();
        }
}