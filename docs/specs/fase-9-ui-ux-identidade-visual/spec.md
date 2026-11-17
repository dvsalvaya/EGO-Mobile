# Fase 9 — UI/UX e identidade visual

## Identificador e status

- **ID:** `fase-9-ui-ux-identidade-visual`
- **Nome:** UI/UX e Identidade Visual do EGO Mobile
- **Status:** `DRAFT`
- **Aprovação:** Pendente de revisão explícita do responsável pelo produto.

Esta proposta especifica uma evolução visual e de navegação sobre a arquitetura existente. O status permanece `DRAFT`; não autoriza implementação até que a especificação e o plano técnico sejam aprovados conforme `docs/specs/README.md`.

## 1. Problema e objetivo

As telas atuais entregam os fluxos de negócio em tema escuro, mas usam o esquema Material 3 padrão sem tokens próprios, estilos locais diferentes, glifos Unicode como ícones e cards também em áreas internas. A navegação principal ainda agrupa Treinos e Cabelo sob Rotinas e apresenta Histórico como aba.

O objetivo desta fase é estabelecer uma identidade visual monocromática compartilhada e organizar o acesso às cinco áreas de produto como destinos principais, preservando as funcionalidades existentes e seus estados persistidos. A Home deve facilitar a leitura do dia e da consistência pessoal sem combinar hábitos e tarefas em uma única pontuação. As telas internas devem priorizar estruturas compactas e adequadas à função de cada área.

## 2. Referências e decisões aprovadas

- `docs/01-PRD.md`: local-first, consistência pessoal, cinco destinos, tema escuro, acessibilidade e requisitos de negócio.
- `docs/02-USER-FLOWS.md`: Home, hábitos, tarefas, treinos, cronograma capilar e Histórico.
- `docs/03-SDD.md`: Compose, MVVM pragmático, Room/Flow, `NavigationState` existente e testes.
- `docs/04-ROADMAP.md`: incrementos e critérios das fases.
- Specs 1–8 em `docs/specs/`: comportamentos de domínio, ciclos existentes, padrões de tarefa e critérios verificáveis.
- Direção visual aprovada nesta solicitação: minimalismo monocromático escuro, preto/branco/cinza com uso semântico pontual de cor, densidade equilibrada, superfícies modulares, animações discretas e gamificação sem XP/moedas/níveis.
- A referência Dropset informa linguagem visual escura e modular; seu layout não deve ser reproduzido. Não foi encontrada cópia versionada da captura em `docs`.

## 3. Estado atual auditado

- Aplicativo Android nativo em um único módulo `app`, Kotlin, Jetpack Compose e Material 3.
- MVVM pragmático: Composables, ViewModels com `StateFlow`, domínio/casos de uso, repositórios e Room/Flow; injeção manual.
- A navegação está em `AppNavigation`/`NavigationState` e usa `Scaffold`/`NavigationBar`, sem Navigation Compose.
- Destinos implementados: Início, Hábitos, Tarefas, Rotinas e Histórico. Rotinas contém abas internas Treinos e Cuidados capilares.
- `EgoApp` aplica `darkColorScheme()` sem paleta explícita. Não há escala de tokens compartilhados; paddings e raios são declarados nas telas. A navegação usa glifos Unicode.
- Home tem atalhos, hábitos previstos, tarefas abertas prioritárias e banner condicional de rotina; não tem saudação, métricas “Seu dia”, seção conjunta “Para hoje” ou grade de consistência.
- Hábitos tem listas ativa/pausada, frequência e conclusão rápida, mas não expõe edição na tela nem progresso resumido.
- Tarefas permite criar/editar/concluir/reagendar/excluir/desfazer e exibir concluídas; a lista não é agrupada em Atrasadas/Hoje/Próximas.
- Treinos tem sessão ativa, registro manual de exercícios e histórico com duração/contagem; não há modelos reutilizáveis nem estatísticas agregadas/progresso por exercício.
- Cabelo tem rotina, etapa atual, próximas etapas, execução, reagendamento e histórico; não há calendário mensal.
- Histórico é uma timeline interna em código, mas atualmente aparece como destino principal.
- Há componentes reutilizáveis de Home, tarefa, hábito, treino, cabelo, histórico e estados vazios; formulários atuais usam `AlertDialog`.
- O banco Room está na versão 5. A criação de modelos de treino persistidos exige uma migração compatível; não alterar entidades/migrações existentes sem testes.

## 4. Escopo

