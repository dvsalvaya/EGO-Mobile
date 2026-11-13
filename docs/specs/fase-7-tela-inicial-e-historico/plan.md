# Plano Técnico — Fase 7: Tela inicial e histórico

- **Feature ID:** `fase-7-tela-inicial-e-historico`
- **Especificação:** `docs/specs/fase-7-tela-inicial-e-historico/spec.md` (Status: `APPROVED`)
- **Decomposição:** `docs/specs/fase-7-tela-inicial-e-historico/tasks.md`
- **Data de revisão:** 2026-10-28
- **Decisão:** `APROVADO`

---

## 1. Referências revisadas

- `docs/specs/fase-7-tela-inicial-e-historico/spec.md` (Status: `APPROVED`)
- `docs/specs/fase-7-tela-inicial-e-historico/tasks.md`
- `docs/01-PRD.md` (Seção 6.5: Tela inicial equilibrada e histórico separado)
- `docs/02-USER-FLOWS.md` (Seção 2: Início e Seção 7: Histórico)
- `docs/03-SDD.md` (Seção 7 e 8: Fluxo de dados e navegação)
- `docs/04-ROADMAP.md` (Fase 7: Tela inicial e histórico)

---

## 2. Problemas concretos encontrados e correções

- **Problema 1: Risco de inconsistência de estado entre Home e telas de funcionalidades**
  - *Análise:* Se a Home mantiver estado estático ou cache próprio desconectado do Room, ações feitas na Home podem divergir da tela de Tarefas ou Histórico.
  - *Correção aplicada:* A Home e o Histórico não possuem banco próprio; consomem os repositórios via `Flow` reativos. Qualquer mutação (ex: conclusão de hábito) atualiza o SQLite, refletindo imediatamente em todas as telas que observam esses dados.
- **Problema 2: Sobrecarga cognitiva na Home**
  - *Análise:* O PRD proíbe expressamente transformar a Home em um feed infinito de tudo o que já aconteceu.
  - *Correção aplicada:* A Home foca estritamente no presente (dia de hoje, pendências imediatas e atalhos de criação rápida). O Histórico completo permanece confinado ao seu destino próprio da barra inferior.

---

## 3. Cobertura dos critérios, dependências, testabilidade e riscos

| Critério da Spec | Tarefa responsável | Verificação planejada |
|---|---|---|
| `AC-01` (Atalhos de criação de hábito e tarefa) | `TASK-03`, `TASK-05` | Teste no ViewModel disparando eventos de criação |
| `AC-02` (Tarefas atrasadas na Home) | `TASK-02`, `TASK-03`, `TASK-05` | Teste de exibição de itens atrasados com badge |
| `AC-03` (Card de treino em andamento) | `TASK-02`, `TASK-03`, `TASK-05` | Teste reagindo à existência de sessão em `IN_PROGRESS` |
| `AC-04` (Área de histórico dedicada) | `TASK-02`, `TASK-04`, `TASK-06` | Teste da tela de Histórico com ordenação cronológica |
| `AC-05` (Consistência reativa) | `TASK-02`, `TASK-07` | Teste de integração validando mutação na Home refletida no Histórico |
| `AC-06` (Suíte completa de testes) | `TASK-07` | `./gradlew testDebugUnitTest` sem falhas |

---

## 4. Testes e comandos de validação

- Compilação e testes: `./gradlew testDebugUnitTest`
