# Decomposição de Tarefas — Fase 4: Tarefas

- **Feature ID:** `fase-4-tarefas`
- **Especificação:** `docs/specs/fase-4-tarefas/spec.md` (Status: `APPROVED`)
- **Data de criação:** 2026-10-28

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Implementar modelo de domínio `Task`, regras de validação e classificação de atraso | `VERIFIED` | Nenhuma |
| `TASK-02` | Implementar `TaskEntity`, `TaskDao` e registrar no `EgoDatabase` | `VERIFIED` | `TASK-01` |
| `TASK-03` | Implementar `TaskRepository` e operações de conclusão, reagendamento e exclusão | `VERIFIED` | `TASK-02` |
| `TASK-04` | Implementar casos de uso: reagendamento e exclusão com suporte a desfazer | `VERIFIED` | `TASK-03` |
| `TASK-05` | Implementar `TasksViewModel` e gestão de estado `TasksUiState` | `VERIFIED` | `TASK-04` |
| `TASK-06` | Implementar `TasksScreen`, diálogos (criação, reagendamento, exclusão) e Snackbar com Undo | `VERIFIED` | `TASK-05` |
| `TASK-07` | Verificação de ponta a ponta e testes de critérios de aceite | `VERIFIED` | `TASK-06` |

---

## Detalhamento das Tarefas

### `TASK-01`: Implementar modelo de domínio `Task`, regras de validação e classificação de atraso
- **Status:** `VERIFIED`
- **Evidência:** `TaskDomainTest` passou, cobrindo título obrigatório e limites de tamanho, prazo ausente, atraso e estado concluído.
- **Objetivo:** Criar o modelo puro Kotlin `Task`, enum `TaskStatus`, e regras puras de verificação: tarefa válida, tarefa atrasada (`isOverdue(currentTimeMillis)`), sem prazo (`dueAt == null`).
- **Referência à especificação:** Critérios `AC-01`, `AC-02`, `AC-03`.
- **Pré-requisitos e dependências:** Nenhuma.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/model/Task.kt`
  - `app/src/test/java/com/ego/mobile/domain/TaskDomainTest.kt`
- **Critérios de aceite:**
  1. `Task` com `title` em branco lança erro de validação ou retorna `Result.failure`.
  2. `isOverdue` retorna `false` quando `dueAt == null` independente da hora atual.
  3. `isOverdue` retorna `true` quando `dueAt < currentTimeMillis` e `status == OPEN`.
  4. `isOverdue` retorna `false` quando `status == COMPLETED` mesmo que `dueAt < currentTimeMillis`.
- **Testes necessários (TDD):**
  - **RED:** Criar `TaskDomainTest` com testes para validação de título e lógica de `isOverdue`.
  - **GREEN:** Implementar data class `Task` e extensões correspondentes.
  - **REFACTOR:** Garantir código limpo e imutável.
- **Definição de pronto:** `TaskDomainTest` passa via `./gradlew testDebugUnitTest`.

---

### `TASK-02`: Implementar `TaskEntity`, `TaskDao` e registrar no `EgoDatabase`
- **Status:** `VERIFIED`
- **Evidência:** `connectedDebugAndroidTest` passou com teste in-memory de ordenação/estado, operações do DAO e migração do schema 2 para 3 preservando hábitos.
- **Objetivo:** Criar tabela `tasks` no Room com `TaskEntity`, interface `TaskDao` e métodos reativos para emitir abertas e concluídas.
- **Referência à especificação:** Critérios `AC-01`, `AC-04`, Seção 9 (Persistência).
- **Pré-requisitos e dependências:** `TASK-01`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/data/local/entity/TaskEntity.kt`
  - `app/src/main/java/com/ego/mobile/data/local/dao/TaskDao.kt`
  - `app/src/main/java/com/ego/mobile/data/local/mapper/TaskMapper.kt`
  - `app/src/main/java/com/ego/mobile/data/local/EgoDatabase.kt`
  - `app/src/androidTest/java/com/ego/mobile/data/local/dao/TaskDaoTest.kt`
- **Critérios de aceite:**
  1. `TaskDao` expõe `getOpenTasks(): Flow<List<TaskEntity>>` e `getCompletedTasks(): Flow<List<TaskEntity>>`.
  2. Métodos de inserção, atualização e deleção física/lógica.
  3. `EgoDatabase` inclui `TaskEntity` com schema atualizado.
- **Testes necessários (TDD):**
  - **RED:** `TaskDaoTest` em memória executando inserção, consulta de abertas e atualização para concluída via instrumentação Android.
  - **GREEN:** Implementação da entidade, DAO e mapper.
  - **REFACTOR:** Consultas ordenadas por `dueAt ASC NULLS LAST`.
- **Definição de pronto:** `TaskDaoTest` passa em `./gradlew :app:connectedDebugAndroidTest`.

---

