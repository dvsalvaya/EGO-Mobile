# Fase 2 — Persistência local

## 1. Identificador e nome

- **ID:** `fase-2-persistencia-local`
- **Nome:** Persistência local com Room
- **Status:** `VERIFIED`
- **Aprovação:** Aprovado pelo responsável do produto em 2026-10-28. Decisões registradas: Q1 (uso de `HabitEntity` como pioneira) e Q2 (gerenciamento singleton manual/provider).

Esta especificação está aprovada e autorizada para decomposição e implementação técnica conforme os portões do projeto.

---

## 2. Problema e objetivo

O aplicativo EGO Mobile é um produto estritamente local (offline-first, sem backend/rede ou autenticação externa no MVP) voltado para acompanhamento de hábitos, tarefas e rotinas com total privacidade. Para viabilizar a retenção das informações além do ciclo de vida da memória da aplicação, é necessário introduzir a camada de persistência relacional local.

O objetivo da Fase 2 é configurar a infraestrutura do Room sobre SQLite, definir o banco de dados da aplicação (`EgoDatabase`), implementar a primeira entidade/DAO/repositório e garantir que operações fundamentais de leitura, escrita e atualização persistam de forma determinística e testável mesmo após o encerramento do processo do app.

---

## 3. Referências e decisões aprovadas

- **`docs/01-PRD.md`**:
  - Seção *Decisões de produto aprovadas*: Armazenamento 100% local no dispositivo; sem backend, sem cadastro, sem telemetria nem sincronização externa.
  - Seção *Requisitos não funcionais*: Persistência confiável no SQLite/Room; dados mantidos no sandbox do app.
- **`docs/03-SDD.md`**:
  - Seção *2. Decisões técnicas iniciais*: Persistência estruturada em Room sobre SQLite; compatibilidade de toolchain; Coroutines e Flow para concorrência e dados observáveis; isolamento via Repositório (MVVM pragmático).
  - Seção *4. Responsabilidades*: Repositório isola a UI e o domínio do Room; Room/DAO como fonte de verdade dos dados duráveis.
  - Seção *5. Modelo de dados inicial*: Estrutura para `Habit` / `HabitOccurrence` e `Task`.
  - Seção *6. Regras de data e tempo*: Armazenamento com tipos de data/tempo apropriados (não texto formatado para UI); fuso `America/Sao_Paulo`.
- **`docs/04-ROADMAP.md`**:
  - Seção *Fase 2 — Persistência local*: Adicionar Room com versões compatíveis; criar banco e primeira entidade; implementar DAO e repositório; testar criação, leitura e atualização; confirmar persistência após fechar e reabrir o app.
  - Critério de conclusão: *Os dados de teste permanecem salvos localmente.*
- **`docs/specs/README.md`**:
  - Ciclo de especificação e status (`DRAFT → APPROVED → IN_PROGRESS → VERIFIED`).

---

## 4. Escopo

1. Configuração do Room e do KSP (Kotlin Symbol Processing) no catálogo de dependências (`libs.versions.toml`) e `app/build.gradle.kts`, com compatibilidade confirmada para Kotlin 2.2.10 e AGP 9.3.3.
2. Definição do banco de dados Room da aplicação (`EgoDatabase`), com exportação de esquema configurada para fins de controle de versão de migrations futuras.
3. Criação da primeira entidade de domínio para persistência, alinhada com as próximas fases do Roadmap (por exemplo, `HabitEntity` para atender à Fase 3 ou entidade inicial aprovada).
4. Implementação da interface DAO correspondente com operações assíncronas (via Kotlin Coroutines / `suspend` e `Flow` para consultas observáveis).
5. Implementação da camada de Repositório correspondente, abstraindo os detalhes de acesso ao DAO e desacoplando os modelos de UI/domínio dos modelos de tabela.
6. Suíte de testes automatizados de integração/DAO em memória (`inMemoryDatabaseBuilder`) validando inserção, consulta, atualização e exclusão/idempotência.
7. Validação de persistência em disco real que comprove que dados persistem entre encerramento e reinicialização da aplicação.

---

## 5. Exclusões

- Implementar interface visual final completa de cadastro ou listagem de hábitos/tarefas (escopo das Fases 3 e 4).
- Criar esquemas para todas as entidades do sistema (sessões de treino, haircare, etc.) antes da necessidade real de cada fase.
- Integrações com nuvem, backup remoto, exportação em arquivos externos ou criptografia complexa fora dos padrões nativos do Android.
- Inclusão de bibliotecas pesadas de injeção de dependência (Hilt/Koin) nesta fase, mantendo o padrão de injeção manual pragmática estabelecido no SDD.
- Alteração ou remoção de requisitos aprovados no PRD ou SDD.

---

## 6. Comportamentos esperados

1. Ao executar operações de gravação através do repositório, os dados devem ser persistidos no SQLite subjacente de forma atômica e consistente.
2. Consultas que retornam listas devem fornecer um `Flow` observável reativo, emitindo automaticamente novos estados sempre que os dados na tabela sofrerem mutação (inserção, atualização, remoção).
3. Ao finalizar o processo da aplicação (reinicialização/fechamento forçado) e reabri-la, os registros persistidos previamente devem permanecer disponíveis e inalterados.
4. Falhas em operações de banco (violações de chave primária ou concorrência) devem ser tratadas sem causar crashes fatais descontrolados para a aplicação.

---

## 7. Regras de negócio

