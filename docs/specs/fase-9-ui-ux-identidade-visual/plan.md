# Revisão técnica — Fase 9: UI/UX e identidade visual

- **Feature ID:** `fase-9-ui-ux-identidade-visual`
- **Especificação:** `spec.md`, status `APPROVED` por `v1-ui/ux` em 19/11/2026, conforme registro no documento.
- **Tarefas/evidências:** [tasks.md](tasks.md) e [reports](reports/).
- **Referências de processo:** `docs/specs/README.md` e `.github/prompts/review-plan.prompt.md`.
- **Referências de produto/arquitetura:** `docs/01-PRD.md`, `docs/02-USER-FLOWS.md`, `docs/03-SDD.md`, `docs/04-ROADMAP.md` e Specs 1–8.
- **Base revisada:** árvore integrada `40b362c` e aprovação de produto subsequente `9e2f040`.

## Problemas concretos encontrados e decisão

1. **Status documental não acompanhava evidência técnica.** TASK-02, TASK-05 e TASK-07 tinham relatórios de implementação/verificação, mas apareciam `PENDING`; TASK-08/09 apareciam `IN_PROGRESS` apesar dos relatórios e testes concluídos. TASK-14 tinha validação documentada, mas status `PENDING`. Corrigi a tabela de status usando apenas evidência registrada. TASK-10 continua `PENDING` porque seu próprio relatório documenta que não houve RED comportamental; nenhum status de aceite foi inferido dessa lacuna.
2. **Aceite global não pode ser inferido da regressão.** TASK-12 passou a suíte instrumentada e build, mas seu relatório limita explicitamente o alcance: não declara AC-01–AC-18 globalmente aceitos, não houve inspeção manual populada em todas as áreas nem uso de TalkBack real e não sana o RED ausente de TASK-10. Esses limites ficam preservados na matriz em `RETOMADA.md`.
3. **Dependências/cobertura.** A sequência proposta está coerente com a arquitetura observada: Room/Flow e `HabitCalendar` como fonte de previsões, prazo civil compartilhado por RF-06, navegação própria, e modelos/estatísticas de treino separados da UI. As dependências de TASK-12/TASK-14 são satisfeitas no código integrado e seus relatórios, mas aceitação final ainda depende de TASK-10 e da revisão das limitações de acessibilidade/fluxos populados.

Não alterei requisitos nem introduzi dependências. A decisão aprova o **plano técnico**, não declara a fase `VERIFIED` e não substitui validações das tarefas.

## Cobertura, dependências e riscos

- **AC-01:** tokens semânticos centralizados e teste de contraste em TASK-01.
- **AC-02–03:** cinco destinos, rótulos/alvos, retorno de Histórico e sessão preservada em TASK-02; regressão da navegação consta em TASK-12/14.
- **AC-04–08:** Home, preferências locais, listas limitadas/ordenadas, conclusão/Undo, classificação civil e grade acessível em TASK-04/05/13/14.
- **AC-09:** grupos de tarefas, datas sem prazo e detalhes em TASK-07 e regressão TASK-12.
- **AC-10–11:** entidades/Room/migrações e estatísticas cobertas por TASK-08/09; UI e uso independente do modelo estão em TASK-10, ainda pendente de RED comportamental.
- **AC-12:** calendário derivado de ocorrências e ações persistentes em TASK-11.
- **AC-13–15:** Bottom Sheets, estados vazios, rolagem, perfis 360/412 dp e escala 1.3 em TASK-05/06/07/10/11/12/14. Evidência manual populada e TalkBack real não estão completos; a matriz registra essa limitação.
- **AC-16:** evidência integrada registrada em TASK-12. Nesta revisão executei novamente unitários e `assembleDebug` (ambos `BUILD SUCCESSFUL`); não executei instrumentados por reserva do AVD para TASK-10.
- **AC-17–18:** planos semanais, preservação de períodos, fechamento e correção retroativa em TASK-13/14, com testes unitários e instrumentados de persistência reportados.

Riscos residuais: TASK-10 precisa fechar sua evidência TDD; inspeção manual de conteúdo populado e acessibilidade com leitor de tela real permanece pendente; os relatórios instrumentados são históricos e não foram repetidos nesta revisão porque o AVD está reservado. Esses itens impedem declarar a fase concluída, mas não inviabilizam a arquitetura, ordem ou testabilidade do plano.

## Validação executada nesta revisão

| Comando | Resultado |
|---|---|
| `ANDROID_HOME=C:\Users\sal\AppData\Local\Android\Sdk .\gradlew.bat :app:testDebugUnitTest :app:assembleDebug` | `BUILD SUCCESSFUL`; tarefas Gradle estavam `UP-TO-DATE`. |
| `git diff --check` | Sem erros de whitespace (a executar após edição). |
| `:app:connectedDebugAndroidTest` | Não executado nesta revisão: AVD reservado exclusivamente à TASK-10, conforme instrução. TASK-12 reporta 54/54 no integrado `40b362c`. |

## Decisão

**APROVADO.** A especificação agora tem aprovação explícita registrada; arquitetura, cobertura e sequência são compatíveis com a base e não exigem nova dependência. As lacunas acima são condições de verificação/fechamento, não mudanças de plano. Manter a fase em `IN_PROGRESS` enquanto TASK-10 e as verificações residuais estiverem abertas; não marcar `VERIFIED` até toda evidência exigida estar concluída e revisada.

## Reavaliação de evidências (19/11/2026)

A integração posterior acrescentou reprodução retrospectiva de RED para TASK-10 (falha comportamental na base `7b75eff`; GREEN em `5b4ec98`, 51/51 instrumentados), inspeção populada com TalkBack ativo e correção de alvos responsivos em Treinos/Cabelo. A lacuna histórica permanece: RED não foi executado antes da implementação original. Os testes focados em 360 dp/font scale 1.3 confirmaram os alvos corrigidos; a suíte integrada e o estado final dos comandos desta reavaliação são registrados no adendo de [TASK-12](reports/TASK-12.md).

**Decisão técnica mantida: APROVADO.** A revisão destas evidências não altera requisitos nem a aprovação de produto da especificação. As inconsistências que impediam fechar TASK-10 e alguns alvos de toque foram resolvidas; o aceite técnico da fase só pode ser elevado quando a suíte final passar e a cobertura AC-01–AC-18 estiver reconciliada. Aprovação da especificação não equivale a aceite de implementação pelo usuário.
