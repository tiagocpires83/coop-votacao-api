package br.com.coop_votocao_api.repository;

import br.com.coop_votocao_api.entity.SessaoVotacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessaoVotacaoRepository extends JpaRepository<SessaoVotacaoEntity, Long> {

    Optional<SessaoVotacaoEntity> findByPauta_Id(Long pautaId);

    boolean existsByPauta_Id(Long pautaId);
}
