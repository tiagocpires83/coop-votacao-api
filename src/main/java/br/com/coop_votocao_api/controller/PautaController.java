package br.com.coop_votocao_api.controller;

import br.com.coop_votocao_api.dto.request.CreatePautaRequest;
import br.com.coop_votocao_api.dto.response.PautaResponse;
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

@Tag(name = "Pautas", description = "Endpoints para criação e consulta de pautas")
@RestController
@RequestMapping("/api/v1/pautas")
@RequiredArgsConstructor
public class PautaController {

    private final VotacaoService service;

    @Operation(summary = "Criar pauta", description = "Cria uma nova pauta para votação.")
    @ApiResponse(responseCode = "201", description = "Pauta criada",
            content = @Content(schema = @Schema(implementation = PautaResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    public ResponseEntity<PautaResponse> criar(@Valid @RequestBody CreatePautaRequest req) {
        PautaResponse resp = service.criarPauta(req);
        return ResponseEntity.created(URI.create("/api/v1/pautas/" + resp.id())).body(resp);
    }

    @Operation(summary = "Buscar pauta", description = "Busca uma pauta pelo ID.")
    @ApiResponse(responseCode = "200", description = "Pauta encontrada",
            content = @Content(schema = @Schema(implementation = PautaResponse.class)))
    @ApiResponse(responseCode = "404", description = "Pauta não encontrada",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{pautaId}")
    public ResponseEntity<PautaResponse> buscar(@PathVariable Long pautaId) {
        return ResponseEntity.ok(service.buscarPauta(pautaId));
    }
}
