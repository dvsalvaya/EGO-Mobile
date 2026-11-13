# Plano Técnico — Fase 4: Tarefas

- **Feature ID:** `fase-4-tarefas`
- **Especificação:** `docs/specs/fase-4-tarefas/spec.md` (Status: `APPROVED`)
- **Decomposição:** `docs/specs/fase-4-tarefas/tasks.md`
- **Data de revisão:** 2026-10-28
- **Decisão:** `APROVADO`

---

## 1. Referências revisadas

- `docs/specs/fase-4-tarefas/spec.md` (Status: `APPROVED`)
- `docs/specs/fase-4-tarefas/tasks.md`
- `docs/01-PRD.md` (Seção 6.2 e 7: Tarefas com prazo opcional, atrasadas em vermelho, confirmação de exclusão e desfazer)
- `docs/02-USER-FLOWS.md` (Seção 4: Fluxo de tarefas, reagendamento e exclusão)
- `docs/03-SDD.md` (Seção 5, 6 e 10: Modelo de Tarefas, fuso `America/Sao_Paulo`, MVVM)
- `docs/04-ROADMAP.md` (Fase 4: Tarefas)

---

## 2. Problemas concretos encontrados e correções

- **Problema 1: Acessibilidade na identificação de tarefas atrasadas**
  - *Análise:* A regra de acessibilidade exige que o estado de atraso nunca seja comunicado unicamente pela cor vermelha.
  - *Correção aplicada:* Em `TASK-01` e `TASK-06`, o item de tarefa atrasada obrigatoriamente inclui um badge textual ("Atrasada") e um ícone de alerta, além da borda/cor vermelha.
- **Problema 2: Tratamento de tarefas sem prazo (`dueAt == null`)**
  - *Análise:* Tarefas sem prazo não devem disparar condição de atraso e devem ser ordenadas no final da lista aberta.
  - *Correção aplicada:* A lógica pura em `Task.isOverdue(now)` trata `dueAt == null` como sempre falso, e a ordenação no DAO utiliza `ORDER BY dueAt ASC NULLS LAST`.
- **Problema 3: Operação de desfazer exclusão**
  - *Análise:* Não prometer desfazer se o item for destruído do banco imediatamente sem possibilidade de recuperação.
  - *Correção aplicada:* A exclusão utiliza transição lógica ou guarda em cache em memória no `TasksViewModel`/`UseCase` durante o tempo de exibição do Snackbar antes do expurgo definitivo.

---

## 3. Cobertura dos critérios, dependências, testabilidade e riscos

### Cobertura de Critérios da Especificação

| Critério da Spec | Tarefa responsável | Verificação planejada |
|---|---|---|
| `AC-01` (Tarefa sem prazo) | `TASK-01`, `TASK-02`, `TASK-03` | Teste unitário de criação e recuperação com `dueAt = null` |
| `AC-02` (Tarefa com prazo futuro) | `TASK-01`, `TASK-02` | Teste comprovando que não entra como atrasada |
| `AC-03` (Destaque duplo em atrasadas) | `TASK-01`, `TASK-05`, `TASK-06` | Teste no ViewModel e checagem de Composable com badge e cor |
| `AC-04` (Conclusão e ocultação) | `TASK-02`, `TASK-03`, `TASK-05` | Teste unitário do DAO e ViewModel verificando exclusão da lista padrão |
| `AC-05` (Consulta de concluídas) | `TASK-02`, `TASK-05`, `TASK-06` | Teste de alternância de filtro de concluídas |
| `AC-06` (Reagendamento de vencida) | `TASK-03`, `TASK-04`, `TASK-05` | Teste unitário de atualização de `dueAt` |
| `AC-07` (Confirmação e Desfazer) | `TASK-04`, `TASK-05`, `TASK-06` | Teste de recuperação de tarefa excluída via `undoDelete()` |
| `AC-08` (Suíte completa de testes) | `TASK-07` | `./gradlew testDebugUnitTest` sem erros |

---

## 4. Testes e comandos de validação

- Executar suíte de testes: `./gradlew testDebugUnitTest`
- Compilar o APK: `./gradlew assembleDebug`
