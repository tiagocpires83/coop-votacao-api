package br.com.coop_votocao_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response de voto registrado")
public class VotoResponse {

    @Schema(example = "10")
    private Long id;

    @Schema(example = "1")
    private Long pautaId;

    @Schema(example = "123")
    private Long associadoId;

    @Schema(example = "SIM")
    private String voto;

    @Schema(example = "2026-01-24T18:00:30Z")
    private OffsetDateTime createdAt;
}
