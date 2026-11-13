---
description: Implementar uma única tarefa aprovada seguindo TDD estrito
---

# Implementar uma tarefa

Execute somente a tarefa `<task-id>` da funcionalidade `docs/specs/<feature-id>/`. Não comece automaticamente outra tarefa.

## Portões de entrada

1. Leia `spec.md`, `plan.md`, `tasks.md` e os documentos de produto/técnicos relevantes: `docs/01-PRD.md`, `docs/02-USER-FLOWS.md`, `docs/03-SDD.md` e `docs/04-ROADMAP.md`.
2. Confirme que a especificação está `APPROVED` ou `IN_PROGRESS`, que o plano está `APROVADO` e que a tarefa existe, está selecionada e tem status `PENDING` ou `IN_PROGRESS`. Não reabra tarefas `VERIFIED` ou `BLOCKED` sem revisão explícita.
3. Confirme que todos os pré-requisitos estão concluídos. Se qualquer condição falhar, pare sem implementar e informe o bloqueio.
4. Inspecione o código e os testes existentes antes de decidir onde atuar. Não assuma estrutura ou comandos: confira o projeto real.

## Ciclo obrigatório

1. Identifique o teste mais relevante para um critério de aceite da tarefa.
2. **RED:** escreva primeiro um teste que expresse o comportamento esperado e execute-o. Confirme que falha pelo motivo funcional esperado. Falha de compilação, configuração, dependência ou ambiente não é RED válido. Se não conseguir reproduzir a falha esperada, pare, explique o resultado e não escreva a implementação.
3. **GREEN:** faça a menor alteração de produção necessária para passar no teste. Não escreva a implementação toda antes do teste.
4. Execute novamente o teste e confirme o resultado observado.
5. **REFACTOR:** melhore o código sem mudar o comportamento especificado; execute os testes afetados novamente e mantenha-os verdes.
6. Execute os testes relevantes de regressão disponíveis. Se a infraestrutura necessária estiver ausente ou algum teste falhar, registre o bloqueio/falha claramente; não finja que TDD foi seguido nem ignore a falha.
7. Revise o diff para escopo, consistência com a especificação e ausência de alterações não relacionadas. Atualize o status e as evidências da tarefa em `tasks.md` somente de acordo com o que foi realmente executado. Não marque como concluída se houver validação pendente.

Pare e solicite revisão se houver ambiguidade relevante, contradição com a especificação aprovada ou necessidade de requisito novo. Não altere a especificação aprovada para justificar uma implementação divergente. Ao terminar, relate a tarefa realizada, mudanças, comandos/resultados observados, status e bloqueios. Não inicie a próxima tarefa.
