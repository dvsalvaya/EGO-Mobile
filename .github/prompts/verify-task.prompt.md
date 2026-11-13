---
description: Verificar uma tarefa implementada contra a especificação, testes e regressões
---

# Verificar tarefa

Verifique `<task-id>` de `docs/specs/<feature-id>/`. Leia `spec.md`, `plan.md`, `tasks.md` e os documentos relevantes `docs/01-PRD.md`, `docs/02-USER-FLOWS.md`, `docs/03-SDD.md` e `docs/04-ROADMAP.md`. Inspecione o diff e execute os testes disponíveis apropriados no ambiente atual.

Não aprove com base apenas na leitura do código, em resultados relatados por outra pessoa ou em testes não executados/verificados. Distinga falhas funcionais de falhas de compilação, configuração e ambiente. Se não for possível executar uma validação, registre explicitamente a limitação; não trate isso como aprovação.

Produza um relatório em `docs/specs/<feature-id>/verification-<task-id>.md` contendo:

1. **Critérios atendidos e não atendidos**, vinculados aos IDs da especificação e com evidência;
2. **Testes executados e resultados observados**, com comandos e resumo do resultado; identifique testes relevantes não executados e motivo;
3. **Alterações fora do escopo**, ou `Nenhuma identificada`;
4. **Riscos e limitações**, incluindo regressões, ambiente e infraestrutura de testes;
5. **Decisão:** `APROVADA` ou `PRECISA DE CORREÇÕES`, com justificativa concreta.

Aprove somente quando os critérios aplicáveis estiverem atendidos, os testes relevantes tiverem sido realmente executados com sucesso e não houver lacuna material ou regressão conhecida. Caso contrário, indique as correções/verificações necessárias, mantenha a tarefa não concluída e não altere a especificação para acomodar a implementação. Atualize `tasks.md` e o status geral apenas quando as evidências justificarem; a funcionalidade só pode chegar a `VERIFIED` depois que todas as tarefas forem verificadas.
