# Relatório — Fase 9 / TASK-03

- **Tarefa:** Padronizar componentes comuns de seção e estado vazio
- **Status:** `IMPLEMENTADO`
- **Nota de escopo:** implementação feita sob autorização direta do solicitante/coordenador. Não altera o status `DRAFT` da especificação nem a decisão pendente do plano técnico.

## Implementação

- Criados `feature/common/EmptyState` e `SectionHeader`, com hierarquia Material 3, ícone vetorial desenhado localmente, descrição semântica, ação opcional de toque mínimo 48 dp e opção de cartão.
- Aplicados estados vazios compartilhados em Hábitos, Tarefas, Histórico, Início, Treinos e Cabelo. Histórico não oferece criação direta; lista vazia filtrada e lista inicial têm mensagens diferentes.
- Loading e falhas permanecem em ramos próprios. A ausência de exercícios e o histórico de treinos oferecem ações contextuais válidas.
- Adicionados testes instrumentados para mensagens, ações, semântica e dimensões mínimas, além de distinção de erros e ausência de criação no Histórico.

## Validação

- **RED:** `connectedDebugAndroidTest` executado antes da implementação; os testes novos de `EmptyState` e `SectionHeader` falharam pelas mensagens/subtítulo ausentes. O processo também registrou uma falha intermitente do teste preexistente `haircareScreenCanShowTheSameOperationErrorAgain` e terminou com falha do processo de instrumentação.
- **GREEN parcial:** após a implementação, a classe `EmptyStateScreensTest` executou 13 testes; 12 passaram e um falhou apenas porque a asserção esperava a frase antiga do vazio de histórico de treinos. O texto e a ação foram ajustados para preservar a frase/ação existentes. O teste não foi repetido porque o coordenador reservou `emulator-5554` para TASK-13.
- `testDebugUnitTest assembleDebug`: `BUILD SUCCESSFUL` após as alterações finais.

## Pendência

Repetir `connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.ego.mobile.feature.EmptyStateScreensTest` quando o emulador for liberado. Por isso, este relatório não declara a tarefa verificada.
