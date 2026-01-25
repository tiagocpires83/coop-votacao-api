package br.com.coop_votocao_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request para registro de voto em uma pauta")
public class CreateVotoRequest {

    @NotNull
    @Schema(description = "Identificador do associado", example = "123")
    private Long associadoId;

    @NotBlank
    @Pattern(regexp = "^[0-9]{11}$", message = "CPF deve conter 11 dígitos numéricos")
    @Schema(description = "CPF do associado (11 dígitos)", example = "12345678901")
    private String cpf;

    @NotNull
    @Schema(description = "Voto do associado", example = "SIM", allowableValues = {"SIM", "NAO"})
    private VotoOpcao voto;

    public enum VotoOpcao { SIM, NAO }
}
