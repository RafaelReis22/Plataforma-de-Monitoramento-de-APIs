package com.monitoring.interceptor;

public record ApiResponse<T>(boolean sucesso, T dados, String erro) {

    public static <T> ApiResponse<T> ok(T dados) {
        return new ApiResponse<>(true, dados, null);
    }

    public static <T> ApiResponse<T> erro(String mensagem) {
        return new ApiResponse<>(false, null, mensagem);
    }
}
