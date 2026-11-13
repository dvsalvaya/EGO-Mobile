# Verificação — Fase 7 / TASK-04

- **Tarefa:** `HistoryViewModel` com filtros por atividade
- **Status:** `VERIFIED`

## Evidências

- **RED:** `.\gradlew.bat testDebugUnitTest --tests com.ego.mobile.feature.history.HistoryViewModelTest` executou; o ViewModel permanecia vazio e ignorava a seleção de filtro.
- **GREEN:** o mesmo comando passou após implementação.

## Comportamentos verificados

- Histórico consolidado apresentado em ordem cronológica decrescente.
- Filtros para todos os tipos via `ActivityType`, com `null` representando "Todos".
- Seleção de filtro mantida durante atualizações do `Flow` de histórico.
