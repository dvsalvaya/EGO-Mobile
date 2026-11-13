# Plano Técnico — Fase 5: Treinos

- **Feature ID:** `fase-5-treinos`
- **Especificação:** `docs/specs/fase-5-treinos/spec.md` (Status: `APPROVED`)
- **Decomposição:** `docs/specs/fase-5-treinos/tasks.md`
- **Data de revisão:** 2026-10-28
- **Decisão:** `APROVADO`

---

## 1. Referências revisadas

- `docs/specs/fase-5-treinos/spec.md` (Status: `APPROVED`)
- `docs/specs/fase-5-treinos/tasks.md`
- `docs/01-PRD.md` (Seção 6.3: Treinos com persistência de sessão e sem perda de dados)
- `docs/02-USER-FLOWS.md` (Seção 5: Fluxo de treinos, retomada e prevenção de duplicatas)
- `docs/03-SDD.md` (Seção 5, 8 e 10: Persistência de WorkoutSession/Exercise no SQLite)
- `docs/04-ROADMAP.md` (Fase 5: Treinos)

---

## 2. Problemas concretos encontrados e correções

- **Problema 1: Duplicação acidental de sessão ao alternar abas da barra inferior**
  - *Análise:* A navegação para outras abas (ex: Início ou Hábitos) não pode perder a sessão em andamento e, ao retornar à aba Rotinas, o app não deve exibir um formulário para iniciar outro treino sem avisar que já há um ativo.
  - *Correção aplicada:* Em `TASK-03`, `WorkoutRepository` mantém a consulta `getActiveSession()` e o método `startSession` verifica previamente a existência de sessão `IN_PROGRESS` ou `PAUSED`, retornando-a idempotentemente.
- **Problema 2: Sobrevivência do timer decorrido a fechamento do app**
  - *Análise:* Se o tempo decorrido depender apenas de um cronômetro em memória, ele zera se o processo for destruído.
  - *Correção aplicada:* O cálculo do tempo de treino é derivado da diferença entre o timestamp atual e o campo persistido `startedAt`, ajustado por períodos pausados se houver.

---

## 3. Cobertura dos critérios, dependências, testabilidade e riscos

| Critério da Spec | Tarefa responsável | Verificação planejada |
|---|---|---|
| `AC-01` (Iniciar sessão e persistir) | `TASK-01`, `TASK-02`, `TASK-03` | Teste unitário de criação no `WorkoutDaoTest` e `WorkoutRepositoryTest` |
| `AC-02` (Adição manual de exercícios) | `TASK-01`, `TASK-02`, `TASK-03` | Teste de relacionamento 1:N com exercícios gravados no SQLite |
| `AC-03` (Recuperação de sessão após destruição) | `TASK-03`, `TASK-04` | Teste comprovando que instâncias novas do repositório detectam a sessão em disco |
| `AC-04` (Prevenção de duplicatas) | `TASK-03`, `TASK-05` | Teste de idempotência em `WorkoutRepositoryTest` |
| `AC-05` (Pausar e retomar) | `TASK-01`, `TASK-03` | Teste de transição de status no domínio e persistência |
| `AC-06` (Finalização e histórico) | `TASK-03`, `TASK-04`, `TASK-06` | Teste de gravação de `endedAt` e listagem de treinos concluídos |
| `AC-07` (Suíte completa de testes) | `TASK-07` | `./gradlew testDebugUnitTest` sem falhas |

---

## 4. Testes e comandos de validação

- Compilação e testes: `./gradlew testDebugUnitTest`
