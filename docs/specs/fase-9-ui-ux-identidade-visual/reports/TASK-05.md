# Verificação — Fase 9 / TASK-05

- **Tarefa:** Atualizar a Home da V1
- **Status:** `VERIFICADO_COM_TESTES_INSTRUMENTADOS_E_VISUAL`
- **Contrato compartilhado:** confirmado com o coordenador: os atalhos preservam `onDismiss` e `onSave(name, description, frequency)` de `HabitFormDialog`; Home hospeda os formulários, salva pelos repositórios existentes e mantém a navegação e a rolagem. TASK-05 não alterou a API do formulário usado pela TASK-06.

## Implementação

- Saudação por horário civil de São Paulo, nome local editável em preferências do app e fallback para “Chefe”. O cabeçalho abre a edição pelo texto e pelo ícone acessível.
- A Home usa `HabitDayGrid.days` em ordem cronológica. A grade de sete datas mostra inicial do dia, estados com cores semânticas, contagens e descrição acessível; hoje tem contorno e previsão em aberto recebe “Em andamento”. “Ver histórico” mantém o callback da navegação existente.
- “Para hoje” mostra no máximo três hábitos e três tarefas com totais. A ordem de hábitos prioriza diário/dias fixos e dias preferidos da meta semanal, preservando a ordem estável dentro da prioridade. Hábitos concluídos ou com meta semanal atingida saem da lista; conclusão rápida oferece “Desfazer” por Snackbar. Toque na linha abre detalhes.
- Tarefas abertas vencidas ou com prazo hoje são classificadas por data civil em `America/Sao_Paulo`, atrasadas primeiro e da mais antiga à mais recente; futuras e sem prazo não entram no resumo. Cada lista mantém “Ver tudo”, contagem total e detalhes acionáveis.
- Sessão de treino ativa/pausada tem prioridade e ação “Continuar treino”. A etapa capilar prevista para hoje ou atrasada aparece em atalho “Abrir Cabelo”, inclusive quando há treino ativo ao mesmo tempo.
- O botão fixo “+” tem rótulo acessível “Criar” e opções “Novo hábito”/“Nova tarefa”. O estado vazio exibe “Tudo em dia”; o primeiro uso orienta a começar pelo “+”. Loading e erro continuam distintos. `SaveableStateHolder` e estado salvo da lista preservam rolagem ao alternar destinos e voltar do Histórico.

## TDD e evidências

- **RED:** o novo teste de totais/limites inicialmente não compilou porque `HomeSummary` ainda não expunha as contagens totais. Depois da alteração do contrato, os testes existentes revelaram tarefas com datas Unix antigas sendo incorretamente esperadas na Home; as expectativas foram atualizadas para a regra civil e o comportamento V1.
- **GREEN:** `:app:testDebugUnitTest` — `BUILD SUCCESSFUL` (174 testes); repetido na validação integrada.
- `:app:connectedDebugAndroidTest` — suíte instrumentada completa, 49/49, aprovada em 360 dp/fonte 1.0, 412 dp/fonte 1.0 e 412 dp/fonte 1.3. Cobertura inclui Home, histórico, hábitos, navegação, rotação, persistência e estados vazios.
- Build final `:app:assembleDebug` — `BUILD SUCCESSFUL`.
- Cobertura adicionada/ajustada: contagens e limite de três itens, ordenação civil de vencidas/hoje, omissão de datas futuras/sem prazo, remoção de hábitos concluídos ou meta atingida, saudação por horário e emissão do evento de desfazer.
- Capturas AVD: [Home 360 dp](evidence/TASK-14-home-360.png), [Home 412 dp](evidence/TASK-14-home-412.png), [Home 412 dp/fonte 1.3](evidence/TASK-14-home-412-font130.png).

## Limitações

- A inspeção visual encontrou cartões vazios da Home muito altos, com o conteúdo de estado vazio centralizado e a grade deslocada para baixo. O conteúdo continua acessível por rolagem e testes comprovam isso; o ajuste de densidade/altura visual ficou pendente.
- `TaskFormDialog` foi convertido para Bottom Sheet com conteúdo rolável e padding de IME; o contrato `onSave(title, description, dueAt)` foi preservado.
