# Plan — Spec 10: Estabilização, Correção de Bugs e Integridade Funcional

## 1. Objetivo do plano

Recuperar a compilação, revalidar os achados do `FIX.md` e corrigir os problemas funcionais ainda presentes. A implementação da nova UI parte do build estabilizado.

**Regras e decisões a registrar**
- Frequência `N vezes por semana`: qualquer `N` dias, sem dias fixos; o progresso é contado dentro da semana ISO (segunda a domingo).
- Prazo de tarefa: comparar datas civis em America/Sao_Paulo, conforme RF-06 e AC-07 da Spec 9.
- Seleção visual de frequência e legibilidade do rótulo semanal: levar RF-09 e RF-13 para a tarefa de Hábitos da Spec 9.
- SUS-001 a SUS-008: fora desta Spec; serão investigados separadamente.

## 2. Princípios de implementação

1. Reproduzir ou criar teste de regressão antes de alterar o comportamento.
2. Corrigir a causa na camada responsável; não mascarar falhas apenas na UI.
3. Preservar a arquitetura e convenções existentes.
4. Não apagar dados históricos para simplificar mudanças.
5. Manter `minSdk = 24` e resolver `java.time` com Core Library Desugaring.
6. Não ampliar o escopo para as oito suspeitas do relatório.
7. Executar testes direcionados após cada grupo e a suíte completa ao final.

## 3. Sequência de trabalho

### Fase 0 — Preparação e baseline

**Objetivo:** garantir que a implementação parte de uma árvore compreendida e que os resultados podem ser comparados.

Atividades:
- Conferir branch, commit e estado do working tree.
- Ler as Specs relevantes (principalmente Specs 3, 4, 7 e 9) e os arquivos indicados no `FIX.md`.
- Confirmar como os testes são organizados e quais comandos instrumentados estão disponíveis.
- Executar `test`, `lint` e `assembleDebug` para registrar baseline.
- Corrigir `:app:compileDebugKotlin` (RF-00) e recompilar até gerar um APK debug. Registrar os resultados de lint e dos testes separadamente.
- Revalidar os achados no novo baseline. Registrar quais continuam presentes, quais já estão atendidos e quais precisam de validação em dispositivo.

Saída:
- APK debug gerado e baseline documentado.
- Lista dos arquivos e contratos existentes que serão afetados.

### Fase 1 — Estabilidade P0/P1

**Objetivo:** eliminar crash crítico, crashes de validação e falha de lint.

Ordem sugerida:
1. **BUG-001:** remover a rolagem vertical aninhada em `WorkoutHistoryView` ou reorganizar os itens para existir apenas um container vertical rolável no mesmo eixo.
2. **BUG-002/003:** implementar validação explícita de nome e descrição no formulário e tratamento de falha no ViewModel; manter as invariantes do domínio.
3. **BUG-004:** habilitar Core Library Desugaring no módulo Android com versão compatível com o projeto, mantendo `minSdk = 24`.

Testes:
- Histórico com zero/um/vários itens e finalização de treino.
- Nome vazio, 1, 80 e 81 caracteres; descrição vazia, 250 e 251 caracteres.
- Criação de hábito pela Home e pela aba Hábitos.
- `test`, `lint` e `assembleDebug`.

Gate:
- Nenhum crash nos cenários P0/P1.
- Lint sem os erros `NewApi` reportados.

### Fase 2 — Regras de domínio e datas

**Objetivo:** corrigir dados calculados incorretamente.

1. **BUG-005 — Duração de treino:** definir representação persistível de pausa (por exemplo, `pausedAt` e duração pausada acumulada, ou modelo equivalente). Avaliar compatibilidade com sessões existentes e defaults para registros antigos. Garantir que o cálculo seja determinístico e testável com relógio controlado.
2. **RF-06 — Atraso de tarefa:** aplicar a regra por data civil e validar a mesma classificação na Home e em Tarefas.
3. **BUG-007 — Frequência semanal:** substituir a regra baseada em `dayOfWeek <= count` por contagem de ocorrências concluídas na semana ISO atual. Criar uma única regra de domínio reutilizável por Home, Hábitos e estatísticas.

