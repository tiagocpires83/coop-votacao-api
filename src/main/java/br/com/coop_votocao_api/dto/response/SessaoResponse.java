package br.com.coop_votocao_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response de sessão de votação")
public class SessaoResponse {

    @Schema(example = "1")
    private Long pautaId;

    @Schema(example = "2026-01-24T18:00:00Z")
    private OffsetDateTime inicio;

    @Schema(example = "2026-01-24T18:01:00Z")
    private OffsetDateTime fim;
}
