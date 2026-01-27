package br.com.coop_votocao_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Resposta do resultado da votação")
public record ResultadoResponse(
        Long pautaId,
        boolean aberta,
        OffsetDateTime inicio,
        OffsetDateTime fim,
        long totalSim,
        long totalNao,
        long total
) {}
