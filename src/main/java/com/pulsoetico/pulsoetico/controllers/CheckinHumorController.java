package com.pulsoetico.pulsoetico.controllers;

import com.pulsoetico.pulsoetico.models.dtos.CheckinHumorRequest;
import com.pulsoetico.pulsoetico.security.FuncionarioUserDetails;
import com.pulsoetico.pulsoetico.services.MoodCheckinService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/checkins-humor")
public class CheckinHumorController {

    private final MoodCheckinService moodCheckinService;

    public CheckinHumorController(MoodCheckinService moodCheckinService) {
        this.moodCheckinService = moodCheckinService;
    }

    @PostMapping
    public ResponseEntity<Void> registrar(
            @AuthenticationPrincipal FuncionarioUserDetails usuario,
            @Valid @RequestBody CheckinHumorRequest request
    ) {
        moodCheckinService.registrar(usuario.getFuncionario(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
