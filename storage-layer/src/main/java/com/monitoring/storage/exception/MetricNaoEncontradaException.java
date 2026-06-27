package com.monitoring.storage.exception;

// Exceção de domínio lançada quando nenhum registro é encontrado para a métrica solicitada
public class MetricNaoEncontradaException extends RuntimeException {

    public MetricNaoEncontradaException(String nome) {
        super("Nenhum registro encontrado para a métrica: " + nome);
    }

    public MetricNaoEncontradaException(Long id) {
        super("Registro de métrica não encontrado: id=" + id);
    }
}
