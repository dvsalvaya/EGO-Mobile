# Verificação — Fase 9 / TASK-04

- **Tarefa:** Calcular a grade de hábitos previstos em sete dias
- **Status:** `VERIFIED`
- **Dependência:** TASK-13 verificada no commit `8948d3ef7f5731a6fc164562d9d9ddca187fda24`.

## Implementação

- `GetHabitDayGridUseCase` agrega por data civil os hábitos esperados e as conclusões emitidas por `HabitCalendar.days`, usando os planos efetivos preservados pela TASK-13.
- A grade sempre contém sete datas consecutivas, da mais antiga até hoje em `America/Sao_Paulo`; frequências semanais sem preferências não criam previsões diárias, e preferências posteriores ao atingimento da meta deixam de ser previstas. Conclusões corrigidas são refletidas na data original e fatos fora do plano são preservados.
- `HabitGridDay` expõe `completionRate`, `band` e `isInProgress`. As faixas são: sem previsão, 0%, abaixo de 50%, de 50% a menos de 100% e 100%. O zero de hoje com previsão aberta é `IN_PROGRESS` (não `MISSED`); `isInProgress` também permanece verdadeiro para hoje parcialmente concluído enquanto houver previsão aberta. A taxa é limitada a 100% para fatos adicionais.
- `Clock` pode ser injetado no caso de uso, enquanto o fluxo de datas continua a atualizar na virada civil. Nenhuma migração ou dependência foi adicionada.

## Histórico do critério anterior

O relatório anterior desta tarefa registrava `WeeklyConsistency`, que agregava eventos de hábitos, tarefas e treinos por nível de volume. A nota em `tasks.md` registrou a mudança de critério para a V1: a Home passa a usar proporção de previsões de hábitos concluídas. A implementação e evidências abaixo validam esse critério novo; o agregador anterior foi mantido no código para não alterar outros consumidores.

- A agregação anterior teve 8 testes direcionados (7 falhavam com resultado vazio forçado e todos passaram após implementação) e a suíte unitária completa foi validada numa árvore isolada, conforme evidência arquivada antes desta atualização.

## Evidências

- **RED:** `:app:testDebugUnitTest --tests com.ego.mobile.domain.HabitGridDayTest.exactlyHalfUsesPartialBand` falhou por asserção quando o limite foi temporariamente alterado de `< 0.5` para `<= 0.5` (`AssertionError`, `HabitGridDayTest.kt:53`). Restaurado o limite requerido pela V1, o teste passou.
- **GREEN:** `:app:testDebugUnitTest --tests com.ego.mobile.domain.HabitGridDayTest --tests com.ego.mobile.domain.usecase.HabitDayGridUseCaseTest --tests com.ego.mobile.domain.HabitCalendarTest` — `BUILD SUCCESSFUL` (30 testes).
- Casos cobertos: ausência de previsão; 0% encerrado; 0% hoje ainda em andamento; abaixo de 50%; exatamente 50%; 100% e taxa limitada; rejeição de contagens negativas; sete datas; dias fixos/diários; meta semanal com/sem dias preferidos; alcance da meta; pausa; alteração de plano no meio da semana; correção histórica; atualização após nova emissão; e relógio fixo no cruzamento da meia-noite UTC para a data civil de São Paulo.
- A validação foi unitária; nenhum AVD foi usado (reservado à TASK-03).

## Contrato para TASK-05

- Consumir `HabitDayGrid.days` em ordem cronológica. Cada `HabitGridDay` fornece `date`, `predicted`, `completed`, `completionRate` (`null` sem previsão), `band`, `isToday` e `isInProgress`.
- Mapear `NO_PREDICTION` para cinza, `MISSED` para vermelho, `LOW` para laranja, `PARTIAL` para verde escuro e `COMPLETE` para verde claro, conforme a V1. `IN_PROGRESS` deve usar tratamento não vermelho; `isInProgress` fornece a semântica “Em andamento” sempre que a previsão de hoje ainda estiver aberta.
- Expor descrição acessível a partir de data, contagens e estado; correções da timeline atualizam automaticamente o fluxo da grade.
