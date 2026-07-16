package com.pulsoetico.pulsoetico.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulsoetico.pulsoetico.models.dtos.CheckinHumorRequest;
import com.pulsoetico.pulsoetico.models.dtos.CheckinHumorStatusResponse;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.MoodCheckinService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/app/empresas/{empresaId}/checkins-humor")
public class CheckinHumorController {

    private final MoodCheckinService moodCheckinService;

    public CheckinHumorController(
            MoodCheckinService moodCheckinService
    ) {
        this.moodCheckinService = moodCheckinService;
    }

    @PostMapping
    public ResponseEntity<Void> registrar(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal,
            @Valid @RequestBody CheckinHumorRequest request
    ) {
        moodCheckinService.registrar(
                principal.getFuncionario(),
                empresaId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping("/status-hoje")
    public CheckinHumorStatusResponse consultarStatusHoje(
            @PathVariable Long empresaId,
            @AuthenticationPrincipal FuncionarioUserDetails principal
    ) {
        return moodCheckinService.consultarStatusHoje(
                principal.getFuncionario(),
                empresaId
        );
    }
}
