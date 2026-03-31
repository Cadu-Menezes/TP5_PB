# Manual de Execucao Local (TP4 Integrado)

## 1. Pre-requisitos

- Java 17 instalado e no PATH
- Maven 3.9+ instalado e no PATH
- PostgreSQL rodando em localhost:5432
- Banco com usuario/senha padrao (ou variaveis customizadas):
  - Usuario padrao: `postgres`
  - Senha padrao: `Sua_Senha_Postgres`
  - Banco padrao: `tp4_integrado_db`

## 2. Clonar e acessar o projeto

```powershell
git clone <URL_DO_REPOSITORIO>
cd tp4-integrado
```

## 3. Build completo

```powershell
mvn clean install
```

Se precisar compilar ignorando verificacao de cobertura temporariamente:

```powershell
mvn clean install -DskipTests "-Djacoco.skip=true"
```

## 4. Subir a aplicacao integrada

No Windows PowerShell, use o comando com caminho explicito do POM para evitar erro de diretorio:

```powershell
mvn -f app-integrado/pom.xml exec:java
```

Ao iniciar com sucesso, a API e frontend ficam em:

- `http://localhost:7000`


## 5. Endpoints principais

- Produtos: `GET/POST/PUT/DELETE /api/produtos`
- Pedidos: `GET/POST/PUT/DELETE /api/pedidos`
- Integrado (cria pedido e baixa estoque): `POST /api/integrado/pedidos`

### Exemplo payload integrado

```json
{
  "produtoId": "uuid-do-produto",
  "quantidade": 2,
  "nomeCliente": "Cliente Exemplo",
  "observacao": "Entrega normal",
  "status": "ABERTO"
}
```