1. Definir design tokens semânticos para cores, tipografia, espaçamento, formas, superfícies, ícones e estados.
2. Atualizar a navegação inferior para Início, Hábitos, Tarefas, Treinos e Cabelo. Histórico permanece tela interna.
3. Atualizar a Home com saudação neutra, indicadores independentes, ações rápidas, listas do dia e grade de consistência de sete dias.
4. Organizar Hábitos e Tarefas em listas compactas e interativas, preservando os ciclos de negócio aprovados.
5. Tornar Treinos uma área principal; incluir modelos locais reutilizáveis, estatísticas de sessão e evolução por exercício, além de manter sessão/histórico atuais.
6. Tornar Cabelo uma área principal com calendário mensal, próximas etapas, execução, reagendamento e histórico.
7. Padronizar Bottom Sheets para criação/edição. Usar diálogos, confirmação, DatePicker, Snackbar ou navegação quando o fluxo exigir.
8. Definir estados de carregamento, vazio, erro e conteúdo; acessibilidade e adaptação de tela.
9. Criar/atualizar testes apenas para regras e comportamentos desta fase, seguindo RED → GREEN → REFACTOR quando houver implementação.

## 5. Exclusões

- Não alterar regras de negócio de hábitos, tarefas, sessões ou cuidados capilares fora do necessário para apresentar os fluxos aprovados.
- Não alterar a regra aprovada da Fase 4: tarefa aberta atrasada quando `dueAt < agora`. Não converter a comparação para data civil nem modificar dados existentes.
- Não adicionar login, nuvem, rede, sincronização, lembretes, telemetria, XP, moedas ou níveis.
- Não criar uma sexta aba principal. Histórico não é destino de navegação inferior.
- Não reproduzir o layout do Dropset nem adicionar bibliotecas de UI/ícones sem necessidade e aprovação.
- Não remover capacidades de treinos previstas nas Specs anteriores: criar modelos, iniciar sessão, registrar exercícios/séries manualmente, executar, pausar/retomar, consultar histórico.
- Não introduzir grafos/pontuações de progresso complexos nem interpretar repetições em texto livre como valor numérico.
- A fase não altera o esquema de dados exceto pela persistência local mínima dos modelos de treino, com migração documentada e compatível.

## 6. Princípios de identidade e tokens

### 6.1 Paleta proposta

Os valores abaixo traduzem as decisões visuais aprovadas em uma paleta concreta para revisão. A especificação está em `DRAFT`; valores e contrastes devem ser aprovados/verificados antes da implementação.

| Token semântico | Valor | Uso |
|---|---:|---|
| `background` | `#0B0B0C` | Fundo principal |
| `surface` | `#141416` | Superfície padrão |
| `surfaceContainer` | `#1D1D20` | Superfície elevada/cartão |
| `surfaceVariant` | `#29292D` | Superfície secundária e agrupamentos |
| `outline` | `#414146` | Contornos e separadores |
| `onBackground` / `onSurface` | `#F5F5F5` | Texto/ícones principais |
| `onSurfaceVariant` | `#B9B9BF` | Texto auxiliar e metadados |
| `primary` | `#F2F2F2` | Ação ou seleção primária |
| `onPrimary` | `#111111` | Conteúdo sobre ação primária |
| `primaryContainer` | `#333337` | Seleção/realce monocromático |
| `onPrimaryContainer` | `#F5F5F5` | Conteúdo sobre contêiner primário |
| `error` | `#FF8A80` | Erro e atraso |
| `errorContainer` | `#5A1B1B` | Fundo de erro/atraso |
| `onErrorContainer` | `#FFDAD6` | Texto sobre erro/atraso |

O uso de vermelho fica restrito a erros/atrasos e sempre acompanhado de rótulo/ícone. Conclusão, status de hábito e tipos de cuidado não dependem exclusivamente de cores. Não usar acentos coloridos por categoria semântica de Hidratação/Nutrição/Reconstrução.

Razões de contraste calculadas para os pares de texto propostos: `#F5F5F5/#0B0B0C` 18,05:1; `#B9B9BF/#141416` 9,42:1; `#B9B9BF/#29292D` 7,42:1; `#FF8A80/#0B0B0C` 8,62:1; `#FFDAD6/#5A1B1B` 10,11:1; `#111111/#F2F2F2` 16,87:1. A implementação deve testar os tokens efetivamente usados, atendendo WCAG AA mínimo 4,5:1 para texto normal.

### 6.2 Tipografia

