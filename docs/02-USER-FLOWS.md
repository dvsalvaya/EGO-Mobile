# 02 — Fluxos de usuário: EGO Mobile

## 1. Navegação principal

A aplicação usa uma barra inferior com cinco destinos. A composição exata dos rótulos e ícones pode ser refinada visualmente, mas as áreas funcionais devem cobrir:

1. **Início** — resumo equilibrado e ações rápidas.
2. **Hábitos** — hábitos ativos, pausados e gestão de frequência.
3. **Tarefas** — tarefas abertas, prazos e reagendamento.
4. **Rotinas** — treinos e cuidados capilares.
5. **Histórico** — registros anteriores e consulta de consistência.

O histórico deve ser uma área própria. Evitar transformar a tela inicial em um feed longo de todos os registros.

## 2. Início

### Objetivo
Dar ao usuário uma visão útil do estado atual sem sobrecarregar a tela.

### Conteúdo
- Resumo de hábitos relevantes para o dia.
- Resumo de tarefas pendentes e atrasadas.
- Indicação de treino/rotina em andamento, quando existir.
- Atalho de criação de hábito.
- Atalho de criação de tarefa.

### Regras
- Priorizar informações acionáveis.
- Tarefas vencidas devem usar destaque vermelho, sem depender apenas da cor para comunicar o estado.
- Hábitos pausados devem estar identificados como pausados e não aparecer como se fossem ativos.
- Não adicionar notificações ou lembretes no MVP.

## 3. Fluxo de hábitos

### Criar hábito
1. Usuário toca em “Criar hábito”.
2. Informa nome e dados obrigatórios.
3. Define uma frequência flexível.
4. Salva.
5. O hábito aparece na lista de hábitos ativos e nos resumos aplicáveis.

### Registrar conclusão
1. Usuário seleciona o hábito.
2. Registra a ocorrência como concluída.
3. O aplicativo salva o registro localmente.
4. A interface atualiza o estado e o histórico.

O produto deve suportar tanto uma ação rápida de conclusão quanto um registro manual no fluxo de detalhes, conforme aplicável.

### Pausar e retomar
1. Usuário abre as opções do hábito.
2. Seleciona pausar.
3. O hábito passa a mostrar claramente o estado pausado.
4. Ao retomar, o usuário reativa o hábito sem apagar o histórico anterior.

### Alterar frequência
1. Usuário edita a frequência.
2. O app pergunta como aplicar a mudança.
3. O usuário escolhe uma opção apresentada pela interface.
4. A regra nova vale conforme a opção escolhida.
5. Registros passados não são reescritos silenciosamente.

### Ocorrência não cumprida
1. O app identifica ou apresenta uma ocorrência pendente de decisão.
2. Pergunta ao usuário como deseja tratá-la.
3. Não classifica automaticamente como falha sem a decisão prevista pelo fluxo.
4. Registra a escolha para manter o histórico coerente.

## 4. Fluxo de tarefas

### Criar tarefa
1. Usuário toca em “Criar tarefa”.
2. Informa o título e os campos necessários.
3. Define prazo se desejar; prazo é opcional.
4. Salva.
5. A tarefa aparece na lista de tarefas abertas.

### Concluir tarefa
1. Usuário marca a tarefa como concluída.
2. O app registra a conclusão.
3. A tarefa deixa de aparecer na lista padrão de abertas.
4. A tarefa pode ser consultada na área de concluídas/histórico.

### Reagendar tarefa atrasada
1. Tarefa vencida aparece com destaque vermelho e indicação textual.
2. Usuário abre a tarefa.
3. Escolhe reagendar.
4. Define novo prazo.
5. O app atualiza o prazo sem apagar a informação histórica necessária.

### Excluir tarefa
1. Usuário escolhe excluir.
2. O app pede confirmação.
3. Se confirmado, remove a tarefa da visualização ativa.
4. Quando possível, oferece desfazer a exclusão.
5. Cancelar a confirmação mantém a tarefa intacta.

## 5. Fluxo de treinos

### Iniciar
1. Usuário abre a área de rotinas/treinos.
2. Inicia um treino.
3. Pode registrar exercícios e dados manualmente.
4. O treino passa para estado em andamento.

### Pausar e retomar
1. Usuário pausa ou sai da tela.
2. O estado atual é persistido localmente.
3. Ao retornar, pode retomar o mesmo treino.
4. O app não deve criar uma sessão duplicada por engano.

### Finalizar
1. Usuário encerra o treino.
2. O app salva o registro final.
3. O treino fica disponível no histórico.

## 6. Fluxo de cuidados capilares

1. Usuário abre cuidados capilares.
2. Escolhe criar uma rotina personalizada ou partir de uma predefinição.
3. Define os cuidados e a frequência aplicável.
4. Registra quando realmente executa o cuidado.
5. Se necessário, pode reagendar.
6. O registro aparece no histórico.

A predefinição é um ponto de partida editável, não uma regra obrigatória.

## 7. Histórico

- O usuário abre o destino Histórico pela navegação inferior.
- Consulta registros de hábitos, tarefas concluídas, treinos e cuidados capilares.
- O histórico deve distinguir execução real, ocorrência pendente e estados que o usuário classificou.
- O histórico não deve depender de conexão com a internet.

## 8. Exclusão e segurança de dados

- Ações destrutivas devem pedir confirmação quando houver risco de perda.
- Para exclusões reversíveis, preservar temporariamente os dados necessários para desfazer.
- Não afirmar que uma exclusão é reversível se o mecanismo não estiver implementado.
- Como o MVP não tem backup, a interface não deve sugerir que os dados estão sincronizados ou protegidos na nuvem.

## 9. Estados de interface que precisam ser cobertos

- Lista vazia.
- Carregamento inicial, quando aplicável.
- Erro ao salvar ou carregar.
- Hábito ativo.
- Hábito pausado.
- Tarefa atrasada.
- Tarefa concluída.
- Treino em andamento.
- Rotina sem registros.
- Confirmação de exclusão.
- Confirmação de alteração de frequência.

## 10. Critério geral de fluxo

Um fluxo está pronto quando o usuário consegue concluí-lo, cancelar quando aplicável, reabrir o app sem perder dados e entender o estado resultante sem depender de explicação externa.
