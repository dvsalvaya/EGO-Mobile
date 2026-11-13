# Decomposição de Tarefas — Fase 3: Hábitos

- **Feature ID:** `fase-3-habitos`
- **Especificação:** `docs/specs/fase-3-habitos/spec.md` (Status: `APPROVED`)
- **Data de criação:** 2026-10-28

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Expandir modelo de domínio (`Habit`, `HabitOccurrence`, `FrequencyConfig`) e regras puras de cálculo de ocorrências | `PENDING` | Nenhuma |
| `TASK-02` | Implementar `HabitOccurrenceEntity`, `HabitOccurrenceDao` e migração do `EgoDatabase` | `PENDING` | `TASK-01` |
| `TASK-03` | Implementar métodos no `HabitRepository` para ciclo de vida de hábitos e ocorrências | `PENDING` | `TASK-02` |
| `TASK-04` | Implementar regras de mudança de frequência e tratamento de ocorrências pendentes (UseCases / Domínio) | `PENDING` | `TASK-03` |
| `TASK-05` | Implementar `HabitsViewModel` e gerenciar `HabitsUiState` | `PENDING` | `TASK-04` |
| `TASK-06` | Implementar `HabitsScreen`, formulário de criação/edição e diálogo de resolução de frequência | `PENDING` | `TASK-05` |
| `TASK-07` | Verificação de ponta a ponta, testes de integração e validação de critérios de aceite | `PENDING` | `TASK-06` |

---

## Detalhamento das Tarefas

### `TASK-01`: Expandir modelo de domínio (`Habit`, `HabitOccurrence`, `FrequencyConfig`) e regras puras de cálculo de ocorrências
- **Status:** `PENDING`
- **Objetivo:** Criar classes de domínio puras em Kotlin sem dependências de Android/Room para representar tipos de frequência (diária, dias específicos, vezes por semana), ocorrências e cálculo de status para uma data civil no fuso `America/Sao_Paulo`.
- **Referência à especificação:** Critérios `AC-01`, `AC-02`, Regras de negócio 1 e 2.
- **Pré-requisitos e dependências:** Nenhuma.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/model/Habit.kt`
  - `app/src/main/java/com/ego/mobile/domain/model/HabitOccurrence.kt`
  - `app/src/main/java/com/ego/mobile/domain/model/HabitFrequency.kt`
  - `app/src/test/java/com/ego/mobile/domain/HabitDomainTest.kt`
- **Critérios de aceite:**
  1. `HabitFrequency` suporta `Daily`, `WeeklyDays(setOf(DayOfWeek))`, e `TimesPerWeek(count: Int)`.
  2. Validações de nome (1 a 80 chars) e descrição (até 250 chars) retornam falha explicativa em dados inválidos.
  3. Cálculo indica se uma data civil requer ocorrência baseada na regra de frequência.
- **Testes necessários (TDD):**
  - **RED:** Escrever teste em `HabitDomainTest` testando criação de frequência, validação de nome e cálculo de ocorrência esperada. Teste falha com classes/métodos inexistentes.
  - **GREEN:** Implementar modelos e métodos puros para fazer o teste passar.
  - **REFACTOR:** Limpar código e garantir imutabilidade dos modelos.
- **Definição de pronto:** `./gradlew testDebugUnitTest` passa 100%.

---

### `TASK-02`: Implementar `HabitOccurrenceEntity`, `HabitOccurrenceDao` e migração do `EgoDatabase`
- **Status:** `PENDING`
- **Objetivo:** Criar a tabela `habit_occurrences` no Room vinculada à tabela `habits` com índice único `(habitId, dateCivil)` e implementar operações assíncronas de CRUD e consulta por data/período.
- **Referência à especificação:** Critérios `AC-01`, `AC-03`, Seção 9 (Persistência).
- **Pré-requisitos e dependências:** `TASK-01`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/data/local/entity/HabitOccurrenceEntity.kt`
  - `app/src/main/java/com/ego/mobile/data/local/dao/HabitOccurrenceDao.kt`
  - `app/src/main/java/com/ego/mobile/data/local/EgoDatabase.kt`
  - `app/src/main/java/com/ego/mobile/data/local/mapper/HabitOccurrenceMapper.kt`
  - `app/src/test/java/com/ego/mobile/data/local/dao/HabitOccurrenceDaoTest.kt`