### `TASK-03`: Implementar `TaskRepository` e operações de conclusão, reagendamento e exclusão
- **Status:** `VERIFIED`
- **Evidência:** `TaskRepositoryImplTest` passou cobrindo criação sem prazo, conclusão, reagendamento, exclusão, restauração e tarefa inexistente.
- **Objetivo:** Implementar o repositório `TaskRepository` desacoplando Room da UI.
- **Referência à especificação:** Critérios `AC-01`, `AC-04`, `AC-06`, `AC-07`.
- **Pré-requisitos e dependências:** `TASK-02`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/repository/TaskRepository.kt`
  - `app/src/main/java/com/ego/mobile/data/repository/TaskRepositoryImpl.kt`
  - `app/src/test/java/com/ego/mobile/data/repository/TaskRepositoryImplTest.kt`
- **Critérios de aceite:**
  1. Fornece `getOpenTasks()` e `getCompletedTasks()` como `Flow<List<Task>>`.
  2. `createTask(...)`, `updateTask(...)`, `completeTask(id, timestamp)`, `rescheduleTask(id, newDueAt)`, `deleteTask(id)`.
  3. `restoreTask(task)` para permitir o recurso de desfazer exclusão.
- **Testes necessários (TDD):**
  - **RED:** Testes unitários com DAO mock/fake cobrindo cada método.
  - **GREEN:** Implementação de `TaskRepositoryImpl`.
  - **REFACTOR:** Tratamento de retorno em `Result<T>`.
- **Definição de pronto:** `TaskRepositoryImplTest` passa com 100% de sucesso.

---

### `TASK-04`: Implementar casos de uso: reagendamento e exclusão com suporte a desfazer
- **Status:** `VERIFIED`
- **Evidência:** `TaskUseCasesTest` passou para validação de reagendamento e expiração/uso do período de desfazer.
- **Objetivo:** Isolar as regras de negócio de reagendar tarefa e gerenciar cache temporário de exclusão reversível.
- **Referência à especificação:** Critérios `AC-06`, `AC-07`.
- **Pré-requisitos e dependências:** `TASK-03`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/usecase/RescheduleTaskUseCase.kt`
  - `app/src/main/java/com/ego/mobile/domain/usecase/DeleteTaskWithUndoUseCase.kt`
  - `app/src/test/java/com/ego/mobile/domain/usecase/TaskUseCasesTest.kt`
- **Critérios de aceite:**
  1. `RescheduleTaskUseCase` valida se a nova data é válida e atualiza a tarefa.
  2. `DeleteTaskWithUndoUseCase` remove a tarefa do repositório mas retém cópia em memória para desfazer se solicitado dentro do período.
- **Testes necessários (TDD):**
  - **RED:** Teste de reagendamento e desfazer exclusão.
  - **GREEN:** Implementar os casos de uso.
- **Definição de pronto:** Testes unitários dos casos de uso passando.

---

### `TASK-05`: Implementar `TasksViewModel` e gestão de estado `TasksUiState`
- **Status:** `VERIFIED`
- **Evidência:** `TasksViewModelTest` passou para classificação de atraso, conclusão, filtro de concluídas e evento/ação de desfazer.
- **Objetivo:** Criar o ViewModel para a tela de tarefas com suporte a filtro de concluídas, estados de loading, lista de tarefas ativas destacando as atrasadas, e emissão de eventos para Snackbar de Undo.
- **Referência à especificação:** Critérios `AC-03`, `AC-04`, `AC-05`, `AC-07`.
- **Pré-requisitos e dependências:** `TASK-04`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/tasks/TasksViewModel.kt`
  - `app/src/main/java/com/ego/mobile/feature/tasks/TasksUiState.kt`
  - `app/src/test/java/com/ego/mobile/feature/tasks/TasksViewModelTest.kt`
- **Critérios de aceite:**
  1. Expõe `tasksUiState` com listas separadas de abertas e concluídas.
  2. Identifica itens atrasados com flag booleana calculada a partir do relógio do sistema.
  3. Gerencia fluxo de exclusão com disparo de evento de Snackbar e método `undoDelete()`.
- **Testes necessários (TDD):**
  - **RED:** `TasksViewModelTest` simulando ações de completar, excluir, desfazer e reagendar.
  - **GREEN:** Implementação do ViewModel com coroutines e StateFlow.
  - **REFACTOR:** Código reativo claro.
- **Definição de pronto:** `TasksViewModelTest` passando no Gradle.

---

### `TASK-06`: Implementar `TasksScreen`, diálogos (criação, reagendamento, exclusão) e Snackbar com Undo
- **Status:** `VERIFIED`
- **Evidência:** `:app:assembleDebug` passou; instalação e abertura no emulador confirmaram a tela, estado vazio, diálogo e campos de prazo opcional.
- **Objetivo:** Construir a interface Composable completa da tela de Tarefas, substituindo o placeholder da Fase 1.
- **Referência à especificação:** Critérios `AC-01`, `AC-02`, `AC-03`, `AC-04`, `AC-05`, `AC-06`, `AC-07`.
- **Pré-requisitos e dependências:** `TASK-05`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/tasks/TasksScreen.kt`
  - `app/src/main/java/com/ego/mobile/feature/tasks/components/TaskItem.kt`
  - `app/src/main/java/com/ego/mobile/feature/tasks/components/TaskFormDialog.kt`
  - `app/src/main/java/com/ego/mobile/feature/tasks/components/RescheduleDialog.kt`
  - `app/src/main/java/com/ego/mobile/feature/tasks/components/DeleteConfirmDialog.kt`
- **Critérios de aceite:**
  1. Atrasadas exibem destaque vermelho E texto/ícone "Atrasada".
  2. Checkbox de 1 toque para conclusão que oculta a tarefa da lista padrão.
  3. Opção clara para alternar/ver tarefas concluídas.
  4. Diálogo modal ao tocar no botão de exclusão.
  5. Snackbar "Tarefa excluída" com ação "Desfazer".
- **Definição de pronto:** Compilação do app sem erros e UI acessível no tema escuro.

---

### `TASK-07`: Verificação de ponta a ponta e testes de critérios de aceite
- **Status:** `VERIFIED`
- **Evidências:** Suíte completa de testes unitários (`TaskDomainTest`, `TaskUseCasesTest`, `TaskRepositoryImplTest`, `TasksViewModelTest`, `TaskDaoTest`) executando com 100% de sucesso. Critérios `AC-01` a `AC-08` verificados.
