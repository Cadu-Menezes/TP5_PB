package com.faculdade.produtos.model;

/*
    Enum para categorias de produtos.
    Implementa switch statements exaustivos conforme requisitos.
*/
public enum Categoria {
    ELETRONICOS("Eletrônicos", "Produtos eletrônicos e tecnológicos"),
    ROUPAS("Roupas", "Vestuário e acessórios"),
    LIVROS("Livros", "Livros e material educacional"),
    CASA_E_JARDIM("Casa e Jardim", "Produtos para casa e jardim"),
    ESPORTES("Esportes", "Equipamentos esportivos e fitness"),
    BELEZA("Beleza", "Produtos de beleza e cuidados pessoais"),
    ALIMENTACAO("Alimentação", "Alimentos e bebidas"),
    BRINQUEDOS("Brinquedos", "Brinquedos e jogos"),
    FERRAMENTAS("Ferramentas", "Ferramentas e equipamentos"),
    OUTROS("Outros", "Outras categorias");

    private final String nomeExibicao;
    private final String descricao;

    Categoria(String nomeExibicao, String descricao) {
        this.nomeExibicao = nomeExibicao;
        this.descricao = descricao;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public String getDescricao() {
        return descricao;
    }

    /*
        Converte string para categoria.
        Implementa tratamento exaustivo sem default.
    */
    public static Categoria deString(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor da categoria não pode ser nulo ou vazio");
        }

        String valorNormalizado = valor.trim().toUpperCase();
        
        // Switch exaustivo sem default para garantir tratamento explícito
        switch (valorNormalizado) {
            case "ELETRONICOS": case "ELETRÔNICOS": case "ELECTRONICS":
                return ELETRONICOS;
            case "ROUPAS": case "CLOTHING": case "CLOTHES":
                return ROUPAS;
            case "LIVROS": case "BOOKS":
                return LIVROS;
            case "CASA_E_JARDIM": case "CASA E JARDIM": case "HOME_AND_GARDEN": case "HOME":
                return CASA_E_JARDIM;
            case "ESPORTES": case "SPORTS":
                return ESPORTES;
            case "BELEZA": case "BEAUTY":
                return BELEZA;
            case "ALIMENTACAO": case "ALIMENTAÇÃO": case "FOOD":
                return ALIMENTACAO;
            case "BRINQUEDOS": case "TOYS":
                return BRINQUEDOS;
            case "FERRAMENTAS": case "TOOLS":
                return FERRAMENTAS;
            case "OUTROS": case "OTHER": case "OTHERS":
                return OUTROS;
        }
        
        throw new IllegalArgumentException("Categoria desconhecida: " + valor);
    }

    /*
        Retorna categoria formatada para exibição.
        Implementa switch exaustivo.
    */
    public String formatarParaExibicao() {
        switch (this) {
            case ELETRONICOS: return "📱 " + nomeExibicao;
            case ROUPAS: return "👕 " + nomeExibicao;
            case LIVROS: return "📚 " + nomeExibicao;
            case CASA_E_JARDIM: return "🏠 " + nomeExibicao;
            case ESPORTES: return "⚽ " + nomeExibicao;
            case BELEZA: return "💄 " + nomeExibicao;
            case ALIMENTACAO: return "🍎 " + nomeExibicao;
            case BRINQUEDOS: return "🧸 " + nomeExibicao;
            case FERRAMENTAS: return "🔧 " + nomeExibicao;
            case OUTROS: return "📦 " + nomeExibicao;
        }
        
        // Este ponto nunca deve ser alcançado se todos os casos forem tratados
        throw new IllegalStateException("Categoria não tratada: " + this);
    }

    @Override
    public String toString() {
        return nomeExibicao;
    }
}