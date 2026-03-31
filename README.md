# TP5

[![CI TP5](https://github.com/Cadu-Menezes/TP5_PB/actions/workflows/ci.yml/badge.svg)](https://github.com/Cadu-Menezes/TP5_PB/actions/workflows/ci.yml)
[![Java 17](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Security SAST+DAST](https://img.shields.io/badge/Security-SAST%20%2B%20DAST-0a7ea4)](https://github.com/Cadu-Menezes/TP5_PB/actions/workflows/ci.yml)

Projeto de integracao dos sistemas de produtos (TP1) e pedidos (TP3), com foco em refatoracao, modularidade e automacao CI/CD.

## Arquitetura Inicial

- core-produtos: dominio e servicos de produtos
- core-pedidos: dominio e servicos de pedidos
- app-integrado: camada web e orquestracao entre modulos

## Endpoints Principais

- Produtos: `GET/POST/PUT/DELETE /api/produtos`
- Pedidos: `GET/POST/PUT/DELETE /api/pedidos`
- Integracao pedido + estoque: `POST /api/integrado/pedidos`

### Exemplo de payload integrado

```json
{
	"produtoId": "uuid-do-produto",
	"quantidade": 2,
	"nomeCliente": "Cliente Exemplo",
	"observacao": "Entrega normal",
	"status": "ABERTO"
}
```

Esse endpoint cria o pedido e reduz o estoque do produto no mesmo fluxo de aplicacao.
Se a criacao do pedido falhar apos reduzir estoque, o sistema tenta rollback do estoque.

## Stack

- Java 17
- Maven multi-modulo
- PostgreSQL
- Javalin
- JUnit 5 + JaCoCo

## Meta de Qualidade

- Cobertura minima: 85%
- Refatoracao guiada por testes
- Integracao interna entre modulos (sem separar em dois apps)

## Documentacao de Operacao CI/CD

- [Manual de workflows na aba Actions](docs/manual-workflows-actions.md)
- [Alertas e troubleshooting dos workflows](docs/alertas-troubleshooting-workflows.md)

## Documentacao da Entrega Final

- [Documento consolidado da entrega](docs/documentacao-entrega-final.md)
