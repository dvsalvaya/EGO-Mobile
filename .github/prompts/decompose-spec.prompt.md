---
description: Decompor uma especificação aprovada em tarefas pequenas e verificáveis
---

# Decompor especificação

Leia a especificação indicada em `docs/specs/<feature-id>/spec.md`, confirme que seu status é `APPROVED` e consulte as partes relevantes de `docs/01-PRD.md`, `docs/02-USER-FLOWS.md`, `docs/03-SDD.md`, `docs/04-ROADMAP.md` e `docs/specs/README.md`.

Se a especificação estiver em `DRAFT`, tiver questões bloqueadoras ou não puder ser localizada, pare e informe o motivo. Não altere seu status, requisitos ou código. Não implemente tarefas.

Crie ou atualize `docs/specs/<feature-id>/tasks.md`. Decomponha o trabalho em unidades pequenas, ordenadas por dependências reais. Cada tarefa deve ser realizável e verificável isoladamente; evite tarefas que agrupem uma funcionalidade inteira. Separe implementação, testes ou infraestrutura quando isso gerar unidades mais claras, sem criar trabalho artificial.

Cada tarefa deve conter:

- **ID único** dentro da funcionalidade;
- **Objetivo** específico;
- **Referência à especificação**, incluindo critérios de aceite pertinentes;
- **Pré-requisitos e dependências**, com IDs de tarefas ou `Nenhum`;
- **Arquivos ou componentes provavelmente envolvidos**, sem tratar a previsão como compromisso;
- **Critérios de aceite** verificáveis;
- **Testes necessários**, incluindo o comportamento que deve ser testado; planeje TDD para implementação;
- **Definição de pronto**, incluindo validação e atualização do status.

Use um status inicial explícito como `PENDING` para cada tarefa. Preserve dependências rastreáveis e não marque tarefas concluídas. Ao final, indique cobertura dos critérios de aceite, ordem proposta e quaisquer bloqueios; mudanças de escopo devem voltar à revisão da especificação, não ser inventadas no plano.
