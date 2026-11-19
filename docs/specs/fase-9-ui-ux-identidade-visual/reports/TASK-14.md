# Verificação integrada — Fase 9 / TASK-14

- **Tarefa:** Validar a V1 em Início, Hábitos e Histórico
- **Status:** `VALIDADA_COM_LIMITAÇÃO_VISUAL_REGISTRADA`
- **AVD:** `Medium_Phone_API_37.0` (`emulator-5554`, API 37), desligado e liberado ao final.
- **Base:** merge integrado de TASK-05, TASK-06, TASK-04 e TASK-13.

## Evidências de validação

- `:app:testDebugUnitTest` e `:app:assembleDebug` — `BUILD SUCCESSFUL`.
- `:app:connectedDebugAndroidTest` — 49/49 testes aprovados em cada execução a 360 dp/fonte 1.0, 412 dp/fonte 1.0 e 412 dp/fonte 1.3. Após os ajustes finais de estados vazios e rolagem dos formulários, o recorte atualizado Home + estados vazios passou 19/19 em 412 dp/fonte 1.0.
- Os testes instrumentados cobrem navegação entre destinos, retorno do Histórico à Home, rotação, resumo/Home e Histórico consistentes após conclusão, formulários da Home, alvos acessíveis, calendário de sete datas, estados vazios/erro, persistência de calendário e transações de conclusão. Os testes unitários existentes cobrem datas civis, limites de dia/semana, temas/contraste e regras de histórico.
- Capturas AVD: [Home 360 dp](evidence/TASK-14-home-360.png), [Home 412 dp](evidence/TASK-14-home-412.png), [Home 412 dp com fonte 1.3](evidence/TASK-14-home-412-font130.png), [Hábitos 412 dp](evidence/TASK-14-habits-412.png), [Histórico 412 dp](evidence/TASK-14-history-412.png).

## Cobertura dos critérios V1

| Critérios | Resultado | Evidência principal |
|---|---|---|
| AC-04 a AC-08 | Aprovados | `HomeScreenTest`, `HomeHistoryConsistencyTest`, unidades de data civil e `HabitDayGrid`; menu de criação, conclusão/Undo, prazos e grade acessível. |
| AC-13 | Aprovado no fluxo Home | Formulários de hábito e tarefa abrem Bottom Sheets roláveis; contrato de salvamento da TASK-06 preservado. |
| AC-14 | Aprovado | `EmptyStateScreensTest` valida vazio, ação e distinção de erro; teste atualizado passou no conjunto final. |
| AC-15 | Parcial | 360/412 dp, fonte 1.3, semântica de sete dias, alvo do botão Criar ≥48 dp, cinco destinos e scroll foram exercitados; a altura dos cartões vazios da Home prejudica a densidade visual e segue pendente. |
| AC-16 | Aprovado | Unitários, instrumentados e build passaram nas execuções reportadas. |
| AC-17 a AC-18 | Aprovados nesta validação | Persistência de calendário/metas, correção histórica, virada de períodos e datas civis cobertas pelos testes existentes, incluindo `HabitCalendarPersistenceTest` e testes unitários de limites semanais. |

## Limitação visual observada

Na captura real da Home, os cartões vazios “Para hoje · Hábitos” e “Para hoje · Tarefas” ocupam altura excessiva e centralizam o conteúdo, deixando a grade semanal abaixo da dobra. A rolagem alcança a grade e os testes confirmam que os sete dias permanecem acessíveis, mas a densidade visual ficou inadequada; é necessária uma correção de layout em uma tarefa posterior. A limitação não foi ocultada nem marcada como conformidade visual plena.

As capturas de Hábitos e Histórico mostram as telas vazias em 412 dp e os estados iniciais dos fluxos; os estados com registros reais foram cobertos pelos testes instrumentados e de persistência, não por cenário visual de ponta a ponta com dados populados. Não foram executados testes instrumentados em aparelho físico.
