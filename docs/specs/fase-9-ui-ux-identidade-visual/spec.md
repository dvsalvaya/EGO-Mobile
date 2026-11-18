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

**V1 confirmada:** Início, Hábitos e consulta/correção do histórico de hábitos. A barra mantém cinco destinos. O redesenho detalhado de Tarefas, Treinos e Cabelo fica para a etapa seguinte. A V1 usa a base estabilizada da Spec 10. Os itens abaixo descrevem o escopo completo da fase.

1. Definir design tokens semânticos para cores, tipografia, espaçamento, formas, superfícies, ícones e estados.
2. Atualizar a navegação inferior para Início, Hábitos, Tarefas, Treinos e Cabelo. Histórico permanece tela interna.
3. Atualizar a Home com saudação personalizada, contagens junto às listas do dia, botão de criação e grade de hábitos de sete dias.
4. Organizar Hábitos e Tarefas em listas compactas e interativas, preservando os ciclos de negócio aprovados.
5. Tornar Treinos uma área principal; incluir modelos locais reutilizáveis, estatísticas de sessão e evolução por exercício, além de manter sessão/histórico atuais.
6. Tornar Cabelo uma área principal com calendário mensal, próximas etapas, execução, reagendamento e histórico.
7. Padronizar Bottom Sheets para criação/edição. Usar diálogos, confirmação, DatePicker, Snackbar ou navegação quando o fluxo exigir.
8. Definir estados de carregamento, vazio, erro e conteúdo; acessibilidade e adaptação de tela.
9. Criar/atualizar testes apenas para regras e comportamentos desta fase, seguindo RED → GREEN → REFACTOR quando houver implementação.

## 5. Exclusões

- Não alterar regras de negócio de hábitos, tarefas, sessões ou cuidados capilares fora do necessário para apresentar os fluxos aprovados.
- A regra de vencimento por data civil vem de RF-06 da Spec 10. Home e Tarefas usam a mesma classificação.
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

Fora da grade de hábitos, reservar vermelho para erro e atraso, acompanhado de rótulo ou ícone. A grade usa cores próprias de progresso e fornece descrição acessível. Estados de hábito e tipos de cuidado também têm indicação além da cor.

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

### 8.1 Início (V1)

- Saudação “Bom dia”, “Boa tarde” ou “Boa noite” conforme o horário de São Paulo, seguida do nome. O padrão é “Chefe”. Tocar na saudação, com ícone de edição discreto, abre a edição local do nome. Valor vazio volta ao padrão.
- Se houver sessão ativa ou pausada, “Continuar treino” é a primeira ação. Caso contrário, “Para hoje” abre o conteúdo. Mostrar a próxima etapa capilar quando estiver prevista para hoje ou atrasada, com atalho para Cabelo.
- “Para hoje” mostra até três hábitos elegíveis e três tarefas abertas. As contagens junto às listas representam o total, mesmo quando há mais de três. Hábitos pausados ficam na aba Hábitos. A conclusão rápida usa um controle próprio e tocar na linha abre os detalhes.
- Priorizar hábitos diários, de dias fixos ou com hoje marcado na meta semanal. A meta sem dias marcados aparece enquanto houver conclusão a fazer. Em dias fora do plano escolhido, o registro continua disponível na aba Hábitos e conta para a meta. Usar ordem estável entre itens de mesma prioridade.
- Tarefas atrasadas vêm antes das que vencem hoje e são ordenadas do prazo mais antigo ao mais recente em cada grupo. Tarefas sem prazo ficam na aba Tarefas. Tocar na linha abre detalhes com “Concluir” como ação principal e edição, reagendamento e exclusão conforme o fluxo existente. “Ver tudo” abre a aba correspondente. Preservar a rolagem ao retornar.
- O botão flutuante “+” tem rótulo acessível “Criar”, fica sempre visível no canto inferior direito, acima da barra inferior, e abre um menu com “Novo hábito” e “Nova tarefa”. A rolagem permite acessar todas as ações sem sobreposição. As demais abas têm ação própria no contexto da tela.
- No primeiro uso, orientar a criação pelo “+”. Quando não houver pendências para hoje, mostrar “Tudo em dia”. Diferenciar carregamento e falha de um estado vazio.

**Grade de hábitos**

