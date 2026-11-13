# Fase 1 — Esqueleto do aplicativo

## Identificador e status

- **ID:** `fase-1-esqueleto-app`
- **Nome:** Esqueleto do aplicativo
- **Status:** `VERIFIED`
- **Aprovação:** Aprovado pelo responsável do produto em 2026-10-28. Decisão Q1 registrada: retorno ao Início a partir de destinos secundários.

Esta especificação descreve a Fase 1 do Roadmap e está autorizada para decomposição e implementação técnica conforme os portões do projeto.

## Problema e objetivo

O projeto precisa de uma estrutura inicial navegável que apresente as cinco áreas funcionais do EGO Mobile e estabeleça o tema visual aprovado. O objetivo desta fase é permitir acessar Início, Hábitos, Tarefas, Rotinas e Histórico, sem ainda implementar suas funcionalidades de negócio.

## Referências e decisões aprovadas

- `docs/04-ROADMAP.md`, **Fase 1 — Esqueleto do aplicativo**: tema escuro, `EgoApp` e `MainActivity`, cinco destinos, telas-placeholder, validação em diferentes tamanhos de tela e critério de conclusão.
- `docs/02-USER-FLOWS.md`, **Navegação principal**: áreas e responsabilidades dos cinco destinos.
- `docs/01-PRD.md`, **Navegação e tela inicial**, **Decisões de produto aprovadas** e **Requisitos não funcionais**: barra inferior com cinco destinos, tema escuro e requisitos básicos de acessibilidade.
- `docs/03-SDD.md`, **Decisões técnicas iniciais**, **Estrutura de pastas sugerida**, **Navegação** e **Estratégia de testes**: Kotlin, Compose conforme suportado pela toolchain, navegação simples e testes de navegação.
- `docs/specs/README.md`: status, aprovação e fluxo de especificação.

Na elaboração desta especificação, foram observados `MainActivity.kt`, `app/EgoApp.kt` e `app/navigation/AppNavigation.kt`: já existe uma atividade que inicia o Compose e um tema escuro básico; `AppNavigation` ainda apresenta apenas um conteúdo provisório, sem os cinco destinos. Esta observação descreve o estado atual, não representa validação de aceite.

## Escopo

- Estruturar a entrada do aplicativo usando `MainActivity` e `EgoApp`.
- Aplicar o tema escuro aprovado na experiência desta fase.
- Oferecer navegação inferior entre Início, Hábitos, Tarefas, Rotinas e Histórico.
- Apresentar um placeholder identificável para cada destino, sem antecipar funcionalidades de negócio.
- Manter os destinos acessíveis e utilizáveis em diferentes tamanhos de tela.
- Validar a navegação e os estados básicos da interface com testes apropriados ao ambiente disponível.

## Exclusões

- Criar ou modificar hábitos, tarefas, treinos ou cuidados capilares.
- Implementar resumos funcionais, ações rápidas, histórico ou regras de negócio.
- Adicionar persistência, banco de dados, contas, rede, sincronização ou notificações.
- Finalizar os nomes/ícones visuais além dos nomes funcionais já usados para identificar os destinos.
- Introduzir dependências ou arquitetura além do necessário para a navegação desta fase.
- Alterar requisitos do PRD, User Flows, SDD ou Roadmap.

## Comportamentos esperados

1. Ao abrir o aplicativo, o usuário encontra a estrutura principal do EGO Mobile em tema escuro.
2. A navegação inferior oferece os cinco destinos funcionais definidos: Início, Hábitos, Tarefas, Rotinas e Histórico.
3. Selecionar um destino apresenta seu placeholder e indica qual área está selecionada.
4. Cada placeholder permite identificar a área correspondente, sem sugerir que funcionalidades de negócio já estão implementadas.
5. O usuário consegue alternar entre os destinos sem falha ou perda de acesso à navegação.
6. Os destinos permanecem utilizáveis em diferentes tamanhos de tela, sem conteúdo essencial inacessível.

## Regras de negócio

- Esta fase não introduz regras de negócio nem dados de usuário.
- Os cinco destinos representam as áreas definidas nos User Flows; não adicionar ou remover destinos.
- O tema escuro é requisito aprovado. Detalhes visuais ainda não especificados, incluindo paleta e ícones finais, não devem ser tratados como decisões de produto.
- Placeholders não devem simular operações, persistência ou estados de funcionalidades ainda não implementadas.
- Manter a solução proporcional ao esqueleto: não adicionar dependências sem justificativa técnica e compatibilidade verificadas.

## Critérios de aceite verificáveis

