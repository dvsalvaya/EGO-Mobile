# Decomposição de Tarefas — Fase 5: Treinos

- **Feature ID:** `fase-5-treinos`
- **Especificação:** `docs/specs/fase-5-treinos/spec.md` (Status: `APPROVED`)
- **Data de criação:** 2026-10-28

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Implementar modelos de domínio (`WorkoutSession`, `WorkoutExercise`) e regras de estado | `PENDING` | Nenhuma |
| `TASK-02` | Implementar entidades Room, DAOs (`WorkoutSessionDao`, `WorkoutExerciseDao`) e registrar no `EgoDatabase` | `PENDING` | `TASK-01` |
| `TASK-03` | Implementar `WorkoutRepository` com suporte a detecção de sessão ativa e idempotência | `PENDING` | `TASK-02` |
| `TASK-04` | Implementar `WorkoutViewModel` e gerenciar `WorkoutUiState` | `PENDING` | `TASK-03` |
| `TASK-05` | Implementar telas de treino: painel de entrada, tela de execução ativa e diálogo de exercícios | `PENDING` | `TASK-04` |
| `TASK-06` | Implementar histórico de treinos e finalização de sessão | `PENDING` | `TASK-05` |
| `TASK-07` | Verificação de ponta a ponta e testes de critérios de aceite | `PENDING` | `TASK-06` |

---

## Detalhamento das Tarefas

### `TASK-01`: Implementar modelos de domínio (`WorkoutSession`, `WorkoutExercise`) e regras de estado
- **Status:** `PENDING`
- **Objetivo:** Criar modelos Kotlin puros em `domain/model` e regras para cálculo de tempo decorrido, validação de transição de estado (`IN_PROGRESS` ↔ `PAUSED` → `COMPLETED`) e ordenação de exercícios.
- **Referência à especificação:** Critérios `AC-01`, `AC-05`, `AC-06`.
- **Pré-requisitos e dependências:** Nenhuma.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/model/WorkoutSession.kt`
  - `app/src/main/java/com/ego/mobile/domain/model/WorkoutExercise.kt`
  - `app/src/test/java/com/ego/mobile/domain/WorkoutDomainTest.kt`
- **Critérios de aceite:**
  1. Transição de estado inválida (ex: `COMPLETED` → `PAUSED`) é rejeitada.
  2. Validação de exercícios exige nome não vazio e séries > 0.
- **Testes necessários (TDD):**
  - **RED:** Testes em `WorkoutDomainTest` validando transições de estado de sessão e validações de modelo.
  - **GREEN:** Implementação dos modelos.
- **Definição de pronto:** `WorkoutDomainTest` passando via `./gradlew testDebugUnitTest`.

---

### `TASK-02`: Implementar entidades Room, DAOs (`WorkoutSessionDao`, `WorkoutExerciseDao`) e registrar no `EgoDatabase`
- **Status:** `PENDING`
- **Objetivo:** Criar tabelas Room `workout_sessions` e `workout_exercises` com relações 1:N e onDelete CASCADE.
- **Referência à especificação:** Critérios `AC-01`, `AC-02`, `AC-06`.
- **Pré-requisitos e dependências:** `TASK-01`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/data/local/entity/WorkoutSessionEntity.kt`
  - `app/src/main/java/com/ego/mobile/data/local/entity/WorkoutExerciseEntity.kt`
  - `app/src/main/java/com/ego/mobile/data/local/dao/WorkoutDao.kt`
  - `app/src/main/java/com/ego/mobile/data/local/EgoDatabase.kt`
  - `app/src/test/java/com/ego/mobile/data/local/dao/WorkoutDaoTest.kt`
- **Critérios de aceite:**
  1. `WorkoutDao` expõe método para obter a sessão ativa: `getActiveSession(): Flow<WorkoutSessionWithExercises?>`.
  2. Operações de CRUD de exercícios e finalização de sessão.
- **Testes necessários (TDD):**
  - **RED:** `WorkoutDaoTest` em memória criando sessão, exercícios, pausando e concluindo.
  - **GREEN:** Implementar entidades, mappers e DAO.
- **Definição de pronto:** `WorkoutDaoTest` passando.

---