- Sete quadrados, um por data civil incluindo hoje, com as iniciais dos dias e o link “Ver histórico”. A grade é informativa. Detalhes ficam no Histórico e a descrição acessível informa data, contagem e estado.
- Em cada data, a proporção é de hábitos previstos que foram concluídos. Contar cada hábito/data uma vez. Usar cinza quando não houver previsão, vermelho para 0%, laranja acima de 0% e abaixo de 50%, verde escuro de 50% a menos de 100% e verde claro em 100%.
- Hoje tem contorno e estado acessível “Em andamento” enquanto houver previsão em aberto. Zero conclusões no dia ainda aberto não usa vermelho. Uma preferência semanal conta como previsão se a meta ainda estava em aberto naquele dia. Após atingir a meta, dias preferidos posteriores deixam de ser esperados. Uma conclusão fora do plano conta para a semana.
- Metas semanais sem dias marcados não geram previsão diária para a grade. O progresso delas aparece na lista de hábitos e no Histórico semanal.
- Correções no Histórico atualizam o dia original. Alterar frequência ou dias escolhidos preserva as previsões e os registros históricos. Tarefas, treinos e cabelo seguem nos respectivos históricos.

### 8.2 Hábitos (V1)

- Cabeçalho com “Novo hábito”. Listas compactas de ativos e pausados, com nome, estado ou progresso curto e controle para concluir quando elegível. Ativos concluídos continuam visíveis. Se só houver pausados, mostrar “Nenhum hábito ativo” e oferecer retomada na seção Pausados.
- Tocar na linha abre um painel com progresso e ações de editar, pausar/retomar, arquivar e registrar conclusão anterior. Arquivar pede confirmação e preserva os registros.
- Criação e edição usam o mesmo formulário com nome obrigatório, descrição opcional e validação no campo. Oferecer “Todos os dias” como padrão, “Dias da semana” para dias fixos e “Vezes por semana” para metas de 1 a 7. Na meta semanal, mostrar abaixo da quantidade as iniciais dos dias e permitir marcar de zero a sete preferências. Usar nomes completos em acessibilidade. A seleção é visual e semanticamente distinguível.
- A confirmação de alteração de frequência ou dos dias escolhidos explica que o plano vale a partir de hoje e preserva o histórico.
- Concluir registra uma ocorrência por hábito/data e remove o item de “Para hoje”. Mostrar “Hábito concluído” com “Desfazer”. A opção de desfazer também fica nos detalhes para a conclusão do dia.

### 8.3 Tarefas

- Organizar tarefas abertas em **Atrasadas**, **Hoje** e **Próximas** pela data civil do prazo em America/Sao_Paulo, conforme RF-06. Cada tarefa pertence a um único grupo.
- Itens sem prazo permanecem visíveis ao fim de “Próximas”, com rótulo “Sem prazo”, sem serem classificados como atrasados.
- Manter identificáveis título, prazo, estado e ações. Atraso usa cor semântica, ícone e texto.
- Toque no item abre detalhes; criar/editar usam Bottom Sheet, reagendamento mantém DatePicker, exclusão usa confirmação e Snackbar com “Desfazer”.
- Manter acesso à lista de concluídas, oculta por padrão, com distinção textual de conclusão.
- Vazio por grupo/filtro usa mensagem e ação apropriada (por exemplo, alternar para abertas/concluídas ou criar tarefa); erro não se confunde com vazio.

### 8.4 Treinos

- Destino próprio com acesso imediato à sessão ativa e ação de iniciar treino. Se uma sessão está ativa/pausada, apresentar “Continuar”; iniciar não cria outra sessão.
- Disponibilizar gestão de modelos locais: criar, editar, selecionar e excluir com confirmação. Um modelo tem nome e lista ordenada de exercícios com `name`, `setsCount`, `reps` opcional em texto, `weightKg` opcional e nota opcional. Aplicar um modelo pré-preenche uma sessão nova; alterar sessão não altera o modelo.
- Reutilizar os modelos locais e a migração Room 5→6 já presentes na árvore. Validar que sessões e exercícios anteriores são preservados.
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
- Para hábitos diários ou de dias fixos, mostrar uma linha por hábito e data. A previsão encerrada sem conclusão fica como “Não fiz” a partir da virada do dia.
- Na V1, esse fechamento substitui o tratamento manual de uma ocorrência não cumprida descrito na Spec 3. O registro segue corrigível pelo Histórico.
- Para metas semanais, resumir por hábito e semana ISO. Se a meta for atingida, o progresso e os dias anteriores ficam disponíveis no Histórico e saem da Home. Se a semana terminar abaixo da meta, registrar “Meta não atingida”, com a contagem realizada. Dias previstos decorridos sem conclusão ficam como “Não fiz” nos detalhes do período. Dias posteriores ao atingimento da meta ficam fora dessa classificação.
- Ao abrir o registro, a pessoa pode corrigir a data original por “Fiz nesse dia” ou “Não fiz”. Contar a correção factual mesmo se a semana passar da meta, como “4 de 3”. Corrigir recalcula o Histórico, o progresso e a grade, sem duplicar ocorrências.
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

