package br.com.coop_votocao_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "PAUTA")
public class PautaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PAUTA_GEN")
    @SequenceGenerator(name = "SEQ_PAUTA_GEN", sequenceName = "SEQ_PAUTA", allocationSize = 1)
    @Column(name = "ID", nullable = false, precision = 19, scale = 0)
    private Long id;

    @Column(name = "TITULO", nullable = false, length = 200)
    private String titulo;

    @Column(name = "DESCRICAO", length = 1000)
    private String descricao;

    @Column(name = "CREATED_AT", nullable = false)
    private OffsetDateTime createdAt;
}
