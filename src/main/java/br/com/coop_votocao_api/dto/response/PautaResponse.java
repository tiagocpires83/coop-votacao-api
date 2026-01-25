package br.com.coop_votocao_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response de pauta")
public class PautaResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Pauta sobre investimento")
    private String titulo;

    @Schema(example = "Discussão sobre investimento em nova iniciativa")
    private String descricao;

    @Schema(example = "2026-01-24T18:00:00Z")
    private OffsetDateTime createdAt;
}
