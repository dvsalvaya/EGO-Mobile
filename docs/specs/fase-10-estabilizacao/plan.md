# Plan — Spec 10: Estabilização, Correção de Bugs e Integridade Funcional

## 1. Objetivo do plano

Implementar os 14 bugs confirmados no `FIX.md`, com mudanças pequenas, rastreáveis e cobertas por testes. O plano prioriza estabilidade e integridade dos dados antes de completar os fluxos de hábitos.

**Decisões de produto já definidas:**
- Frequência `N vezes por semana`: qualquer `N` dias, sem dias fixos; o progresso é contado dentro da semana ISO (segunda a domingo).
- Escopo: todos os BUG-001 a BUG-014.
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
- Reproduzir BUG-001 e registrar evidência do crash, se o ambiente permitir.

Saída:
- Baseline documentado.
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
2. **BUG-006 — Atraso de tarefa:** comparar datas civis no fuso definido pelo projeto. Evitar comparar diretamente um `dueAt` normalizado para meia-noite com o timestamp atual.
3. **BUG-007 — Frequência semanal:** substituir a regra baseada em `dayOfWeek <= count` por contagem de ocorrências concluídas na semana ISO atual. Criar uma única regra de domínio reutilizável por Home, Hábitos e estatísticas.

Testes:
- Treino ativo, pausado, retomado e com pausas múltiplas; sessão recuperada após morte do processo.
- Tarefa vencendo hoje, ontem e amanhã, incluindo virada de dia.
- Frequência `1x`, `3x` e outros valores suportados: 0/N, N-1/N, N/N; conclusão em dias quaisquer; troca de semana; desfazer ocorrência.
- Garantir que ocorrências antigas continuem no histórico.

Gate:
- Home e Hábitos concordam sobre elegibilidade e progresso.
- Nenhuma ocorrência histórica é apagada ou reinterpretada indevidamente.

### Fase 3 — Módulo de hábitos: validação e estado

**Objetivo:** corrigir feedback e ações já esperadas pelo módulo.

1. **BUG-008:** estado de erro inline para nome vazio/whitespace.
2. **BUG-009:** aplicar `selected` ao componente de escolha de frequência e expor estado semântico.
3. **BUG-010:** fornecer ao cartão o estado/progresso de conclusão correspondente ao período; implementar desfazer com atualização imediata e sem duplicação.
4. **BUG-011:** reorganizar as ações do cartão para que conclusão rápida e pausa coexistam; exibir retomar para hábitos pausados.

Testes:
- Estados de erro e recuperação do campo.
- Seleção visual e valor persistido.
- Conclusão/desfazer em hábitos diários e semanais.
- Pausar/retomar e verificar listas, estado e persistência.

Gate:
- Nenhuma ação essencial fica inacessível.
- O estado mostrado na UI corresponde ao estado persistido.

### Fase 4 — Navegação, layout e funcionalidades ausentes

**Objetivo:** restaurar estado de navegação e completar o escopo declarado na Spec 3.

1. **BUG-012:** salvar/restaurar a aba atual pelo mecanismo de estado salvo do Compose/Android, respeitando a implementação de navegação existente.
2. **BUG-013:** corrigir largura, quebra de linha e dimensionamento do chip `3x/sem`.
3. **BUG-014:** implementar:
   - edição de nome/descrição;
   - ação de arquivar;
   - alteração de frequência com diálogo de confirmação;
   - preservação das ocorrências históricas;
   - feedback de sucesso/erro/cancelamento.

A implementação deve reutilizar componentes e casos de uso existentes quando isso não gerar acoplamento inadequado. Conectar `onUpdateFrequency` se o contrato existente for adequado; caso contrário, ajustar o contrato e os testes em vez de criar caminho paralelo.

Testes:
- Rotação em cada aba.
- Chip em larguras e fontes suportadas.
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
- Repetir os 12 cenários mínimos de regressão da Spec.
- Verificar persistência após force-stop e reabertura.
- Se houver mudança no schema Room, testar migração de banco existente.
- Gerar relatório de implementação com evidências.

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

Antes de adicionar campos ao Room, verificar entidades, DAOs, versões e migrações existentes. Para sessões antigas, definir defaults compatíveis que não alterem indevidamente durações históricas. Não investigar ou alterar a estratégia `REPLACE`/`CASCADE` associada a SUS-001 nesta Spec, salvo se uma correção dos 14 bugs demonstrar dependência direta; nesse caso, documentar a dependência e limitar a mudança ao necessário.

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
5. `fix(tasks): treat due-today tasks as not overdue`
6. `fix(habits): calculate weekly frequency by completion count`
7. `fix(habits): expose validation and completion states`
8. `fix(habits): make pause and resume actions reachable`
9. `fix(navigation): restore selected tab after recreation`
10. `fix(habits): implement edit archive and frequency update`
11. `test(app): add spec 10 regression coverage`

Os commits são sugestões; seguir o padrão real do repositório se for diferente.

## 7. Definição de pronto

- [ ] Todos os 14 bugs corrigidos.
- [ ] Testes de regressão adicionados para cada regra corrigida.
- [ ] `gradlew test` aprovado.
- [ ] `gradlew lint` aprovado.
- [ ] `gradlew assembleDebug` aprovado.
- [ ] Testes instrumentados disponíveis executados ou limitação registrada.
- [ ] Persistência e histórico preservados.
- [ ] Nenhuma suspeita SUS-001 a SUS-008 declarada resolvida por inferência.
- [ ] Relatório final com evidências, arquivos alterados e limitações.
