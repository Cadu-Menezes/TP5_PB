package com.faculdade.produtos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa um produto no sistema.
 * Implementa imutabilidade para prevenir efeitos colaterais.
 */
public final class Produto {
    private final ProdutoId id;
    private final Nome nome;
    private final Descricao descricao;
    private final Preco preco;
    private final Categoria categoria;
    private final QuantidadeEstoque quantidadeEstoque;
    private final LocalDateTime criadoEm;
    private final LocalDateTime atualizadoEm;

    private Produto(Builder builder) {
        this.id = builder.id;
        this.nome = builder.nome;
        this.descricao = builder.descricao;
        this.preco = builder.preco;
        this.categoria = builder.categoria;
        this.quantidadeEstoque = builder.quantidadeEstoque;
        this.criadoEm = builder.criadoEm;
        this.atualizadoEm = builder.atualizadoEm;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Produto criar(Nome nome, Descricao descricao, Preco preco, 
                               Categoria categoria, QuantidadeEstoque quantidadeEstoque) {
        LocalDateTime agora = LocalDateTime.now();
        return new Builder()
                .id(ProdutoId.gerar())
                .nome(nome)
                .descricao(descricao)
                .preco(preco)
                .categoria(categoria)
                .quantidadeEstoque(quantidadeEstoque)
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();
    }

    public Produto atualizar(Nome nome, Descricao descricao, Preco preco, 
                            Categoria categoria, QuantidadeEstoque quantidadeEstoque) {
        return new Builder()
                .id(this.id)
                .nome(nome)
                .descricao(descricao)
                .preco(preco)
                .categoria(categoria)
                .quantidadeEstoque(quantidadeEstoque)
                .criadoEm(this.criadoEm)
                .atualizadoEm(LocalDateTime.now())
                .build();
    }

    public Produto atualizarEstoque(QuantidadeEstoque novaQuantidade) {
        return new Builder()
                .id(this.id)
                .nome(this.nome)
                .descricao(this.descricao)
                .preco(this.preco)
                .categoria(this.categoria)
                .quantidadeEstoque(novaQuantidade)
                .criadoEm(this.criadoEm)
                .atualizadoEm(LocalDateTime.now())
                .build();
    }

    // Getters (apenas consulta - CQS)
    public ProdutoId getId() { return id; }
    public Nome getNome() { return nome; }
    public Descricao getDescricao() { return descricao; }
    public Preco getPreco() { return preco; }
    public Categoria getCategoria() { return categoria; }
    public QuantidadeEstoque getQuantidadeEstoque() { return quantidadeEstoque; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Produto produto = (Produto) obj;
        return Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome=" + nome +
                ", descricao=" + descricao +
                ", preco=" + preco +
                ", categoria=" + categoria +
                ", quantidadeEstoque=" + quantidadeEstoque +
                ", criadoEm=" + criadoEm +
                ", atualizadoEm=" + atualizadoEm +
                '}';
    }

    public static final class Builder {
        private ProdutoId id;
        private Nome nome;
        private Descricao descricao;
        private Preco preco;
        private Categoria categoria;
        private QuantidadeEstoque quantidadeEstoque;
        private LocalDateTime criadoEm;
        private LocalDateTime atualizadoEm;

        private Builder() {}

        public Builder id(ProdutoId id) {
            this.id = id;
            return this;
        }

        public Builder nome(Nome nome) {
            this.nome = nome;
            return this;
        }

        public Builder descricao(Descricao descricao) {
            this.descricao = descricao;
            return this;
        }

        public Builder preco(Preco preco) {
            this.preco = preco;
            return this;
        }

        public Builder categoria(Categoria categoria) {
            this.categoria = categoria;
            return this;
        }

        public Builder quantidadeEstoque(QuantidadeEstoque quantidadeEstoque) {
            this.quantidadeEstoque = quantidadeEstoque;
            return this;
        }

        public Builder criadoEm(LocalDateTime criadoEm) {
            this.criadoEm = criadoEm;
            return this;
        }

        public Builder atualizadoEm(LocalDateTime atualizadoEm) {
            this.atualizadoEm = atualizadoEm;
            return this;
        }

        public Produto build() {
            validarCamposObrigatorios();
            return new Produto(this);
        }

        private void validarCamposObrigatorios() {
            if (id == null) throw new IllegalArgumentException("ID do produto é obrigatório");
            if (nome == null) throw new IllegalArgumentException("Nome do produto é obrigatório");
            if (descricao == null) throw new IllegalArgumentException("Descrição do produto é obrigatória");
            if (preco == null) throw new IllegalArgumentException("Preço do produto é obrigatório");
            if (categoria == null) throw new IllegalArgumentException("Categoria do produto é obrigatória");
            if (quantidadeEstoque == null) throw new IllegalArgumentException("Quantidade em estoque do produto é obrigatória");
            if (criadoEm == null) throw new IllegalArgumentException("Data de criação do produto é obrigatória");
            if (atualizadoEm == null) throw new IllegalArgumentException("Data de atualização do produto é obrigatória");
        }
    }
}