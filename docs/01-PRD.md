# 01 — PRD: EGO Mobile

## 1. Visão geral

O EGO Mobile é um aplicativo Android local-first para organizar rotinas pessoais. O usuário consegue acompanhar hábitos, tarefas, treinos e cuidados capilares em uma única aplicação, com foco em consistência e baixa fricção.

O aplicativo deve ser simples no MVP e evoluir por incrementos. Não há prazo artificial para finalizar o MVP: a prioridade é construir uma base funcional e melhorá-la gradualmente.

## 2. Problema

A organização de rotinas pode ficar espalhada entre aplicativos e anotações. O EGO reúne atividades recorrentes e pontuais, permitindo registrar o que foi feito, consultar histórico e lidar com ocorrências perdidas sem impor uma rotina rígida.

## 3. Objetivos

- Reunir hábitos, tarefas, treinos e cuidados capilares.
- Tornar o registro de execução rápido.
- Ajudar o usuário a perceber consistência ao longo do tempo.
- Permitir ajustar a frequência das rotinas sem perder o histórico.
- Funcionar sem conexão com a internet no MVP.
- Manter uma experiência visual simples, discreta e coerente.

## 4. Princípios do produto

1. **Consistência antes de gamificação:** a métrica central de evolução é a regularidade do usuário, não XP, níveis ou moedas.
2. **Local-first:** os dados permanecem no dispositivo no MVP.
3. **Flexibilidade:** o usuário pode definir frequências compatíveis com sua rotina.
4. **Baixa fricção:** ações frequentes devem exigir poucos passos.
5. **Transparência:** estados pausados, atrasados e concluídos devem ser visualmente distintos.
6. **Evolução incremental:** não adicionar complexidade sem necessidade demonstrada.

## 5. Público-alvo

Pessoa que deseja organizar rotinas pessoais recorrentes e tarefas pontuais em um único aplicativo Android, sem depender de uma conta ou serviço online.

## 6. Escopo do MVP

### 6.1 Hábitos

- Criar, editar, pausar, retomar e arquivar hábitos.
- Definir frequência flexível.
- Registrar a conclusão de uma ocorrência.
- Permitir concluir uma ocorrência ou registrar manualmente, conforme o fluxo da funcionalidade.
- Exibir histórico separado da tela principal.
- Ao alterar a frequência, perguntar como aplicar a mudança; não alterar o passado silenciosamente.
- Quando uma ocorrência não for cumprida, perguntar ao usuário como deseja tratá-la, em vez de presumir automaticamente que falhou.
- Exibir visualmente hábitos pausados.

### 6.2 Tarefas

- Criar, editar, concluir e excluir tarefas.
- Prazo é opcional.
- Tarefas vencidas devem ter destaque visual vermelho.
- Ao tentar excluir uma tarefa, pedir confirmação.
- Permitir desfazer exclusões quando a operação e o estado da tela permitirem.
- Tarefas concluídas ficam ocultas por padrão, com opção de consultar o histórico/lista de concluídas.
- Se uma tarefa ficar atrasada, permitir reagendar em vez de impor uma única resposta.

### 6.3 Treinos

- Registrar treinos.
- Permitir iniciar, pausar e retomar um treino.
- Permitir registrar exercícios de forma manual.
- Preservar o estado de um treino em andamento quando o usuário sair da tela ou o app for interrompido, dentro das limitações do armazenamento local.
- Histórico de treinos acessível separadamente.

### 6.4 Cuidados capilares

- Registrar rotinas/cuidados capilares.
- Permitir configurar uma rotina personalizada ou usar uma predefinição.
- Permitir registrar a execução real e reagendar quando necessário.
- Não forçar uma rotina única para todos os usuários.

### 6.5 Navegação e tela inicial

