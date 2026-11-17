# Verificação — Fase 9 / TASK-04

- **Tarefa:** Agregação deduplicada de consistência de sete dias
- **Status:** `IMPLEMENTADO` (evidência unitária; verificação formal da fase pendente — spec ainda `DRAFT`)

## Evidências

- **RED:** com o agregador devolvendo sempre `WeeklyConsistency.empty(today)`, `testDebugUnitTest --tests com.ego.mobile.domain.usecase.WeeklyConsistencyUseCaseTest` executou 8 testes e 7 falharam por asserção de comportamento.
- **GREEN:** após a implementação, os 8 testes passaram.
- **Regressão:** `testDebugUnitTest` completo executou com `BUILD SUCCESSFUL` na árvore isolada de validação (vide observação abaixo).

## Comportamentos verificados

- Sete dias civis em `America/Sao_Paulo`, do mais antigo a hoje, com níveis 0, 1, 2 e 3+ e sem percentual geral.
- Chave canônica `(tipo de fonte, id persistido)`;IDs iguais em tipos diferentes não colidem e apresentações repetidas contam uma vez.
- conclusão de hábito usa a data civil da ocorrência; tarefa usa a data local de `completedAt` e sessão usa a data local de `endedAt`.
- Pendências, tarefas abertas, sessões ativas, eventos fora da janela e Cabelo são excluídos.
- Data/fuso injetáveis (`zoneId` e `today`), com agregação pura em `aggregateWeeklyConsistency`.

## Observação sobre o ambiente

O worktree compartilhado continha, durante esta execução, alterações concorrentes e incompletas de outras tarefas da Fase 9 (Home, Tarefas, Cabelo, tokens), com centenas de erros de compilação alheios a esta tarefa. Para produzir evidência rastreável, os arquivos desta tarefa foram validados em uma árvore isolada criada a partir de `HEAD`, com o restante do código no estado commitsado; nenhum arquivo alheio foi alterado.
