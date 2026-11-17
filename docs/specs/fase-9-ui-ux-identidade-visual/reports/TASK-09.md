# Verificação — Fase 9 / TASK-09

- **Tarefa:** Calcular estatísticas e evolução de carga por exercício
- **Status:** `IMPLEMENTADO` (evidência unitária; verificação formal da fase pendente — spec ainda `DRAFT`)

## Evidências

- `testDebugUnitTest --tests com.ego.mobile.domain.usecase.GetWorkoutStatisticsUseCaseTest --tests com.ego.mobile.feature.routines.workouts.WorkoutViewModelTest` — `BUILD SUCCESSFUL` (11 testes).
- `testDebugUnitTest` completo — `BUILD SUCCESSFUL` na árvore isolada de validação.

## Comportamentos verificados

- Apenas sessões `COMPLETED` com `endedAt` entram nas estatísticas; `IN_PROGRESS`, `PAUSED` e `CANCELLED` são ignoradas.
- Total de treinos concluídos e duração média em minutos derivados apenas de sessões finalizadas.
- Agrupamento por nome após `trim` e comparação sem distinção de caixa; rótulo de exibição preserva a grafia da primeira ocorrência.
- Para cada sessão, um ponto com a maior carga **positiva** do exercício; carga nula/zero não gera ponto, mas a sessão permanece no histórico e o exercício continua listado.
- Pontos em ordem cronológica por `endedAt` (desempate por ID); `reps` em texto livre nunca é interpretado como número.
- Sem pontuação geral/XP/níveis; integração mínima em `WorkoutUiState.stats` sem alterar os fluxos de sessão.

## Correções aplicadas ao trabalho pré-existente

O arquivo `GetWorkoutStatisticsUseCase.kt` encontrado na árvore não compilava e não atendia a spec (usava `firstOrNull()` em `Flow`, groups por nome exato, não produzia sequência cronológica de carga). Foi reescrito preservando a API pública já referenciada pelo teste existente e acrescentando `LoadPoint`/`points`.

Duas asserções do teste pré-existente estavam incorretas e foram corrigidas com justificativa:

- duração média: o comentário dizia `2000ms ≈ 33,3 minutos`; o correto é ≈ 0,0333 minuto;
- `sessionCount` de "Remada" sem carga: um teste esperava 0 e outro esperava 1 para o mesmo caso; padronizado para 1 (sessões em que o exercício aparece), preservando a intenção de que a sessão continue visível sem carga comparável.
