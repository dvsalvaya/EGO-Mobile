# Decomposição de Tarefas — Fase 8: Qualidade e acabamento do MVP

- **Feature ID:** `fase-8-qualidade-e-acabamento`
- **Especificação:** `docs/specs/fase-8-qualidade-e-acabamento/spec.md` (Status: `COMPLETED`)
- **Data de criação:** 2026-10-28

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Auditoria de acessibilidade, contraste de cores e áreas de toque mínimas (48dp) | `VERIFIED` | Nenhuma |
| `TASK-02` | Revisão de estados vazios e tratamento amigável de erros em todas as 5 abas | `VERIFIED` | `TASK-01` |
| `TASK-03` | Auditoria de segurança, permissões no `AndroidManifest.xml` e escopo negativo do PRD | `VERIFIED` | `TASK-02` |
| `TASK-04` | Limpeza de dependências mortas e otimização de Gradle | `VERIFIED` | `TASK-03` |
| `TASK-05` | Atualização do `README.md` com instruções reais de execução e compilação | `VERIFIED` | `TASK-04` |
| `TASK-06` | Execução da suíte completa de testes de regressão e validação final do MVP | `VERIFIED` | `TASK-05` |

---

## Detalhamento das Tarefas

### `TASK-01`: Auditoria de acessibilidade, contraste de cores e áreas de toque mínimas (48dp)
- **Status:** `VERIFIED`
- **Objetivo:** Inspecionar todos os componentes composable, garantindo que botões e ícones tenham `contentDescription` preenchido e padding adequado para target de toque de 48dp.
- **Referência à especificação:** Critérios `AC-01`, `AC-02`.
- **Comportamentos verificados:**
  1. Os controles com símbolo isolado (adicionar e concluir hábito, concluir tarefa) têm rótulos semânticos.
  2. Checkbox e ações compactas da tarefa têm alvo mínimo de 48dp; os demais controles usam componentes Material 3 com rótulos visíveis.
  3. As combinações de cores do tema escuro usadas para texto e estados atingem contraste WCAG AA.
- **Evidência:** `TaskItemAccessibilityTest`, `DarkThemeContrastTest` e `.\gradlew.bat testDebugUnitTest connectedDebugAndroidTest` passaram; relatório em `reports/TASK-01.md`.
- **Definição de pronto:** Verificação de acessibilidade concluída sem avisos críticos do Compose.

---

### `TASK-02`: Revisão de estados vazios e tratamento amigável de erros em todas as 5 abas
- **Status:** `VERIFIED`
- **Objetivo:** Garantir que quando o banco estiver limpo, Início, Hábitos, Tarefas, Rotinas e Histórico exibam textos convidativos com ícones e botões de criação direta.
- **Referência à especificação:** Critério `AC-03`.
- **Comportamentos verificados:**
  1. Início, Hábitos, Tarefas, Treinos, Cuidados capilares e Histórico exibem estados vazios informativos usando banco Room em memória; as áreas de criação exibem ações diretas.
  2. Falhas ao carregar hábitos, tarefas e histórico não são apresentadas como se os dados estivessem vazios.
  3. Erros de validação/operação dos treinos são apresentados por Snackbar e falhas ao carregar treinos ficam no estado de UI.
  4. Erros capilares repetidos podem ser apresentados novamente depois que a Snackbar anterior é dispensada.
  5. Histórico permanece uma área de consulta, conforme `docs/02-USER-FLOWS.md`; seu estado vazio informa que ainda não há atividades, sem introduzir criação de registros nessa tela.
- **Evidência:** `EmptyStateScreensTest`, `HomeScreenTest` e `WorkoutViewModelTest`; relatório em `reports/TASK-02.md`.

---

### `TASK-03`: Auditoria de segurança, permissões no `AndroidManifest.xml` e escopo negativo do PRD
- **Status:** `VERIFIED`
- **Objetivo:** Inspecionar `AndroidManifest.xml` e o código-fonte para garantir que não há permissões de rede, telemetria, login remoto ou gamificação punitiva.
- **Referência à especificação:** Critérios `AC-04`, `AC-05`.
- **Evidência:** Manifesto de origem e manifesto mesclado auditados; pesquisa de código e dependências sem achados de rede, login remoto, telemetria ou gamificação. Relatório em `reports/TASK-03.md`.
- **Definição de pronto:** Conformidade total com a lista de itens fora de escopo do PRD.

---

### `TASK-04`: Limpeza de dependências mortas e otimização de Gradle
- **Status:** `VERIFIED`
- **Objetivo:** Revisar `gradle/libs.versions.toml` e `app/build.gradle.kts` removendo bibliotecas ou plugins que não estejam sendo utilizados.
- **Referência à especificação:** Critério `AC-07`.
- **Comportamentos verificados:**
  1. Removidas dependências AndroidX de teste da configuração de testes JVM, onde não eram usadas; permanecem nas configurações de teste instrumentado.
  2. Mantido Espresso como dependência explícita de teste instrumentado; removê-lo fez a suíte instrumentada falhar em runtime, então não é dependência morta.
  3. Kotlin e Java configurados para gerar bytecode JVM 17.
- **Evidência:** `testDebugUnitTest`, `assembleDebug` e `connectedDebugAndroidTest` passaram; relatório em `reports/TASK-04.md`.

---

### `TASK-05`: Atualização do `README.md` com instruções reais de execução e compilação
- **Status:** `VERIFIED`
- **Objetivo:** Documentar no `README.md` a versão exata do JDK do Gradle (JDK 25), o alvo de bytecode JVM 17, comandos de build/teste, arquitetura e instruções do emulador.
- **Comportamentos verificados:**
  1. README cobre requisitos e versões reais da toolchain, funcionalidades atuais, arquitetura, build, testes e instalação em Windows/macOS/Linux.
  2. `.\gradlew.bat :app:installDebug` instalou o APK no emulador API 37 e o app foi iniciado via ADB.
  3. Os comandos de teste e build documentados também foram executados com sucesso na validação da TASK-04.
- **Evidência:** relatório em `reports/TASK-05.md`.
- **Referência à especificação:** Critério `AC-06`.
- **Definição de pronto:** `README.md` atualizado e testado.

---

### `TASK-06`: Execução da suíte completa de testes de regressão e validação final do MVP
- **Status:** `VERIFIED`
- **Objetivo:** Executar `./gradlew testDebugUnitTest` e `./gradlew assembleDebug` validando todos os critérios de aceite de ponta a ponta.
- **Referência à especificação:** Critério `AC-07`.
- **Comportamentos verificados:**
  1. A suíte de testes unitários do módulo app concluiu com sucesso sem falhas.
  2. O processo de build do debug concluiu com sucesso, confirmando que o APK pode ser gerado.
  3. O aviso do Kotlin sobre o JDK 25 foi observado, mas não bloqueou a compilação nem a validação final.
- **Evidência:** comando executado `./gradlew.bat testDebugUnitTest assembleDebug` com `BUILD SUCCESSFUL`; relatório em `reports/TASK-06.md`.
- **Definição de pronto:** 100% dos testes executados com sucesso no terminal e build de debug validada.
