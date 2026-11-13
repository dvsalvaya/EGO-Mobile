# Decomposição de Tarefas — Fase 6: Cuidados capilares

- **Feature ID:** `fase-6-cuidados-capilares`
- **Especificação:** `docs/specs/fase-6-cuidados-capilares/spec.md` (Status: `APPROVED`)
- **Data de criação:** 2026-10-28

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Implementar modelos de domínio (`HaircareRoutine`, `HaircareOccurrence`, `HaircareStepType`) e predefinições | `PENDING` | Nenhuma |
| `TASK-02` | Implementar entidades Room, `HaircareDao` e registrar no `EgoDatabase` | `PENDING` | `TASK-01` |
| `TASK-03` | Implementar `HaircareRepository` e casos de uso de execução e reagendamento | `PENDING` | `TASK-02` |
| `TASK-04` | Implementar `HaircareViewModel` e gerenciar `HaircareUiState` | `PENDING` | `TASK-03` |
| `TASK-05` | Implementar interface Composable de cuidados capilares (etapa atual, reagendamento e seleção de predefinição) | `PENDING` | `TASK-04` |
| `TASK-06` | Implementar histórico de cuidados capilares executados | `PENDING` | `TASK-05` |
| `TASK-07` | Verificação de ponta a ponta e testes de critérios de aceite | `PENDING` | `TASK-06` |

---

## Detalhamento das Tarefas

### `TASK-01`: Implementar modelos de domínio (`HaircareRoutine`, `HaircareOccurrence`, `HaircareStepType`) e predefinições
- **Status:** `PENDING`
- **Objetivo:** Criar modelos Kotlin puros em `domain/model` e a predefinição editável "Cronograma Capilar Básico" (composto por etapas intercaladas de Hidratação, Nutrição e Reconstrução).
- **Referência à especificação:** Critérios `AC-01`, `AC-02`.
- **Pré-requisitos e dependências:** Nenhuma.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/model/HaircareRoutine.kt`
  - `app/src/main/java/com/ego/mobile/domain/model/HaircareOccurrence.kt`
  - `app/src/main/java/com/ego/mobile/domain/model/HaircarePreset.kt`
  - `app/src/test/java/com/ego/mobile/domain/HaircareDomainTest.kt`
- **Critérios de aceite:**
  1. `HaircarePreset.standardSchedule()` retorna lista estruturada de etapas editáveis.
  2. Validações garantem nome da rotina válido e tipos de etapa suportados.
- **Testes necessários (TDD):**
  - **RED:** `HaircareDomainTest` verificando estrutura do preset e criação de rotina personalizada.
  - **GREEN:** Implementar modelos e presets puros.
- **Definição de pronto:** `HaircareDomainTest` passando via `./gradlew testDebugUnitTest`.

---

### `TASK-02`: Implementar entidades Room, `HaircareDao` e registrar no `EgoDatabase`
- **Status:** `PENDING`
- **Objetivo:** Criar tabelas Room `haircare_routines` e `haircare_occurrences` com relacionamento 1:N.
- **Referência à especificação:** Critérios `AC-01`, `AC-03`, `AC-04`.
- **Pré-requisitos e dependências:** `TASK-01`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/data/local/entity/HaircareRoutineEntity.kt`
  - `app/src/main/java/com/ego/mobile/data/local/entity/HaircareOccurrenceEntity.kt`
  - `app/src/main/java/com/ego/mobile/data/local/dao/HaircareDao.kt`
  - `app/src/main/java/com/ego/mobile/data/local/EgoDatabase.kt`
  - `app/src/test/java/com/ego/mobile/data/local/dao/HaircareDaoTest.kt`
- **Critérios de aceite:**
  1. `HaircareDao` permite buscar rotina ativa, próxima ocorrência pendente e lista de concluídas.
  2. Inserção em lote para criação inicial de etapas da rotina.
- **Testes necessários (TDD):**
  - **RED:** `HaircareDaoTest` em memória testando inserção de rotina, busca de próxima pendente e atualização para executada.
  - **GREEN:** Implementar entidades, mappers e DAO.
- **Definição de pronto:** `HaircareDaoTest` passando com sucesso.

---