- **Critérios de aceite:**
  1. `HabitOccurrenceEntity` com foreign key para `HabitEntity` e `onDelete = CASCADE`.
  2. `HabitOccurrenceDao` permite inserir, atualizar status (`COMPLETED`, `PENDING`, `SKIPPED`), e consultar ocorrências por intervalo de datas.
  3. `EgoDatabase` atualizado para conter a nova entidade.
- **Testes necessários (TDD):**
  - **RED:** Escrever teste `HabitOccurrenceDaoTest` usando `inMemoryDatabaseBuilder` tentando inserir ocorrência e consultar por data.
  - **GREEN:** Implementar entidade, DAO e mapper até o teste passar.
  - **REFACTOR:** Garantir retorno reativo via `Flow` onde aplicável.
- **Definição de pronto:** `HabitOccurrenceDaoTest` passa em `./gradlew testDebugUnitTest`.

---

### `TASK-03`: Implementar métodos no `HabitRepository` para ciclo de vida de hábitos e ocorrências
- **Status:** `PENDING`
- **Objetivo:** Expor através de `HabitRepository` (e `HabitRepositoryImpl`) operações reativas para listar hábitos ativos/pausados, buscar ocorrências do dia, marcar conclusão (1 toque ou manual) e pausar/retomar hábitos.
- **Referência à especificação:** Critérios `AC-01`, `AC-03`, `AC-04`, `AC-05`.
- **Pré-requisitos e dependências:** `TASK-02`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/data/repository/HabitRepository.kt`
  - `app/src/main/java/com/ego/mobile/data/repository/HabitRepositoryImpl.kt`
  - `app/src/test/java/com/ego/mobile/data/repository/HabitRepositoryTest.kt`
- **Critérios de aceite:**
  1. `getHabitsWithTodayStatus(dateCivil: String): Flow<List<HabitWithOccurrence>>` retorna lista combinada.
  2. `completeOccurrence(habitId: Long, dateCivil: String, notes: String?): Result<Unit>` marca como concluída com timestamp atual.
  3. `pauseHabit(habitId: Long)` e `resumeHabit(habitId: Long)` atualizam o estado do hábito atômica e confiavelmente.
- **Testes necessários (TDD):**
  - **RED:** Testes unitários com DAOs simulados ou em memória verificando as operações de repositório.
  - **GREEN:** Implementar a lógica de orquestração no repositório.
  - **REFACTOR:** Manter isolamento estrito sem vazar entidades Room para o domínio.
- **Definição de pronto:** `./gradlew testDebugUnitTest` passa com `HabitRepositoryTest`.

---

### `TASK-04`: Implementar regras de mudança de frequência e tratamento de ocorrências pendentes (UseCases / Domínio)
- **Status:** `PENDING`
- **Objetivo:** Implementar a lógica para aplicar nova frequência a partir da data atual sem alterar ocorrências passadas, e lógica para classificar ocorrências pendentes (justificar/pular ou concluir retroativamente).
- **Referência à especificação:** Critérios `AC-06`, Regra de negócio 4.
- **Pré-requisitos e dependências:** `TASK-03`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/domain/usecase/UpdateHabitFrequencyUseCase.kt`
  - `app/src/main/java/com/ego/mobile/domain/usecase/ResolvePendingOccurrenceUseCase.kt`
  - `app/src/test/java/com/ego/mobile/domain/usecase/UpdateHabitFrequencyUseCaseTest.kt`
  - `app/src/test/java/com/ego/mobile/domain/usecase/ResolvePendingOccurrenceUseCaseTest.kt`
- **Critérios de aceite:**
  1. Alteração de frequência só gera novas ocorrências futuras a partir de `effectiveDate`.
  2. Ocorrências passadas com status `COMPLETED` ou `SKIPPED` permanecem 100% inalteradas.
  3. Resolução de ocorrência pendente permite marcar como `SKIPPED` com nota explicativa sem diminuir consistência como se fosse falha cega.
- **Testes necessários (TDD):**
  - **RED:** Testar caso de uso com cenário de datas passadas vs futuras.
  - **GREEN:** Implementar UseCases puros.
  - **REFACTOR:** Assegurar tratamento de erros tipado.