- Usar a escala tipográfica Material 3 já disponível, sem adicionar família de fonte.
- Aplicar `headlineSmall` para títulos de tela; `titleLarge`/`titleMedium` para cabeçalhos de seção/itens; `bodyLarge`/`bodyMedium` para conteúdo; `bodySmall`/`labelMedium` para datas e metadados; `labelLarge` para ações e status.
- Não reduzir tamanho de fonte para acomodar conteúdo; permitir escala do sistema e quebra de linha.

### 6.3 Espaçamento, formas e superfícies

Escala de espaçamento compartilhada: 4, 8, 12, 16, 20, 24 e 32 dp. Usar 16 dp como gutter padrão de tela compacta, 12–16 dp entre itens compactos e 24 dp entre seções; 32 dp somente para separação de blocos maiores na Home.

Formas: raio 12 dp para controles/elementos compactos, 16 dp para superfícies e Bottom Sheets; pills reservadas a chips/badges. Alvos interativos continuam com mínimo 48 × 48 dp. Diferenciar superfícies por tom, contorno e hierarquia tipográfica, sem sombra forte ou card sem função.

Cards de maior dimensão concentram-se na Home. Telas internas usam listas, linhas, chips, calendários e superfícies compactas, recorrendo a card somente quando delimitar conteúdo ou uma ação independente.

### 6.4 Iconografia, feedback e movimento

- Usar ícones vetoriais consistentes, monocromáticos e disponíveis na plataforma/projeto; se não houver set existente compatível, usar VectorDrawables locais. Não usar glifos Unicode/emoji como ícone funcional e não adicionar dependência sem aprovação.
- Todo ícone sem rótulo visível tem semântica/descrição contextual. Estados nunca dependem apenas de cor.
- Feedback de sucesso/erro é discreto e explícito, usando texto, Snackbar ou estado inline conforme persistência/urgência. Falha de gravação não pode parecer sucesso.
- Preferir transições e feedback de interação Material 3 existentes; animações curtas e discretas, sem animação obrigatória para entender estado. Respeitar “remover animações”/escala reduzida do sistema quando disponível.

## 7. Navegação e comportamento global

- Barra inferior fixa com exatamente cinco itens e rótulos sempre visíveis: **Início**, **Hábitos**, **Tarefas**, **Treinos**, **Cabelo**.
- Usar a estrutura `NavigationState` + `Scaffold` já existente, evoluindo destinos/callbacks. Não introduzir Navigation Compose sem requisito de back stack que a solução atual não possa atender.
- Preservar a sessão de treino ao trocar de destino; retornar a Treinos não inicia sessão duplicada.
- A ação “Ver tudo” das seções da Home abre Hábitos ou Tarefas conforme a seção.
- A ação “Ver histórico”, junto à grade de consistência na Home, abre a tela interna de Histórico consolidado. Back nessa tela volta à Home.
- Back do sistema em destino principal secundário continua voltando a Início; Back em Início mantém o comportamento nativo de sair/minimizar.
- Treinos e Cabelo são destinos separados, sem abas adicionais como destinos principais.

## 8. Especificação das telas

### 8.1 Início

**Cabeçalho**
- Exibir saudação neutra curta (“Olá”), sem nome de perfil, data ou botão de configurações.

**“Seu dia”**
- Apresentar dois indicadores independentes, sem percentual agregado:
  - Hábitos: ocorrências previstas para hoje entre hábitos ativos, no formato concluídos/previstos.
  - Tarefas: tarefas abertas com prazo hoje e quantidade de tarefas atrasadas em indicador separado. Tarefas sem prazo não entram na contagem com prazo.
- A classificação de atraso segue a regra existente: tarefa aberta atrasada quando `dueAt < agora`. Se o seletor escolheu a data civil e a persistiu como início do dia, ela pode estar atrasada no mesmo dia após esse instante. A Fase 9 não muda essa regra.

**Ações rápidas**
- Ações “Novo hábito” e “Nova tarefa” permanecem evidentes e abrem os respectivos Bottom Sheets de criação.

**“Para hoje”**
- Mostrar hábitos previstos para hoje e tarefas abertas atrasadas/previstas para hoje em listas compactas.
- Tocar num hábito registra a conclusão da ocorrência uma única vez.
- Tocar numa tarefa abre seus detalhes em Bottom Sheet; a partir dos detalhes, expor as ações já aprovadas (concluir, editar, reagendar quando atrasada e excluir com confirmação/desfazer).
- “Ver tudo” leva à aba correspondente.
- Itens atrasados têm texto “Atrasada”, ícone/semântica e token de erro, sem depender de cor.

