# Especificações de funcionalidades

Cada funcionalidade relevante deve ter sua própria pasta em `docs/specs/<feature-id>/`, com especificação (`spec.md`), decomposição (`tasks.md`), plano técnico revisado (`plan.md`) e relatórios de verificação das tarefas. Não crie uma especificação fictícia ou comece a implementar uma funcionalidade sem uma especificação aprovada.

As referências de produto e arquitetura são `docs/01-PRD.md`, `docs/02-USER-FLOWS.md`, `docs/03-SDD.md` e `docs/04-ROADMAP.md`. Elas continuam sendo a fonte das decisões aprovadas; este processo não autoriza requisitos novos.

## Status

Use um status geral em `spec.md`:

`DRAFT → APPROVED → IN_PROGRESS → VERIFIED`

- **DRAFT:** proposta ainda não aprovada. Não decomponha como trabalho autorizado nem implemente.
- **APPROVED:** responsável pelo produto revisou e aprovou escopo, comportamentos e critérios de aceite. O plano técnico também precisa ser aprovado antes da implementação.
- **IN_PROGRESS:** há tarefas em execução, após aprovação da especificação e do plano.
- **VERIFIED:** todas as tarefas foram implementadas e verificadas com evidências; nenhum critério aplicável ficou pendente.

A aprovação deve ser explícita pelo responsável pelo produto, registrada no `spec.md` com data e aprovador. Uma especificação em `DRAFT` nunca equivale a uma aprovação. Alterações materiais em uma especificação aprovada exigem nova revisão e aprovação antes de continuar o trabalho afetado. Não avance status sem atender seus portões.

Em `tasks.md`, mantenha o status individual de cada tarefa, por exemplo `PENDING`, `IN_PROGRESS`, `BLOCKED` ou `VERIFIED`, e registre dependências por ID. `VERIFIED` exige evidência de validação, não apenas código escrito.

## Fluxo de trabalho

1. **Criar especificação:** use `.github/prompts/create-spec.prompt.md`. Leia PRD, User Flows, SDD e Roadmap; registre o escopo, exclusões, comportamentos e critérios verificáveis em `docs/specs/<feature-id>/spec.md`. Ambiguidades relevantes devem ser esclarecidas, não decididas por suposição. A especificação começa como `DRAFT`.
2. **Aprovar especificação:** o responsável pelo produto confere requisitos, limites e critérios de aceite, resolve questões que afetem o comportamento e registra explicitamente a aprovação, a data e o aprovador. Sem isso, o trabalho não avança.
3. **Decompor:** após aprovação, use `.github/prompts/decompose-spec.prompt.md` para criar `tasks.md`, com tarefas pequenas, critérios, testes, pré-requisitos e dependências reais.
4. **Revisar o plano:** use `.github/prompts/review-plan.prompt.md` para avaliar cobertura, arquitetura, ordem, testabilidade, regressões, complexidade e viabilidade dos testes. Registre a decisão e correções em `plan.md`. Não implemente antes da aprovação técnica do plano.
5. **Implementar uma tarefa:** selecione explicitamente uma tarefa pronta, confirme pré-requisitos e use `.github/prompts/implement-task.prompt.md`. Faça apenas essa tarefa, seguindo TDD: RED (teste executado falha pelo comportamento esperado), GREEN (menor alteração para passar), REFACTOR (melhoria com testes verdes). Falhas de compilação/configuração/ambiente não provam RED. Se não houver infraestrutura para testar, registre bloqueio; não declare TDD concluído.
6. **Verificar e concluir:** use `.github/prompts/verify-task.prompt.md` para comparar implementação e critérios, executar testes relevantes, revisar regressões e registrar evidências. Corrija tarefas reprovadas antes de marcá-las verificadas. Só marque a funcionalidade `VERIFIED` quando todas as tarefas e critérios aplicáveis estiverem verificados.

Não inicie automaticamente a próxima tarefa ao concluir uma. Se a implementação exigir mudança de requisito ou contradizer a especificação aprovada, pare e encaminhe a especificação para revisão.

## Seleção de modelo

Separe planejamento de execução. Prefira manualmente um modelo de maior capacidade de raciocínio para especificação, decisões arquiteturais, revisão do plano e falhas complexas; para uma tarefa pequena e localizada, um modelo mais econômico pode ser suficiente. Se o problema se mostrar complexo, retorne manualmente a um modelo de maior capacidade. A seleção depende do cliente e da configuração disponíveis: não presuma que o agente consiga trocar de modelo automaticamente. Os prompts são independentes de fornecedor e não fixam nomes de modelos.

## Invocação dos prompts

Os arquivos `.prompt.md` são prompts reutilizáveis para clientes Copilot que ofereçam suporte a prompt files. Quando disponíveis, invoque pelo nome no chat e forneça a funcionalidade/tarefa e o contexto indicado:

- `/create-spec` — descreva a funcionalidade a especificar;
- `/decompose-spec` — informe o `<feature-id>` aprovado;
- `/review-plan` — informe o `<feature-id>` com `spec.md` e `tasks.md` preparados;
- `/implement-task` — informe `<feature-id>` e `<task-id>` selecionados;
- `/verify-task` — informe `<feature-id>` e `<task-id>` implementados.

O mecanismo de descoberta, a sintaxe de chamada e a disponibilidade em modo agente variam por IDE, cliente e versão. Se o cliente não oferecer prompt files, copie o conteúdo do arquivo desejado para o chat e forneça os caminhos da especificação, tarefa e documentos pertinentes como contexto/anexos. Em qualquer cliente, confira o status e os pré-requisitos antes de executar; nomes como `/create-spec` não são garantidos fora de clientes que suportem essa convenção.

O arquivo `.github/copilot-instructions.md` fornece instruções permanentes a clientes Copilot compatíveis com instruções de repositório. O suporte e o escopo de aplicação também variam por IDE/cliente/versão; quando necessário, carregue estas instruções explicitamente no contexto. Nenhum recurso de cliente substitui os portões de aprovação e validação descritos acima.
