package br.com.coop_votocao_api.service;

import br.com.coop_votocao_api.client.CpfValidationClient;
import br.com.coop_votocao_api.dto.request.CreatePautaRequest;
import br.com.coop_votocao_api.dto.request.CreateVotoRequest;
import br.com.coop_votocao_api.dto.request.OpenSessaoRequest;
import br.com.coop_votocao_api.entity.PautaEntity;
import br.com.coop_votocao_api.entity.SessaoVotacaoEntity;
import br.com.coop_votocao_api.entity.VotoEntity;
import br.com.coop_votocao_api.exception.BusinessException;
import br.com.coop_votocao_api.exception.NotFoundException;
import br.com.coop_votocao_api.repository.PautaRepository;
import br.com.coop_votocao_api.repository.SessaoVotacaoRepository;
import br.com.coop_votocao_api.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VotacaoServiceTest {

    @Mock private PautaRepository pautaRepository;
    @Mock private SessaoVotacaoRepository sessaoRepository;
    @Mock private VotoRepository votoRepository;
    @Mock private CpfValidationClient cpfClient;

    private VotacaoService service;

    private final Clock fixedClock =
            Clock.fixed(Instant.parse("2026-01-24T18:00:00Z"), ZoneOffset.UTC);

    private static final OffsetDateTime T_18_00 =
            OffsetDateTime.parse("2026-01-24T18:00:00Z");
    private static final OffsetDateTime T_18_01 =
            OffsetDateTime.parse("2026-01-24T18:01:00Z");

    @BeforeEach
    void setup() throws Exception {
        service = new VotacaoService(pautaRepository, sessaoRepository, votoRepository, cpfClient);

        // Injeta Clock fixo via reflection (porque o clock está hardcoded no service)
        Field clockField = VotacaoService.class.getDeclaredField("clock");
        clockField.setAccessible(true);
        clockField.set(service, fixedClock);
    }

    @Test
    void criarPauta_ok() throws Exception {
        var req = CreatePautaRequest.builder()
                .titulo("Pauta A")
                .descricao("Desc")
                .build();

        when(pautaRepository.save(any(PautaEntity.class))).thenAnswer(inv -> {
            var e = inv.getArgument(0, PautaEntity.class);

            Field idField = PautaEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(e, 1L);

            return e;
        });

        var resp = service.criarPauta(req);

        assertEquals(1L, resp.getId());
        assertEquals("Pauta A", resp.getTitulo());
        assertEquals(T_18_00, resp.getCreatedAt());
        verify(pautaRepository).save(any(PautaEntity.class));
    }

    @Test
    void abrirSessao_default_1_minuto_quando_request_null() {
        var pauta = PautaEntity.builder()
                .id(1L)
                .titulo("Pauta A")
                .descricao("Desc")
                .createdAt(OffsetDateTime.parse("2026-01-24T17:00:00Z"))
                .build();

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.existsByPauta_Id(1L)).thenReturn(false);

        var resp = service.abrirSessao(1L, null);

        assertEquals(T_18_00, resp.getInicio());
        assertEquals(T_18_01, resp.getFim());

        verify(sessaoRepository).save(any(SessaoVotacaoEntity.class));
    }

    @Test
    void abrirSessao_falha_quando_pauta_nao_existe() {
        when(pautaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.abrirSessao(999L, new OpenSessaoRequest()));
    }

    @Test
    void abrirSessao_falha_quando_ja_existe_sessao() {
        var pauta = PautaEntity.builder()
                .id(1L)
                .titulo("Pauta A")
                .createdAt(OffsetDateTime.parse("2026-01-24T17:00:00Z"))
                .build();

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.existsByPauta_Id(1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.abrirSessao(1L, new OpenSessaoRequest()));
    }

    @Test
    void votar_ok() throws Exception {
        var pauta = PautaEntity.builder()
                .id(1L)
                .titulo("Pauta A")
                .createdAt(OffsetDateTime.parse("2026-01-24T17:00:00Z"))
                .build();

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        when(sessaoRepository.findByPauta_Id(1L)).thenReturn(Optional.of(
                SessaoVotacaoEntity.builder()
                        .pauta(pauta)
                        .inicio(OffsetDateTime.parse("2026-01-24T17:59:00Z"))
                        .fim(OffsetDateTime.parse("2026-01-24T18:10:00Z"))
                        .build()
        ));

        when(votoRepository.existsByPauta_IdAndAssociadoId(1L, 10L)).thenReturn(false);

        when(cpfClient.validate("12345678901"))
                .thenReturn(CpfValidationClient.VotingStatus.ABLE_TO_VOTE);

        when(votoRepository.save(any(VotoEntity.class))).thenAnswer(inv -> {
            var e = inv.getArgument(0, VotoEntity.class);
            Field idField = VotoEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(e, 100L);
            return e;
        });

        var req = CreateVotoRequest.builder()
                .associadoId(10L)
                .cpf("12345678901")
                .voto(CreateVotoRequest.VotoOpcao.SIM)
                .build();

        var resp = service.votar(1L, req);

        assertEquals(100L, resp.getId());
        assertEquals(1L, resp.getPautaId());
        assertEquals(10L, resp.getAssociadoId());
        assertEquals("SIM", resp.getVoto());
        assertEquals(T_18_00, resp.getCreatedAt());

        verify(cpfClient).validate("12345678901");
        verify(votoRepository).save(any(VotoEntity.class));
    }

    @Test
    void votar_falha_quando_sessao_nao_aberta() {
        var pauta = PautaEntity.builder()
                .id(1L)
                .titulo("Pauta A")
                .createdAt(OffsetDateTime.parse("2026-01-24T17:00:00Z"))
                .build();

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPauta_Id(1L)).thenReturn(Optional.empty());

        var req = CreateVotoRequest.builder()
                .associadoId(10L)
                .cpf("12345678901")
                .voto(CreateVotoRequest.VotoOpcao.SIM)
                .build();

        assertThrows(BusinessException.class, () -> service.votar(1L, req));
    }

    @Test
    void votar_falha_quando_sessao_encerrada() {
        var pauta = PautaEntity.builder()
                .id(1L)
                .titulo("Pauta A")
                .createdAt(OffsetDateTime.parse("2026-01-24T17:00:00Z"))
                .build();

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        when(sessaoRepository.findByPauta_Id(1L)).thenReturn(Optional.of(
                SessaoVotacaoEntity.builder()
                        .pauta(pauta)
                        .inicio(OffsetDateTime.parse("2026-01-24T17:00:00Z"))
                        .fim(OffsetDateTime.parse("2026-01-24T17:10:00Z"))
                        .build()
        ));

        var req = CreateVotoRequest.builder()
                .associadoId(10L)
                .cpf("12345678901")
                .voto(CreateVotoRequest.VotoOpcao.SIM)
                .build();

        assertThrows(BusinessException.class, () -> service.votar(1L, req));
    }

    @Test
    void votar_falha_quando_associado_ja_votou() {
        var pauta = PautaEntity.builder()
                .id(1L)
                .titulo("Pauta A")
                .createdAt(OffsetDateTime.parse("2026-01-24T17:00:00Z"))
                .build();

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        when(sessaoRepository.findByPauta_Id(1L)).thenReturn(Optional.of(
                SessaoVotacaoEntity.builder()
                        .pauta(pauta)
                        .inicio(OffsetDateTime.parse("2026-01-24T17:59:00Z"))
                        .fim(OffsetDateTime.parse("2026-01-24T18:10:00Z"))
                        .build()
        ));

        when(votoRepository.existsByPauta_IdAndAssociadoId(1L, 10L)).thenReturn(true);

        var req = CreateVotoRequest.builder()
                .associadoId(10L)
                .cpf("12345678901")
                .voto(CreateVotoRequest.VotoOpcao.SIM)
                .build();

        assertThrows(BusinessException.class, () -> service.votar(1L, req));
        verifyNoInteractions(cpfClient);
    }

    @Test
    void resultado_ok() {
        var pauta = PautaEntity.builder()
                .id(1L)
                .titulo("Pauta A")
                .createdAt(OffsetDateTime.parse("2026-01-24T17:00:00Z"))
                .build();

        when(pautaRepository.existsById(1L)).thenReturn(true);

        when(sessaoRepository.findByPauta_Id(1L)).thenReturn(Optional.of(
                SessaoVotacaoEntity.builder()
                        .pauta(pauta)
                        .inicio(OffsetDateTime.parse("2026-01-24T17:59:00Z"))
                        .fim(OffsetDateTime.parse("2026-01-24T18:10:00Z"))
                        .build()
        ));

        when(votoRepository.countByPauta_IdAndVoto(1L, VotoEntity.VotoOpcao.SIM)).thenReturn(2L);
        when(votoRepository.countByPauta_IdAndVoto(1L, VotoEntity.VotoOpcao.NAO)).thenReturn(1L);

        var resp = service.resultado(1L);

        assertTrue(resp.isAberta());
        assertEquals(2, resp.getTotalSim());
        assertEquals(1, resp.getTotalNao());
        assertEquals(3, resp.getTotal());
    }
}
