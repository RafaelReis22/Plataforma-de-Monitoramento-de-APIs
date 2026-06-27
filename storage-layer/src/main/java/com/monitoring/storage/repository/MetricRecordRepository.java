package com.monitoring.storage.repository;

import com.monitoring.storage.model.MetricRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

// Repositório Spring Data JPA para persistência de registros históricos
public interface MetricRecordRepository extends JpaRepository<MetricRecord, Long> {

    // Busca todos os registros de uma métrica, ordenados do mais recente ao mais antigo
    List<MetricRecord> findByNomeOrderByTimestampDesc(String nome);

    // Busca registros de uma métrica dentro de um intervalo de tempo
    List<MetricRecord> findByNomeAndTimestampBetweenOrderByTimestampAsc(
            String nome, Instant inicio, Instant fim);

    // Conta quantos registros existem para um nome de métrica
    long countByNome(String nome);
}
