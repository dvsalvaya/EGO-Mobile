# Verificação — Fase 9 / TASK-05

- **Tarefa:** Atualizar a Home da V1
- **Status:** `VERIFICADO_UNITARIAMENTE`
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
- **GREEN:** `:app:testDebugUnitTest` — `BUILD SUCCESSFUL` (174 testes).
- **Compilação Android:** `:app:compileDebugAndroidTestKotlin` — `BUILD SUCCESSFUL`, incluindo a atualização dos testes instrumentados da Home para o menu “Criar”.
- Cobertura adicionada/ajustada: contagens e limite de três itens, ordenação civil de vencidas/hoje, omissão de datas futuras/sem prazo, remoção de hábitos concluídos ou meta atingida, saudação por horário e emissão do evento de desfazer.

## Limitações

- Nenhum teste instrumentado foi executado: o `emulator-5554` continua reservado pela TASK-06, conforme instrução. Os testes Android apenas compilaram; validação visual, rolagem real e interação Compose aguardam o emulador liberado.
- Os formulários existentes continuam sendo reutilizados pelo contrato confirmado; esta tarefa não alterou sua implementação visual compartilhada.
