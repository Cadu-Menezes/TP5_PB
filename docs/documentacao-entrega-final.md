# Documentacao da Entrega Final

## 1. Arquitetura detalhada do sistema

O sistema foi estruturado como monorepo Maven multi-modulo, com separacao entre dominio e camada de entrega web.

### 1.1 Modulos

- `core-produtos`
- `core-pedidos`
- `app-integrado`

### 1.2 Responsabilidades por modulo

`core-produtos`:

- regras de negocio de produtos
- servicos de produto
- repositorios e contratos relacionados ao dominio de produtos

`core-pedidos`:

- regras de negocio de pedidos
- servicos de pedido
- repositorios e contratos relacionados ao dominio de pedidos

`app-integrado`:

- camada web (API HTTP + frontend estatico)
- orquestracao entre `ProdutoService` e `PedidoService`
- fluxo integrado de criacao de pedido com baixa de estoque

### 1.3 Visao de execucao

1. A aplicacao inicializa conexao com PostgreSQL.
2. Executa migracoes de banco.
3. Instancia repositorios e servicos de produtos e pedidos.
4. Sobe o servidor web integrado (porta padrao `7000`, com override por `PORT`).
5. Expoe API e frontend no mesmo processo de aplicacao.

### 1.4 Integracao funcional principal

No endpoint integrado de pedidos, o sistema executa uma transacao de negocio em duas etapas:

1. cria o pedido
2. reduz o estoque do produto

Se houver erro na segunda parte do fluxo, existe estrategia de compensacao para manter consistencia.

## 2. Workflows GitHub Actions e pipeline de deploy

Workflow principal:

- `.github/workflows/ci.yml`

### 2.1 Gatilhos configurados

- `push` em `main` e `master`
- `pull_request` em `main` e `master`
- `release` (published)
- `workflow_dispatch`

### 2.2 Jobs implementados

`build-test`:

- checkout
- setup Java 17
- build + testes Maven
- empacotamento
- upload de artefatos de build e relatorios

`manual-approval`:

- gate manual por `environment` (`approval-gate`)
- aplicado em `release` e `workflow_dispatch`

`teste-selenium-pos-deploy`:

- validacao funcional de interface com Selenium
- executado apos aprovacao manual
- roda em ambiente temporario no runner (sem dependencia de hospedagem externa)

`sast-codeql`:

- analise estatica de seguranca (SAST) com CodeQL

`dast-zap`:

- analise dinamica de seguranca (DAST) com OWASP ZAP
- sobe app + PostgreSQL no runner e escaneia `http://localhost:7000`
- publica relatorios do ZAP como artefatos

### 2.3 Como o pipeline de deploy foi tratado

Nao ha deploy para um provedor externo nesta entrega.

Mesmo sem publicacao em nuvem, o pipeline foi organizado com fluxo de liberacao:

1. build e testes
2. aprovacao manual
3. validacoes pos-aprovacao (Selenium)
4. seguranca (SAST/DAST)

Esse desenho permite evoluir para deploy externo depois, sem refatorar o restante do pipeline.

## 3. Guia de execucao do sistema e testes

## 3.1 Pre-requisitos locais

- Java 17
- Maven 3.9+
- PostgreSQL em `localhost:5432`

Variaveis padrao de banco (podem ser sobrescritas):

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`

### 3.2 Build local

```powershell
mvn clean install
```

### 3.3 Execucao local da aplicacao

```powershell
mvn -f app-integrado/pom.xml exec:java
```

Aplicacao disponivel em:

- `http://localhost:7000`

### 3.4 Execucao de testes locais

Todos os testes do modulo web:

```powershell
mvn -pl app-integrado test
```

Selenium (suite dedicada):

```powershell
$env:SELENIUM_BASE_URL="http://localhost:7000"
mvn -pl app-integrado -Dtest=TesteSeleniumPosDeploy test
```

### 3.5 Como interpretar resultados

Build verde:

- compilacao ok
- testes ok
- jobs de seguranca executados sem erro de infraestrutura

Build vermelho:

- verificar primeiro passo que falhou
- baixar artifacts (`dast-app-log`, `selenium-app-log`, relatorios)
- usar o guia de troubleshooting em `docs/alertas-troubleshooting-workflows.md`

## 4. Estrategias de validacao pos-deploy e seguranca

## 4.1 Validacao pos-deploy (funcional)

Foi implementada validacao funcional com Selenium cobrindo cenarios basicos de navegacao:

- carregamento da pagina principal
- alternancia entre abas de produtos e pedidos
- abertura e fechamento do modal de criacao de produto

Esses testes sao executados no job `teste-selenium-pos-deploy`.

## 4.2 Seguranca implementada

SAST (CodeQL):

- identifica problemas de seguranca no codigo-fonte
- resultados centralizados em `Security` / `Code scanning alerts`

DAST (OWASP ZAP Baseline):

- avalia comportamento da aplicacao em execucao
- identifica ausencias de headers e configuracoes inseguras de runtime
- relatorios publicados como artifacts (`dast-zap-reports`)

## 4.3 Observabilidade de pipeline

Para depuracao e rastreabilidade foram adicionados:

- logs de contexto por job
- resumo de execucao em `GITHUB_STEP_SUMMARY`
- artifacts tecnicos de diagnostico

## 5. Referencias internas

- `README.md`
- `docs/manual-execucao-local.md`
- `docs/manual-workflows-actions.md`
- `docs/alertas-troubleshooting-workflows.md`
- `.github/workflows/ci.yml`
