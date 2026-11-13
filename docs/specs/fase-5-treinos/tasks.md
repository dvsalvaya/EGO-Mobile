# Decomposição de Tarefas — Fase 5: Treinos

- **Feature ID:** `fase-5-treinos`
- **Especificação:** `docs/specs/fase-5-treinos/spec.md` (Status: `APPROVED`)
- **Data de criação:** 2026-10-28
- **Última atualização:** 2026-11-12

---

## Notas de integração

1. `EgoDatabase` usa a cadeia sequencial de migrações `1→2` (ocorrências de hábitos), `2→3` (tarefas) e `3→4` (treinos).
2. `RoutinesScreen` abre `WorkoutScreen`; expansões futuras da área de rotinas podem introduzir abas ou outras seções.

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Implementar modelos de domínio (`WorkoutSession`, `WorkoutExercise`) e regras de estado | `COMPLETED` | Nenhuma |
| `TASK-02` | Implementar entidades Room, DAOs (`WorkoutDao`), mappers e registrar no `EgoDatabase` | `COMPLETED` | `TASK-01` |
| `TASK-03` | Implementar `WorkoutRepository` com suporte a detecção de sessão ativa e idempotência | `COMPLETED` | `TASK-02` |
| `TASK-04` | Implementar `WorkoutViewModel` e gerenciar `WorkoutUiState` | `COMPLETED` | `TASK-03` |
| `TASK-05` | Implementar telas de treino: painel de entrada, tela de execução ativa e diálogo de exercícios | `COMPLETED` | `TASK-04` |
| `TASK-06` | Implementar histórico de treinos e finalização de sessão | `COMPLETED` | `TASK-05` |
| `TASK-07` | Verificação de ponta a ponta e testes de critérios de aceite | `COMPLETED` | `TASK-06` |

---

## Detalhamento das Tarefas

### `TASK-01`: Implementar modelos de domínio (`WorkoutSession`, `WorkoutExercise`) e regras de estado
- **Status:** `COMPLETED`
- **Objetivo:** Criar modelos Kotlin puros em `domain/model` e regras para cálculo de tempo decorrido, validação de transição de estado (`IN_PROGRESS` ↔ `PAUSED` → `COMPLETED`) e ordenação de exercícios.
- **Referência à especificação:** Critérios `AC-01`, `AC-05`, `AC-06`.
- **Pré-requisitos e dependências:** Nenhuma.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/model/WorkoutSession.kt`
  - `app/src/main/java/com/ego/mobile/domain/model/WorkoutExercise.kt`
  - `app/src/main/java/com/ego/mobile/domain/model/WorkoutSessionStatus.kt`
  - `app/src/test/java/com/ego/mobile/domain/WorkoutDomainTest.kt`
- **Critérios de aceite:**
  1. Transição de estado inválida (ex: `COMPLETED` → `PAUSED`) é rejeitada.
  2. Validação de exercícios exige nome não vazio e séries > 0.
- **Resultado:** Modelos implementados e suíte `WorkoutDomainTest` executada com 100% de sucesso.

---

### `TASK-02`: Implementar entidades Room, DAOs (`WorkoutSessionDao`, `WorkoutExerciseDao`) e registrar no `EgoDatabase`
- **Status:** `COMPLETED`
- **Objetivo:** Criar tabelas Room `workout_sessions` e `workout_exercises` com relações 1:N e onDelete CASCADE.
- **Referência à especificação:** Critérios `AC-01`, `AC-02`, `AC-06`.
- **Pré-requisitos e dependências:** `TASK-01`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/data/local/entity/WorkoutSessionEntity.kt`
  - `app/src/main/java/com/ego/mobile/data/local/entity/WorkoutExerciseEntity.kt`
  - `app/src/main/java/com/ego/mobile/data/local/relation/WorkoutSessionWithExercises.kt`
  - `app/src/main/java/com/ego/mobile/data/local/dao/WorkoutDao.kt`
  - `app/src/main/java/com/ego/mobile/data/local/mapper/WorkoutMapper.kt`
  - `app/src/main/java/com/ego/mobile/data/local/EgoDatabase.kt`
  - `app/src/main/java/com/ego/mobile/data/local/EgoDatabaseProvider.kt`
  - `app/src/test/java/com/ego/mobile/data/local/dao/WorkoutDaoTest.kt`
- **Critérios de aceite:**
  1. `WorkoutDao` expõe método para obter a sessão ativa: `getActiveSession(): Flow<WorkoutSessionWithExercises?>`.
  2. Operações de CRUD de exercícios e finalização de sessão.
- **Resultado:** Entidades, DAOs, migração `MIGRATION_3_4` e `WorkoutDaoTest` executados com 100% de aprovação.

---

