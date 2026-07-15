package com.pulsoetico.pulsoetico.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_BEARER = "bearerAuth";

    @Bean
    public OpenAPI pulsoeticoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pulso Ético API")
                        .version("0.0.1")
                        .description("""
                                Plataforma de prevenção preditiva de riscos psicossociais no trabalho.
                                Calcula um índice de risco por setor (nunca por pessoa), a partir de dados
                                anônimos como check-ins de humor, horas extras, rotatividade e denúncias,
                                e sugere ações preventivas para RH e gestores. Alinhado à NR-1.
                                """)
                        .license(new License()
                                .name("Pulso Etico")))
                // Isso é o que faz o botão "Authorize" aparecer no Swagger UI
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BEARER))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_BEARER, new SecurityScheme()
                                .name(ESQUEMA_BEARER)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Cole aqui o token retornado por /api/auth/login (sem o prefixo 'Bearer ')")));
    }
}
