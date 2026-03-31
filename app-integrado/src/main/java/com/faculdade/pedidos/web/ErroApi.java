package com.faculdade.pedidos.web;

public record ErroApi(String mensagem) {
    public static ErroApi of(String mensagem) {
        return new ErroApi(mensagem);
    }
}