- Navegação inferior com **cinco destinos**.
- Tela inicial com equilíbrio entre resumo e ações rápidas.
- Atalhos de criação rápida para **hábito** e **tarefa**.
- Histórico em área separada, não misturado integralmente à tela inicial.
- Interface em tema escuro.
- Não enviar lembretes/notificações no MVP.

## 7. Decisões de produto aprovadas

| Área | Decisão |
|---|---|
| Navegação | Barra inferior com cinco destinos |
| Home | Equilibrada: resumo útil + ações rápidas |
| Atalhos | Criar hábito e tarefa |
| Histórico | Separado |
| Frequência dos hábitos | Flexível |
| Conclusão dos hábitos | Suportar os dois modos definidos pelo fluxo: registro direto e registro manual |
| Mudança de frequência | Perguntar como aplicar a mudança |
| Tarefas | Prazo opcional; atrasadas em vermelho |
| Tarefa atrasada | Oferecer reagendamento |
| Exclusão de tarefa | Confirmar antes |
| Concluídas | Ocultar por padrão |
| Treino | Iniciar, pausar, retomar; registrar manualmente; preservar estado |
| Cuidados capilares | Rotina personalizada ou predefinida; execução real e reagendamento |
| Tema | Escuro |
| Notificações | Sem lembretes no MVP |
| Exclusão | Desfazer quando possível |
| Fuso horário | `America/Sao_Paulo` |
| Armazenamento | Local, sem backup/sincronização no MVP |
| Gamificação | Discreta; sem XP, níveis ou moedas |
| Prazo | Sem deadline artificial para o MVP |

## 8. Fora do escopo do MVP

- Login, contas e perfis remotos.
- Sincronização entre dispositivos ou backup em nuvem.
- Lembretes e notificações.
- XP, níveis, moedas e economia virtual.
- Integrações externas.
- Recursos sociais.
- Painéis analíticos complexos.
- Qualquer funcionalidade que exija servidor próprio.

## 9. Requisitos não funcionais

- Funcionar offline após instalação.
- Persistir dados entre reinicializações do aplicativo.
- Interface utilizável em diferentes tamanhos de tela.
- Estados vazios e erros tratados de forma clara.
- Evitar perda silenciosa de dados.
- Manter código legível e testável.
- Usar acessibilidade básica: rótulos, contraste, áreas de toque e suporte a fontes maiores.
- Não coletar nem transmitir dados pessoais no MVP.

## 10. Métrica principal

**Consistência registrada ao longo do tempo**, mostrada por histórico compreensível e sem penalizar automaticamente ocorrências que o usuário ainda não classificou.

Não implementar pontuação de XP como substituto dessa métrica.

## 11. Critérios de aceite do MVP

- O usuário consegue criar e editar hábitos, tarefas, treinos e rotinas capilares.
- Os dados persistem localmente depois de fechar e reabrir o app.
- As cinco áreas de navegação estão acessíveis pela barra inferior.
- A tela inicial mostra resumo e atalhos de hábito/tarefa.
- O histórico está em uma área própria.
- Hábitos podem ser concluídos, pausados e retomados.
- Alterações de frequência pedem confirmação de como aplicar a regra.
- Tarefas com prazo vencido são destacadas em vermelho.
- Exclusão de tarefa exige confirmação e pode ser desfeita quando tecnicamente possível.
- Treino em andamento pode ser retomado após sair da tela.
- Não há notificações, login, nuvem ou gamificação com XP/níveis/moedas.
- Os fluxos principais têm testes adequados e não há falhas conhecidas que impeçam o uso básico.

## 12. Questões que podem ser refinadas depois

- Nomes e ícones finais dos cinco destinos da navegação.
- Detalhes visuais finais da tela inicial.
- Conjunto exato de frequências oferecidas no editor de hábitos.
- Detalhes do modelo de exercícios e séries dos treinos.
- Campos específicos das rotinas capilares.

Essas questões não devem bloquear a construção da fundação, desde que a implementação não invente regras de negócio irreversíveis.
