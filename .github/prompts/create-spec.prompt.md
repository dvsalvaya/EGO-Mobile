---
description: Criar uma especificação de funcionalidade do EGO Mobile sem inventar decisões de produto
---

# Criar especificação

Crie uma especificação para a funcionalidade solicitada. Antes de escrever, leia as partes relevantes de:

- `docs/01-PRD.md` (requisitos e limites do produto);
- `docs/02-USER-FLOWS.md` (navegação e comportamentos aprovados);
- `docs/03-SDD.md` (arquitetura, tecnologia, persistência, privacidade e testes);
- `docs/04-ROADMAP.md` (etapas e dependências do produto);
- `docs/specs/README.md` (processo e ciclo de status).

Inspecione especificações existentes apenas para evitar IDs duplicados e manter consistência. Não implemente código nem altere os documentos de produto.

## Regras

- Não invente decisões de produto, regras, telas, campos, estados ou comportamento ausentes das referências.
- Diferencie fatos aprovados, propostas e questões em aberto. Referencie o documento e a seção que sustentam cada decisão relevante.
- Registre ambiguidades. Se uma ambiguidade puder mudar significativamente o comportamento esperado, não a resolva por suposição: peça esclarecimento antes de declarar a especificação pronta para aprovação.
- Não marque a especificação como aprovada. Toda especificação nova começa com status `DRAFT`; a aprovação cabe ao responsável pelo produto.
- Use um `<feature-id>` curto, único, estável e em kebab-case. Salve o resultado em `docs/specs/<feature-id>/spec.md`. Não crie especificações para funcionalidades não solicitadas.

## Conteúdo obrigatório de `spec.md`

1. **Identificador e nome**
2. **Status:** `DRAFT`
3. **Problema e objetivo**
4. **Referências e decisões aprovadas:** caminhos/seções relevantes nos documentos do projeto
5. **Escopo**
6. **Exclusões**
7. **Comportamentos esperados**
8. **Regras de negócio**
9. **Critérios de aceite verificáveis:** numerados, observáveis e sem termos vagos
10. **Fluxos principais e alternativos:** incluindo cancelamento quando aplicável
11. **Estados de erro e casos-limite**
12. **Persistência e privacidade:** requisitos aplicáveis; se não aplicável, declarar isso
13. **Dependências e restrições técnicas:** sem fixar versões não verificadas
14. **Questões ainda não resolvidas:** impacto, responsável pela decisão e bloqueio causado

Não transforme critérios incertos em compromissos. Ao terminar, informe o caminho criado, ambiguidades que exigem decisão e que a especificação continua em `DRAFT`, portanto não está autorizada para implementação.