1. `AC-01`: Tema escuro monocromático com cor semântica para erro e atraso, além das cores da grade de hábitos. As combinações de texto usadas passam teste WCAG AA.
2. `AC-02`: A barra inferior apresenta exatamente Início, Hábitos, Tarefas, Treinos e Cabelo, com rótulos visíveis e alvos ≥48 dp; Histórico não aparece como item inferior.
3. `AC-03`: Selecionar cada destino exibe a tela correspondente; voltar de Histórico retorna à Home; voltar de destino principal secundário retorna à Home; trocar destinos durante treino não duplica nem perde a sessão.
4. `AC-04`: Saudação pelo horário de São Paulo e nome persistido localmente, com “Chefe” por padrão. O “+” flutuante está na Home, respeita a barra inferior e abre os dois formulários.
5. `AC-05`: “Para hoje” limita a lista a três itens por área com contagens totais, omite pausados e usa as prioridades da seção 8.1.
6. `AC-06`: Concluir um hábito atualiza a Home e oferece desfazer. Tocar na linha abre detalhes. “Ver tudo” e “Ver histórico” preservam a rolagem no retorno.
7. `AC-07`: Uma tarefa aberta com prazo de hoje só atrasa no dia seguinte em America/Sao_Paulo. Prazo ausente nunca gera atraso.
8. `AC-08`: A grade representa os hábitos previstos em sete datas locais, com as faixas da seção 8.1, descrição acessível e atualização após correção histórica.
9. `AC-09`: Tarefas abertas são agrupadas uma vez em Atrasadas/Hoje/Próximas, por prioridade da classificação de atraso; tarefas sem prazo continuam acessíveis ao fim de Próximas como “Sem prazo”. Filtro de concluídas e ações existentes continuam funcionais.
10. `AC-10`: Modelos de treino são criados/editados/excluídos e persistem localmente; selecionar modelo preenche uma sessão independente; migration 5→6 preserva sessões e exercícios existentes.
11. `AC-11`: Estatísticas mostram treinos concluídos/duração média e sequência de maior carga por exercício; não interpretam `reps` textual nem mostram pontuação. Sem carga, histórico segue acessível sem score.
12. `AC-12`: Calendário capilar navega meses, seleciona um dia e lista etapas; selecionar pendente permite executar ou reagendar, preservando timestamp/histórico e estado persistido.
13. `AC-13`: Criação/edição nas áreas abrangidas usa Bottom Sheets; confirmação, DatePicker, Snackbar Undo e navegação usam o padrão correspondente ao fluxo.
14. `AC-14`: Cada estado vazio aplicável mostra ícone, mensagem e ação válida; carregamento/erro não se confundem com vazio nem com sucesso.
15. `AC-15`: Em 360 dp, 412 dp e com fonte ampliada, conteúdo essencial, calendário, cinco destinos e controles permanecem acessíveis sem corte; elementos interativos satisfazem 48 × 48 dp e rótulos semânticos.
16. `AC-16`: `testDebugUnitTest`, testes instrumentados focados e build de debug passam ao final da implementação. Evidências são registradas em relatórios da fase; falhas de ambiente não contam como aprovação.

17. `AC-17`: O formulário permite de zero a sete preferências semanais. O registro fora desses dias conta para a meta. Alterações preservam os períodos anteriores.
18. `AC-18`: A virada do dia e da semana fecha os períodos aplicáveis e gera os estados da seção 8.6. A correção mantém a data original e aceita um total histórico acima da meta.

## 12. Dependências, riscos e validações

- Reutiliza Compose/Material 3, Room, Coroutines/Flow, ViewModels e dependências já configuradas.
- A integração de modelos usa as tabelas Room e a migração 5→6 existentes. Validar preservação de dados e idempotência de seleção.
- A grade usa uma fonte estável de previsões e conclusões por hábito/data. Se houver migração, ela deve preservar as ocorrências existentes e evitar inferir previsões antigas sem dados suficientes.
- Estatísticas por carga precisam tratar carga nula/zero e nomes de exercícios variáveis; não inferir valor a partir de `reps`.
- A classificação civil de prazo depende de RF-06 da Spec 10, com validação de virada de data e tarefas sem prazo.
- Navegação de Histórico como tela interna exige callback/estado de retorno sem tirar funcionalidade de filtros ou mudar fonte de verdade.
- Testes relevantes: contraste/tokens; modelo de contagem e deduplicação; classificação de prazo; ViewModels; navegação; teste de migration; testes instrumentados de bottom sheets, calendário, acessibilidade e tamanhos de tela.

## 13. Questões de produto

O responsável confirmou o recorte e as decisões de produto da V1. Esta revisão registra as decisões no rascunho e o plano técnico segue para revisão antes da implementação.
