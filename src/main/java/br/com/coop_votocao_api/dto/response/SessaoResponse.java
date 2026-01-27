package br.com.coop_votocao_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Resposta da sessão de votação")
public record SessaoResponse(
        Long pautaId,
        OffsetDateTime inicio,
        OffsetDateTime fim
) {}
