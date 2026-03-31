package com.faculdade.integrado.web;

import com.faculdade.integrado.service.PedidoEstoqueIntegracaoService;
import com.faculdade.pedidos.exception.NaoEncontradoException;
import com.faculdade.pedidos.exception.ValidacaoException;
import com.faculdade.pedidos.service.PedidoService;
import com.faculdade.pedidos.web.ErroApi;
import com.faculdade.pedidos.web.PedidoController;
import com.faculdade.produtos.service.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.faculdade.produtos.web.ProdutoController;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.HttpStatus;
import io.javalin.json.JavalinJackson;

public final class WebServerIntegrado {

    private final Javalin app;
    private final int porta;
    private int portaEfetiva;

    public WebServerIntegrado(ProdutoService produtoService, PedidoService pedidoService, int porta) {
        this.porta = porta;
        this.portaEfetiva = porta;
        this.app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
            config.staticFiles.add("/public");
            ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            config.jsonMapper(new JavalinJackson(objectMapper));
        });
        registrarRotas(produtoService, pedidoService);
        registrarTratamentoErros();
    }

    public void iniciar() {
        app.start(porta);
        this.portaEfetiva = app.port();
    }

    public int getPortaEfetiva() {
        return portaEfetiva;
    }

    public void parar() {
        app.stop();
    }

    private void registrarRotas(ProdutoService produtoService, PedidoService pedidoService) {
        ProdutoController produtoController = new ProdutoController(produtoService);
        PedidoController pedidoController = new PedidoController(pedidoService);
        PedidoIntegradoController pedidoIntegradoController =
            new PedidoIntegradoController(new PedidoEstoqueIntegracaoService(produtoService, pedidoService));

        app.get("/", ctx -> ctx.redirect("/index.html"));

        app.get("/api/produtos/categoria/{categoria}", produtoController::listarPorCategoria);
        app.get("/api/produtos/estoque/baixo", produtoController::listarEstoqueBaixo);
        app.get("/api/categorias", produtoController::listarCategorias);
        app.get("/api/estatisticas", produtoController::obterEstatisticas);
        app.get("/api/produtos", produtoController::listarTodos);
        app.get("/api/produtos/{id}", produtoController::buscarPorId);
        app.post("/api/produtos", produtoController::criarProduto);
        app.put("/api/produtos/{id}", produtoController::atualizarProduto);
        app.delete("/api/produtos/{id}", produtoController::excluirProduto);
        app.patch("/api/produtos/{id}/estoque", produtoController::atualizarEstoque);

        app.get("/api/pedidos", pedidoController::listar);
        app.post("/api/pedidos", pedidoController::criar);
        app.put("/api/pedidos/{id}", pedidoController::atualizar);
        app.delete("/api/pedidos/{id}", pedidoController::excluir);

        app.post("/api/integrado/pedidos", pedidoIntegradoController::criarComBaixaEstoque);
    }

    private void registrarTratamentoErros() {
        app.exception(BadRequestResponse.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(ErroApi.of("Requisicao invalida."));
        });

        app.exception(ValidacaoException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(ErroApi.of(e.getMessage()));
        });

        app.exception(NaoEncontradoException.class, (e, ctx) -> {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.json(ErroApi.of(e.getMessage()));
        });

        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(ErroApi.of("Erro interno no servidor."));
            e.printStackTrace();
        });
    }
}
