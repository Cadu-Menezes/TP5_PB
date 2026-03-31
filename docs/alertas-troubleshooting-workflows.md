# Alertas e Troubleshooting dos Workflows

Este guia documenta como monitorar o pipeline e como diagnosticar falhas comuns no workflow `CI TP4 Integrado`.

## 1. Alertas recomendados

### 1.1 Alertas de falha de workflow (GitHub Notifications)

Objetivo: ser avisado quando algum job quebrar (`build-test`, `sast-codeql`, `dast-zap`, `teste-selenium-pos-deploy`).

Passos:

1. No repositorio, abra `Watch` (canto superior direito).
2. Selecione `Custom`.
3. Marque `Actions`.
4. Salve.

Resultado: toda falha de workflow gera notificacao no GitHub (e por e-mail, se habilitado na conta).

### 1.2 Alertas de seguranca (Code Scanning)

Objetivo: receber alertas de SAST quando o CodeQL detectar risco.

Passos:

1. Abra `Security` no repositorio.
2. Entre em `Code scanning alerts`.
3. Habilite notificacoes para novos alertas (dependendo da configuracao da conta/org).

Resultado: findings de SAST ficam centralizados em `Security` e rastreaveis por severidade.

### 1.3 Protecao da branch principal

Objetivo: impedir merge com pipeline quebrado.

Passos:

1. Abra `Settings` > `Branches`.
2. Crie ou edite regra para `main`.
3. Habilite `Require status checks to pass before merging`.
4. Selecione checks do workflow `CI TP4 Integrado`.

Resultado: PR so pode ser mergeado com checks obrigatorios aprovados.

## 2. Logs e evidencias disponiveis

Durante os runs, o pipeline publica:

- `ci-artifacts`
- `selenium-test-reports`
- `selenium-app-log`
- `dast-zap-reports`
- `dast-app-log`

Nos jobs tambem existem:

- logs agrupados por contexto (`CI Context`, `SAST Context`, `DAST Context`, `Selenium Context`)
- resumo em `GITHUB_STEP_SUMMARY`

## 3. Troubleshooting rapido

### 3.1 `manual-approval` aparece como skipped

Causa comum:

- run disparado por `push` ou `pull_request`.

Correcao:

1. Execute por `workflow_dispatch` ou `release`.
2. Confirme `environment: approval-gate` configurado com reviewers.

### 3.2 `Wait for application` falha com `connection refused`

Causas comuns:

- app nao subiu em background
- falha de dependencia interna Maven
- falha de conexao com PostgreSQL

Correcao:

1. Verifique artifact `dast-app-log` ou `selenium-app-log`.
2. Confirme variaveis `DB_*` e `PORT=7000` no job.
3. Confirme servico `postgres` ativo no job.

### 3.3 Erro Maven `mainClass ... missing or invalid`

Causa comum:

- `exec:java` executado no projeto raiz (agregador).

Correcao:

1. Executar `exec:java` no modulo correto (`app-integrado`).

### 3.4 Erro Maven `Could not find artifact core-produtos/core-pedidos`

Causa comum:

- modulos internos nao instalados no runner antes do `exec:java`.

Correcao:

1. Rodar `mvn -DskipTests -Djacoco.skip=true install` antes de subir a app.

### 3.5 Falha por cobertura JaCoCo no job de DAST/Selenium

Causa comum:

- `jacoco:check` bloqueando job tecnico que so prepara runtime.

Correcao:

1. Usar `-Djacoco.skip=true` no install desses jobs.

### 3.6 Erro ZAP `Permission denied: /zap/wrk/zap.yaml`

Causa comum:

- permissao de escrita no volume montado.

Correcao:

1. Criar e ajustar permissao dos arquivos de saida antes do scan.

### 3.7 Erro ZAP `artifact name ... is not valid`

Causa comum:

- upload interno da action do ZAP falhando no ambiente.

Correcao:

1. Executar ZAP via Docker diretamente.
2. Fazer upload com `actions/upload-artifact`.

## 4. Checklist de analise de incidente

Quando um run falhar:

1. Identifique o primeiro passo que falhou (nao o ultimo).
2. Baixe os artifacts de log.
3. Compare contexto do run: `event`, `ref`, `sha`, `job`.
4. Classifique causa em: build, teste, infra local do runner, seguranca.
5. Abra issue com:
   - run id
   - job/step
   - trecho do erro
   - acao corretiva aplicada

## 5. Manutencao da documentacao

Atualize este arquivo sempre que surgir nova falha recorrente ou nova regra de monitoramento.
