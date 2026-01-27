package br.com.coop_votocao_api.controller;

import br.com.coop_votocao_api.config.TestClockConfig;
import br.com.coop_votocao_api.dto.request.CreateVotoRequest;
import br.com.coop_votocao_api.dto.response.VotoResponse;
import br.com.coop_votocao_api.exception.GlobalExceptionHandler;
import br.com.coop_votocao_api.service.VotacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VotoControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private VotacaoService service;

    @InjectMocks
    private VotoController controller;

    @BeforeEach
    void setup() {
        Clock clock = new TestClockConfig().clock();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(clock))
                .build();
    }

    @Test
    void votar_deveRetornar201_comLocationEBody() throws Exception {
        var request = new CreateVotoRequest("12345678901", CreateVotoRequest.VotoOpcao.SIM);

        when(service.votar(1L, request))
                .thenReturn(new VotoResponse(
                        100L,
                        1L,
                        "12345678901",
                        "SIM",
                        OffsetDateTime.parse("2026-01-24T18:00:00Z")
                ));

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/pautas/1/votos/100"))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.pautaId").value(1))
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.voto").value("SIM"));
    }

    @Test
    void votar_deveRetornar400_quandoCpfInvalido() throws Exception {
        // CPF inválido pelo regex (não 11 dígitos, ou vazio)
        var request = new CreateVotoRequest("123", CreateVotoRequest.VotoOpcao.SIM);

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void votar_deveRetornar400_quandoBodyInvalido_semVoto() throws Exception {
        // JSON sem campo "voto"
        String json = """
                {"cpf":"12345678901"}
                """;

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
