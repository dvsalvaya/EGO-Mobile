# Verificação — Fase 9 / TASK-06

- **Tarefa:** Atualizar Hábitos com progresso, edição e estados compactos
- **Status:** `VERIFIED`
- **Nota de escopo:** implementado sob autorização direta do solicitante/coordenador. A especificação e seu plano geral permanecem inalterados.

## Implementação

- A aba Hábitos agora oferece cabeçalho com “Novo hábito”, listas separadas de ativos e pausados com contagem, estado vazio de ativos quando só existem pausados, progresso diário/semanal e cartões menores.
- Tocar no item abre painel de detalhes com progresso, editar, pausar/retomar, arquivar com confirmação, desfazer conclusão de hoje e registrar uma conclusão em data anterior.
- Criação/edição usam o mesmo Bottom Sheet. O formulário oferece “Todos os dias”, “Dias da semana” e metas semanais de 1 a 7, com zero a sete dias preferidos. As iniciais têm nomes completos e seleção semântica acessíveis; frequência e dias podem ser alterados na edição.
- Alteração do plano pede confirmação e informa que vale a partir de hoje, preservando histórico e ocorrências. O `HabitFrequency.TimesPerWeek(count, preferredDays)` e o versionamento de plano da TASK-13 permanecem a fonte persistida; mudanças passam por `updateHabit` sem substituir registros anteriores.
- A assinatura compartilhada `HabitFormDialog(onDismiss, onSave(name, description, frequency))` foi preservada para a Home; preferências seguem no argumento `frequency`. A Home não troca de destino ao abrir o formulário.
- O formulário agora valida os valores calculados antes de salvar; erros permanecem no campo e não fecham o sheet.

## Evidências

- **RED:** a validação de campo identificou que a confirmação podia fechar o formulário com entrada inválida devido ao uso imediato de estados Compose ainda antigos. A verificação de UI também cobriu as três frequências e dias preferidos acessíveis.
- **GREEN / ViewModel:** `:app:testDebugUnitTest` — `BUILD SUCCESSFUL`, incluindo `HabitsViewModelTest` para edição de meta semanal e preferências.
- **GREEN / compilação UI:** `:app:compileDebugAndroidTestKotlin` — `BUILD SUCCESSFUL`; contratos da Home e demais consumidores compilam com a assinatura compartilhada mantida.
- **GREEN / instrumentação em AVD exclusivo:** `:app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.ego.mobile.feature.habits.HabitStabilizationTest` — `BUILD SUCCESSFUL`, 4/4 testes no `Medium_Phone_API_37.0(AVD) - 17` (`emulator-5554`). Cobriu validação, conclusão/desfazer, pausa/retomada/edição, seleção semanal acessível e confirmação da alteração de plano com ocorrência anterior preservada.
- Um teste instrumentado já existente em `EmptyStateScreensTest` não passava compilação porque omitia o segundo argumento obrigatório de `HistoryViewModel`; foi atualizado para fornecer o repositório de hábitos, sem mudança de comportamento.

## Contratos para Home

- Continuar usando o formulário compartilhado com `initialName`, `initialDescription`, `initialFrequency`, `onDismiss` e `onSave(name, description, frequency)`; `onSave` recebe os dias preferidos dentro de `HabitFrequency.TimesPerWeek`.
- A Home pode continuar abrindo a criação localmente sem alterar a navegação. Para alterações em planos semanais, preferências vazias continuam válidas e representam uma meta sem dias preferidos.
- A persistência permanece compatível com TASK-13: nova revisão efetiva a partir de hoje, ocorrências e histórico anteriores preservados.

## AVD

- O AVD `emulator-5554` ficou reservado durante as validações desta tarefa e foi encerrado após a execução final para liberar o dispositivo.
