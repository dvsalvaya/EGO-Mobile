# Decomposição de Tarefas — Fase 7: Tela inicial e histórico

- **Feature ID:** `fase-7-tela-inicial-e-historico`
- **Especificação:** `docs/specs/fase-7-tela-inicial-e-historico/spec.md` (Status: `APPROVED`)
- **Data de criação:** 2026-10-28

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Implementar modelos de consolidação (`HomeSummary`, `HistoryItem`, `ActivityType`) | `PENDING` | Nenhuma |
| `TASK-02` | Implementar casos de uso `GetHomeSummaryUseCase` e `GetConsolidatedHistoryUseCase` | `PENDING` | `TASK-01` |
| `TASK-03` | Implementar `HomeViewModel` e gerenciar `HomeUiState` com atalhos de criação | `PENDING` | `TASK-02` |
| `TASK-04` | Implementar `HistoryViewModel` com filtros por tipo de atividade e ordenação | `PENDING` | `TASK-02` |
| `TASK-05` | Implementar `HomeScreen` com blocos resumidos (ações rápidas, hábitos, tarefas, treino) | `PENDING` | `TASK-03` |
| `TASK-06` | Implementar `HistoryScreen` com abas de filtro e linha do tempo de realizações | `PENDING` | `TASK-04` |
| `TASK-07` | Verificação de consistência entre Home, Histórico e listas específicas | `PENDING` | `TASK-05`, `TASK-06` |

---

## Detalhamento das Tarefas

### `TASK-01`: Implementar modelos de consolidação (`HomeSummary`, `HistoryItem`, `ActivityType`)
- **Status:** `PENDING`
- **Objetivo:** Criar modelos Kotlin puros para representar a agregação de dados do dia na Home e itens unificados do Histórico.
- **Referência à especificação:** Critérios `AC-01`, `AC-04`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/model/HomeSummary.kt`
  - `app/src/main/java/com/ego/mobile/domain/model/HistoryItem.kt`
  - `app/src/test/java/com/ego/mobile/domain/HistoryDomainTest.kt`
- **Critérios de aceite:**
  1. `HistoryItem` encapsula data/hora, tipo de atividade, título, subtítulo e status.
  2. `HomeSummary` agrupa hábitos do dia, tarefas prioritárias e alerta de rotina ativa.
- **Definição de pronto:** `HistoryDomainTest` passando via `./gradlew testDebugUnitTest`.

---

### `TASK-02`: Implementar casos de uso `GetHomeSummaryUseCase` e `GetConsolidatedHistoryUseCase`
- **Status:** `PENDING`
- **Objetivo:** Orquestrar `Flow` combinados a partir de `HabitRepository`, `TaskRepository`, `WorkoutRepository` e `HaircareRepository`.
- **Referência à especificação:** Critérios `AC-02`, `AC-03`, `AC-04`, `AC-05`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/usecase/GetHomeSummaryUseCase.kt`
  - `app/src/main/java/com/ego/mobile/domain/usecase/GetConsolidatedHistoryUseCase.kt`
  - `app/src/test/java/com/ego/mobile/domain/usecase/HomeSummaryUseCaseTest.kt`
  - `app/src/test/java/com/ego/mobile/domain/usecase/HistoryUseCaseTest.kt`
- **Critérios de aceite:**
  1. `GetHomeSummaryUseCase` emite atualização quando qualquer repositório subjacente for alterado.
  2. `GetConsolidatedHistoryUseCase` junta e ordena cronologicamente de forma decrescente todos os registros concluídos.
- **Definição de pronto:** Testes unitários dos casos de uso passando.

---

### `TASK-03`: Implementar `HomeViewModel` e gerenciar `HomeUiState` com atalhos de criação
- **Status:** `PENDING`
- **Objetivo:** Criar o ViewModel da Home para expor estado reativo e despachar ações de conclusão rápida e navegação para criação de hábitos/tarefas.
- **Referência à especificação:** Critérios `AC-01`, `AC-02`, `AC-03`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/home/HomeViewModel.kt`
  - `app/src/main/java/com/ego/mobile/feature/home/HomeUiState.kt`
  - `app/src/test/java/com/ego/mobile/feature/home/HomeViewModelTest.kt`
- **Definição de pronto:** `HomeViewModelTest` passando no Gradle.

---

### `TASK-04`: Implementar `HistoryViewModel` com filtros por tipo de atividade e ordenação
- **Status:** `PENDING`
- **Objetivo:** Criar ViewModel da área de histórico com suporte a filtro por atividade (Todas, Hábitos, Tarefas, Treinos, Cuidados).
- **Referência à especificação:** Critério `AC-04`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/history/HistoryViewModel.kt`
  - `app/src/main/java/com/ego/mobile/feature/history/HistoryUiState.kt`
  - `app/src/test/java/com/ego/mobile/feature/history/HistoryViewModelTest.kt`
- **Definição de pronto:** `HistoryViewModelTest` passando no Gradle.

---

### `TASK-05`: Implementar `HomeScreen` com blocos resumidos (ações rápidas, hábitos, tarefas, treino)
- **Status:** `PENDING`
- **Objetivo:** Construir a interface Composable completa da Home, substituindo o placeholder da Fase 1.
- **Referência à especificação:** Critérios `AC-01`, `AC-02`, `AC-03`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/home/HomeScreen.kt`
  - `app/src/main/java/com/ego/mobile/feature/home/components/QuickActionHeader.kt`
  - `app/src/main/java/com/ego/mobile/feature/home/components/TodayHabitsSummaryCard.kt`
  - `app/src/main/java/com/ego/mobile/feature/home/components/PriorityTasksCard.kt`
  - `app/src/main/java/com/ego/mobile/feature/home/components/ActiveRoutineBanner.kt`
- **Definição de pronto:** Telas renderizadas no tema escuro e integradas à navegação.

---

### `TASK-06`: Implementar `HistoryScreen` com abas de filtro e linha do tempo de realizações
- **Status:** `PENDING`
- **Objetivo:** Construir a interface Composable completa do Histórico, substituindo o placeholder da Fase 1.
- **Referência à especificação:** Critério `AC-04`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/history/HistoryScreen.kt`
  - `app/src/main/java/com/ego/mobile/feature/history/components/HistoryFilterChips.kt`
  - `app/src/main/java/com/ego/mobile/feature/history/components/HistoryTimelineItem.kt`
- **Definição de pronto:** Interface de histórico acessível pela barra inferior.

---

### `TASK-07`: Verificação de consistência entre Home, Histórico e listas específicas
- **Status:** `PENDING`
- **Objetivo:** Executar testes automatizados provando que marcar itens na Home atualiza instantaneamente o Histórico e as telas originais.
- **Referência à especificação:** Critérios `AC-01` a `AC-06`.
- **Componentes envolvidos:** Toda a suíte de testes do projeto.
- **Definição de pronto:** `./gradlew testDebugUnitTest` 100% verde com relatório de consistência.
