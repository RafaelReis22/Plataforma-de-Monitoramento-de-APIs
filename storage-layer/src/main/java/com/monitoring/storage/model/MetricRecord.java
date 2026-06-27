package com.monitoring.storage.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

// Entidade JPA que representa um registro histórico de métrica no PostgreSQL
@Entity
@Table(name = "registros_metricas", indexes = {
        @Index(name = "idx_nome_timestamp", columnList = "nome, timestamp"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(nullable = false)
    private Double valor;

    @Column(length = 50)
    private String unidade;

    // Instante de coleta da métrica
    @Column(nullable = false)
    private Instant timestamp;

    // Tags em formato JSON serializado (ex: {"host":"srv01","env":"prod"})
    @Column(length = 500)
    private String tags;

    // Origem da métrica (ex: monitoring-agent, observability-pipeline)
    @Column(length = 100)
    private String fonte;
}
