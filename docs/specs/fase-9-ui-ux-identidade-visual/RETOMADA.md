# Retomada da Spec 9

## Estado atual

- A especificação foi aprovada por `v1-ui/ux` em 19/11/2026 no commit `9e2f040`.
- O plano técnico foi revisado e aprovado nesta rodada; ver [plan.md](plan.md).
- Os status de TASK-01–09 e TASK-11–14 foram reconciliados com relatórios e evidências. TASK-10 segue `PENDING`: seu relatório confirma ausência de RED comportamental pré-implementação. A fase não está `VERIFIED`.
- A suíte instrumentada integrada mais recente está registrada em [TASK-12](reports/TASK-12.md): 54/54 testes no AVD API 37. Esta rodada não usou emulador, pois está reservado à TASK-10. Foram executados `:app:testDebugUnitTest :app:assembleDebug` nesta árvore; ambos passaram (`UP-TO-DATE`).

## Matriz de aceite AC-01–AC-18

“Cobertura técnica” abaixo resume os relatórios existentes; “limite/fechamento” impede interpretar cobertura parcial como aceite global.

| AC | Resultado da auditoria | Evidência principal | Limite/fechamento |
|---|---|---|---|
| AC-01 | Coberto | TASK-01: tokens e teste de contraste; regressão em TASK-12. | Revalidado unitariamente nesta rodada; combinação efetiva completa não inspecionada manualmente em cada tela. |
| AC-02 | Coberto | TASK-02 e testes instrumentados da navegação em TASK-12/14. | Nenhuma lacuna registrada. |
| AC-03 | Coberto | TASK-02, preservação de sessão; TASK-12/14 navegação e retorno. | TASK-10 continua com lacuna de processo TDD, sem evidência de regressão funcional. |
| AC-04 | Coberto | TASK-05: saudação SP, nome local, menu Criar e formulários. | Capturas e testes registrados; sem aparelho físico. |
| AC-05 | Coberto | TASK-05/14: limite de três, totais, prioridades e omissões. | Cobertura em testes unitários/instrumentados. |
| AC-06 | Coberto | TASK-05/06/14: conclusão, Undo, detalhes, histórico e retorno/rolagem. | Cenários com dados exercitados por testes; inspeção manual populada da Home não registrada. |
| AC-07 | Coberto | TASK-05/07: prazo civil RF-06 e tarefas sem prazo; testes de virada. | O histórico de validação consta nos relatórios e regressão. |
| AC-08 | Coberto | TASK-04/05/13/14: sete dias, proporções, correção, semântica acessível e relógio controlado. | Testes/visual registrados; sem TalkBack real. |
| AC-09 | Coberto | TASK-07/12: grupos únicos, ordenação, sem prazo, filtro e ações. | Não houve inspeção visual manual de Tarefas nesta implementação (registrado em TASK-07). |
| AC-10 | Coberto funcionalmente | TASK-08 migration/CRUD; TASK-10 teste Compose e suíte instrumentada reportados. | TASK-10 pendente por falta de RED comportamental; não declarar TDD completo. |
| AC-11 | Coberto funcionalmente | TASK-09 testes de regra e integração com ViewModel; TASK-10 renderização/seleção e histórico. | Mesmo limite de TASK-10; não foi feita inspeção manual populada completa. |
| AC-12 | Coberto | TASK-11 testes de ViewModel/Compose e regressão 51/54 em execuções registradas. | A captura manual cobre rotina ausente; calendário populado coberto em teste instrumentado, sem TalkBack real. |
| AC-13 | Coberto | TASK-05/06/07/10/11 e testes de sheets/diálogos; TASK-12. | DatePicker/confirmação/Undo de tarefas mantidos, sem teste visual dedicado conforme TASK-07. |
| AC-14 | Coberto | TASK-03/12/14 estados vazios, ação válida, erro separado e mensagens acessíveis. | Inspeção manual visual reportada para estados vazios; fluxo populado limitado aos testes. |
| AC-15 | Parcialmente coberto | TASK-05/12/14: 360/412 dp, fonte 1.3, rolagem, alvos e semântica em testes/capturas. | Faltam inspeção manual populada em todas as áreas, leitor de tela real e aparelho físico; não alegar validação desses cenários. |
| AC-16 | Coberto nas execuções registradas | TASK-12: unitários, instrumentados 54/54 e build; esta rodada repetiu unitários/build com sucesso. | Instrumentados não repetidos agora por reserva do AVD. |
| AC-17 | Coberto | TASK-06/13/14: zero a sete dias preferidos, fato fora do plano e preservação histórica com testes. | Evidência automatizada, sem lacuna relatada. |
| AC-18 | Coberto | TASK-13/14: fechamento diário/semanal idempotente e correção retroativa acima da meta. | Migration não inventa previsão histórica; limite documentado e compatível com dados legados. |

## Próximas ações

1. TASK-10 deve resolver a lacuna de RED sem editar este arquivo de relatório por outro agente; preservar o relatório atual e acrescentar evidência rastreável apropriada quando o coordenador concluir a tarefa.
2. Completar inspeção manual em fluxos populados e acessibilidade relevante, sem afirmar teste com TalkBack/aparelho físico antes de executá-los. A autorização de uso do AVD continua exclusiva da TASK-10 até liberação do coordenador.
3. Após essas evidências, revisar a matriz e decidir o status geral. Aprovação de produto e aprovação técnica do plano não equivalem ao aceite de implementação.

[Plano técnico](plan.md) · [Tarefas e dependências](tasks.md)
