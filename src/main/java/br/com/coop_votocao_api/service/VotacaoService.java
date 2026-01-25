package br.com.coop_votocao_api.service;

import br.com.coop_votocao_api.client.CpfValidationClient;
import br.com.coop_votocao_api.dto.request.CreatePautaRequest;
import br.com.coop_votocao_api.dto.request.CreateVotoRequest;
import br.com.coop_votocao_api.dto.request.OpenSessaoRequest;
import br.com.coop_votocao_api.dto.response.PautaResponse;
import br.com.coop_votocao_api.dto.response.ResultadoResponse;
import br.com.coop_votocao_api.dto.response.SessaoResponse;
import br.com.coop_votocao_api.dto.response.VotoResponse;
import br.com.coop_votocao_api.entity.PautaEntity;
import br.com.coop_votocao_api.entity.SessaoVotacaoEntity;
import br.com.coop_votocao_api.entity.VotoEntity;
import br.com.coop_votocao_api.exception.BusinessException;
import br.com.coop_votocao_api.exception.NotFoundException;
import br.com.coop_votocao_api.repository.PautaRepository;
import br.com.coop_votocao_api.repository.SessaoVotacaoRepository;
import br.com.coop_votocao_api.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class VotacaoService {

    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoRepository;
    private final VotoRepository votoRepository;
    private final CpfValidationClient cpfClient;

    /**
     * Mantemos um Clock para facilitar testes com tempo controlado.
     * Em runtime, usa UTC.
     */
    private final Clock clock = Clock.systemUTC();

    private OffsetDateTime nowUtc() {
        return OffsetDateTime.now(clock.withZone(ZoneOffset.UTC));
    }

    @Transactional
    public PautaResponse criarPauta(CreatePautaRequest req) {
        OffsetDateTime now = nowUtc();

        PautaEntity pauta = PautaEntity.builder()
                .titulo(req.getTitulo())
                .descricao(req.getDescricao())
                .createdAt(now)
                .build();

        PautaEntity saved = pautaRepository.save(pauta);

        return PautaResponse.builder()
                .id(saved.getId())
                .titulo(saved.getTitulo())
                .descricao(saved.getDescricao())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public PautaResponse buscarPauta(Long pautaId) {
        PautaEntity pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new NotFoundException("Pauta não encontrada"));

        return PautaResponse.builder()
                .id(pauta.getId())
                .titulo(pauta.getTitulo())
                .descricao(pauta.getDescricao())
                .createdAt(pauta.getCreatedAt())
                .build();
    }

    @Transactional
    public SessaoResponse abrirSessao(Long pautaId, OpenSessaoRequest req) {
        PautaEntity pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new NotFoundException("Pauta não encontrada"));

        if (sessaoRepository.existsByPauta_Id(pautaId)) {
            throw new BusinessException("Sessão já foi aberta para esta pauta");
        }

        int duracao = (req == null || req.getDuracaoEmMinutos() == null) ? 1 : req.getDuracaoEmMinutos();

        OffsetDateTime inicio = nowUtc();
        OffsetDateTime fim = inicio.plus(duracao, ChronoUnit.MINUTES);

        SessaoVotacaoEntity sessao = SessaoVotacaoEntity.builder()
                .pauta(pauta)
                .inicio(inicio)
                .fim(fim)
                .build();

        sessaoRepository.save(sessao);

        return SessaoResponse.builder()
                .pautaId(pautaId)
                .inicio(inicio)
                .fim(fim)
                .build();
    }

    @Transactional
    public VotoResponse votar(Long pautaId, CreateVotoRequest req) {
        PautaEntity pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new NotFoundException("Pauta não encontrada"));

        SessaoVotacaoEntity sessao = sessaoRepository.findByPauta_Id(pautaId)
                .orElseThrow(() -> new BusinessException("Sessão não foi aberta para esta pauta"));

        OffsetDateTime now = nowUtc();

        // Sessão aberta se: now >= inicio e now < fim
        if (now.isBefore(sessao.getInicio()) || !now.isBefore(sessao.getFim())) {
            throw new BusinessException("Sessão de votação encerrada");
        }

        if (votoRepository.existsByPauta_IdAndAssociadoId(pautaId, req.getAssociadoId())) {
            throw new BusinessException("Associado já votou nesta pauta");
        }

        // Validação CPF (opcional / mockável via config no client)
        cpfClient.validate(req.getCpf());

        VotoEntity.VotoOpcao votoEnum = switch (req.getVoto()) {
            case SIM -> VotoEntity.VotoOpcao.SIM;
            case NAO -> VotoEntity.VotoOpcao.NAO;
        };

        VotoEntity voto = VotoEntity.builder()
                .pauta(pauta)
                .associadoId(req.getAssociadoId())
                .cpf(req.getCpf())
                .voto(votoEnum)
                .createdAt(now)
                .build();

        VotoEntity saved = votoRepository.save(voto);

        return VotoResponse.builder()
                .id(saved.getId())
                .pautaId(saved.getPauta().getId())
                .associadoId(saved.getAssociadoId())
                .voto(saved.getVoto().name())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public ResultadoResponse resultado(Long pautaId) {
        if (!pautaRepository.existsById(pautaId)) {
            throw new NotFoundException("Pauta não encontrada");
        }

        SessaoVotacaoEntity sessao = sessaoRepository.findByPauta_Id(pautaId)
                .orElseThrow(() -> new BusinessException("Sessão não foi aberta para esta pauta"));

        OffsetDateTime now = nowUtc();
        boolean aberta = now.isBefore(sessao.getFim());

        long sim = votoRepository.countByPauta_IdAndVoto(pautaId, VotoEntity.VotoOpcao.SIM);
        long nao = votoRepository.countByPauta_IdAndVoto(pautaId, VotoEntity.VotoOpcao.NAO);

        return ResultadoResponse.builder()
                .pautaId(pautaId)
                .aberta(aberta)
                .inicio(sessao.getInicio())
                .fim(sessao.getFim())
                .totalSim(sim)
                .totalNao(nao)
                .total(sim + nao)
                .build();
    }
}
