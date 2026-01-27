package br.com.coop_votocao_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Resposta da pauta")
public record PautaResponse(
        Long id,
        String titulo,
        String descricao,
        OffsetDateTime createdAt
) {}
