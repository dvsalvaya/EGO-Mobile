# Plano Técnico — Fase 3: Hábitos

- **Feature ID:** `fase-3-habitos`
- **Especificação:** `docs/specs/fase-3-habitos/spec.md` (Status: `APPROVED`)
- **Decomposição:** `docs/specs/fase-3-habitos/tasks.md`
- **Data de revisão:** 2026-10-28
- **Decisão:** `APROVADO`

---

## 1. Referências revisadas

- `docs/specs/fase-3-habitos/spec.md` (Status: `APPROVED`)
- `docs/specs/fase-3-habitos/tasks.md`
- `docs/01-PRD.md` (Seção 6.1 e 7: Hábitos, flexibilidade de frequência, preservação de histórico, offline-first)
- `docs/02-USER-FLOWS.md` (Seção 3: Fluxo de hábitos, pausa/retomada, registro de conclusão rápida e manual)
- `docs/03-SDD.md` (Seção 5, 6, 7 e 10: Estrutura relacional Habit/HabitOccurrence, fuso `America/Sao_Paulo`, MVVM pragmático)
- `docs/04-ROADMAP.md` (Fase 3: Hábitos)

---

## 2. Problemas concretos encontrados e correções

- **Problema 1: Isolamento de dados de ocorrência por data civil**
  - *Análise:* O fuso `America/Sao_Paulo` deve ser utilizado para determinar qual é o "dia civil de hoje". O uso de `System.currentTimeMillis()` bruto para agrupar datas pode gerar discrepâncias em viradas de horário de verão ou limites de meia-noite.
  - *Correção aplicada:* Utilizar `LocalDate` (via `java.time` desugared ou formatado como String ISO `YYYY-MM-DD` / epoch day) indexado na chave `(habitId, dateCivil)` para garantir unicidade e consultas por período determinísticas.
- **Problema 2: Preservação de dados ao alterar frequência**
  - *Análise:* Se a frequência mudar de "Diária" para "3x por semana", ocorrências passadas não podem ser excluídas nem recalculadas retroativamente.
  - *Correção aplicada:* A `TASK-04` isola a regra em um `UseCase` que recebe a data efetiva da mudança e aplica a alteração apenas a partir dessa data.

---

## 3. Cobertura dos critérios, dependências, testabilidade e riscos

### Cobertura de Critérios da Especificação

| Critério da Spec | Tarefa responsável | Verificação planejada |
|---|---|---|
| `AC-01` (Criação e persistência) | `TASK-01`, `TASK-02`, `TASK-03` | Teste de inserção e consulta via `HabitRepositoryTest` |
| `AC-02` (Validação de entrada) | `TASK-01`, `TASK-05` | Teste unitário de validação de nome em branco / tamanhos inválidos |
| `AC-03` (Conclusão de 1 toque) | `TASK-03`, `TASK-05`, `TASK-06` | Teste de atualização de status e timestamp no `HabitRepositoryTest` |
| `AC-04` (Hábitos pausados visualmente distintos) | `TASK-05`, `TASK-06` | Teste de filtragem no ViewModel e verificação Compose |
| `AC-05` (Retomada de hábito) | `TASK-03`, `TASK-05` | Teste de transição de estado `PAUSED` → `ACTIVE` |
| `AC-06` (Histórico intocado na mudança de frequência) | `TASK-04` | Teste do `UpdateHabitFrequencyUseCaseTest` comparando registros anteriores |
| `AC-07` (Estado vazio informativo) | `TASK-05`, `TASK-06` | Verificação do estado `HabitsUiState.Empty` |
| `AC-08` (Suíte de testes verdes) | `TASK-07` | `./gradlew testDebugUnitTest` sem falhas |

### Ordem de Execução e Dependências

A ordem de execução é sequencial e respeita as camadas arquiteturais:
`TASK-01` (Domínio) → `TASK-02` (Room & DAO) → `TASK-03` (Repositório) → `TASK-04` (Casos de uso e regras) → `TASK-05` (ViewModel) → `TASK-06` (UI Compose) → `TASK-07` (Verificação final).

### Testabilidade (TDD: RED → GREEN → REFACTOR)

- O projeto possui o runner de testes unitários JUnit 4 operacional.
- Cada tarefa especifica o teste RED antes da implementação da funcionalidade correspondente.

---

## 4. Testes e comandos de validação

- Compilação do módulo: `./gradlew assembleDebug`
- Execução dos testes unitários: `./gradlew testDebugUnitTest`
