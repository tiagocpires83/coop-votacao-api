package br.com.coop_votocao_api.exception;

import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload padrão de erro da API")
public class ApiError {
    @Schema(example = "2026-01-24T18:00:00Z")
    private Instant timestamp;

    @Schema(example = "404")
    private int status;

    @Schema(example = "NOT_FOUND")
    private String error;

    @Schema(example = "Pauta não encontrada")
    private String message;

    @Schema(example = "/api/v1/pautas/1")
    private String path;

    @Schema(description = "Detalhes adicionais por campo (validação)", nullable = true)
    private Map<String, String> details;
}