**“Sua consistência”**
- Grade com um ponto por cada um dos últimos sete dias, incluindo hoje, em `America/Sao_Paulo`.
- Contar somente eventos de conclusão: uma ocorrência de hábito por `habitId`/data civil em que ficou `COMPLETED`; uma tarefa por ID na data local de `completedAt`; uma sessão por ID na data local de `endedAt` quando concluída. Pausas, eventos pendentes, tarefas apenas abertas, exercícios/séries e Cabelo não contam.
- Deduplicar por par `(tipo de fonte, id persistido)`, usando a data canônica do evento; a mesma atividade exibida em Home, lista e Histórico continua sendo um único evento.
- Níveis de intensidade diária: 0, 1, 2, 3 ou mais atividades. Cada nível tem tonalidade neutra crescente e rótulo/descrição acessível com data e quantidade.
- “Ver histórico” abre o Histórico interno.

**Treinos e Cabelo na Home**
- Mostrar Treinos somente quando houver sessão ativa/pausada com ação “Continuar treino”.
- Mostrar Cabelo somente se houver etapa pendente prevista para hoje ou atrasada; futuro distante permanece na tela Cabelo.

**Estados**
- Loading: indicador sem conteúdo falso.
- Erro: mensagem clara sem ser apresentada como lista vazia; oferecer nova tentativa somente se a fonte puder ser reconsultada.
- Sem hábitos/tarefas: seções informam ausência e mantêm ações rápidas.
- Sem eventos na grade: sete pontos no nível 0; não exibir pontuação total.

### 8.2 Hábitos

- Lista compacta com seções Ativos e Pausados; arquivados não aparecem na lista ativa padrão.
- Cada item mostra nome, frequência e progresso do dia/período com estados textual/semântico.
- Conclusão rápida fica acessível no item ativo previsto para o dia, sem navegação para outra tela; repetir a ação após concluído não cria ocorrência duplicada.
- Abrir detalhes/editar permite criar/editar com Bottom Sheet e expõe pausar, retomar e arquivar conforme as regras existentes.
- Hábito pausado tem rótulo “Pausado” e não oferece concluir.
- Vazio: ícone discreto, explicação e ação “Criar hábito”. Falha de leitura não se apresenta como vazio.

### 8.3 Tarefas

- Organizar tarefas abertas em **Atrasadas**, **Hoje** e **Próximas**, priorizando a classificação existente `Task.isOverdue(now)`. Uma tarefa pertence a um único grupo: atraso tem precedência; depois prazo de hoje; depois prazo futuro.
- Itens sem prazo permanecem visíveis ao fim de “Próximas”, com rótulo “Sem prazo”, sem serem classificados como atrasados.
- Manter identificáveis título, prazo, estado e ações. Atraso usa cor semântica, ícone e texto.
- Toque no item abre detalhes; criar/editar usam Bottom Sheet, reagendamento mantém DatePicker, exclusão usa confirmação e Snackbar com “Desfazer”.
- Manter acesso à lista de concluídas, oculta por padrão, com distinção textual de conclusão.
- Vazio por grupo/filtro usa mensagem e ação apropriada (por exemplo, alternar para abertas/concluídas ou criar tarefa); erro não se confunde com vazio.

### 8.4 Treinos

- Destino próprio com acesso imediato à sessão ativa e ação de iniciar treino. Se uma sessão está ativa/pausada, apresentar “Continuar”; iniciar não cria outra sessão.
- Disponibilizar gestão de modelos locais: criar, editar, selecionar e excluir com confirmação. Um modelo tem nome e lista ordenada de exercícios com `name`, `setsCount`, `reps` opcional em texto, `weightKg` opcional e nota opcional. Aplicar um modelo pré-preenche uma sessão nova; alterar sessão não altera o modelo.
- Persistir modelos com entidades locais separadas das sessões. O esquema atual é Room versão 5; a implementação deve adicionar migration sequencial 5→6, preservando todos os dados e sem fallback destrutivo.
- Exibir estatísticas básicas: treinos concluídos e duração média, junto da evolução por exercício; evitar pontuação geral/XP.
- Evolução: agrupar pelo nome do exercício após trim e comparação sem distinção de caixa; para cada sessão concluída, registrar o maior `weightKg` positivo daquele exercício. Mostrar valores em kg em ordem cronológica, sem converter repetições (texto livre) em métrica numérica. Sessões/exercícios sem carga comparável continuam disponíveis no histórico, sem score ou inferência de regressão.
- Preservar iniciar, registrar manualmente, pausar, retomar, finalizar, descartar conforme regra existente, retomar sessão persistida e consultar histórico.
- Loading, erro e vazio são distintos; tela vazia de histórico de treinos ainda permite iniciar ou escolher um modelo.

