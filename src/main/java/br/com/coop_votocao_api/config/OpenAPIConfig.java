package br.com.coop_votocao_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Votação")
                        .description("API REST para criação de pautas, abertura de sessão de votação, registro de votos e apuração de resultado.")
                        .version("v1"));
    }
}
