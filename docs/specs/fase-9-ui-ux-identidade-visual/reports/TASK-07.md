# Verificação — Fase 9 / TASK-07

- **Tarefa:** Agrupar Tarefas por prazo e adicionar detalhes em Bottom Sheet
- **Status:** `VERIFICADA`
- **Base incorporada:** `feat/spec9-integracao` em `44222650fcaee9e7ff3de8dcaeb017befa0f6b03` (fast-forward).

## Implementação

- Tarefas abertas são projetadas em três grupos por data civil em `America/Sao_Paulo`: Atrasadas, Hoje e Próximas. Atrasadas e Hoje usam `Task.isOverdue(currentTimeMillis)`, compartilhando RF-06 e respeitando a virada do dia; tarefas sem prazo entram no fim de Próximas.
- Os itens de cada grupo têm ordem cronológica de vencimento, com desempate estável por criação. Cada tarefa aberta é exibida uma única vez. Grupos vazios têm mensagem própria.
- A lista compacta mantém título, prazo, checkbox de conclusão e indicação textual com ícone de atraso. Tocar no item abre `TaskDetailsBottomSheet`, com estado, prazo/ausência de prazo, descrição e acesso a concluir, editar, reagendar ou excluir.
- Criação/edição continuam no `TaskFormDialog` em Bottom Sheet; reagendamento continua usando o DatePicker existente; exclusão confirmada mantém Snackbar com Desfazer. Tarefas concluídas seguem ocultas por padrão e ficam acessíveis no filtro próprio, em lista separada.

## TDD e evidências

- **RED:** com a propriedade de agrupamento temporariamente vazia, `TaskGroupingTest` falhou nos dois cenários por ausência dos grupos esperados (`IndexOutOfBoundsException`, 2 testes falhos). A classificação foi implementada em seguida.
- **GREEN:** `:app:testDebugUnitTest` — `BUILD SUCCESSFUL`; 177 testes em 37 suítes, sem falhas.
- **Build:** `:app:assembleDebug` — `BUILD SUCCESSFUL`.
- **Compilação de UI instrumentada:** `:app:compileDebugAndroidTestKotlin` — `BUILD SUCCESSFUL`, incluindo os testes novos de agrupamento/detalhes e acessibilidade.
- **Teste instrumentado focado:** `:app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.ego.mobile.feature.tasks.TasksGroupingDetailsTest` — 2/2 testes passaram no `Medium_Phone_API_37.0`.
- **Regressão instrumentada completa:** `:app:connectedDebugAndroidTest` — 52/52 testes passaram no `Medium_Phone_API_37.0` (API 37).
- A primeira execução completa encontrou uma asserção ambígua no teste de detalhe (o rótulo “Sem prazo” aparece no item e no painel); o teste foi ajustado para consultar o rótulo do prazo no painel por `testTag`, e a classe focada e a suíte completa passaram depois da correção.
- `git diff --check` — sem erros de whitespace.

## Encerramento do AVD

- O AVD foi desligado após as execuções; liberação para TASK-11 informada ao coordenador no encerramento desta dispatch.
- Não houve inspeção visual manual. O teste instrumentado confirma classificação, abertura do painel e presença dos controles; os testes de ViewModel cobrem conclusão, reagendamento, exclusão e Undo. DatePicker, confirmação de exclusão e Snackbar foram mantidos sem alterações e não receberam teste visual dedicado nesta tarefa.
