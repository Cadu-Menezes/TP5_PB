package com.faculdade.integrado.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.faculdade.integrado.support.InMemoryRepositories;
import com.faculdade.pedidos.service.PedidoService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebServerIntegradoTest {

    private WebServerIntegrado server;
    private HttpClient client;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        InMemoryRepositories.ProdutoRepository produtoRepository = new InMemoryRepositories.ProdutoRepository();
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
    void deveResponderEndpointRaiz() throws Exception {
        HttpResponse<String> response = get("/");

        assertEquals(302, response.statusCode());
        assertTrue(response.headers().firstValue("location").orElse("").contains("index.html"));
    }

    @Test
    void deveExecutarFluxoCompletoDeProdutos() throws Exception {
        HttpResponse<String> createResponse = post("/api/produtos", """
                {
                  "nome": "Notebook Teste",
                  "descricao": "Ultrafino",
                  "preco": "4500.50",
                  "categoria": "ELETRONICOS",
                  "quantidadeEstoque": "10"
                }
                """);

        assertEquals(201, createResponse.statusCode());
        JsonObject createJson = JsonParser.parseString(createResponse.body()).getAsJsonObject();
        String produtoId = createJson.get("id").getAsString();

        HttpResponse<String> getByIdResponse = get("/api/produtos/" + produtoId);
        assertEquals(200, getByIdResponse.statusCode());
        JsonObject produtoJson = JsonParser.parseString(getByIdResponse.body()).getAsJsonObject();
        assertEquals("Notebook Teste", produtoJson.get("nome").getAsString());

        HttpResponse<String> updateResponse = put("/api/produtos/" + produtoId, """
                {
                  "nome": "Notebook Atualizado",
                  "descricao": "Ultrafino 2",
                  "preco": "4700.00",
                  "categoria": "ELETRONICOS",
                  "quantidadeEstoque": "10"
                }
                """);
        assertEquals(200, updateResponse.statusCode());

        HttpResponse<String> patchEstoqueResponse = patch("/api/produtos/" + produtoId + "/estoque", """
                {
                  "quantidade": 4
                }
                """);
        assertEquals(200, patchEstoqueResponse.statusCode());

        HttpResponse<String> listResponse = get("/api/produtos");
        assertEquals(200, listResponse.statusCode());
        JsonArray produtos = JsonParser.parseString(listResponse.body()).getAsJsonArray();
        assertEquals(1, produtos.size());

        HttpResponse<String> categoriaResponse = get("/api/produtos/categoria/ELETRONICOS");
        assertEquals(200, categoriaResponse.statusCode());

        HttpResponse<String> estoqueBaixoResponse = get("/api/produtos/estoque/baixo?limite=5");
        assertEquals(200, estoqueBaixoResponse.statusCode());
        assertEquals(1, JsonParser.parseString(estoqueBaixoResponse.body()).getAsJsonArray().size());

        HttpResponse<String> categoriasResponse = get("/api/categorias");
        assertEquals(200, categoriasResponse.statusCode());
        assertTrue(JsonParser.parseString(categoriasResponse.body()).getAsJsonArray().size() > 0);

        HttpResponse<String> estatisticasResponse = get("/api/estatisticas");
        assertEquals(200, estatisticasResponse.statusCode());
        JsonObject stats = JsonParser.parseString(estatisticasResponse.body()).getAsJsonObject();
        assertEquals(1, stats.get("totalProdutos").getAsInt());

        HttpResponse<String> deleteResponse = delete("/api/produtos/" + produtoId);
        assertEquals(200, deleteResponse.statusCode());
    }

    @Test
    void deveRetornarErroParaDadosInvalidosDeProduto() throws Exception {
        HttpResponse<String> invalidCreate = post("/api/produtos", """
                {
                  "descricao": "Sem nome",
                  "preco": "10.00",
                  "categoria": "ELETRONICOS",
                  "quantidadeEstoque": "1"
                }
                """);

        assertEquals(400, invalidCreate.statusCode());
        assertTrue(invalidCreate.body().contains("Dados inválidos"));

        HttpResponse<String> invalidQuery = get("/api/produtos/estoque/baixo?limite=abc");
        assertEquals(400, invalidQuery.statusCode());
        assertTrue(invalidQuery.body().contains("Parâmetro inválido"));

  HttpResponse<String> invalidCategory = get("/api/produtos/categoria/NAO_EXISTE");
  assertEquals(400, invalidCategory.statusCode());
  assertTrue(invalidCategory.body().contains("Categoria inválida"));

  HttpResponse<String> invalidId = get("/api/produtos/id-invalido");
  assertEquals(400, invalidId.statusCode());
  assertTrue(invalidId.body().contains("ID inválido"));
    }

    @Test
    void deveRetornarConflitoQuandoProdutoDuplicadoEErrosDeOperacoesInexistentes() throws Exception {
  HttpResponse<String> createResponse = post("/api/produtos", """
    {
      "nome": "Produto Unico",
      "descricao": "Descricao",
      "preco": "99.90",
      "categoria": "ELETRONICOS",
      "quantidadeEstoque": "2"
    }
    """);
  assertEquals(201, createResponse.statusCode());

  HttpResponse<String> duplicateResponse = post("/api/produtos", """
    {
      "nome": "Produto Unico",
      "descricao": "Descricao 2",
      "preco": "109.90",
      "categoria": "ELETRONICOS",
      "quantidadeEstoque": "3"
    }
    """);
  assertEquals(409, duplicateResponse.statusCode());
  assertTrue(duplicateResponse.body().contains("Produto já existe"));

  HttpResponse<String> notFoundGet = get("/api/produtos/00000000-0000-0000-0000-000000000000");
  assertEquals(404, notFoundGet.statusCode());

  HttpResponse<String> notFoundUpdate = put("/api/produtos/00000000-0000-0000-0000-000000000000", """
    {
      "nome": "Outro",
      "descricao": "Descricao",
      "preco": "10.00",
      "categoria": "ELETRONICOS",
      "quantidadeEstoque": "1"
    }
    """);
  assertEquals(404, notFoundUpdate.statusCode());

  HttpResponse<String> notFoundDelete = delete("/api/produtos/00000000-0000-0000-0000-000000000000");
  assertEquals(404, notFoundDelete.statusCode());
    }

    @Test
    void deveCobrirEstatisticasComSemEstoqueEAtualizacaoInvalida() throws Exception {
  HttpResponse<String> createA = post("/api/produtos", """
    {
      "nome": "Produto A",
      "descricao": "Descricao A",
      "preco": "10.00",
      "categoria": "ELETRONICOS",
      "quantidadeEstoque": "0"
    }
    """);
  assertEquals(201, createA.statusCode());

  HttpResponse<String> createB = post("/api/produtos", """
    {
      "nome": "Produto B",
      "descricao": "Descricao B",
      "preco": "20.00",
      "categoria": "ELETRONICOS",
      "quantidadeEstoque": "5"
    }
    """);
  assertEquals(201, createB.statusCode());
  String produtoId = JsonParser.parseString(createB.body()).getAsJsonObject().get("id").getAsString();

  HttpResponse<String> stats = get("/api/estatisticas");
  assertEquals(200, stats.statusCode());
  JsonObject statsJson = JsonParser.parseString(stats.body()).getAsJsonObject();
  assertEquals(2, statsJson.get("totalProdutos").getAsInt());
  assertEquals(1, statsJson.get("produtosSemEstoque").getAsInt());
  assertEquals(1, statsJson.get("produtosEstoqueBaixo").getAsInt());

  HttpResponse<String> invalidPatch = patch("/api/produtos/" + produtoId + "/estoque", """
    {
      "quantidade": -1
    }
    """);
  assertEquals(400, invalidPatch.statusCode());
  assertTrue(invalidPatch.body().contains("Dados inválidos"));
    }

    @Test
    void deveExecutarFluxoDePedidos() throws Exception {
        HttpResponse<String> createResponse = post("/api/pedidos", """
                {
                  "nomeCliente": "Cliente Pedido",
                  "observacao": "Entrega manha",
                  "status": "ABERTO"
                }
                """);

        assertEquals(201, createResponse.statusCode());
        JsonObject created = JsonParser.parseString(createResponse.body()).getAsJsonObject();
        String pedidoId = created.get("id").getAsString();

        HttpResponse<String> listResponse = get("/api/pedidos");
        assertEquals(200, listResponse.statusCode());
        assertEquals(1, JsonParser.parseString(listResponse.body()).getAsJsonArray().size());

        HttpResponse<String> updateResponse = put("/api/pedidos/" + pedidoId, """
                {
                  "nomeCliente": "Cliente Pedido 2",
                  "observacao": "Entrega tarde",
                  "status": "EM_ANDAMENTO"
                }
                """);
        assertEquals(200, updateResponse.statusCode());

        HttpResponse<String> deleteResponse = delete("/api/pedidos/" + pedidoId);
        assertEquals(204, deleteResponse.statusCode());
    }

    @Test
    void deveRetornarErroDeValidacaoEmPedido() throws Exception {
        HttpResponse<String> response = post("/api/pedidos", """
                {
                  "nomeCliente": "Cliente Erro",
                  "observacao": "Teste",
                  "status": "INVALIDO"
                }
                """);

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Status inválido"));
    }

    @Test
    void deveCriarPedidoIntegradoComBaixaDeEstoque() throws Exception {
        HttpResponse<String> createProduto = post("/api/produtos", """
                {
                  "nome": "Mouse Integrado",
                  "descricao": "Mouse sem fio",
                  "preco": "120.00",
                  "categoria": "ELETRONICOS",
                  "quantidadeEstoque": "5"
                }
                """);
        String produtoId = JsonParser.parseString(createProduto.body()).getAsJsonObject().get("id").getAsString();

        HttpResponse<String> integrateResponse = post("/api/integrado/pedidos", """
                {
                  "produtoId": "%s",
                  "quantidade": 2,
                  "nomeCliente": "Cliente Integrado",
                  "observacao": "Tudo certo",
                  "status": "ABERTO"
                }
                """.formatted(produtoId));

        assertEquals(201, integrateResponse.statusCode());
        JsonObject result = JsonParser.parseString(integrateResponse.body()).getAsJsonObject();
        assertEquals(produtoId, result.get("produtoId").getAsString());
        assertEquals(3, result.get("estoqueAtual").getAsInt());
    }

    @Test
    void deveRetornarErrosNoEndpointIntegrado() throws Exception {
        HttpResponse<String> produto = post("/api/produtos", """
                {
                  "nome": "Teclado Integrado",
                  "descricao": "Teclado",
                  "preco": "200.00",
                  "categoria": "ELETRONICOS",
                  "quantidadeEstoque": "1"
                }
                """);
        String produtoId = JsonParser.parseString(produto.body()).getAsJsonObject().get("id").getAsString();

        HttpResponse<String> estoqueInsuficiente = post("/api/integrado/pedidos", """
                {
                  "produtoId": "%s",
                  "quantidade": 3,
                  "nomeCliente": "Cliente",
                  "observacao": "Teste",
                  "status": "ABERTO"
                }
                """.formatted(produtoId));
        assertEquals(400, estoqueInsuficiente.statusCode());

        HttpResponse<String> naoEncontrado = post("/api/integrado/pedidos", """
                {
                  "produtoId": "00000000-0000-0000-0000-000000000000",
                  "quantidade": 1,
                  "nomeCliente": "Cliente",
                  "observacao": "Teste",
                  "status": "ABERTO"
                }
                """);
        assertEquals(404, naoEncontrado.statusCode());

              HttpResponse<String> statusInvalido = post("/api/integrado/pedidos", """
                {
                  "produtoId": "%s",
                  "quantidade": 1,
                  "nomeCliente": "Cliente",
                  "observacao": "Teste",
                  "status": "INVALIDO"
                }
                """.formatted(produtoId));
              assertEquals(400, statusInvalido.statusCode());
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
}
