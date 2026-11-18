# Verificação — Fase 9 / TASK-13

- **Tarefa:** Fechar períodos, preservar planos e corrigir histórico de hábitos
- **Status:** `IN_PROGRESS` (implementação e testes unitários direcionados concluídos; execução instrumentada não conclusiva)

## Implementação

- Frequências semanais agora armazenam até sete dias preferidos, mantendo leitura do formato legado `TIMES_PER_WEEK:n`.
- Planos de hábito são persistidos por data efetiva. Edições de frequência, preferências, pausa, retomada e arquivamento gravam uma nova revisão sem substituir o histórico anterior.
- `HabitCalendar` deriva previsões e fechamentos diários/semanais, exclui previsões posteriores ao atingimento da meta e mantém os registros factuais para correções acima da meta.
- O Histórico consolida hábitos previstos e concluídos, expõe os dias do período e permite marcar “Fiz nesse dia” ou “Não fiz” na data original. Correções são persistidas de forma idempotente.
- Room evoluiu da versão 7 para 8 com `MIGRATION_7_8`. A migration preserva as ocorrências e sessões existentes e inicia um plano conhecido na data da atualização, sem inferir regras passadas que o schema 7 não guardava.

## Evidências

- **RED:** `testDebugUnitTest --tests com.ego.mobile.domain.HabitCalendarTest.weeklyGoalRoundTripsItsPreferencesAndStillReadsLegacyGoals` falhou ao remover temporariamente a serialização dos dias preferidos (`ComparisonFailure`, `HabitCalendarTest.kt:55`).
- **GREEN:** o mesmo teste passou após restaurar a serialização; o conjunto direcionado abaixo também passou.
- **Testes unitários direcionados:** `:app:testDebugUnitTest --tests com.ego.mobile.domain.HabitCalendarTest --tests com.ego.mobile.domain.usecase.HabitDayGridUseCaseTest --tests com.ego.mobile.data.repository.HabitRepositoryImplTest --tests com.ego.mobile.domain.usecase.HistoryUseCaseTest --tests com.ego.mobile.feature.history.HistoryViewModelTest` — `BUILD SUCCESSFUL`.
- **Compilação instrumentada:** `:app:compileDebugAndroidTestKotlin` — `BUILD SUCCESSFUL`.
- **Migração e persistência instrumentadas:** tentativas de `:app:connectedDebugAndroidTest` com `WorkoutTemplateMigrationTest` e `HabitCalendarPersistenceTest`, e isolando `migratesSevenWithoutInventingPastPlansOrChangingOccurrences`, terminaram com `Instrumentation run failed due to Process crashed` no AVD API 37. O relatório não contém stack trace/assertion da migration; portanto a execução não confirma nem reprova o comportamento em runtime.

## Contratos para tarefas dependentes

- **TASK-04:** usar `HabitRepository.observeHabitCalendars()` como fonte de planos e ocorrências; `HabitCalendar.days(from, through)` informa `expected` e `completed`. Para a grade semanal, considerar somente datas previstas, contar conclusões inclusive correções factuais e não criar previsões diárias para metas sem preferências ou após atingir a meta. O tipo persistido é `HabitFrequency.TimesPerWeek(count, preferredDays)`.
- **TASK-06:** o formulário pode ler/gravar `preferredDays` no mesmo `HabitFrequency.TimesPerWeek`; conjunto vazio permanece válido e representa meta semanal sem dias preferidos. Mudanças em frequência/preferências chamam `updateHabit` e passam a valer desde hoje, preservando revisões e ocorrências anteriores.

## Pendência

Repetir os dois testes instrumentados de persistência/migration em um AVD estável ou dispositivo conectado antes de marcar `TASK-13` como `VERIFIED`.
