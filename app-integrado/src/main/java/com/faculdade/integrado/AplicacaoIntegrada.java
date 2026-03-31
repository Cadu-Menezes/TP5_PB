package com.faculdade.integrado;

import com.faculdade.integrado.config.DatabaseConfigIntegrada;
import com.faculdade.integrado.web.WebServerIntegrado;
import com.faculdade.pedidos.config.DatabaseMigration;
import com.faculdade.pedidos.repository.impl.RepositorioPostgreSqlPedido;
import com.faculdade.pedidos.service.PedidoService;
import com.faculdade.produtos.repository.impl.RepositorioPostgreSqlProduto;
import com.faculdade.produtos.service.ProdutoService;

import javax.sql.DataSource;

public final class AplicacaoIntegrada {

    private AplicacaoIntegrada() {
    }

    public static void main(String[] args) {
        System.out.println("TP4 Integrado - Produtos + Pedidos");

        try {
            DataSource dataSource = DatabaseConfigIntegrada.createDefaultDataSource();

            com.faculdade.produtos.config.DatabaseMigration.migrate(dataSource);
            DatabaseMigration.migrate(dataSource);

            RepositorioPostgreSqlProduto repositorioProduto = new RepositorioPostgreSqlProduto(dataSource);
            ProdutoService produtoService = new ProdutoService(repositorioProduto, repositorioProduto);

            RepositorioPostgreSqlPedido repositorioPedido = new RepositorioPostgreSqlPedido(dataSource);
            PedidoService pedidoService = new PedidoService(repositorioPedido, repositorioPedido);

            WebServerIntegrado webServer = new WebServerIntegrado(produtoService, pedidoService, portaFromEnvOrDefault());
            webServer.iniciar();

            System.out.println("Aplicacao integrada iniciada em http://localhost:" + webServer.getPortaEfetiva());
        } catch (Exception e) {
            System.err.println("Falha ao iniciar aplicacao integrada: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static int portaFromEnvOrDefault() {
        String value = System.getProperty("PORT");
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv("PORT");
        }
        if (value == null || value.trim().isEmpty()) {
            return 7000;
        }

        try {
            int porta = Integer.parseInt(value.trim());
            return porta > 0 ? porta : 7000;
        } catch (NumberFormatException e) {
            return 7000;
        }
    }
}
