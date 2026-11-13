# Decomposição de Tarefas — Fase 6: Cuidados capilares

- **Feature ID:** `fase-6-cuidados-capilares`
- **Especificação:** `docs/specs/fase-6-cuidados-capilares/spec.md` (Status: `VERIFIED`)
- **Data de criação:** 2026-10-28

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Implementar modelos de domínio (`HaircareRoutine`, `HaircareOccurrence`, `HaircareStepType`) e predefinições | `VERIFIED` | Nenhuma |
| `TASK-02` | Implementar entidades Room, `HaircareDao` e registrar no `EgoDatabase` | `VERIFIED` | `TASK-01` |
| `TASK-03` | Implementar `HaircareRepository` e casos de uso de execução e reagendamento | `VERIFIED` | `TASK-02` |
| `TASK-04` | Implementar `HaircareViewModel` e gerenciar `HaircareUiState` | `VERIFIED` | `TASK-03` |
| `TASK-05` | Implementar interface Composable de cuidados capilares (etapa atual, reagendamento e seleção de predefinição) | `VERIFIED` | `TASK-04` |
| `TASK-06` | Implementar histórico de cuidados capilares executados | `VERIFIED` | `TASK-05` |
| `TASK-07` | Verificação de ponta a ponta e testes de critérios de aceite | `VERIFIED` | `TASK-06` |

---

## Detalhamento das Tarefas

### `TASK-01`: Implementar modelos de domínio (`HaircareRoutine`, `HaircareOccurrence`, `HaircareStepType`) e predefinições
- **Status:** `VERIFIED`
- **Evidências:** Ciclo TDD estrito executado. 11 testes em `HaircareDomainTest` criados (RED com 9 falhas funcionais esperadas em validações, cálculos de data e preset → GREEN com implementação dos modelos puros e `HaircarePreset.standardSteps()`). 75 testes unitários executados com 100% de sucesso via Gradle.
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
- **Status:** `VERIFIED`
- **Objetivo:** Criar tabelas Room `haircare_routines` e `haircare_occurrences` com relacionamento 1:N.
- **Referência à especificação:** Critérios `AC-01`, `AC-03`, `AC-04`.
- **Pré-requisitos e dependências:** `TASK-01`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/data/local/entity/HaircareRoutineEntity.kt`
  - `app/src/main/java/com/ego/mobile/data/local/entity/HaircareOccurrenceEntity.kt`
  - `app/src/main/java/com/ego/mobile/data/local/dao/HaircareDao.kt`
  - `app/src/main/java/com/ego/mobile/data/local/EgoDatabase.kt`
  - `app/src/androidTest/java/com/ego/mobile/data/local/dao/HaircareDaoTest.kt`
- **Critérios de aceite:**
  1. `HaircareDao` permite buscar rotina ativa, próxima ocorrência pendente e lista de concluídas.
  2. Inserção em lote para criação inicial de etapas da rotina.
- **Evidência:** `HaircareDaoTest` passou no emulador para consulta da rotina/próxima pendência/histórico, inserção em lote e exclusão em cascata; `TaskDaoTest` passou após migração cumulativa até a versão 5.
- **Testes necessários (TDD):**
  - **RED:** `HaircareDaoTest` em memória testando inserção de rotina, busca de próxima pendente e atualização para executada.
  - **GREEN:** Implementar entidades, mappers e DAO.
- **Definição de pronto:** `HaircareDaoTest` passando com sucesso.

---

### `TASK-03`: Implementar `HaircareRepository` e casos de uso de execução e reagendamento
- **Status:** `VERIFIED`
- **Objetivo:** Criar repositório `HaircareRepository` isolando persistência e implementando as regras de avanço de etapa e reagendamento de datas.
- **Referência à especificação:** Critérios `AC-01`, `AC-03`, `AC-04`.
- **Pré-requisitos e dependências:** `TASK-02`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/repository/HaircareRepository.kt`
  - `app/src/main/java/com/ego/mobile/data/repository/HaircareRepositoryImpl.kt`
  - `app/src/main/java/com/ego/mobile/data/local/mapper/HaircareMapper.kt`
  - `app/src/main/java/com/ego/mobile/domain/usecase/RecordHaircareExecutionUseCase.kt`
  - `app/src/main/java/com/ego/mobile/domain/usecase/RescheduleHaircareStepUseCase.kt`
  - `app/src/test/java/com/ego/mobile/data/repository/HaircareRepositoryTest.kt`