1. **Privacidade e Sandbox:** Toda base de dados SQLite/Room deve residir estritamente no armazenamento interno seguro do app (`context.filesDir` / diretório padrão de databases do Android).
2. **Separação de Modelos:** A entidade do Room (`*Entity`) pertence à camada de infraestrutura/dados (`data/local/entity`). Ela não deve ser exposta diretamente para a camada de apresentação; conversões para o modelo de domínio puro (`domain/model`) devem ocorrer no repositório.
3. **Consistência Temporal:** Campos de timestamp (ex.: `createdAt`, `updatedAt`) devem armazenar instantes canônicos (UTC ou epoch millis) e ser convertidos considerando o fuso horário oficial do produto (`America/Sao_Paulo`) quando tratados em regras de data civil.
4. **Fonte da Verdade:** O banco de dados local é a única fonte da verdade para o estado do usuário; estados em memória de ViewModels não devem ser considerados definitivos até a confirmação da transação no repositório.

---

## 8. Critérios de aceite verificáveis

1. **Compilação e Toolchain:** KSP e Room runtime / compiler configurados com versões válidas e compatíveis com Kotlin 2.2.10, executando build sem erros ou advertências impeditivas.
2. **Definição do Banco:** Classe abstrata `EgoDatabase` estendendo `RoomDatabase`, registrando a versão 1 do schema e a primeira entidade.
3. **Contrato de Acesso (DAO):** Interface DAO contendo métodos `suspend` para inserção, atualização e busca por ID, além de consulta retornando `Flow` da lista.
4. **Contrato do Repositório:** Repositório implementado expondo funções suspensas e fluxos desacoplados das entidades do Room.
5. **Cobertura de Testes de Persistência:** Testes automatizados executados via Gradle cobrindo:
   - Inserção com sucesso e conferência de geração de ID.
   - Consulta reativa via `Flow` refletindo nova inserção.
   - Atualização de registro existente conferindo alteração de campos.
   - Leitura de registro inexistente retornando nulo ou resultado esperado.
6. **Validação de Ciclo de Vida do Processo:** Teste/verificação documental que comprove que dados salvos em uma instância de banco persistente continuam legíveis após recriação da instância do banco com o mesmo arquivo de banco.

---

## 9. Fluxos principais e alternativos

### Fluxo Principal 1: Inserção e Leitura Reativa
1. Repositório recebe uma entidade de domínio a ser criada.
2. Repositório mapeia o domínio para a entidade Room e delega ao DAO em uma rotina assíncrona.
3. DAO executa a inserção no SQLite.
4. O `Flow` observador emite a nova lista contendo o registro recém-adicionado.

### Fluxo Principal 2: Atualização de Registro Existente
1. Repositório recebe comando de atualização com dados alterados de um registro existente.
2. DAO localiza o registro pela chave primária e aplica as alterações.
3. Observadores ativos do `Flow` recebem a projeção atualizada.

### Fluxo Alternativo: Tentativa de Acesso a Registro Inexistente
1. Repositório solicita busca por um identificador que não consta na tabela.
2. DAO retorna `null`.
3. Repositório traduz para o resultado seguro de domínio (ex.: `Result` de sucesso com valor nulo ou erro mapeado), sem lançar exceção não capturada.

---

## 10. Estados de erro e casos-limite

- **Conflito de ID ou Chave Primária:** Definir estratégia no DAO (ex.: `OnConflictStrategy.REPLACE` ou `ABORT` conforme semântica do caso de uso).
- **Dados nulos em colunas obrigatórias:** Validação no nível de tipos Kotlin (não permitir nulos no modelo de domínio e entidade onde a regra proíbe).
- **Concorrência de escrita assíncrona:** Acesso orquestrado por Coroutines / SQLite dispatcher garantindo que transações não bloqueiem a Main Thread (UI).
- **Reinicialização de instância:** Instância de `EgoDatabase` deve seguir padrão de instância única (Singleton / factory compartilhada) para evitar conflitos de múltiplos pools SQLite sobre o mesmo arquivo de banco.

---

## 11. Persistência e privacidade

- **Persistência:** Banco relacional SQLite via biblioteca Room. Banco nomeado `ego_mobile.db`.
- **Privacidade:** Armazenado exclusivamente no diretório de dados privados do pacote da aplicação (`/data/data/com.ego.mobile/databases/`).
- Não há coleta de dados, analytics, relatórios de crash remotos ou tráfego de rede envolvidos.

---

## 12. Dependências e restrições técnicas

- Adicionar plugin KSP compatível com Kotlin 2.2.10 (ex.: `com.google.devtools.ksp`).
- Dependências Room: `androidx.room:room-runtime`, `androidx.room:room-ktx`, processador `androidx.room:room-compiler` via KSP.
- Compatibilidade verificada: as versões de Room e KSP devem ser checadas rigorosamente contra o catálogo e o suporte do Kotlin 2.2.10.
- Nenhuma dependência externa à Google/AndroidX/JetBrains deve ser introduzida sem aprovação técnica.

---

## 13. Decisões registradas

- **Q1: Qual deve ser a primeira entidade implementada como pioneira da persistência?**
  - *Decisão aprovada:* Utilizar `HabitEntity` (junto a `HabitDao` e `HabitRepository`) como entidade pioneira para estruturar a base direta da Fase 3. Caso no futuro haja necessidade de interface mais genérica, poderá ser introduzido `TaskEntity`.
  - *Data da decisão:* 2026-10-28.
- **Q2: Modo de instância do banco no ciclo de vida do app:**
  - *Decisão aprovada:* Adotar singleton manual (`EgoDatabaseProvider`) mantido no contexto da aplicação sem frameworks de DI pesados, conforme diretrizes do SDD.
  - *Data da decisão:* 2026-10-28.
