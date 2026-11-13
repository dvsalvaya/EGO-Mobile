# 03 — SDD: Software Design Document

## 1. Objetivo

Definir uma arquitetura Android nativa simples para o EGO Mobile, implementado em Kotlin. A arquitetura deve permitir desenvolver o MVP por partes, testar regras de negócio e evitar abstrações prematuras.

## 2. Decisões técnicas iniciais

| Área | Decisão |
|---|---|
| Linguagem | Kotlin |
| Plataforma | Android nativo |
| UI | Jetpack Compose, se suportado pela toolchain escolhida |
| Concorrência | Coroutines e Flow quando houver trabalho assíncrono/observável |
| Persistência estruturada | Room sobre SQLite |
| Preferências simples | DataStore apenas se houver configurações que precisem persistir |
| Arquitetura de UI | MVVM pragmático: Compose + ViewModel + repositório |
| Injeção de dependência | Manual no início; adicionar biblioteca só se a complexidade justificar |
| Navegação | Navigation Compose se compatível; manter navegação simples |
| Testes | JUnit para regras e ViewModel; testes de UI para fluxos críticos |
| Rede | Não necessária para o MVP |
| Autenticação | Não existe no MVP |
| Backup/sincronização | Fora do escopo |
| Fuso horário | `America/Sao_Paulo` para regras de data do produto |

**Compatibilidade:** confirmar as versões compatíveis de Android Gradle Plugin, Kotlin, Compose Compiler/plugin e dependências na documentação oficial e no projeto real antes de fixar versões. Não inventar versões no código sem verificar.

### Base inicial validada para o projeto

- Android Gradle Plugin **9.3.3**, com Kotlin integrado **2.2.10**; o plugin Compose usa a mesma versão do compilador Kotlin.
- Gradle Wrapper **9.5.0**; o AGP 9.3 requer Gradle 9.5.0 ou superior.
- `compileSdk` e `targetSdk` **37**, suportados pelo AGP 9.3; `minSdk` **24**.
- Compose BOM **2026.09.00** e Activity Compose **1.13.0**.
- SDK Build-Tools **36.0.0**, conforme a versão padrão do AGP 9.3.

Referências oficiais consultadas:

- [AGP 9.3 — compatibilidade](https://developer.android.com/build/releases/agp-9-3-0-release-notes)
- [Kotlin — compatibilidade do Kotlin Gradle Plugin](https://kotlinlang.org/docs/gradle-configure-project.html#apply-the-plugin)
- [Gradle — compatibilidade com Java](https://docs.gradle.org/current/userguide/compatibility.html)
- [Compose — configuração do compilador e dependências](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler)

## 3. Estrutura de pastas sugerida

Estrutura inicial, sujeita à organização real do projeto:

```text
app/src/main/java/<package>/
├── MainActivity.kt
├── app/
│   ├── EgoApp.kt
│   └── navigation/
│       └── AppNavigation.kt
├── core/
│   ├── time/
│   ├── ui/
│   └── result/
├── data/
│   ├── local/
│   │   ├── EgoDatabase.kt
│   │   ├── dao/
│   │   └── entity/
│   └── repository/
├── domain/
│   ├── model/
│   └── usecase/          # Criar apenas quando regras forem reutilizadas/complexas
└── feature/
    ├── home/
    ├── habits/
    ├── tasks/
    ├── routines/
    │   ├── workouts/
    │   └── haircare/
    └── history/
```

Não criar todas as classes/pastas vazias de uma vez. A estrutura é uma orientação para quando as funcionalidades forem implementadas.

## 4. Responsabilidades

### UI — Jetpack Compose
- Renderizar estado.
- Capturar ações do usuário.
- Não conter regras de negócio complexas.
- Não acessar DAOs diretamente.

### ViewModel
- Expor estado de tela observável.
- Receber eventos da UI.
- Coordenar operações de repositório.
- Executar trabalho assíncrono em `viewModelScope`.
- Não guardar estado durável apenas em memória quando ele precisar sobreviver ao fechamento do app.

### Repositórios
- Fornecer uma interface consistente para leitura e gravação dos dados.
- Isolar a UI e o domínio dos detalhes do Room.
- Não criar interfaces de repositório para toda classe automaticamente; manter a abstração proporcional à necessidade.

### Room / DAO
- Persistir entidades estruturadas localmente.
- Fornecer consultas e operações transacionais.
- Ser a fonte de verdade dos dados persistentes no MVP.

### Domain
- Conter modelos e regras que não dependam de componentes visuais.
- Criar casos de uso quando uma regra tiver complexidade, reutilização ou exigir teste isolado.
- Evitar uma camada de domínio artificial para operações CRUD triviais.

## 5. Modelo de dados inicial

O esquema final deve ser validado antes da implementação, mas o MVP provavelmente precisa das seguintes entidades.

### Habit
- `id`
- `name`
- `description` opcional
- `frequency`/configuração de frequência
- `status` (ativo, pausado, arquivado)
- `createdAt`
- `updatedAt`

### HabitOccurrence
- `id`
- `habitId`
- `scheduledDate` ou referência temporal da ocorrência
- `status` (pendente, concluída e outros estados explicitamente definidos pelo produto)
- `completedAt` opcional
- metadados necessários para não reescrever o histórico quando a frequência mudar

### Task
- `id`
- `title`
- `description` opcional
- `dueAt` opcional
- `status` (aberta, concluída, excluída/arquivada conforme implementação)
- `createdAt`
- `completedAt` opcional
- dados de recuperação temporária se for implementado desfazer exclusão

### WorkoutSession
- `id`
- `startedAt`
- `endedAt` opcional
- `status` (em andamento, pausado, concluído)
- dados necessários para retomar a sessão

### WorkoutExercise
- `id`
- `sessionId`
- `name`
- ordem e campos de registro manual definidos durante a implementação

### HaircareRoutine
- `id`
- `name`
- origem (personalizada ou predefinição)
- configuração/frequência
- `status`

### HaircareOccurrence
- `id`
- `routineId`
- data prevista/real conforme necessário
- estado e registro de execução real
- dados de reagendamento quando aplicável

### AppPreference (opcional)
Usar DataStore para preferências simples, como opções de interface. Não duplicar em DataStore os dados de negócio já mantidos no Room.

**Regra:** não implementar todos os campos hipotéticos antes de fechar os fluxos. Persistir apenas o necessário para requisitos aprovados.

## 6. Regras de data e tempo

- Usar tipos de data/tempo apropriados em vez de armazenar datas como texto formatado para exibição.
- Distinguir uma data civil (por exemplo, dia de ocorrência) de um instante exato (por exemplo, horário de conclusão).
- Aplicar `America/Sao_Paulo` às regras de calendário do produto.
- Formatar datas somente na camada de apresentação.
- Testar mudanças de dia, limites de semana/mês e conversões de fuso quando relevantes.

## 7. Estado e fluxo de dados

Fluxo preferencial:

```text
Ação do usuário
      ↓
Composable
      ↓ evento
ViewModel
      ↓
Repository
      ↓
Room DAO / Database
      ↓ Flow ou resultado
ViewModel
      ↓ estado de UI
Composable atualizado
```

A UI não deve ser a fonte de verdade para dados persistentes. Após salvar, a tela deve refletir o estado armazenado, não apenas uma alteração visual temporária.

## 8. Navegação

- Cinco destinos principais na barra inferior.
- Início, Hábitos, Tarefas, Rotinas e Histórico.
- Criar hábito e criar tarefa devem ser acessíveis a partir da tela inicial.
- A navegação não deve recriar sessões de treino ou duplicar registros.
- Estado de tela que precisa sobreviver à rotação deve ser tratado pelo ViewModel; estado de negócio durável deve ser persistido.

## 9. Tratamento de erros

- Validar entradas antes de salvar.
- Exibir mensagens claras e acionáveis.
- Não ignorar exceções silenciosamente.
- Não apresentar sucesso se a gravação falhar.
- Usar transações quando uma operação modificar entidades relacionadas.
- Registrar logs técnicos sem incluir dados pessoais desnecessários.

## 10. Estratégia de testes

### Unitários
- Regras de frequência e mudança de frequência.
- Classificação e reagendamento de tarefas atrasadas.
- Conclusão/pausa/retomada de hábitos.
- Retomada de treino.
- Regras de datas no fuso definido.

### Repositório/Room
- Criar, ler, atualizar e excluir registros.
- Persistência após recriar o acesso ao banco.
- Relações entre entidades.
- Operações transacionais.

### UI/instrumentados
- Navegação entre os cinco destinos.
- Criar hábito e tarefa.
- Confirmar/cancelar exclusão.
- Reagendar tarefa vencida.
- Pausar e retomar treino.
- Exibir corretamente listas vazias e estados especiais.

## 11. Segurança e privacidade

- Os dados permanecem locais no MVP.
- Não adicionar permissões de rede ou integrações sem necessidade.
- Não armazenar credenciais porque o produto não tem login.
- Não prometer backup ou recuperação após desinstalação.
- Evitar logs com conteúdo privado do usuário.

## 12. Diretrizes de implementação Kotlin

- Preferir `val` a `var` quando mutabilidade não for necessária.
- Usar `data class` para modelos de dados quando adequado.
- Usar tipos anuláveis explicitamente e validar valores que possam ser nulos.
- Evitar `!!` salvo quando houver uma garantia forte e documentada; preferir tratamento explícito.
- Usar coroutines para operações assíncronas, sem bloquear a thread principal.
- Manter Composables pequenos e orientados a estado.
- Não misturar SQL/DAO, lógica de negócio e UI no mesmo arquivo.
- Não converter toda lógica em interfaces abstratas sem necessidade.
- Não adicionar bibliotecas apenas por preferência; justificar pelo problema que resolvem.

## 13. Decisões que devem permanecer simples

- Não introduzir Clean Architecture completa por padrão.
- Não criar backend para um produto que deve funcionar localmente.
- Não adicionar autenticação, notificações ou sincronização.
- Não implementar gamificação com XP/níveis/moedas.
- Não criar uma abstração genérica para cada entidade.
- Não adotar bibliotecas ou versões sem verificar compatibilidade com a toolchain.

## 14. Definição de pronto para cada funcionalidade

Uma funcionalidade é considerada pronta quando:
- O fluxo feliz funciona.
- Entradas inválidas e cancelamentos são tratados.
- Os dados persistem corretamente.
- Os estados vazios e erros são apresentados.
- Testes relevantes passam.
- A interface permanece consistente após navegar para fora e voltar.
- Nenhuma regra aprovada do PRD é violada.
