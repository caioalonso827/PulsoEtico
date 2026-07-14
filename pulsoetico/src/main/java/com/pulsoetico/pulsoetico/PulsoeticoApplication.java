package com.pulsoetico.pulsoetico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // necessário para o RiskCalculationScheduler (@Scheduled) funcionar
public class PulsoeticoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PulsoeticoApplication.class, args);
	}

}
