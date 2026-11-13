# Plano Técnico — Fase 6: Cuidados capilares

- **Feature ID:** `fase-6-cuidados-capilares`
- **Especificação:** `docs/specs/fase-6-cuidados-capilares/spec.md` (Status: `APPROVED`)
- **Decomposição:** `docs/specs/fase-6-cuidados-capilares/tasks.md`
- **Data de revisão:** 2026-10-28
- **Decisão:** `APROVADO`

---

## 1. Referências revisadas

- `docs/specs/fase-6-cuidados-capilares/spec.md` (Status: `APPROVED`)
- `docs/specs/fase-6-cuidados-capilares/tasks.md`
- `docs/01-PRD.md` (Seção 6.4: Rotinas personalizadas ou predefinidas editáveis, execução real, reagendamento)
- `docs/02-USER-FLOWS.md` (Seção 6: Fluxo de cuidados capilares)
- `docs/03-SDD.md` (Seção 5: Modelo HaircareRoutine e HaircareOccurrence)
- `docs/04-ROADMAP.md` (Fase 6: Cuidados capilares)

---

## 2. Problemas concretos encontrados e correções

- **Problema 1: Imutabilidade vs Flexibilidade de predefinições**
  - *Análise:* O PRD determina que o usuário não pode ser forçado a uma rotina única e a predefinição deve ser apenas um ponto de partida editável.
  - *Correção aplicada:* A adoção de um preset cria registros normais na tabela `haircare_routines` com `source = 'PRESET'` e gera instâncias editáveis de `HaircareOccurrenceEntity`. O usuário tem permissão para alterar, reordenar ou excluir qualquer etapa posteriormente.
- **Problema 2: Distinção entre data prevista e data de execução real**
  - *Análise:* Se o usuário lavar o cabelo 2 dias após a data prevista, o sistema não deve reescrever a data original prevista como se tivesse ocorrido na data certa.
  - *Correção aplicada:* `scheduledDate` guarda a intenção original e `executedAt` (timestamp) armazena o momento real da realização.

---

## 3. Cobertura dos critérios, dependências, testabilidade e riscos

| Critério da Spec | Tarefa responsável | Verificação planejada |
|---|---|---|
| `AC-01` (Adoção de predefinição editável) | `TASK-01`, `TASK-02`, `TASK-03` | Teste unitário gerando rotina a partir de `HaircarePreset.standardSchedule()` |
| `AC-02` (Criação de rotina personalizada) | `TASK-01`, `TASK-02`, `TASK-03` | Teste de persistência de rotina customizada no DAO |
| `AC-03` (Registro de execução real) | `TASK-02`, `TASK-03`, `TASK-04` | Teste gravando `executedAt` e verificando avanço de etapa |
| `AC-04` (Reagendamento sem perdas) | `TASK-03`, `TASK-04`, `TASK-05` | Teste de atualização de `scheduledDate` |
| `AC-05` (Histórico de cuidados) | `TASK-02`, `TASK-06` | Consulta ordenada por data de execução no Room |
| `AC-06` (Suíte completa de testes) | `TASK-07` | `./gradlew testDebugUnitTest` sem falhas |

---

## 4. Testes e comandos de validação

- Compilação e testes: `./gradlew testDebugUnitTest`