### `TASK-03`: Implementar `WorkoutRepository` com suporte a detecção de sessão ativa e idempotência
- **Status:** `PENDING`
- **Objetivo:** Criar camada de repositório que impeça a criação de sessões concorrentes e permita recuperar imediatamente a sessão em andamento.
- **Referência à especificação:** Critérios `AC-03`, `AC-04`.
- **Pré-requisitos e dependências:** `TASK-02`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/data/repository/WorkoutRepository.kt`
  - `app/src/main/java/com/ego/mobile/data/repository/WorkoutRepositoryImpl.kt`
  - `app/src/test/java/com/ego/mobile/data/repository/WorkoutRepositoryTest.kt`
- **Critérios de aceite:**
  1. `startSession(title)` retorna a sessão existente se já houver uma ativa em vez de criar uma duplicada.
  2. Métodos para pausar, retomar, adicionar exercício, remover exercício e finalizar sessão.
- **Testes necessários (TDD):**
  - **RED:** Teste garantindo que chamar `startSession` duas vezes consecutivas não cria duas sessões abertas.
  - **GREEN:** Implementar lógica no repositório.
- **Definição de pronto:** `WorkoutRepositoryTest` passando.

---

### `TASK-04`: Implementar `WorkoutViewModel` e gerenciar `WorkoutUiState`
- **Status:** `PENDING`
- **Objetivo:** Construir o ViewModel de treinos responsável por emitir se há sessão ativa, gerenciar cronômetro decorrido e despachar ações de usuário.
- **Referência à especificação:** Critérios `AC-01` a `AC-06`.
- **Pré-requisitos e dependências:** `TASK-03`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/routines/workouts/WorkoutViewModel.kt`
  - `app/src/main/java/com/ego/mobile/feature/routines/workouts/WorkoutUiState.kt`
  - `app/src/test/java/com/ego/mobile/feature/routines/workouts/WorkoutViewModelTest.kt`
- **Critérios de aceite:**
  1. Emite estado `WorkoutUiState.Idle`, `WorkoutUiState.ActiveSession`, ou `WorkoutUiState.Historical`.
  2. Manipula ações da UI sem travar a thread principal.
- **Testes necessários (TDD):**
  - **RED:** `WorkoutViewModelTest` testando fluxo completo de iniciar, pausar, adicionar exercício e finalizar.
  - **GREEN:** Implementar `WorkoutViewModel`.
- **Definição de pronto:** `WorkoutViewModelTest` passando no Gradle.

---

### `TASK-05`: Implementar telas de treino: painel de entrada, tela de execução ativa e diálogo de exercícios
- **Status:** `PENDING`
- **Objetivo:** Criar os Composables da funcionalidade de treinos integrando com o ViewModel.
- **Referência à especificação:** Critérios `AC-01`, `AC-02`, `AC-03`, `AC-04`, `AC-05`.
- **Pré-requisitos e dependências:** `TASK-04`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/routines/workouts/WorkoutScreen.kt`
  - `app/src/main/java/com/ego/mobile/feature/routines/workouts/components/ActiveWorkoutView.kt`
  - `app/src/main/java/com/ego/mobile/feature/routines/workouts/components/AddExerciseDialog.kt`
- **Critérios de aceite:**
  1. Exibe banner de continuação se houver treino em andamento.
  2. Na tela ativa, exibe lista dinâmica de exercícios com botão de exclusão/edição.
  3. Diálogo de exercício com campos claros e validação.
- **Definição de pronto:** Compilação do módulo de UI executada com sucesso.

---

### `TASK-06`: Implementar histórico de treinos e finalização de sessão
- **Status:** `PENDING`
- **Objetivo:** Apresentar a lista de treinos concluídos com data, duração e resumo de exercícios.
- **Referência à especificação:** Critério `AC-06`.
- **Pré-requisitos e dependências:** `TASK-05`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/routines/workouts/components/WorkoutHistoryView.kt`
- **Critérios de aceite:**
  1. Treinos finalizados aparecem listados por ordem cronológica decrescente.
  2. Cada item mostra título, data, duração formatada e total de exercícios.
- **Definição de pronto:** Visualização de histórico integrada à tela de treinos.

---

### `TASK-07`: Verificação de ponta a ponta e testes de critérios de aceite
- **Status:** `PENDING`
- **Objetivo:** Executar suíte completa de testes e validar todos os critérios `AC-01` a `AC-07`.
- **Referência à especificação:** Critérios `AC-01` a `AC-07`.
- **Pré-requisitos e dependências:** `TASK-06`.
- **Componentes envolvidos:** Toda a suíte de testes.
- **Definição de pronto:** `./gradlew testDebugUnitTest` executado com 100% de sucesso.
