package br.com.coop_votocao_api.controller;

import br.com.coop_votocao_api.dto.request.CreateVotoRequest;
import br.com.coop_votocao_api.dto.response.VotoResponse;
import br.com.coop_votocao_api.exception.ApiError;
import br.com.coop_votocao_api.service.VotacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Votos", description = "Endpoints para registro de votos em pautas")
@RestController
@RequestMapping("/api/v1/pautas")
@RequiredArgsConstructor
public class VotoController {

    private final VotacaoService service;

    @Operation(summary = "Votar em uma pauta (SIM/NAO)",
            description = "Registra um voto para a pauta informada. O CPF deve estar apto (quando validação externa estiver habilitada) e não pode votar duas vezes na mesma pauta.")
    @ApiResponse(responseCode = "201", description = "Voto registrado",
            content = @Content(schema = @Schema(implementation = VotoResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Pauta não encontrada ou CPF inválido",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Sessão encerrada / sessão não aberta / voto duplicado",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "422", description = "CPF não apto a votar",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "502", description = "Falha ao consultar serviço externo de validação de CPF",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/{pautaId}/votos")
    public ResponseEntity<VotoResponse> votar(
            @PathVariable Long pautaId,
            @Valid @RequestBody CreateVotoRequest req
    ) {
        VotoResponse resp = service.votar(pautaId, req);
        return ResponseEntity.created(
                URI.create("/api/v1/pautas/" + pautaId + "/votos/" + resp.id())
        ).body(resp);
    }
}