Testes:
- Treino ativo, pausado, retomado e com pausas múltiplas; sessão recuperada após morte do processo.
- Tarefa vencendo hoje, ontem e amanhã, incluindo virada de dia e prazo ausente.
- Frequência `1x`, `3x` e outros valores suportados, incluindo troca de semana, registro retroativo, concorrência pelo último registro elegível e desfazer ocorrência.
- Garantir que ocorrências antigas continuem no histórico.

Gate:
- Home e Hábitos concordam sobre elegibilidade e progresso.
- Nenhuma ocorrência histórica é apagada ou reinterpretada indevidamente.

### Fase 3 — Módulo de hábitos: validação e estado

**Objetivo:** corrigir feedback e ações já esperadas pelo módulo.

1. **BUG-008:** estado de erro inline para nome vazio/whitespace.
2. **BUG-010:** fornecer à tela o estado/progresso do período e implementar desfazer com atualização imediata e sem duplicação.
3. **BUG-011:** disponibilizar conclusão rápida e pausa no fluxo de hábitos ativos e retomar no fluxo de pausados.

Testes:
- Estados de erro e recuperação do campo.
- Conclusão/desfazer em hábitos diários e semanais.
- Pausar/retomar e verificar listas, estado e persistência.

Gate:
- Nenhuma ação essencial fica inacessível.
- O estado mostrado na UI corresponde ao estado persistido.

### Fase 4 — Navegação e funcionalidades ausentes

**Objetivo:** restaurar estado de navegação e completar o escopo declarado na Spec 3.

1. **BUG-012:** salvar/restaurar a aba atual pelo mecanismo de estado salvo do Compose/Android, respeitando a implementação de navegação existente.
2. **BUG-014:** implementar:
   - edição de nome/descrição;
   - ação de arquivar;
   - alteração de frequência com diálogo de confirmação;
   - preservação das ocorrências históricas;
   - feedback de sucesso/erro/cancelamento.

A implementação deve reutilizar componentes e casos de uso existentes quando isso não gerar acoplamento inadequado. Conectar `onUpdateFrequency` se o contrato existente for adequado; caso contrário, ajustar o contrato e os testes em vez de criar caminho paralelo.

Testes:
- Rotação em cada destino efetivamente disponível.
- Editar e cancelar edição.
- Alterar frequência, confirmar e cancelar.
- Arquivar hábito ativo e pausado; verificar que não pode ser concluído e que o histórico permanece.

Gate:
- Funcionalidades de edição, alteração de frequência e arquivamento acessíveis pela UI e persistidas.

### Fase 5 — Regressão completa e entrega

**Objetivo:** provar que os fixes não introduziram regressões.

Atividades:
- Executar suíte unitária completa.
- Executar lint e assembleDebug.
- Executar testes instrumentados disponíveis.
- Executar os cenários aplicáveis de regressão da Spec 10 no build estabilizado.
- Verificar persistência após force-stop e reabertura.
- Se houver mudança no schema Room, testar migração de banco existente.
- Gerar relatório de implementação com evidências.
- Registrar para a integração da Spec 9 (TASK-12) a repetição dos cenários nas telas novas, com foco em Hábitos, Treinos e restauração de navegação.

Gate final:
- Todos os critérios da Spec atendidos.
- Nenhum dos SUS-001 a SUS-008 marcado como corrigido sem investigação específica.

## 4. Orientações técnicas por área

### 4.1 Compose e rolagem

Antes de alterar a UI, verificar a árvore de composição e o eixo de rolagem. Preferir um único `LazyColumn` como dono da rolagem vertical; conteúdo interno simples pode usar `Column` quando o pai já controla a rolagem. Não aplicar altura fixa arbitrária apenas para suprimir a exceção.

