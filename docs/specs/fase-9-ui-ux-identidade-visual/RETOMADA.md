# Retomada da Spec 9

## Estado técnico atual

- A especificação e o plano técnico têm aprovação registrada em 19/11/2026 (`9e2f040` e decisão em [plan.md](plan.md)). Essa aprovação é de escopo/plano, não aceite do produto implementado pelo usuário.
- TASK-01–09 e TASK-11–14 estão `VERIFIED`; TASK-10 foi reconciliada para `VERIFIED` tecnicamente pela reprodução **retrospectiva** registrada no próprio relatório: comportamento RED na base `7b75eff`, GREEN em `5b4ec98` (51/51). O RED não ocorreu na cronologia pré-implementação original.
- A integração nesta revisão encontrou lacunas reais de responsividade/acessibilidade em Treinos e Cabelo, corrigidas e cobertas por teste. Evidência populada com leitor de tela e validações finais estão no adendo de [TASK-12](reports/TASK-12.md); não editamos o relatório TASK-10.
- O estado global fica **tecnicamente verificado, aguardando aceite explícito de produto**. Não retrodatamos aprovação nem afirmamos validação manual de toda combinação/tela ou avaliação humana de áudio.

## Matriz de aceite AC-01–AC-18

| AC | Cobertura/evidência reconciliada | Limite conhecido |
|---|---|---|
| AC-01 | Tokens, contraste e regressão TASK-01/12. | Combinações cromáticas não foram medidas manualmente em cada tela. |
| AC-02 | Cinco destinos, rótulos e seleção em TASK-02/12/14. | Sem teste em aparelho físico. |
| AC-03 | Retorno de Histórico e preservação da sessão em TASK-02/10/12/14. | RED de TASK-10 foi reproduzido retrospectivamente, não antes da implementação original. |
| AC-04 | Saudação, nome local, criação e formulários TASK-05/14; inspeção populada da Home. | Nenhum aceite de produto global inferido. |
| AC-05 | Limite, totais, prioridades e omissões em TASK-05/14. | Validado automatizadamente e em conteúdo populado coberto na Home. |
| AC-06 | Conclusão, Undo, detalhes, histórico e retorno/rolagem em TASK-05/06/14; conteúdo populado da Home inspecionado. | Sem uso em aparelho físico. |
| AC-07 | Prazo civil RF-06, tarefas sem prazo e virada em TASK-05/07/12. | Evidência registrada nos relatórios e regressão. |
| AC-08 | Grade de 7 dias, proporções, correção e semântica em TASK-04/05/13/14. | Leitor de tela verificado em componentes amostrados; áudio não foi avaliado por ouvinte humano. |
| AC-09 | Agrupamento, ordem, prazo, filtro e painel de detalhes TASK-07/12; tela populada inspecionada. | Sem aparelho físico. |
| AC-10 | Room/migração/CRUD TASK-08; reprodução RED/GREEN e regressão de UI TASK-10/12. | Preservada distinção da reprodução retrospectiva. |
| AC-11 | Regras, ViewModel e histórico TASK-09/10/12; reprodução RED/GREEN retrospectiva. | Nenhuma alegação de RED histórico pré-implementação. |
| AC-12 | Calendário derivado de ocorrências e ações TASK-11/12. | Inspeção manual populada e semântica TalkBack incluídas nesta revisão; não cobre aparelho físico. |
| AC-13 | Sheets, date picker, confirmações, Undo e detalhes TASK-05–07/10–12. | Cobertura direta depende dos casos descritos em cada relatório. |
| AC-14 | Estados vazio/erro/conteúdo, ações válidas e mensagens TASK-03/12/14. | Cobertura visual manual inclui estados vazios e alguns fluxos populados, não todas as áreas. |
| AC-15 | Testes/inspeção em 360/412 dp e escala 1.3; modelo de treino populado e calendário capilar populado; foco, labels, ações e alvos verificados com TalkBack ativo. Correções: ação “Criar modelo” visível; ações de modelo ≥48 dp sem colisão; células capilares ≥48 dp. | TTS recebeu solicitações pt-BR e houve foco acessível; não se avaliou inteligibilidade sonora por pessoa, nem todas as telas/gestos com TalkBack ou dispositivo físico. |
| AC-16 | Unitários, regressão instrumentada 54/54 e `assembleDebug` da execução final TASK-12/adendo. | Suíte instrumentada requer perfil padrão e TalkBack desligado; foco de acessibilidade foi inspecionado separadamente. |
| AC-17 | Dias preferidos, ocorrência fora do plano e histórico preservado TASK-06/13/14. | Cobertura automatizada conforme relatórios. |
| AC-18 | Fechamento idempotente e correção retroativa TASK-13/14. | Migração não inventa previsão histórica para dados legados; limite compatível e documentado. |

## Evidência manual de AC-15

No AVD `Medium_Phone_API_37.0` foram exercitados fluxos com dados em Treinos, Cabelo, Hábitos e Tarefas. TalkBack foi ativado; árvore de acessibilidade exibiu labels/descrições e ações, o foco visual acompanhou navegação, e os logs registraram solicitação de síntese pt-BR. Inspeção verificou calendário com ocorrências e ações, formulário e modelo de treino com ações separadas; a suíte direcionada provou alvos mínimos de 48 dp em 360 dp/font scale 1.3. As capturas e detalhes da execução estão vinculados no adendo de TASK-12.

## Próximo passo

Não há lacuna técnica conhecida bloqueando a conclusão desta revisão. Aguardamos aceite explícito do usuário para considerar a fase aprovada como produto; essa decisão não pode ser inferida da aprovação da spec/plano, nem registrada retroativamente. O AVD foi restaurado ao perfil padrão, TalkBack desativado e desligado/liberado ao final.

[Plano técnico](plan.md) · [Tarefas e dependências](tasks.md)
