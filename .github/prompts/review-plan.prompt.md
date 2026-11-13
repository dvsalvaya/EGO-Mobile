---
description: Revisar e aprovar tecnicamente um plano sem implementar código
---

# Revisar plano técnico

Leia `docs/specs/<feature-id>/spec.md`, `docs/specs/<feature-id>/tasks.md` e os trechos relevantes de `docs/01-PRD.md`, `docs/02-USER-FLOWS.md`, `docs/03-SDD.md`, `docs/04-ROADMAP.md` e `docs/specs/README.md`.

Confirme que a especificação está `APPROVED` e que não há questões bloqueadoras. Se não estiver, não aprove o plano. Analise concretamente:

- cobertura de todos os critérios de aceite por tarefas e validações;
- consistência com a arquitetura e decisões existentes;
- dependências, pré-requisitos e ordem de execução;
- testabilidade, incluindo um caminho realista para RED → GREEN → REFACTOR;
- riscos de regressão e casos-limite relevantes;
- complexidade, abstrações e dependências desnecessárias;
- viabilidade de executar os testes no ambiente e na toolchain disponíveis.

Não implemente código, não altere requisitos e não aprove com base em afirmações não verificadas. Aponte cada problema com sua tarefa/critério relacionado, impacto e correção proposta. Se a infraestrutura de testes ou o ambiente necessário estiver indisponível, registre isso como bloqueio em vez de presumir viabilidade.

Salve o resultado em `docs/specs/<feature-id>/plan.md`, com:

1. referências revisadas;
2. problemas concretos encontrados e correções necessárias (ou `Nenhum`);
3. cobertura dos critérios, dependências, testabilidade e riscos;
4. testes e comandos de validação planejados, identificando o que ainda precisa ser confirmado no ambiente;
5. decisão `APROVADO` ou `PRECISA DE CORREÇÕES`, com justificativa.

Se houver problemas, corrija `tasks.md` apenas para manter o plano alinhado à especificação e registre o que mudou; não altere a especificação aprovada. O plano só pode ser marcado `APROVADO` quando todos os bloqueios forem resolvidos. Não altere o status da funcionalidade para `IN_PROGRESS` nesta etapa.
