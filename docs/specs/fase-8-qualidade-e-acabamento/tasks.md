# Decomposição de Tarefas — Fase 8: Qualidade e acabamento do MVP

- **Feature ID:** `fase-8-qualidade-e-acabamento`
- **Especificação:** `docs/specs/fase-8-qualidade-e-acabamento/spec.md` (Status: `APPROVED`)
- **Data de criação:** 2026-10-28

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Auditoria de acessibilidade, contraste de cores e áreas de toque mínimas (48dp) | `PENDING` | Nenhuma |
| `TASK-02` | Revisão de estados vazios e tratamento amigável de erros em todas as 5 abas | `PENDING` | `TASK-01` |
| `TASK-03` | Auditoria de segurança, permissões no `AndroidManifest.xml` e escopo negativo do PRD | `PENDING` | `TASK-02` |
| `TASK-04` | Limpeza de dependências mortas e otimização de Gradle | `PENDING` | `TASK-03` |
| `TASK-05` | Atualização do `README.md` com instruções reais de execução e compilação | `PENDING` | `TASK-04` |
| `TASK-06` | Execução da suíte completa de testes de regressão e validação final do MVP | `PENDING` | `TASK-05` |

---

## Detalhamento das Tarefas

### `TASK-01`: Auditoria de acessibilidade, contraste de cores e áreas de toque mínimas (48dp)
- **Status:** `PENDING`
- **Objetivo:** Inspecionar todos os componentes composable, garantindo que botões e ícones tenham `contentDescription` preenchido e padding adequado para target de toque de 48dp.
- **Referência à especificação:** Critérios `AC-01`, `AC-02`.
- **Definição de pronto:** Verificação de acessibilidade concluída sem avisos críticos do Compose.

---

### `TASK-02`: Revisão de estados vazios e tratamento amigável de erros em todas as 5 abas
- **Status:** `PENDING`
- **Objetivo:** Garantir que quando o banco estiver limpo, Início, Hábitos, Tarefas, Rotinas e Histórico exibam textos convidativos com ícones e botões de criação direta.
- **Referência à especificação:** Critério `AC-03`.
- **Definição de pronto:** Todas as 5 abas testadas com base vazia.

---

### `TASK-03`: Auditoria de segurança, permissões no `AndroidManifest.xml` e escopo negativo do PRD
- **Status:** `PENDING`
- **Objetivo:** Inspecionar `AndroidManifest.xml` e o código-fonte para garantir que não há permissões de rede, telemetria, login remoto ou gamificação punitiva.
- **Referência à especificação:** Critérios `AC-04`, `AC-05`.
- **Definição de pronto:** Conformidade total com a lista de itens fora de escopo do PRD.

---

### `TASK-04`: Limpeza de dependências mortas e otimização de Gradle
- **Status:** `PENDING`
- **Objetivo:** Revisar `gradle/libs.versions.toml` e `app/build.gradle.kts` removendo bibliotecas ou plugins que não estejam sendo utilizados.
- **Referência à especificação:** Critério `AC-07`.
- **Definição de pronto:** Build limpo e sem avisos de dependências órfãs.

---

### `TASK-05`: Atualização do `README.md` com instruções reais de execução e compilação
- **Status:** `PENDING`
- **Objetivo:** Documentar no `README.md` a versão exata do JDK (JDK 17/21), comandos `./gradlew assembleDebug` e `./gradlew testDebugUnitTest`, arquitetura e instruções do emulador.
- **Referência à especificação:** Critério `AC-06`.
- **Definição de pronto:** `README.md` atualizado e testado.

---

### `TASK-06`: Execução da suíte completa de testes de regressão e validação final do MVP
- **Status:** `PENDING`
- **Objetivo:** Executar `./gradlew testDebugUnitTest` e `./gradlew assembleDebug` validando todos os critérios de aceite de ponta a ponta.
- **Referência à especificação:** Critério `AC-07`.
- **Definição de pronto:** 100% dos testes executados com sucesso no terminal.
