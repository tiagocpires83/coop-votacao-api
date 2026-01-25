package br.com.coop_votocao_api.repository;

import br.com.coop_votocao_api.entity.PautaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PautaRepository extends JpaRepository<PautaEntity, Long> {
}
