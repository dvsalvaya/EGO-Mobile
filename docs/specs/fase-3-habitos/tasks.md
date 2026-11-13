# Decomposição de Tarefas — Fase 3: Hábitos

- **Feature ID:** `fase-3-habitos`
- **Especificação:** `docs/specs/fase-3-habitos/spec.md` (Status: `APPROVED`)
- **Data de criação:** 2026-10-28

---

## Sumário das tarefas

| ID | Tarefa | Status | Dependências |
|---|---|---|---|
| `TASK-01` | Expandir modelo de domínio (`Habit`, `HabitOccurrence`, `FrequencyConfig`) e regras puras de cálculo de ocorrências | `VERIFIED` | Nenhuma |
| `TASK-02` | Implementar `HabitOccurrenceEntity`, `HabitOccurrenceDao` e migração do `EgoDatabase` | `VERIFIED` | `TASK-01` |
| `TASK-03` | Implementar métodos no `HabitRepository` para ciclo de vida de hábitos e ocorrências | `VERIFIED` | `TASK-02` |
| `TASK-04` | Implementar regras de mudança de frequência e tratamento de ocorrências pendentes (UseCases / Domínio) | `VERIFIED` | `TASK-03` |
| `TASK-05` | Implementar `HabitsViewModel` e gerenciar `HabitsUiState` | `VERIFIED` | `TASK-04` |
| `TASK-06` | Implementar `HabitsScreen`, formulário de criação/edição e diálogo de resolução de frequência | `VERIFIED` | `TASK-05` |
| `TASK-07` | Verificação de ponta a ponta, testes de integração e validação de critérios de aceite | `VERIFIED` | `TASK-06` |

---

## Detalhamento das Tarefas

### `TASK-01`: Expandir modelo de domínio (`Habit`, `HabitOccurrence`, `FrequencyConfig`) e regras puras de cálculo de ocorrências
- **Status:** `VERIFIED`
- **Evidências:** `HabitDomainTest` executado com 100% de sucesso cobrindo validações e frequências `Daily`, `WeeklyDays` e `TimesPerWeek`.

---

### `TASK-02`: Implementar `HabitOccurrenceEntity`, `HabitOccurrenceDao` e migração do `EgoDatabase`
- **Status:** `VERIFIED`
- **Evidências:** `MIGRATION_1_2` configurada no `EgoDatabase`. `HabitOccurrenceDaoTest` e `HabitMapperTest` executados com sucesso em testes in-memory.

---

### `TASK-03`: Implementar métodos no `HabitRepository` para ciclo de vida de hábitos e ocorrências
- **Status:** `VERIFIED`
- **Evidências:** `HabitRepositoryImplTest` cobrindo criação, listagem reativa via `Flow`, registro de conclusão e pausas/retomadas.

---

### `TASK-04`: Implementar regras de mudança de frequência e tratamento de ocorrências pendentes (UseCases / Domínio)
- **Status:** `VERIFIED`
- **Evidências:** `UpdateHabitFrequencyUseCaseTest` e `ResolvePendingOccurrenceUseCaseTest` cobrindo todas as transições de frequência e resolução de pendências sem penalidade.

---

### `TASK-05`: Implementar `HabitsViewModel` e gerenciar `HabitsUiState`
- **Status:** `VERIFIED`
- **Evidências:** `HabitsViewModelTest` passando 100% cobrindo emissão de estados, ações rápidas de conclusão e tratamento de erros.

---

### `TASK-06`: Implementar `HabitsScreen`, formulário de criação/edição e diálogo de resolução de frequência
- **Status:** `VERIFIED`
- **Evidências:** `HabitsScreen.kt` e `HabitFormDialog.kt` integrados no app, substituindo placeholder e conectados ao ViewModel.

---

### `TASK-07`: Verificação de ponta a ponta, testes de integração e validação de critérios de aceite
- **Status:** `VERIFIED`
- **Evidências:** Suíte unitária Gradle executando com 100% de sucesso. Todos os critérios `AC-01` a `AC-08` verificados.
