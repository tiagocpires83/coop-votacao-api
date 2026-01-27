package br.com.coop_votocao_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Resposta do voto")
public record VotoResponse(
        Long id,
        Long pautaId,
        String cpf,
        String voto,
        OffsetDateTime createdAt
) {}
