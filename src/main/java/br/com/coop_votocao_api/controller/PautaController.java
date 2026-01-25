package br.com.coop_votocao_api.controller;

import br.com.coop_votocao_api.dto.request.CreatePautaRequest;
import br.com.coop_votocao_api.dto.request.CreateVotoRequest;
import br.com.coop_votocao_api.dto.request.OpenSessaoRequest;
import br.com.coop_votocao_api.dto.response.PautaResponse;
import br.com.coop_votocao_api.dto.response.ResultadoResponse;
import br.com.coop_votocao_api.dto.response.SessaoResponse;
import br.com.coop_votocao_api.dto.response.VotoResponse;
import br.com.coop_votocao_api.exception.ApiError;
import br.com.coop_votocao_api.service.VotacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Pautas", description = "Operações de pauta, sessão e votação")
@RestController
@RequestMapping("/api/v1/pautas")
@RequiredArgsConstructor
public class PautaController {

    private final VotacaoService service;

    @Operation(summary = "Criar pauta")
    @ApiResponse(responseCode = "201", description = "Pauta criada",
            content = @Content(schema = @Schema(implementation = PautaResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    public ResponseEntity<PautaResponse> criar(@Valid @RequestBody CreatePautaRequest req) {
        PautaResponse resp = service.criarPauta(req);
        return ResponseEntity
                .created(URI.create("/api/v1/pautas/" + resp.getId()))
                .body(resp);
    }

    @Operation(summary = "Buscar pauta por id")
    @ApiResponse(responseCode = "200", description = "Pauta encontrada",
            content = @Content(schema = @Schema(implementation = PautaResponse.class)))
    @ApiResponse(responseCode = "404", description = "Pauta não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{pautaId}")
    public ResponseEntity<PautaResponse> buscar(@PathVariable Long pautaId) {
        return ResponseEntity.ok(service.buscarPauta(pautaId));
    }

    @Operation(summary = "Abrir sessão de votação para uma pauta")
    @ApiResponse(responseCode = "201", description = "Sessão aberta",
            content = @Content(schema = @Schema(implementation = SessaoResponse.class)))
    @ApiResponse(responseCode = "404", description = "Pauta não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Sessão já aberta",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/{pautaId}/sessao")
    public ResponseEntity<SessaoResponse> abrirSessao(
            @PathVariable Long pautaId,
            @Valid @RequestBody(required = false) OpenSessaoRequest req
    ) {
        SessaoResponse resp = service.abrirSessao(pautaId, req);
        return ResponseEntity
                .created(URI.create("/api/v1/pautas/" + pautaId + "/sessao"))
                .body(resp);
    }

    @Operation(summary = "Votar em uma pauta (SIM/NAO)")
    @ApiResponse(responseCode = "201", description = "Voto registrado",
            content = @Content(schema = @Schema(implementation = VotoResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Pauta não encontrada ou CPF inválido",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Sessão encerrada, sessão não aberta ou voto duplicado",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "422", description = "CPF não apto a votar",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/{pautaId}/votos")
    public ResponseEntity<VotoResponse> votar(
            @PathVariable Long pautaId,
            @Valid @RequestBody CreateVotoRequest req
    ) {
        VotoResponse resp = service.votar(pautaId, req);
        return ResponseEntity
                .created(URI.create("/api/v1/pautas/" + pautaId + "/votos/" + resp.getId()))
                .body(resp);
    }

    @Operation(summary = "Resultado da votação da pauta")
    @ApiResponse(responseCode = "200", description = "Resultado retornado",
            content = @Content(schema = @Schema(implementation = ResultadoResponse.class)))
    @ApiResponse(responseCode = "404", description = "Pauta não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Sessão não aberta",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{pautaId}/resultado")
    public ResponseEntity<ResultadoResponse> resultado(@PathVariable Long pautaId) {
        return ResponseEntity.ok(service.resultado(pautaId));
    }
}