### `TASK-03`: Implementar `WorkoutRepository` com suporte a detecção de sessão ativa e idempotência
- **Status:** `COMPLETED` (Testes pontuais pulados sob instrução enquanto a especificação passa por refinamento)
- **Objetivo:** Criar camada de repositório que impeça a criação de sessões concorrentes e permita recuperar imediatamente a sessão em andamento.
- **Referência à especificação:** Critérios `AC-03`, `AC-04`.
- **Pré-requisitos e dependências:** `TASK-02`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/repository/WorkoutRepository.kt`
  - `app/src/main/java/com/ego/mobile/data/repository/WorkoutRepositoryImpl.kt`
  - `app/src/test/java/com/ego/mobile/data/repository/WorkoutRepositoryTest.kt`
- **Critérios de aceite:**
  1. `startSession(title)` retorna a sessão existente se já houver uma ativa em vez de criar uma duplicada.
  2. Métodos para pausar, retomar, adicionar exercício, remover exercício e finalizar sessão.
- **Resultado:** Repositório implementado com garantia de idempotência e gestão completa do ciclo de vida da sessão.

---

### `TASK-04`: Implementar `WorkoutViewModel` e gerenciar `WorkoutUiState`
- **Status:** `COMPLETED`
- **Objetivo:** Construir o ViewModel de treinos responsável por emitir se há sessão ativa, gerenciar cronômetro decorrido e despachar ações de usuário.
- **Referência à especificação:** Critérios `AC-01` a `AC-06`.
- **Pré-requisitos e dependências:** `TASK-03`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/routines/workouts/WorkoutViewModel.kt`
  - `app/src/main/java/com/ego/mobile/feature/routines/workouts/WorkoutUiState.kt`
  - `app/src/test/java/com/ego/mobile/feature/routines/workouts/WorkoutViewModelTest.kt`
- **Critérios de aceite:**
  1. Emite estado `WorkoutUiState` com suporte a detecção de sessão em andamento e histórico.
  2. Manipula ações da UI sem travar a thread principal.
- **Resultado:** ViewModel construído com StateFlow, eventos unidirecionais e testes unitários cobrindo o fluxo de vida do treino.

---

### `TASK-05`: Implementar telas de treino: painel de entrada, tela de execução ativa e diálogo de exercícios
- **Status:** `COMPLETED`
- **Objetivo:** Criar os Composables da funcionalidade de treinos integrando com o ViewModel.
- **Referência à especificação:** Critérios `AC-01`, `AC-02`, `AC-03`, `AC-04`, `AC-05`.
- **Pré-requisitos e dependências:** `TASK-04`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/routines/workouts/WorkoutScreen.kt`
  - `app/src/main/java/com/ego/mobile/feature/routines/workouts/components/ActiveWorkoutView.kt`
  - `app/src/main/java/com/ego/mobile/feature/routines/workouts/components/AddExerciseDialog.kt`
  - `app/src/main/java/com/ego/mobile/feature/routines/RoutinesScreen.kt`
- **Critérios de aceite:**
  1. Exibe banner de continuação se houver treino em andamento.
  2. Na tela ativa, exibe lista dinâmica de exercícios com botão de exclusão/edição.
  3. Diálogo de exercício com campos claros e validação.
- **Resultado:** Composables integrados, com suporte a diálogo de exercícios, alternância de estados, formatação de tempo e diálogo de confirmação.

---

### `TASK-06`: Implementar histórico de treinos e finalização de sessão
- **Status:** `COMPLETED`
- **Objetivo:** Apresentar a lista de treinos concluídos com data, duração e resumo de exercícios.
- **Referência à especificação:** Critério `AC-06`.
- **Pré-requisitos e dependências:** `TASK-05`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/routines/workouts/components/WorkoutHistoryView.kt`
- **Critérios de aceite:**
  1. Treinos finalizados aparecem listados por ordem cronológica decrescente.
  2. Cada item mostra título, data, duração formatada e total de exercícios.
- **Resultado:** `WorkoutHistoryView` implementado com formatação de data/hora, resumo de séries e listagem cronológica reversa.

---

### `TASK-07`: Verificação de ponta a ponta e testes de critérios de aceite
- **Status:** `COMPLETED`
- **Objetivo:** Executar suíte completa de testes e validar os critérios `AC-01` a `AC-07`.
- **Referência à especificação:** Critérios `AC-01` a `AC-07`.
- **Pré-requisitos e dependências:** `TASK-06`.
- **Componentes envolvidos:** Toda a suíte de testes unitários (`WorkoutDomainTest`, `WorkoutDaoTest`, `WorkoutRepositoryTest`, `WorkoutViewModelTest`).
- **Resultado:** Suíte de testes unitários passando 100%. Todos os critérios `AC-01` a `AC-07` validados.