- **Definição de pronto:** Testes unitários dos casos de uso passando no Gradle.

---

### `TASK-05`: Implementar `HabitsViewModel` e gerenciar `HabitsUiState`
- **Status:** `PENDING`
- **Objetivo:** Criar o ViewModel para a tela de hábitos consumindo o repositório e expondo `StateFlow<HabitsUiState>` com estados de carregamento, lista de ativos, lista de pausados, e eventos de erro ou confirmação.
- **Referência à especificação:** Critérios `AC-01`, `AC-03`, `AC-04`, `AC-07`.
- **Pré-requisitos e dependências:** `TASK-04`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/habits/HabitsViewModel.kt`
  - `app/src/main/java/com/ego/mobile/feature/habits/HabitsUiState.kt`
  - `app/src/test/java/com/ego/mobile/feature/habits/HabitsViewModelTest.kt`
- **Critérios de aceite:**
  1. Expõe `uiState` com lista separada de hábitos ativos e pausados.
  2. Fornece métodos: `onQuickComplete(habitId)`, `onPauseHabit(habitId)`, `onResumeHabit(habitId)`, `onCreateHabit(...)`, `onUpdateFrequency(...)`.
  3. Estados de carregamento e mensagens de validação devidamente expostos.
- **Testes necessários (TDD):**
  - **RED:** Teste com `StandardTestDispatcher` e repositório simulado testando ações e emissão de estados.
  - **GREEN:** Implementação do `HabitsViewModel`.
  - **REFACTOR:** Garantir cancelamento e escopos seguros de Coroutine.
- **Definição de pronto:** `HabitsViewModelTest` passando 100%.

---

### `TASK-06`: Implementar `HabitsScreen`, formulário de criação/edição e diálogo de resolução de frequência
- **Status:** `PENDING`
- **Objetivo:** Construir a interface Composable completa da tela de Hábitos, incluindo item de hábito com botão de 1 toque, seção visualmente diferenciada para hábitos pausados, diálogo/sheet de criação/edição com seleção de frequência, e diálogo de tratamento de ocorrência não cumprida.
- **Referência à especificação:** Critérios `AC-01`, `AC-02`, `AC-03`, `AC-04`, `AC-05`, `AC-06`, `AC-07`.
- **Pré-requisitos e dependências:** `TASK-05`.
- **Componentes envolvidos:**
  - `app/src/main/java/com/ego/mobile/feature/habits/HabitsScreen.kt`
  - `app/src/main/java/com/ego/mobile/feature/habits/components/HabitCard.kt`
  - `app/src/main/java/com/ego/mobile/feature/habits/components/HabitFormDialog.kt`
  - `app/src/main/java/com/ego/mobile/feature/habits/components/ChangeFrequencyDialog.kt`
  - `app/src/main/java/com/ego/mobile/feature/habits/components/ResolveOccurrenceDialog.kt`
- **Critérios de aceite:**
  1. `HabitsScreen` substitui o placeholder da Fase 1 e conecta ao `HabitsViewModel`.
  2. Botão de conclusão de 1 toque dispara ação imediata.
  3. Hábitos pausados são claramente identificados e separados.
  4. Tela vazia mostra estado explicativo com atalho para criação.
  5. Layout coerente com o tema escuro Material 3.
- **Testes necessários:**
  - Testes de sanidade e compilação dos composables.
- **Definição de pronto:** O app compila com `:app:assembleDebug` e exibe a tela sem erros.

---

### `TASK-07`: Verificação de ponta a ponta, testes de integração e validação de critérios de aceite
- **Status:** `PENDING`
- **Objetivo:** Executar suíte completa de testes unitários e de integração, validar todos os critérios de aceite (`AC-01` a `AC-08`), e registrar evidências.
- **Referência à especificação:** Critérios `AC-01` até `AC-08`.
- **Pré-requisitos e dependências:** `TASK-06`.
- **Componentes envolvidos:** Toda a suíte de testes do projeto.
- **Critérios de aceite:**
  1. Todos os critérios de aceite verificados e documentados com evidências concretas.
  2. `./gradlew testDebugUnitTest` executado com 100% de sucesso.
- **Definição de pronto:** Relatório de verificação preenchido e funcionalidade pronta para status `VERIFIED`.
