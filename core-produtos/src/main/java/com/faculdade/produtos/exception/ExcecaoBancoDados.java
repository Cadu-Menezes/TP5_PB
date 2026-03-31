package com.faculdade.produtos.exception;

/**
 * Exceção para erros relacionados ao banco de dados.
 */
public final class ExcecaoBancoDados extends ExcecaoProduto {
    private static final String CODIGO_ERRO = "ERRO_BANCO_DADOS";
    
    private final String operacao;

    public ExcecaoBancoDados(String operacao, String message) {
        super(String.format("Erro no banco de dados durante %s: %s", operacao, message));
        this.operacao = operacao;
    }

    public ExcecaoBancoDados(String operacao, String message, Throwable cause) {
        super(String.format("Erro no banco de dados durante %s: %s", operacao, message), cause);
        this.operacao = operacao;
    }

    public String getOperacao() {
        return operacao;
    }

    @Override
    public String obterCodigoErro() {
        return CODIGO_ERRO;
    }

    @Override
    public CategoriaErro obterCategoriaErro() {
        return CategoriaErro.INFRAESTRUTURA;
    }
}