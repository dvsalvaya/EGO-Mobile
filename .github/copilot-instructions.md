# Instruções do projeto EGO Mobile

- Kotlin é a linguagem principal. O projeto é um aplicativo Android nativo; respeite a toolchain e os padrões já existentes.
- Trate `docs/01-PRD.md`, `docs/02-USER-FLOWS.md`, `docs/03-SDD.md` e `docs/04-ROADMAP.md` como referências aprovadas. Não altere requisitos de produto durante a implementação.
- Não implemente uma funcionalidade relevante sem uma especificação aprovada em `docs/specs/<feature-id>/spec.md`.
- Não execute uma funcionalidade inteira de uma vez. Trabalhe somente na tarefa selecionada, respeitando seus pré-requisitos e o plano técnico aprovado.
- Use TDD estritamente na ordem RED → GREEN → REFACTOR. RED exige executar um teste que falhe pelo comportamento esperado; falha de compilação, configuração ou ambiente não conta.
- Nunca afirme que um teste passou sem executá-lo ou verificar o resultado observado.
- Não ignore testes falhos para concluir uma tarefa. Não altere testes só para fazer a implementação passar; se um teste estiver incorreto, justifique a correção e preserve a intenção da especificação.
- Se não houver infraestrutura para testar o comportamento, registre o bloqueio e não alegue que TDD foi seguido.
- Evite abstrações, dependências e complexidade desnecessárias. Siga a arquitetura existente e as decisões do SDD.
- Mantenha documentação e status da especificação/tarefas sincronizados com mudanças relevantes. Só marque trabalho como concluído após as validações exigidas.
- Não introduza requisitos novos nem resolva ambiguidades de produto por suposição. Registre-as e solicite esclarecimento ou revisão quando afetarem o comportamento.
- Se implementação e especificação aprovada entrarem em conflito, pare e solicite revisão antes de alterar código ou requisitos.
