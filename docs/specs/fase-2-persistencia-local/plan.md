# Plano Técnico — Fase 2: Persistência local

- **Feature ID:** `fase-2-persistencia-local`
- **Especificação:** `docs/specs/fase-2-persistencia-local/spec.md` (Status: `APPROVED`)
- **Decomposição:** `docs/specs/fase-2-persistencia-local/tasks.md`
- **Data de revisão:** 2026-10-28
- **Decisão:** `APROVADO`

---

## 1. Referências revisadas

- `docs/specs/fase-2-persistencia-local/spec.md` (Status: `APPROVED`)
- `docs/specs/fase-2-persistencia-local/tasks.md`
- `docs/01-PRD.md` (Armazenamento 100% local, privacidade e offline-first)
- `docs/03-SDD.md` (Room sobre SQLite, MVVM pragmático, Coroutines/Flow, injeção manual)
- `docs/04-ROADMAP.md` (Fase 2: Persistência local)
- `docs/specs/README.md` (Fluxo de trabalho e portões de qualidade)

---

## 2. Problemas concretos encontrados e correções

- **Problema 1: Compatibilidade KSP com Kotlin 2.2.10**
  - *Análise:* O projeto usa Kotlin 2.2.10. O KSP precisa ter versão alinhada explicitamente (por exemplo `2.2.10-x.y.z`). Deve-se verificar no catálogo e no build a versão exata do plugin KSP compatível antes de fixar.
  - *Correção aplicada:* Em `TASK-01`, a compatibilidade da toolchain será validada via compilação Gradle real (`./gradlew assembleDebug`) antes de prosseguir com as entidades.
- **Outros problemas:** Nenhum bloqueio arquitetural. As decisões Q1 (entidade pioneira `HabitEntity`) e Q2 (singleton manual) estão documentadas e aprovadas.

---

## 3. Cobertura dos critérios, dependências, testabilidade e riscos

### Cobertura de Critérios da Especificação

| Critério da Spec | Tarefa responsável | Verificação planejada |
|---|---|---|
| Critério 1 (Toolchain e KSP/Room) | `TASK-01` | `./gradlew testDebugUnitTest assembleDebug` |
| Critério 2 (Banco de dados e Entidade) | `TASK-02`, `TASK-03` | Testes de mapeamento e compilação do schema Room |
| Critério 3 (Contrato do DAO) | `TASK-03` | Testes unitários com Room in-memory / DAO test |
| Critério 4 (Contrato do Repositório) | `TASK-04` | Testes unitários isolados de `HabitRepositoryImpl` |
| Critério 5 (Testes de Persistência) | `TASK-05` | Testes de CRUD e emissão reativa em `Flow` |
| Critério 6 (Ciclo de Vida do Processo) | `TASK-05` | Teste de reabertura de instância de banco persistente em arquivo temporário |

### Ordem de Execução e Dependências

A ordem estrita é linear e respeita as dependências técnicas:
`TASK-01` (Toolchain) → `TASK-02` (Domínio & Entidade) → `TASK-03` (DAO & Database) → `TASK-04` (Repositório & Provedor) → `TASK-05` (Integração & Validação final).

### Testabilidade (TDD: RED → GREEN → REFACTOR)

- O projeto possui ambiente Java/Gradle configurado e funcional.
- Cada tarefa de implementação (`TASK-02` a `TASK-05`) define o teste RED específico a ser escrito antes da escrita do código de produção.

### Riscos e Mitigações

- **Risco de concorrência:** Operações Room e SQLite serão executadas em dispatchers adequados (`Dispatchers.IO`), evitando qualquer acesso ao banco na thread principal (UI thread).
- **Risco de regressão:** A suíte de testes de persistência será executável diretamente via `./gradlew testDebugUnitTest`.

---

## 4. Testes e comandos de validação planejados

- Compilação geral: `./gradlew assembleDebug`
- Execução de testes unitários: `./gradlew testDebugUnitTest`
- Verificação de schema e geração KSP: arquivos gerados em `app/build/generated/ksp` inspecionados automaticamente pelo build do Android.

---

## 5. Decisão de aprovação

**Decisão:** `APROVADO`

**Justificativa:** A especificação está aprovada com decisões resolvidas pelo responsável do produto. As tarefas cobrem 100% dos critérios de aceite da Fase 2 do Roadmap, respeitam a arquitetura do SDD e definem testes claros e viáveis sob a disciplina de TDD. A fase está pronta para ter sua primeira tarefa iniciada (`TASK-01`).
