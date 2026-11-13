# Decomposição de Tarefas — Fase 7: Tela inicial e histórico

- **Feature ID:** `fase-7-tela-inicial-e-historico`
- **Especificação:** `docs/specs/fase-7-tela-inicial-e-historico/spec.md` (Status: `IN_PROGRESS`)
- **Data de criação:** 2026-10-28

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Implementar modelos de consolidação (`HomeSummary`, `HistoryItem`, `ActivityType`) | `VERIFIED` | Nenhuma |
| `TASK-02` | Implementar casos de uso `GetHomeSummaryUseCase` e `GetConsolidatedHistoryUseCase` | `VERIFIED` | `TASK-01`; Fase 6 `TASK-02` e `TASK-03` |
| `TASK-03` | Implementar `HomeViewModel` e gerenciar `HomeUiState` com atalhos de criação | `VERIFIED` | `TASK-02` |
| `TASK-04` | Implementar `HistoryViewModel` com filtros por tipo de atividade e ordenação | `VERIFIED` | `TASK-02` |
| `TASK-05` | Implementar `HomeScreen` com blocos resumidos (ações rápidas, hábitos, tarefas, treino) | `VERIFIED` | `TASK-03` |
| `TASK-06` | Implementar `HistoryScreen` com abas de filtro e linha do tempo de realizações | `VERIFIED` | `TASK-04` |
| `TASK-07` | Verificação de consistência entre Home, Histórico e listas específicas | `VERIFIED` | `TASK-05`, `TASK-06` |

---

## Detalhamento das Tarefas

### `TASK-01`: Implementar modelos de consolidação (`HomeSummary`, `HistoryItem`, `ActivityType`)
- **Status:** `VERIFIED`
- **Objetivo:** Criar modelos Kotlin puros para representar a agregação de dados do dia na Home e itens unificados do Histórico.
- **Referência à especificação:** Critérios `AC-01`, `AC-04`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/model/HomeSummary.kt`
  - `app/src/main/java/com/ego/mobile/domain/model/HistoryItem.kt`
  - `app/src/test/java/com/ego/mobile/domain/HistoryDomainTest.kt`
- **Critérios de aceite:**
  1. `HistoryItem` encapsula data/hora, tipo de atividade, título, subtítulo e status.
  2. `HomeSummary` agrupa hábitos do dia, tarefas prioritárias e alerta de rotina ativa.
- **Evidência:** `HistoryDomainTest` e a suíte completa passaram via `.\gradlew.bat testDebugUnitTest` após RED funcional (modelos ausentes) e implementação.
- **Definição de pronto:** `HistoryDomainTest` passando via `./gradlew testDebugUnitTest`.

---

### `TASK-02`: Implementar casos de uso `GetHomeSummaryUseCase` e `GetConsolidatedHistoryUseCase`
- **Status:** `VERIFIED`
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
- **Evidência:** `.\gradlew.bat testDebugUnitTest` passou após RED funcional nos dois testes dos casos de uso.
- **Dependências:** Fase 6 `TASK-02` (Room/DAO) e `TASK-03` (repositório), ambas verificadas.
- **Comportamentos verificados:**
  1. Resumo reativo de hábitos ativos agendados para hoje, tarefas abertas ordenadas por prazo e rotina ativa.
  2. Treino ativo tem precedência sobre a próxima etapa capilar quando ambos existem.
  3. Histórico consolida execuções concluídas dos quatro tipos, com timestamps reais em ordem decrescente.
  4. Alterações emitidas por cada repositório atualizam os fluxos consolidados.
- **Definição de pronto:** Testes unitários dos casos de uso passando.

---

### `TASK-03`: Implementar `HomeViewModel` e gerenciar `HomeUiState` com atalhos de criação
- **Status:** `VERIFIED`
- **Objetivo:** Criar o ViewModel da Home para expor estado reativo e despachar ações de conclusão rápida e navegação para criação de hábitos/tarefas.
- **Referência à especificação:** Critérios `AC-01`, `AC-02`, `AC-03`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/home/HomeViewModel.kt`
  - `app/src/main/java/com/ego/mobile/feature/home/HomeUiState.kt`
  - `app/src/test/java/com/ego/mobile/feature/home/HomeViewModelTest.kt`
- **Evidência:** `HomeViewModelTest` e `.\gradlew.bat testDebugUnitTest` passaram após RED funcional.
- **Comportamentos verificados:**
  1. Emite o resumo reativo em `HomeUiState.Content`.
  2. Atalhos emitem eventos de solicitação de criação de hábito/tarefa.
  3. Conclusões rápidas usam os repositórios existentes e propagam falhas ao estado da tela.
- **Definição de pronto:** `HomeViewModelTest` passando no Gradle.

---

