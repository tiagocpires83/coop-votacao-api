package br.com.coop_votocao_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(
            RestClient.Builder builder,
            @Value("${cpf-validation.base-url:http://localhost:8089}") String baseUrl
    ) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
