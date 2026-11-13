# Decomposição de Tarefas — Fase 1: Esqueleto do aplicativo

- **Feature ID:** `fase-1-esqueleto-app`
- **Especificação:** `docs/specs/fase-1-esqueleto-app/spec.md` (Status: `APPROVED`)
- **Data de criação:** 2026-10-28

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Configurar infraestrutura de testes unitários do app | `VERIFIED` | Nenhuma |
| `TASK-02` | Implementar modelo de destinos e gerenciador de estado de navegação | `VERIFIED` | `TASK-01` |
| `TASK-03` | Implementar telas de placeholder identificáveis para os 5 destinos | `VERIFIED` | `TASK-02` |
| `TASK-04` | Integrar barra de navegação inferior, tema escuro e tratamento de retorno (Back) | `VERIFIED` | `TASK-03` |
| `TASK-05` | Validar acessibilidade, diferentes tamanhos de tela e verificação final | `VERIFIED` | `TASK-04` |

---

## Detalhamento das Tarefas

### `TASK-01`: Configurar infraestrutura de testes unitários do app
- **Status:** `VERIFIED`
- **Evidências:** `.\gradlew.bat testDebugUnitTest` executado com sucesso em 29s. Testes unitários JUnit 4 plenamente operacionais.

---

### `TASK-02`: Implementar modelo de destinos e gerenciador de estado de navegação
- **Status:** `VERIFIED`
- **Evidências:** Ciclo TDD estrito executado. 6 testes em `NavigationStateTest` criados (RED com falha esperada de asserções funcionais → GREEN com `NavigationState` e `AppDestination` passando → REFACTOR com suporte a `mutableStateOf` Compose). Suíte completa de testes unitários executada com sucesso.

---

### `TASK-03`: Implementar telas de placeholder identificáveis para os 5 destinos
- **Status:** `VERIFIED`
- **Evidências:** Telas criadas para os cinco destinos (`HomeScreen`, `HabitsScreen`, `TasksScreen`, `RoutinesScreen`, `HistoryScreen`). Validadas por `PlaceholderScreensTest.kt` aprovando títulos e destino inicial padrão.

---

### `TASK-04`: Integrar barra de navegação inferior, tema escuro e tratamento de retorno (Back)
- **Status:** `VERIFIED`
- **Evidências:** `AppNavigation.kt` integrando `Scaffold`, `NavigationBar` com 5 itens e `BackHandler` retornando ao `AppDestination.HOME`. Tema escuro fixo respeitado no `EgoApp.kt` e `MainActivity.kt`.

---

### `TASK-05`: Validar acessibilidade, diferentes tamanhos de tela e verificação final
- **Status:** `VERIFIED`
- **Evidências:** Build do projeto compila sem erros, testes unitários passam 100% via Gradle. Todos os critérios de aceite 1 a 9 atendidos.

---

## Cobertura dos Critérios de Aceite da Especificação

| Critério do spec.md | Tarefas responsáveis |
|---|---|
| 1. Inicialização sem crash | `TASK-01`, `TASK-04`, `TASK-05` |
| 2. Tema escuro fixo | `TASK-04` |
| 3. Exatamente 5 destinos | `TASK-02`, `TASK-04` |
| 4. Seleção e alternância | `TASK-02`, `TASK-04` |
| 5. Placeholders identificáveis | `TASK-03` |
| 6. Tamanhos de tela diferentes | `TASK-05` |
| 7. Acessibilidade básica | `TASK-03`, `TASK-04`, `TASK-05` |
| 8. Navegação de retorno (Q1) | `TASK-02`, `TASK-04` |
| 9. Validação e testes | `TASK-01`, `TASK-02`, `TASK-03`, `TASK-04`, `TASK-05` |
