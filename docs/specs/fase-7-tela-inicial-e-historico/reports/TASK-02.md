# Verificação — Fase 7 / TASK-02

- **Tarefa:** Casos de uso de resumo da Home e histórico consolidado
- **Status:** `VERIFIED`

## Evidências

- **RED:** `.\gradlew.bat testDebugUnitTest --tests com.ego.mobile.domain.usecase.HomeSummaryUseCaseTest --tests com.ego.mobile.domain.usecase.HistoryUseCaseTest --tests com.ego.mobile.domain.HistoryDomainTest` executou; os testes de comportamento falharam porque os casos de uso ainda retornavam coleções vazias.
- **GREEN:** `.\gradlew.bat testDebugUnitTest` passou com toda a suíte unitária.

## Comportamentos verificados

- O resumo considera hábitos ativos com frequência aplicável ao dia atual e mantém o estado da ocorrência do dia.
- Tarefas abertas são ordenadas por prazo, deixando as sem prazo por último.
- A sessão de treino ativa tem precedência sobre a próxima etapa capilar no banner único da Home.
- O histórico consolida apenas conclusões com timestamps reais de hábitos, tarefas, treinos e cuidados capilares, ordenadas por data/hora decrescente.
- Alterações reativas em cada repositório atualizam os fluxos consolidados.

## Decisão técnica

`HomeSummary.TodayHabit` identifica o hábito por `habitId`, compatível com `HabitRepository.completeOccurrence(habitId, dateCivil)`. Ocorrências do dia ainda inexistentes são representadas como `PENDING`.