### 4.2 Validação

A UI deve orientar o usuário, mas o domínio continua sendo a última barreira de integridade. ViewModels devem converter erros esperados de validação em estado de UI, sem deixar exceções de entrada escaparem de coroutines.

Limites:
- Nome: 1–80 caracteres após `trim`.
- Descrição: até 250 caracteres.

### 4.3 Tempo e datas

Usar abstrações de relógio/data já existentes no projeto, se disponíveis. Se não existirem, introduzir a menor abstração necessária para testes determinísticos. Reutilizar o fuso oficial adotado pelo app; não introduzir fuso diferente em telas isoladas.

### 4.4 Frequência semanal

Criar uma regra de domínio única para:
- início/fim da semana;
- contagem de ocorrências concluídas;
- progresso `completedCount/targetCount`;
- elegibilidade para conclusão;
- transição de semana.

Não usar o número do dia da semana como substituto da contagem. Não criar dias fixos nem sorteados.

### 4.5 Persistência e compatibilidade

O Room está na versão 6 na árvore revisada. Se a pausa exigir alteração de schema, adicionar a migração 6→7 e verificar também a cadeia 5→6→7. Preservar treinos, exercícios, modelos e demais dados existentes. Usar o cálculo de duração no histórico e nas estatísticas, com defaults compatíveis para sessões antigas sem dados de pausa.

## 5. Dependências e riscos

| Item | Dependência/Risco | Mitigação |
|---|---|---|
| BUG-001 | Estrutura de listas da tela de Rotinas | Teste de UI e inspeção da árvore Compose |
| BUG-002/003/008 | Validações compartilhadas entre Home e Hábitos | Unificar regra e testar ambos os caminhos |
| BUG-004 | Versões de AGP/Gradle/desugaring | Consultar compatibilidade com versões já usadas no projeto |
| BUG-005 | Modelo persistido de sessão | Planejar defaults/migração e testar retomada |
| BUG-006/007 | Fuso e definição de semana | Usar fonte única de data/relógio e testes de fronteira |
| BUG-010/011/014 | Estado e ações do HabitCard | Definir modelo de UI único e evitar callbacks mutuamente excludentes |
| BUG-012 | Estratégia atual de navegação | Preservar API e testar recriação real da Activity |
| BUG-014 | Estado `ARCHIVED` e caso de uso existente | Reutilizar domínio/repositório; não excluir registros |

## 6. Estratégia de commits sugerida

Manter commits pequenos e agrupados por comportamento, por exemplo:

1. `fix(workouts): prevent nested lazy list crash`
2. `fix(habits): validate habit input safely`
3. `build(android): enable core library desugaring`
4. `fix(workouts): exclude paused time from duration`
5. `fix(tasks): align overdue classification with due dates`
6. `fix(habits): calculate weekly frequency by completion count`
7. `fix(habits): expose validation and completion states`
8. `fix(habits): make pause and resume actions reachable`
9. `fix(navigation): restore selected tab after recreation`
10. `fix(habits): implement edit archive and frequency update`
11. `test(app): add spec 10 regression coverage`

Os commits são sugestões; seguir o padrão real do repositório se for diferente.

## 7. Definição de pronto

- [ ] APK gerado e requisitos aplicáveis revalidados ou corrigidos.
- [ ] RF-06 validado e critérios de RF-09/RF-13 encaminhados para a Spec 9.
- [ ] Testes de regressão adicionados para cada regra corrigida.
- [ ] `gradlew test` aprovado.
- [ ] `gradlew lint` aprovado.
- [ ] `gradlew assembleDebug` aprovado.
- [ ] Testes instrumentados disponíveis executados ou limitação registrada.
- [ ] Persistência e histórico preservados.
- [ ] Nenhuma suspeita SUS-001 a SUS-008 declarada resolvida por inferência.
- [ ] Relatório final com evidências, arquivos alterados e limitações.
