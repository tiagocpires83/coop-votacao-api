package br.com.coop_votocao_api.controller;

import br.com.coop_votocao_api.config.TestClockConfig;
import br.com.coop_votocao_api.dto.request.OpenSessaoRequest;
import br.com.coop_votocao_api.dto.response.SessaoResponse;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SessaoControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Mock
    private VotacaoService service;

    @InjectMocks
    private SessaoController controller;

    @BeforeEach
    void setup() {
        Clock clock = new TestClockConfig().clock();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(clock))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void abrirSessao_deveRetornar201_comLocationEBody() throws Exception {
        var request = new OpenSessaoRequest(1);

        when(service.abrirSessao(1L, request))
                .thenReturn(new SessaoResponse(
                        1L,
                        OffsetDateTime.parse("2026-01-24T18:00:00Z"),
                        OffsetDateTime.parse("2026-01-24T18:01:00Z")
                ));

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessao", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/pautas/1/sessao"))
                .andExpect(jsonPath("$.pautaId").value(1))
                .andExpect(jsonPath("$.inicio").value("2026-01-24T18:00:00Z"))
                .andExpect(jsonPath("$.fim").value("2026-01-24T18:01:00Z"));
    }

    @Test
    void abrirSessao_deveAceitarBodyNull_eRetornar201() throws Exception {
        when(service.abrirSessao(1L, null))
                .thenReturn(new SessaoResponse(
                        1L,
                        OffsetDateTime.parse("2026-01-24T18:00:00Z"),
                        OffsetDateTime.parse("2026-01-24T18:01:00Z")
                ));

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessao", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/pautas/1/sessao"))
                .andExpect(jsonPath("$.pautaId").value(1));
    }
}