- **Critérios de aceite:**
  1. `recordExecution(occurrenceId, timestamp, notes)` marca como concluída e preenche `executedAt`.
  2. `rescheduleStep(occurrenceId, newDateCivil)` atualiza `scheduledDate` preservando outros dados.
- **Evidência:** `HaircareRepositoryTest` passou verificando persistência das etapas, execução com timestamp real, preservação de notas e validações; `.\gradlew.bat testDebugUnitTest` passou com toda a suíte.
- **Testes necessários (TDD):**
  - **RED:** Testes cobrindo execução e reagendamento.
  - **GREEN:** Implementar repositório.
- **Definição de pronto:** `HaircareRepositoryTest` passando via Gradle.

---

### `TASK-04`: Implementar `HaircareViewModel` e gerenciar `HaircareUiState`
- **Status:** `VERIFIED`
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
- **Evidência:** `HaircareViewModelTest` passou verificando estado vazio, adoção de preset, criação personalizada, execução real e reagendamento.
- **Testes necessários (TDD):**
  - **RED:** `HaircareViewModelTest` simulando adoção de cronograma e execução da 1ª etapa.
  - **GREEN:** Implementar ViewModel.
- **Definição de pronto:** `HaircareViewModelTest` passando.

---

### `TASK-05`: Implementar interface Composable de cuidados capilares (etapa atual, reagendamento e seleção de predefinição)
- **Status:** `VERIFIED`
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
- **Evidência:** `RoutinesScreenTest` passou no emulador, validando a aba de cuidados capilares e as opções iniciais de predefinição e rotina personalizada; o fluxo integra os componentes de execução e reagendamento.
- **Testes necessários (TDD):**
  - **RED:** `RoutinesScreenTest` falhou porque a aba ainda não existia.
  - **GREEN:** `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.ego.mobile.feature.routines.RoutinesScreenTest` passou.
- **Definição de pronto:** A tela Compose compila, renderiza e oferece os fluxos iniciais de configuração.

---

### `TASK-06`: Implementar histórico de cuidados capilares executados
- **Status:** `VERIFIED`
- **Objetivo:** Exibir a lista de todas as etapas capilares concluídas com data real de execução e notas.
- **Referência à especificação:** Critério `AC-05`.
- **Pré-requisitos e dependências:** `TASK-05`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/routines/haircare/components/HaircareHistoryList.kt`
- **Critérios de aceite:**
  1. Lista ordenada cronologicamente das etapas executadas.
- **Evidência:** `HaircareHistoryListTest` passou no emulador, verificando etapas concluídas, timestamp em `America/Sao_Paulo`, notas e ordem da mais recente para a mais antiga.
- **Testes necessários (TDD):**
  - **RED:** `HaircareHistoryListTest` falhou funcionalmente porque o cabeçalho do histórico não era exibido.
  - **GREEN:** `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.ego.mobile.feature.routines.haircare.HaircareHistoryListTest` passou.
- **Definição de pronto:** Histórico visível e acessível na tela, com data/hora, tipo e notas.

---

### `TASK-07`: Verificação de ponta a ponta e testes de critérios de aceite
- **Status:** `VERIFIED`
- **Objetivo:** Executar suíte completa de testes e validar todos os critérios `AC-01` a `AC-06`.
- **Referência à especificação:** Critérios `AC-01` a `AC-06`.
- **Pré-requisitos e dependências:** `TASK-06`.
- **Evidência:** `.\gradlew.bat testDebugUnitTest connectedDebugAndroidTest` passou; todos os testes unitários e os 7 testes instrumentados terminaram com sucesso no emulador.
- **Definição de pronto:** Suítes unitária e instrumentada executadas com sucesso; critérios `AC-01` a `AC-06` cobertos pelas verificações de domínio, Room, repositório, ViewModel e UI.
