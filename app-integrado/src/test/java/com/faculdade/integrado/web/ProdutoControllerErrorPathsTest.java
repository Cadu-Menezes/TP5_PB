package com.faculdade.integrado.web;

import com.faculdade.integrado.support.InMemoryRepositories;
import com.faculdade.pedidos.service.PedidoService;
import com.faculdade.produtos.exception.ExcecaoBancoDados;
import com.faculdade.produtos.model.Categoria;
import com.faculdade.produtos.model.Nome;
import com.faculdade.produtos.model.Produto;
import com.faculdade.produtos.model.ProdutoId;
import com.faculdade.produtos.repository.RepositorioComandoProduto;
import com.faculdade.produtos.repository.RepositorioConsultaProduto;
import com.faculdade.produtos.service.ProdutoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProdutoControllerErrorPathsTest {

    private WebServerIntegrado server;
    private HttpClient client;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        FailingProdutoRepository produtoRepository = new FailingProdutoRepository();
        InMemoryRepositories.PedidoRepository pedidoRepository = new InMemoryRepositories.PedidoRepository(false);

        ProdutoService produtoService = new ProdutoService(produtoRepository, produtoRepository);
        PedidoService pedidoService = new PedidoService(pedidoRepository, pedidoRepository);

        server = new WebServerIntegrado(produtoService, pedidoService, 0);
        server.iniciar();

        client = HttpClient.newHttpClient();
        baseUrl = "http://localhost:" + server.getPortaEfetiva();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.parar();
        }
    }

    @Test
    void deveRetornar500QuandoInfraFalhaEmRotasDeProduto() throws Exception {
        HttpResponse<String> create = post("/api/produtos", """
                {
                  "nome": "Erro Infra",
                  "descricao": "Descricao",
                  "preco": "10.00",
                  "categoria": "ELETRONICOS",
                  "quantidadeEstoque": "1"
                }
                """);
        assertEquals(500, create.statusCode());
        assertTrue(create.body().contains("Erro no banco de dados"));

        HttpResponse<String> list = get("/api/produtos");
        assertEquals(500, list.statusCode());

        HttpResponse<String> byId = get("/api/produtos/00000000-0000-0000-0000-000000000001");
        assertEquals(500, byId.statusCode());

        HttpResponse<String> update = put("/api/produtos/00000000-0000-0000-0000-000000000001", """
                {
                  "nome": "Nome",
                  "descricao": "Descricao",
                  "preco": "11.00",
                  "categoria": "ELETRONICOS",
                  "quantidadeEstoque": "2"
                }
                """);
        assertEquals(500, update.statusCode());

        HttpResponse<String> patch = patch("/api/produtos/00000000-0000-0000-0000-000000000001/estoque", """
                {
                  "quantidade": 2
                }
                """);
        assertEquals(500, patch.statusCode());

        HttpResponse<String> delete = delete("/api/produtos/00000000-0000-0000-0000-000000000001");
        assertEquals(500, delete.statusCode());
    }

    @Test
    void deveRetornar500QuandoInfraFalhaEmConsultasDeCategoriaEEstatisticas() throws Exception {
        HttpResponse<String> porCategoria = get("/api/produtos/categoria/ELETRONICOS");
        assertEquals(500, porCategoria.statusCode());

        HttpResponse<String> estoqueBaixo = get("/api/produtos/estoque/baixo?limite=3");
        assertEquals(500, estoqueBaixo.statusCode());

        HttpResponse<String> estatisticas = get("/api/estatisticas");
        assertEquals(500, estatisticas.statusCode());
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> post(String path, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> put(String path, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> patch(String path, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> delete(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).DELETE().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static final class FailingProdutoRepository implements RepositorioComandoProduto, RepositorioConsultaProduto {
        @Override
        public void salvar(Produto produto) throws ExcecaoBancoDados {
            throw new ExcecaoBancoDados("salvar", "falha simulada");
        }

        @Override
        public void atualizar(Produto produto) throws ExcecaoBancoDados {
            throw new ExcecaoBancoDados("atualizar", "falha simulada");
        }

        @Override
        public void excluirPorId(ProdutoId produtoId) throws ExcecaoBancoDados {
            throw new ExcecaoBancoDados("excluir", "falha simulada");
        }

        @Override
        public void excluirTodos() throws ExcecaoBancoDados {
            throw new ExcecaoBancoDados("excluirTodos", "falha simulada");
        }

        @Override
        public Optional<Produto> buscarPorId(ProdutoId produtoId) throws ExcecaoBancoDados {
            throw new ExcecaoBancoDados("buscarPorId", "falha simulada");
        }

        @Override
        public Optional<Produto> buscarPorNome(Nome nome) throws ExcecaoBancoDados {
            throw new ExcecaoBancoDados("buscarPorNome", "falha simulada");
        }

        @Override
        public List<Produto> buscarTodos() throws ExcecaoBancoDados {
            throw new ExcecaoBancoDados("buscarTodos", "falha simulada");
        }

        @Override
        public List<Produto> buscarPorCategoria(Categoria categoria) throws ExcecaoBancoDados {
            throw new ExcecaoBancoDados("buscarPorCategoria", "falha simulada");
        }

        @Override
        public List<Produto> buscarProdutosSemEstoque() throws ExcecaoBancoDados {
            throw new ExcecaoBancoDados("buscarProdutosSemEstoque", "falha simulada");
        }

        @Override
        public List<Produto> buscarProdutosEstoqueBaixo(int limite) throws ExcecaoBancoDados {
            throw new ExcecaoBancoDados("buscarProdutosEstoqueBaixo", "falha simulada");
        }

        @Override
        public long contar() throws ExcecaoBancoDados {
            throw new ExcecaoBancoDados("contar", "falha simulada");
        }

        @Override
        public boolean existePorNome(Nome nome) throws ExcecaoBancoDados {
            throw new ExcecaoBancoDados("existePorNome", "falha simulada");
        }

        @Override
        public boolean existePorId(ProdutoId produtoId) throws ExcecaoBancoDados {
            throw new ExcecaoBancoDados("existePorId", "falha simulada");
        }
    }
}
