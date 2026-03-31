# Manual dos Workflows na aba Actions

## 1. Onde acessar

- Abra o repositorio no GitHub.
- Clique na aba `Actions`.
- Selecione o workflow: `CI TP4 Integrado`.

Arquivo do workflow:

- `.github/workflows/ci.yml`

## 2. Quando o workflow executa

Gatilhos configurados:

- `push` para branches `main` e `master`
- `pull_request` para branches `main` e `master`
- `workflow_dispatch` (execucao manual)

## 3. Como executar manualmente

1. Abra `Actions`.
2. Selecione `CI TP4 Integrado`.
3. Clique em `Run workflow`.
4. Escolha a branch e confirme.

## 4. O que o workflow faz

Job: `build-test` em `ubuntu-latest`

Passos:

1. `Checkout` do codigo
2. `Setup Java 17` (Temurin) com cache Maven
3. `Build and Test` (`mvn -B -ntp clean test`)
4. `Package` (`mvn -B -ntp -DskipTests package`)
5. `Upload JaCoCo Reports` (artefato com pastas `target/site/jacoco/`)

## 5. Como interpretar resultado

- Verde (success): build e testes passaram.
- Vermelho (failure): houve falha em compilacao, teste ou cobertura.

Em caso de falha:

1. Abra o run do workflow.
2. Entre no passo que falhou.
3. Leia o log completo do Maven.
4. Corrija localmente e execute `mvn clean install`.
5. Envie novo commit para disparar novamente.

## 6. Onde baixar relatorios de cobertura

1. Abra um run concluido.
2. No final da pagina, em `Artifacts`, baixe `jacoco-reports`.
3. Extraia e abra os HTMLs de cobertura (`index.html`) dentro das pastas `target/site/jacoco/`.