### 8.5 Cabelo (cronograma capilar)

- Destino próprio com calendário mensal como estrutura principal e acesso a próximas etapas e histórico.
- Grade inicia semana na segunda-feira, usa mês local e permite navegar para mês anterior/próximo. O dia selecionado mostra suas etapas em lista abaixo.
- Marcar dias com ocorrências pendentes, reagendadas ou concluídas por indicadores não exclusivamente cromáticos. Distinguir Hidratação, Nutrição e Reconstrução também por rótulo/ícone/semântica.
- Selecionar etapa pendente/reagendada oferece “Registrar execução” e “Reagendar”; execução guarda timestamp real, reagendamento mantém o histórico e as regras existentes.
- Não criar tratamentos avulsos fora da rotina configurada.
- Próximos tratamentos e histórico permanecem acessíveis sem sobrecarregar a grade.
- Sem rotina: ícone discreto, mensagem e ações para adotar preset ou criar rotina. Mês sem etapas: indicar ausência e permitir voltar a hoje. Erro de carga é distinto de ausência de dados.

### 8.6 Histórico interno

- Manter timeline e filtros existentes: Todas, Hábitos, Tarefas, Treinos e Cuidados.
- Acesso pela ação “Ver histórico” da Home; não inserir História como item inferior.
- Preservar data/hora e tipo de registro canônico, estados reais e ordenação cronológica existente.
- Vazio: ícone, mensagem e ação de retorno à Home, sem introduzir criação de registros na tela.

## 9. Padrões de interação e estados comuns

- Criação/edição de hábito, tarefa, modelo de treino e rotina capilar usam Bottom Sheet, com validação, salvar/cancelar e suporte a teclado/área de gesto.
- Confirmar exclusão/descartar e transições de frequência com diálogo quando a regra existente requer confirmação.
- DatePicker permanece diálogo/material date picker; Snackbar é usada para Undo e feedback transitório.
- Não usar Bottom Sheet para toda ação; navegação direta é usada para destinos, diálogo para decisão crítica e controle inline para conclusão simples.
- Os componentes vazios incluem ícone discreto, mensagem contextual e ação válida para iniciar/próximo passo. Não oferecer ação que o destino não possa executar.
- Estados de erro não podem ser renderizados como sucesso ou vazio. Exibir mensagem útil e mecanismo de recuperação quando disponível.
- Loading não deve bloquear navegação inferior nem ocultar estado persistido em outra tela.

## 10. Acessibilidade e responsividade

- Contraste mínimo WCAG AA 4,5:1 para texto normal; os tokens e combinações efetivamente usados devem ter teste automatizado de contraste.
- Alvo mínimo de toque de 48 × 48 dp para navegação e controle interativo.
- Rótulos acessíveis para ícones; combinar texto/ícone/semântica para atraso, seleção, conclusão e tipo capilar.
- Grade de consistência e calendário expõem data, contagem, tipo e estado a leitor de tela; ordem de foco corresponde à ordem visual.
- Não truncar nomes/prazos essenciais; acomodar fontes maiores sem cortar controles, diálogos ou Bottom Sheets.
- Validar larguras compacta de 360 dp e padrão de 412 dp, além de escala de fonte ampliada, orientação disponível e rolagem até ações inferiores.
- Layout primariamente em uma coluna; conteúdo rola sob a barra inferior fixa. Não exigir dependência de Window Size Class para os perfis de telefone aprovados.
- A grade mensal pode ocupar toda a largura útil da tela, mantendo alvos de dia utilizáveis em 360 dp.

## 11. Critérios de aceite verificáveis