1. **Inicialização:** abrir o aplicativo exibe a estrutura Compose do EGO Mobile sem crash nem tela vazia inesperada.
2. **Tema:** a estrutura e os cinco destinos são apresentados em tema escuro, inclusive quando o dispositivo está configurado em tema claro.
3. **Destinos:** a navegação inferior expõe exatamente cinco destinos identificáveis como Início, Hábitos, Tarefas, Rotinas e Histórico.
4. **Seleção:** a partir de cada destino, é possível selecionar qualquer outro pela navegação inferior; o conteúdo exibido corresponde ao destino selecionado e o estado selecionado é perceptível.
5. **Placeholders:** cada destino mostra uma identificação própria e não aparenta executar funcionalidades de negócio fora do escopo desta fase.
6. **Tamanhos de tela:** em pelo menos dois perfis de tela disponíveis com dimensões diferentes, os cinco destinos continuam acessíveis e o conteúdo apresentado não fica cortado ou inacessível por causa do layout.
7. **Acessibilidade básica:** os destinos podem ser identificados por texto/semântica, e não somente por ícones; aumentar a escala de fonte não torna a navegação ou o conteúdo essencial inacessível. Revisar também contraste e áreas de toque conforme o requisito geral do PRD.
8. **Navegação de retorno:** a navegação e a ação de voltar do sistema não causam crash nem deixam a interface inutilizável. Ao acionar voltar a partir de um destino secundário (Hábitos, Tarefas, Rotinas, Histórico), o usuário retorna para Início; ao acionar voltar a partir de Início, a atividade encerra/minimiza.
9. **Validação:** testes pertinentes à navegação são executados e seus resultados registrados. Falhas de compilação, configuração ou ambiente não contam como evidência de comportamento correto; se faltar infraestrutura de testes, registrar bloqueio em vez de declarar aceite.

## Fluxos principais e alternativos

### Fluxo principal — navegar entre áreas

1. O usuário abre o aplicativo.
2. A estrutura principal aparece em tema escuro, com um destino inicial.
3. O usuário seleciona um destino na navegação inferior.
4. O placeholder correspondente é exibido e o destino selecionado permanece identificável.
5. O usuário seleciona outros destinos e consegue retornar a qualquer uma das cinco áreas.

### Fluxo alternativo — voltar pelo sistema

1. O usuário está em um destino diferente do inicial.
2. Aciona a ação de voltar do sistema.
3. O aplicativo permanece utilizável e não falha.
4. O usuário é reconduzido para o destino Início. Acionar voltar a partir de Início encerra ou minimiza a aplicação.

### Fluxo alternativo — telas de dimensões diferentes

1. O aplicativo é aberto em cada perfil disponível de dimensões distintas.
2. O usuário acessa cada destino pela navegação inferior.
3. Confirma que controles e conteúdo continuam acessíveis, sem corte que impeça o uso.

## Estados de erro e casos-limite

- Inicialização sem crash ou conteúdo inesperadamente em branco.
- Seleção repetida do destino atual não deve deixar a navegação em estado inválido.
- Alternar rapidamente entre destinos não deve causar crash nem exibir permanentemente o placeholder de outro destino.
- Layout em telas de dimensões diferentes e com fonte ampliada, preservando acesso à navegação e ao conteúdo essencial.
- Acionamento de voltar no destino inicial encerra/minimiza a tela e nos destinos secundários retorna para o destino inicial (Início).
- Falha de compilação ou ausência de dispositivo/emulador não é falha funcional reproduzida nem teste de aceite aprovado.

## Persistência e privacidade

- Não há persistência de dados de negócio nesta fase.
- Não coletar nem transmitir dados pessoais; não adicionar conta, sincronização, rede ou permissões sem necessidade aprovada.
- Não prometer backup nem recuperação de dados.

## Dependências e restrições técnicas

- Linguagem principal: Kotlin; aplicação Android nativa.
- Usar Jetpack Compose conforme as decisões e compatibilidade descritas em `docs/03-SDD.md` e a configuração real do projeto.
- O projeto atualmente contém `MainActivity`, `EgoApp` e `AppNavigation`; inspecionar e evoluir essa estrutura em vez de presumir arquivos novos ou reescrever a entrada sem justificativa.
- O SDD recomenda Navigation Compose se compatível, mas também orienta manter a navegação simples. A necessidade de uma dependência deve ser avaliada no plano técnico; não fixar versão sem verificar compatibilidade.
- Os arquivos Gradle atuais definem a toolchain do projeto; qualquer necessidade de alterar dependências ou configuração deve ser justificada e revisada antes da implementação.
- O Roadmap exige validação em diferentes tamanhos de tela. Confirmar quais perfis de emulador/dispositivo e quais testes são viáveis no ambiente antes de aprovar o plano.
- Na inspeção inicial, não foram localizados arquivos de teste em `app/src/test` ou `app/src/androidTest`, nem dependências de teste no `app/build.gradle.kts`. A viabilidade de testes de navegação deve ser confirmada; se a infraestrutura for insuficiente, registrar e resolver esse bloqueio no plano, sem alegar que TDD foi cumprido.

## Questões resolvidas

| ID | Questão | Impacto | Responsável pela decisão | Bloqueio |
|---|---|---|---|---|
| Q1 | Ao acionar voltar do sistema em um destino secundário, o usuário deve retornar a Início, ao destino visitado anteriormente ou sair do aplicativo? | Retornar para Início. A partir de Início, encerra ou minimiza a aplicação. | Responsável pelo produto (2026-10-28) | Resolvido |
| Q2 | Quais perfis de dimensões/dispositivos serão a referência para a validação de layouts desta fase? | Perfis de tela compacta (ex.: 360dp de largura) e tela padrão/média (ex.: 412dp), testando também com escala de fonte ampliada. | Responsável técnico / produto | Resolvido |
| Q3 | O ambiente disponível oferece infraestrutura e dispositivos/emuladores suficientes para testes de navegação e tamanhos de tela? | Ambiente local dispõe do JDK/JBR do Android Studio e Android SDK. Testes serão executados via testes de navegação e Compose UI. | Responsável técnico | Resolvido |

## Próximo portão

Especificação aprovada (`APPROVED`). O próximo passo é decompor em tarefas em `tasks.md` e elaborar a revisão técnica em `plan.md` antes de iniciar a implementação com TDD.
