# Verificação — Fase 8 / TASK-02

- **Tarefa:** Estados vazios e tratamento amigável de erros
- **Status:** `VERIFIED`

## Auditoria

- Com o Room em memória sem registros, foram verificadas as superfícies Início, Hábitos, Tarefas, Treinos, Cuidados capilares e Histórico.
- Início mostra os estados sem hábitos/tarefas e mantém os atalhos de criação. Hábitos e Tarefas oferecem ações de criação; Treinos permite iniciar a primeira sessão; Cuidados capilares oferece predefinição ou criação personalizada.
- Histórico informa que não há atividades. Ele é mantido como área de consulta, seguindo `docs/02-USER-FLOWS.md`, sem introduzir um fluxo de criação novo nessa tela.
- Erros de carregamento em Hábitos, Tarefas e Histórico não se confundem mais com estados sem dados.
- Treinos apresenta erros de operação em Snackbar e converte falhas nos fluxos do repositório em mensagens de estado; erros capilares são liberados após a Snackbar para que a mesma falha possa ser apresentada novamente.

## Evidências

- **RED:** Os testes instrumentados falharam pelo comportamento esperado para erro de Hábitos não exibido, erros de carregamento de Tarefas e Histórico acompanhados por mensagens de vazio incorretas, erro de validação de Treino não visível e erro capilar idêntico que não reaparecia.
- **RED:** `WorkoutViewModelTest` falhou ao manter `isLoading` após a falha do fluxo de carregamento.
- **GREEN:** Os testes instrumentados focados e a suíte unitária focada passaram após as correções.
- **Regressão final:** `.\gradlew.bat testDebugUnitTest connectedDebugAndroidTest` passou, incluindo 24 testes instrumentados.