1. `AC-01`: O tema aplicado em telas Compose usa tokens explícitos monocromáticos; vermelho/semântica de erro só aparece em estados de erro/atraso. As combinações de texto usadas passam teste WCAG AA.
2. `AC-02`: A barra inferior apresenta exatamente Início, Hábitos, Tarefas, Treinos e Cabelo, com rótulos visíveis e alvos ≥48 dp; Histórico não aparece como item inferior.
3. `AC-03`: Selecionar cada destino exibe a tela correspondente; voltar de Histórico retorna à Home; voltar de destino principal secundário retorna à Home; trocar destinos durante treino não duplica nem perde a sessão.
4. `AC-04`: Home mostra saudação neutra sem data/perfil/configurações e mantém dois indicadores separados; nenhum percentual combina hábito e tarefa.
5. `AC-05`: Contagem de hábito no “Seu dia” considera somente hábitos ativos previstos para a data civil local; tarefas sem prazo ficam fora do contador de prazo de hoje.
6. `AC-06`: A lista “Para hoje” conclui ocorrência de hábito pelo controle rápido, abre detalhes da tarefa ao tocar nela, e “Ver tudo” navega à área correspondente.
7. `AC-07`: Classificação de tarefa atrasada usa exatamente `dueAt < agora` da Fase 4. Uma tarefa que satisfaz esse predicado aparece em Atrasadas mesmo que a data civil do prazo seja hoje; atraso tem rótulo/ícone além da cor.
8. `AC-08`: Grade exibe sete dias locais com contagem 0/1/2/3+, conta cada ocorrência concluída/tarefa/sessão uma única vez e exclui Cabelo, pendências, itens abertos e duplicatas de apresentação. Testes cobrem limite de data/fuso e deduplicação.
9. `AC-09`: Tarefas abertas são agrupadas uma vez em Atrasadas/Hoje/Próximas, por prioridade da classificação de atraso; tarefas sem prazo continuam acessíveis ao fim de Próximas como “Sem prazo”. Filtro de concluídas e ações existentes continuam funcionais.
10. `AC-10`: Modelos de treino são criados/editados/excluídos e persistem localmente; selecionar modelo preenche uma sessão independente; migration 5→6 preserva sessões e exercícios existentes.
11. `AC-11`: Estatísticas mostram treinos concluídos/duração média e sequência de maior carga por exercício; não interpretam `reps` textual nem mostram pontuação. Sem carga, histórico segue acessível sem score.
12. `AC-12`: Calendário capilar navega meses, seleciona um dia e lista etapas; selecionar pendente permite executar ou reagendar, preservando timestamp/histórico e estado persistido.
13. `AC-13`: Criação/edição nas áreas abrangidas usa Bottom Sheets; confirmação, DatePicker, Snackbar Undo e navegação usam o padrão correspondente ao fluxo.
14. `AC-14`: Cada estado vazio aplicável mostra ícone, mensagem e ação válida; carregamento/erro não se confundem com vazio nem com sucesso.
15. `AC-15`: Em 360 dp, 412 dp e com fonte ampliada, conteúdo essencial, calendário, cinco destinos e controles permanecem acessíveis sem corte; elementos interativos satisfazem 48 × 48 dp e rótulos semânticos.
16. `AC-16`: `testDebugUnitTest`, testes instrumentados focados e build de debug passam ao final da implementação. Evidências são registradas em relatórios da fase; falhas de ambiente não contam como aprovação.

## 12. Dependências, riscos e validações

- Reutiliza Compose/Material 3, Room, Coroutines/Flow, ViewModels e dependências já configuradas.
- Templates introduzem tabelas Room novas e migration 5→6; risco principal é preservar dados e idempotência de inclusão/seleção.
- A grade de consistência requer identidade estável do evento de origem; `HistoryItem` atual não expõe ID de origem. Implementar agregação com chave canônica por tipo/ID ou consulta de domínio equivalente, sem contar a mesma exibição em mais de uma fonte.
- Estatísticas por carga precisam tratar carga nula/zero e nomes de exercícios variáveis; não inferir valor a partir de `reps`.
- A classificação `dueAt < agora` é preservada. Como o DatePicker atual pode gravar a data local no início do dia, atrasos no próprio dia são conhecidos e não devem ser “corrigidos” nesta fase.
- Navegação de Histórico como tela interna exige callback/estado de retorno sem tirar funcionalidade de filtros ou mudar fonte de verdade.
- Testes relevantes: contraste/tokens; modelo de contagem e deduplicação; classificação de prazo; ViewModels; navegação; teste de migration; testes instrumentados de bottom sheets, calendário, acessibilidade e tamanhos de tela.

## 13. Questões de produto

As decisões apresentadas na solicitação e esclarecimentos posteriores foram incorporados. A regra de prazo da Fase 4 foi explicitamente confirmada para ser preservada. A aprovação formal desta especificação continua pendente; ajustes nos valores concretos de tokens propostos devem ser resolvidos nessa revisão antes de implementação.
