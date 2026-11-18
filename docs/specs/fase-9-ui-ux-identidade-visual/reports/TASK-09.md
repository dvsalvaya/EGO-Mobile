# Verificação — Fase 9 / TASK-09

- **Tarefa:** Calcular estatísticas e evolução de carga por exercício
- **Status:** `VALIDADO_NA_BASE_ROOM_7` (testes unitários e integração mínima com ViewModel validados com 100% de sucesso)

## Evidências

- **Testes de Caso de Uso e ViewModel:** `testDebugUnitTest --tests com.ego.mobile.domain.usecase.GetWorkoutStatisticsUseCaseTest --tests com.ego.mobile.feature.routines.workouts.WorkoutViewModelTest` — `BUILD SUCCESSFUL` (12 testes no total: 8 em `GetWorkoutStatisticsUseCaseTest`, 4 em `WorkoutViewModelTest`).
- **Suíte focada de treinos/modelos/estatísticas:** Execução conjunta com `WorkoutTemplateDomainTest` e `WorkoutTemplateRepositoryImplTest` — `BUILD SUCCESSFUL` (22 testes unitários no total, 0 falhas).

## Comportamentos verificados

- Apenas sessões `COMPLETED` com `endedAt` entram nas estatísticas; `IN_PROGRESS`, `PAUSED` e `CANCELLED` são ignoradas.
- Total de treinos concluídos e duração média em minutos derivados apenas de sessões finalizadas (com suporte adequado a sessões com pausas registradas na v7).
- Agrupamento por nome após `trim` e comparação sem distinção de caixa; rótulo de exibição preserva a grafia da primeira ocorrência.
- Para cada sessão, um ponto com a maior carga **positiva** do exercício; carga nula/zero não gera ponto, mas a sessão permanece no histórico e o exercício continua listado.
- Pontos em ordem cronológica por `endedAt` (desempate por ID); `reps` em texto livre nunca é interpretado como número.
- Sem pontuação geral/XP/níveis; integração mínima em `WorkoutUiState.stats` sem alterar os fluxos de sessão.

## Correções aplicadas ao trabalho pré-existente

O arquivo `GetWorkoutStatisticsUseCase.kt` encontrado na árvore não compilava e não atendia a spec (usava `firstOrNull()` em `Flow`, groups por nome exato, não produzia sequência cronológica de carga). Foi reescrito preservando a API pública já referenciada pelo teste existente e acrescentando `LoadPoint`/`points`.

Duas asserções do teste pré-existente estavam incorretas e foram corrigidas com justificativa:

- duração média: o comentário dizia `2000ms ≈ 33,3 minutos`; o correto é ≈ 0,0333 minuto;
- `sessionCount` de "Remada" sem carga: um teste esperava 0 e outro esperava 1 para o mesmo caso; padronizado para 1 (sessões em que o exercício aparece), preservando a intenção de que a sessão continue visível sem carga comparável.

## Validação na Base Room 7 e Lacunas

- **Status na base Room 7:** Validado com o schema v7; os cálculos de duração e sessões finalizadas respeitam o modelo atual de `workout_sessions` com suporte a pausa.
- **Lacunas restantes:**
  - Renderização visual completa das estatísticas e gráficos de evolução de carga na UI de treinos, escopo reservado para a `TASK-10` (`PENDING`).
