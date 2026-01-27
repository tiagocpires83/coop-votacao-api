package br.com.coop_votocao_api.rules;

import br.com.coop_votocao_api.dto.request.OpenSessaoRequest;
import br.com.coop_votocao_api.entity.SessaoVotacaoEntity;
import br.com.coop_votocao_api.exception.SessaoEncerradaException;
import br.com.coop_votocao_api.exception.SessaoJaAbertaException;
import br.com.coop_votocao_api.repository.SessaoVotacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class SessaoRules {

    private final SessaoVotacaoRepository sessaoRepository;

    public void assertSessaoNaoExiste(Long pautaId) {
        if (sessaoRepository.existsByPauta_Id(pautaId)) {
            throw new SessaoJaAbertaException("Sessão já foi aberta para esta pauta");
        }
    }

    public SessaoVotacaoEntity getSessaoOrThrow(Long pautaId) {
        return sessaoRepository.findByPauta_Id(pautaId)
                .orElseThrow(() -> new br.com.coop_votocao_api.exception.BusinessException(
                        "Sessão não foi aberta para esta pauta"
                ) {});
    }

    public OffsetDateTime calcularFim(OffsetDateTime inicio, OpenSessaoRequest req) {
        int duracao = (req == null || req.duracaoEmMinutos() == null) ? 1 : req.duracaoEmMinutos();
        return inicio.plus(duracao, ChronoUnit.MINUTES);
    }

    public void assertSessaoAberta(SessaoVotacaoEntity sessao, OffsetDateTime now) {
        if (now.isBefore(sessao.getInicio()) || !now.isBefore(sessao.getFim())) {
            throw new SessaoEncerradaException("Sessão de votação encerrada");
        }
    }

    public boolean isAberta(SessaoVotacaoEntity sessao, OffsetDateTime now) {
        return now.isBefore(sessao.getFim());
    }
}