### `TASK-04`: Implementar `HistoryViewModel` com filtros por tipo de atividade e ordenação
- **Status:** `VERIFIED`
- **Objetivo:** Criar ViewModel da área de histórico com suporte a filtro por atividade (Todas, Hábitos, Tarefas, Treinos, Cuidados).
- **Referência à especificação:** Critério `AC-04`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/history/HistoryViewModel.kt`
  - `app/src/main/java/com/ego/mobile/feature/history/HistoryUiState.kt`
  - `app/src/test/java/com/ego/mobile/feature/history/HistoryViewModelTest.kt`
- **Evidência:** `HistoryViewModelTest` passou após RED funcional; a suíte unitária e instrumentada completa também passou após a conclusão das telas.
- **Comportamentos verificados:**
  1. Emite o histórico consolidado em ordem decrescente de execução.
  2. Filtra por tipo de atividade e retorna a "Todas" quando o filtro é removido.
  3. Mantém o filtro selecionado enquanto as fontes reativas emitem novos registros.
- **Definição de pronto:** `HistoryViewModelTest` passando no Gradle.

---

### `TASK-05`: Implementar `HomeScreen` com blocos resumidos (ações rápidas, hábitos, tarefas, treino)
- **Status:** `VERIFIED`
- **Objetivo:** Construir a interface Composable completa da Home, substituindo o placeholder da Fase 1.
- **Referência à especificação:** Critérios `AC-01`, `AC-02`, `AC-03`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/home/HomeScreen.kt`
  - `app/src/main/java/com/ego/mobile/feature/home/components/QuickActionHeader.kt`
  - `app/src/main/java/com/ego/mobile/feature/home/components/TodayHabitsSummaryCard.kt`
  - `app/src/main/java/com/ego/mobile/feature/home/components/PriorityTasksCard.kt`
  - `app/src/main/java/com/ego/mobile/feature/home/components/ActiveRoutineBanner.kt`
- **Comportamentos verificados:**
  1. Exibe atalhos e formulários funcionais para criar hábitos e tarefas, persistindo pela fonte de dados existente.
  2. Mostra hábitos de hoje e permite concluir ocorrências; lista tarefas abertas e destaca prazos vencidos com o rótulo acessível "Atrasada".
  3. Exibe treino em andamento com ação que abre a área de rotinas.
  4. Testes instrumentados da Home passaram após RED funcional; relatório em `reports/TASK-05.md`.
- **Evidência:** `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.ego.mobile.feature.home.HomeScreenTest"` passou.
- **Definição de pronto:** Telas renderizadas no tema escuro e integradas à navegação.

---

### `TASK-06`: Implementar `HistoryScreen` com abas de filtro e linha do tempo de realizações
- **Status:** `VERIFIED`
- **Objetivo:** Construir a interface Composable completa do Histórico, substituindo o placeholder da Fase 1.
- **Referência à especificação:** Critério `AC-04`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/history/HistoryScreen.kt`
  - `app/src/main/java/com/ego/mobile/feature/history/components/HistoryFilterChips.kt`
  - `app/src/main/java/com/ego/mobile/feature/history/components/HistoryTimelineItem.kt`
- **Comportamentos verificados:**
  1. Apresenta itens concluídos consolidados em uma linha do tempo e estados vazios/carregando.
  2. Filtra por Todas, Hábitos, Tarefas, Treinos e Cuidados, mantendo filtro no estado do `HistoryViewModel`.
  3. Teste instrumentado de filtros e integração com Room passou após RED funcional; relatório em `reports/TASK-06.md`.
- **Evidência:** `.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.ego.mobile.feature.history.HistoryScreenTest"` passou.
- **Definição de pronto:** Interface de histórico acessível pela barra inferior.

---

### `TASK-07`: Verificação de consistência entre Home, Histórico e listas específicas
- **Status:** `VERIFIED`
- **Objetivo:** Executar testes automatizados provando que marcar itens na Home atualiza instantaneamente o Histórico e as telas originais.
- **Referência à especificação:** Critérios `AC-01` a `AC-06`.
- **Componentes envolvidos:**
  - `app/src/androidTest/java/com/ego/mobile/feature/HomeHistoryConsistencyTest.kt`
  - ViewModels e repositórios de Home, Histórico, Hábitos e Tarefas.
- **Comportamentos verificados:**
  1. Conclusão rápida de hábito na Home altera o resumo do dia, persiste a ocorrência e publica o registro no Histórico.
  2. Conclusão rápida de tarefa na Home remove-a da lista aberta, apresenta-a nas concluídas e publica o registro no Histórico.
  3. As telas de Hábitos e Tarefas e o Histórico observam os mesmos repositórios Room, sem cópias locais da fonte de verdade.
- **Evidência:** `.\gradlew.bat testDebugUnitTest connectedDebugAndroidTest` passou, incluindo o teste de integração de consistência; relatório em `reports/TASK-07.md`.
- **Definição de pronto:** `.\gradlew.bat testDebugUnitTest connectedDebugAndroidTest` 100% verde com relatório de consistência.