### `TASK-03`: Implementar `HaircareRepository` e casos de uso de execução e reagendamento
- **Status:** `PENDING`
- **Objetivo:** Criar repositório `HaircareRepository` isolando persistência e implementando as regras de avanço de etapa e reagendamento de datas.
- **Referência à especificação:** Critérios `AC-01`, `AC-03`, `AC-04`.
- **Pré-requisitos e dependências:** `TASK-02`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/data/repository/HaircareRepository.kt`
  - `app/src/main/java/com/ego/mobile/data/repository/HaircareRepositoryImpl.kt`
  - `app/src/test/java/com/ego/mobile/data/repository/HaircareRepositoryTest.kt`
- **Critérios de aceite:**
  1. `recordExecution(occurrenceId, timestamp, notes)` marca como concluída e preenche `executedAt`.
  2. `rescheduleStep(occurrenceId, newDateCivil)` atualiza `scheduledDate` preservando outros dados.
- **Testes necessários (TDD):**
  - **RED:** Testes cobrindo execução e reagendamento.
  - **GREEN:** Implementar repositório.
- **Definição de pronto:** `HaircareRepositoryTest` passando via Gradle.

---

### `TASK-04`: Implementar `HaircareViewModel` e gerenciar `HaircareUiState`
- **Status:** `PENDING`
- **Objetivo:** Criar ViewModel gerenciando o ciclo de vida da rotina capilar, emissão da etapa corrente e histórico.
- **Referência à especificação:** Critérios `AC-01` a `AC-05`.
- **Pré-requisitos e dependências:** `TASK-03`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/routines/haircare/HaircareViewModel.kt`
  - `app/src/main/java/com/ego/mobile/feature/routines/haircare/HaircareUiState.kt`
  - `app/src/test/java/com/ego/mobile/feature/routines/haircare/HaircareViewModelTest.kt`
- **Critérios de aceite:**
  1. Emite estados: `NoRoutineConfigured`, `ActiveRoutine(currentStep, upcomingSteps)`, etc.
  2. Métodos para adotar preset, criar personalizada, registrar execução e reagendar.
- **Testes necessários (TDD):**
  - **RED:** `HaircareViewModelTest` simulando adoção de cronograma e execução da 1ª etapa.
  - **GREEN:** Implementar ViewModel.
- **Definição de pronto:** `HaircareViewModelTest` passando.

---

### `TASK-05`: Implementar interface Composable de cuidados capilares (etapa atual, reagendamento e seleção de predefinição)
- **Status:** `PENDING`
- **Objetivo:** Criar a tela Compose para cuidados capilares, incluindo card de destaque da próxima etapa e diálogos de adoção de preset / reagendamento.
- **Referência à especificação:** Critérios `AC-01`, `AC-03`, `AC-04`.
- **Pré-requisitos e dependências:** `TASK-04`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/routines/haircare/HaircareScreen.kt`
  - `app/src/main/java/com/ego/mobile/feature/routines/haircare/components/CurrentStepCard.kt`
  - `app/src/main/java/com/ego/mobile/feature/routines/haircare/components/RescheduleStepDialog.kt`
- **Critérios de aceite:**
  1. Destaque visual por cor/ícone para Hidratação, Nutrição e Reconstrução.
  2. Botão de 1 toque para confirmar execução real.
  3. Opção de reagendar facilmente.
- **Definição de pronto:** Código compila e renderiza sem erros.

---

### `TASK-06`: Implementar histórico de cuidados capilares executados
- **Status:** `PENDING`
- **Objetivo:** Exibir a lista de todas as etapas capilares concluídas com data real de execução e notas.
- **Referência à especificação:** Critério `AC-05`.
- **Pré-requisitos e dependências:** `TASK-05`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/routines/haircare/components/HaircareHistoryList.kt`
- **Critérios de aceite:**
  1. Lista ordenada cronologicamente das etapas executadas.
- **Definição de pronto:** Histórico visível e acessível na tela.

---

### `TASK-07`: Verificação de ponta a ponta e testes de critérios de aceite
- **Status:** `PENDING`
- **Objetivo:** Executar suíte completa de testes e validar todos os critérios `AC-01` a `AC-06`.
- **Referência à especificação:** Critérios `AC-01` a `AC-06`.
- **Pré-requisitos e dependências:** `TASK-06`.
- **Definição de pronto:** `./gradlew testDebugUnitTest` executado com 100% de sucesso.
