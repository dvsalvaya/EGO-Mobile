# 04 — Roadmap: EGO Mobile

## Princípio

O MVP não tem prazo artificial. O trabalho deve avançar em incrementos pequenos, com cada etapa funcionando antes de adicionar a próxima.

## Fase 0 — Preparar o ambiente

- [x] Confirmar que a IDE escolhida consegue abrir e compilar um projeto Android Kotlin.
- [x] Confirmar compatibilidade entre Kotlin, Gradle, Android Gradle Plugin e Jetpack Compose.
- [x] Criar/abrir o projeto Android com Kotlin.
- [x] Executar o app vazio em emulador ou dispositivo.
- [ ] Configurar Git e primeiro commit do projeto.
- [x] Manter as decisões documentadas em `03-SDD.md`.

**Concluída quando:** o projeto compila e inicia de forma reproduzível.

**Validação em 2026-10-28:** Android Studio Rabbit 1 (2026.2.1) abriu o projeto e concluiu a sincronização Gradle; `:app:assembleDebug` passou. O app foi instalado e iniciado no AVD `Medium_Phone_API_37.0` (API 37), e a tela inicial foi conferida. A compatibilidade das versões está registrada no SDD.

**Pendente:** o Git está configurado, mas o `Initial commit` existente tem árvore vazia e os arquivos do projeto ainda não estão commitados. Não foi criado um commit durante esta verificação.

## Fase 1 — Esqueleto do aplicativo

- [ ] Criar tema escuro.
- [ ] Criar `EgoApp` e `MainActivity`.
- [ ] Implementar a navegação inferior com cinco destinos.
- [ ] Criar telas-placeholder para Início, Hábitos, Tarefas, Rotinas e Histórico.
- [ ] Validar a navegação em diferentes tamanhos de tela.

**Concluída quando:** todas as áreas podem ser acessadas e voltar sem erros.

## Fase 2 — Persistência local

- [ ] Adicionar Room com versões compatíveis verificadas.
- [ ] Criar banco e primeira entidade.
- [ ] Implementar DAO e repositório para a primeira funcionalidade.
- [ ] Testar criação, leitura e atualização.
- [ ] Confirmar persistência após fechar e reabrir o app.

**Concluída quando:** os dados de teste permanecem salvos localmente.

## Fase 3 — Hábitos

- [ ] Criar hábito.
- [ ] Listar hábitos.
- [ ] Editar hábito.
- [ ] Pausar e retomar.
- [ ] Registrar conclusão.
- [ ] Implementar frequência flexível.
- [ ] Perguntar como aplicar mudança de frequência.
- [ ] Tratar ocorrências pendentes conforme a decisão do usuário.
- [ ] Exibir estados ativo e pausado.
- [ ] Criar histórico de ocorrências.
- [ ] Adicionar testes de regras e persistência.

**Concluída quando:** o fluxo de hábitos funciona sem perder histórico nem alterar registros antigos silenciosamente.

## Fase 4 — Tarefas

- [ ] Criar e editar tarefa.
- [ ] Tornar prazo opcional.
- [ ] Concluir tarefa.
- [ ] Ocultar concluídas da lista padrão.
- [ ] Destacar tarefas atrasadas em vermelho e por texto/ícone.
- [ ] Permitir reagendamento.
- [ ] Confirmar exclusão.
- [ ] Implementar desfazer exclusão se viável no fluxo.
- [ ] Testar casos com e sem prazo.

**Concluída quando:** o ciclo de vida de uma tarefa funciona e os estados são claros.

## Fase 5 — Treinos

- [ ] Iniciar sessão.
- [ ] Registrar exercícios manualmente.
- [ ] Pausar e retomar.
- [ ] Persistir sessão em andamento.
- [ ] Finalizar sessão.
- [ ] Exibir histórico.
- [ ] Testar saída e reabertura do app durante um treino.

**Concluída quando:** o usuário consegue continuar uma sessão existente sem criar duplicatas por engano.

## Fase 6 — Cuidados capilares

- [ ] Criar rotina personalizada.
- [ ] Oferecer predefinição editável.
- [ ] Registrar execução real.
- [ ] Reagendar cuidado.
- [ ] Exibir histórico.
- [ ] Testar frequências e estados.

**Concluída quando:** o usuário consegue configurar e acompanhar sua rotina sem ser obrigado a seguir uma predefinição.

## Fase 7 — Tela inicial e histórico

- [ ] Criar resumo equilibrado.
- [ ] Adicionar atalhos de criação de hábito e tarefa.
- [ ] Mostrar tarefas atrasadas.
- [ ] Mostrar estados de hábitos e rotinas de forma útil.
- [ ] Completar a área de histórico separada.
- [ ] Verificar consistência entre resumo, listas e histórico.

**Concluída quando:** a tela inicial informa o que é útil sem duplicar todo o histórico.

## Fase 8 — Qualidade e acabamento do MVP

- [ ] Revisar acessibilidade e contraste.
- [ ] Revisar tratamento de erros.
- [ ] Executar testes unitários e instrumentados relevantes.
- [ ] Testar persistência após reinício do app.
- [ ] Testar navegação e estados vazios.
- [ ] Remover dependências não utilizadas.
- [ ] Conferir permissões Android: não pedir permissões desnecessárias.
- [ ] Atualizar README com instruções reais para executar o projeto.
- [ ] Verificar se não foram adicionados login, nuvem, lembretes ou XP/níveis/moedas.

**Concluída quando:** os fluxos principais funcionam de ponta a ponta e as limitações conhecidas estão documentadas.

## Regras de execução

1. Fazer uma funcionalidade por vez.
2. Antes de implementar, conferir o PRD e o fluxo correspondente.
3. Não escrever todo o aplicativo em uma única etapa.
4. Não adicionar abstrações sem uma necessidade concreta.
5. Não marcar tarefas como concluídas sem executar validações.
6. Se a IDE não suportar uma dependência, verificar a causa e a compatibilidade antes de trocar a arquitetura.
7. Registrar decisões novas no SDD e requisitos novos no PRD antes de implementá-los.
