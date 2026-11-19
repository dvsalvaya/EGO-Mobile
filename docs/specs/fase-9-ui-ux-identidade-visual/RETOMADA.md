# Retomada da Spec 9

A rodada foi pausada após o commit local `0d2f4a9`. A base para continuar é a branch `feat/spec9-integracao`, no worktree Orca `spec9-integracao`. A execução foi `run_8970d5bcccd5`. O emulador API 37 foi desligado e liberado.

## Trabalho integrado

- TASK-01 a TASK-04 entregaram tema, navegação, componentes comuns e grade de sete dias.
- TASK-06 e TASK-13 entregaram a tela de Hábitos e as regras de previsão, fechamento e correção histórica. A migração Room 7→8 e a persistência passaram na suíte focada de seis testes.
- TASK-08 e TASK-09 validaram a base existente de modelos e estatísticas de treino.
- TASK-05 implementou a Home. TASK-14 validou os fluxos integrados de Início, Hábitos e Histórico. Os testes unitários e o build passaram. A suíte instrumentada registrou 49/49 em cada perfil de 360 dp e 412 dp com fonte normal e 412 dp com fonte a 130%. O aceite visual de AC-15 continua parcial. [Relatório da Home](reports/TASK-05.md) e [validação da V1, com capturas e escopo das reexecuções](reports/TASK-14.md).

## Próxima sessão

1. Começar pelos cartões vazios de hábitos e tarefas da seção “Para hoje”. Ambos centralizam o conteúdo em uma área alta e empurram a grade semanal para baixo. Usar as capturas da TASK-14 como referência e ajustar a altura dos cartões na Home, preservando semântica, alvos de toque e rolagem.
2. Repetir a inspeção nos três perfis, registrar capturas e atualizar os relatórios TASK-05/14 e o resultado de AC-15. Reservar o emulador para um único agente por vez.
3. Após o aceite da V1, atualizar os worktrees de Tarefas, Treinos e Cabelo a partir da integração e retomar TASK-07, TASK-10 e TASK-11 em paralelo. TASK-12 encerra a fase com a regressão completa.

[Plano técnico](plan.md) · [Tarefas e dependências](tasks.md)
