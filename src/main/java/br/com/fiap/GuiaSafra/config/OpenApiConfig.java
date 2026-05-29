package br.com.fiap.GuiaSafra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI guiaSafraOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GuiaSafra API - AgroMonitor")
                        .description("API Java do projeto AgroMonitor (Global Solution FIAP 2026/1). " +
                                "Responsável pela operação: usuários, previsão climática, leituras de sensores, " +
                                "eventos de rega e alertas.")
                        .version("1.0.0"));
    }
}
