# Spec 10 — Estabilização, Correção de Bugs e Integridade Funcional

- **Status:** Draft para implementação
- **Base:** `FIX.md` — auditoria de QA das Specs 1–8, realizada entre 14/11/2026 e 16/11/2026
- **Commit auditado:** `7aa20d4`
- **Plataforma:** Android — Kotlin, Jetpack Compose, Room
- **Escopo:** BUG-001 a BUG-014
- **Fora do escopo:** SUS-001 a SUS-008 (investigação reservada para uma Spec futura)

---

## 1. Objetivo

Corrigir todos os 14 bugs confirmados no relatório `FIX.md`, restaurando a estabilidade do aplicativo, a precisão das regras de negócio, a completude das funcionalidades de hábitos e a preservação do estado de navegação.

A implementação deve preservar os dados existentes e a arquitetura atual do projeto. Não faz parte desta Spec redesenhar a identidade visual ou antecipar o trabalho de UI/UX da Spec 9, exceto pelos ajustes visuais estritamente necessários para corrigir os bugs descritos aqui.

## 2. Contexto e diagnóstico

A auditoria identificou:

- 1 bug P0: crash no histórico/conclusão de treino.
- 3 bugs P1: dois crashes na criação de hábitos e falha do `lint` por uso de `java.time` com `minSdk = 24`.
- 9 bugs P2: cronômetro pausado, prazo de tarefa no dia atual, frequência 3x/semana, validação silenciosa, seleção visual de frequência, feedback de conclusão, pausa de hábitos, restauração da navegação e funcionalidades ausentes de edição/arquivamento/mudança de frequência.
- 1 bug P3: quebra de linha no chip `3x/sem`.

O relatório também registra oito suspeitas não confirmadas. Elas não devem ser corrigidas ou incorporadas automaticamente nesta Spec; devem permanecer registradas para investigação separada.

## 3. Objetivos específicos

1. Eliminar o crash causado por listas roláveis aninhadas no histórico de treinos.
2. Impedir que entradas inválidas de hábitos derrubem o aplicativo.
3. Fazer `gradlew lint` passar mantendo `minSdk = 24`.
4. Fazer o cronômetro excluir o tempo em que o treino permaneceu pausado.
5. Corrigir a regra de atraso de tarefas com prazo no dia atual.
6. Definir frequência `N vezes por semana` por quantidade de conclusões, sem dias fixos.
7. Tornar explícitos os erros de validação do formulário de hábitos.
8. Indicar visualmente a frequência selecionada.
9. Exibir o estado de conclusão do hábito na aba Hábitos e permitir desfazer a conclusão do dia.
10. Tornar acessíveis as ações de pausar e retomar hábitos.
11. Preservar a aba atual durante recriação da Activity, incluindo rotação.
12. Corrigir o layout do chip `3x/sem`.
13. Implementar edição, arquivamento e alteração de frequência de hábitos, incluindo confirmação quando a frequência mudar.

## 4. Escopo

### 4.1 Incluído

- BUG-001 a BUG-014, conforme descritos no `FIX.md`.
- Ajustes de domínio, ViewModels, repositórios, DAOs, UI Compose e configuração Gradle necessários para os bugs.
- Testes unitários, testes de integração disponíveis no projeto e testes manuais/instrumentados dos fluxos afetados.
- Migrações Room somente se forem indispensáveis às correções desta Spec.
- Verificação de regressão de persistência dos dados já existentes.

### 4.2 Excluído

- Investigação ou correção de SUS-001 a SUS-008.
- Redesenho amplo da UI/UX ou da identidade visual da Spec 9.
- Funcionalidades novas não relacionadas aos 14 bugs confirmados.
- Novo ciclo de cuidados capilares e reagendamento de cuidados para hoje, citados como suspeitas no relatório.

## 5. Decisões de produto

### 5.1 Frequência por quantidade semanal (`TimesPerWeek`)

A frequência `N vezes por semana` não define dias fixos. O usuário pode concluir o hábito em quaisquer dias da semana.

Regras:

- A semana segue o padrão ISO: segunda-feira a domingo.
- O hábito fica elegível para conclusão enquanto o total de ocorrências concluídas na semana atual for menor que `N`.
- Ao atingir `N` conclusões na semana, o hábito é considerado concluído para aquela semana e deixa de ser apresentado como pendente/previsto para conclusão até o início da próxima semana.
- O progresso deve ser representável como `concluídas/N` (por exemplo, `2/3`).
- No início da semana seguinte, a contagem semanal recomeça, sem apagar ocorrências anteriores.
- Não selecionar, sortear ou persistir dias específicos da semana.
- A conclusão rápida não deve permitir gerar ocorrências adicionais além da meta semanal, salvo se o fluxo de desfazer remover primeiro uma ocorrência válida.
- A regra deve ser aplicada de forma consistente na Home, na aba Hábitos, nas estatísticas e no histórico, respeitando o significado de cada tela.

### 5.2 Prazo de tarefa

Uma tarefa cuja data de vencimento seja hoje não está atrasada durante o dia de hoje. Ela passa a estar atrasada a partir do próximo dia civil, se continuar incompleta.

A comparação deve usar a data civil no fuso horário definido pelo projeto, e não comparar diretamente meia-noite do prazo com o timestamp atual.

### 5.3 Treino pausado

O tempo pausado não integra a duração efetiva do treino. O cronômetro deve congelar ao pausar e continuar do mesmo valor ao retomar. O valor final salvo no histórico deve excluir todos os intervalos pausados.

### 5.4 Hábitos: conclusão, pausa e arquivamento

- Um hábito concluído hoje deve ter estado visual inequívoco na aba Hábitos.
- Deve existir uma ação para desfazer a conclusão do dia, sem duplicar ocorrências.
- Pausar e retomar devem ser ações alcançáveis na interface.
- Arquivar deve preservar o histórico e as ocorrências já registradas.
- Hábitos arquivados não devem aparecer entre hábitos ativos ou pausados nem ser elegíveis para novas conclusões.

### 5.5 Alteração de frequência

Ao alterar a frequência de um hábito, exibir confirmação explicando que as ocorrências anteriores serão preservadas e que a nova frequência será aplicada dali em diante. Não apagar nem reescrever ocorrências históricas.

## 6. Requisitos funcionais e critérios de aceitação

### RF-01 — Histórico de treinos sem crash (BUG-001, P0)

- **Descrição:** Corrigir a composição de `WorkoutHistoryView` dentro da tela de Rotinas para não medir um componente verticalmente rolável com altura máxima infinita.
- **Critérios de aceitação:**
  - O histórico com zero, um ou vários treinos concluídos abre sem crash.
  - Finalizar um treino e acessar o histórico não encerra o processo.
  - A tela mantém rolagem funcional.
  - Não há `LazyColumn` vertical aninhado sem restrição de altura no fluxo afetado.

### RF-02 — Validação segura do nome do hábito (BUG-002, P1)

- Nome obrigatório, após `trim`, com 1 a 80 caracteres.
- Entradas inválidas exibem erro no formulário ou mensagem equivalente.
- A tentativa de salvar não encerra o aplicativo.
- A regra de domínio continua protegida mesmo que a UI seja contornada.

### RF-03 — Validação segura da descrição do hábito (BUG-003, P1)

- Descrição opcional com no máximo 250 caracteres.
- A UI impede ou sinaliza claramente o excesso de caracteres.
- O ViewModel trata falhas de validação sem crash.
- Nome e descrição são validados de maneira consistente nos pontos de criação disponíveis (Home e aba Hábitos).

### RF-04 — Compatibilidade de `java.time` e lint (BUG-004, P1)

- Manter `minSdk = 24`.
- Configurar Core Library Desugaring corretamente no módulo Android e adicionar a dependência compatível com a versão do projeto.
- `gradlew lint` deve concluir sem os erros `NewApi` identificados.
- `gradlew test` e `gradlew assembleDebug` devem continuar passando.
- Não elevar `minSdk` como atalho para eliminar o alerta.

### RF-05 — Duração correta de treino pausado (BUG-005, P2)

- O modelo de sessão deve representar o instante da pausa e/ou a duração acumulada das pausas.
- Ao pausar, a duração exibida congela.
- Ao retomar, o cronômetro continua sem incluir o intervalo pausado.
- Múltiplos ciclos pausar/retomar são contabilizados corretamente.
- Ao finalizar, a duração persistida e exibida no histórico exclui o tempo pausado.
- Sessões pausadas que sobrevivam à morte do processo mantêm cálculo consistente após reabertura.

### RF-06 — Prazo hoje não é atraso (BUG-006, P2)

- Uma tarefa incompleta com vencimento hoje não recebe estado/badge de atrasada durante o dia.
- A tarefa passa a atrasada no próximo dia civil.
- Tarefas com vencimento anterior continuam atrasadas; tarefas futuras continuam dentro do prazo.
- Testes devem cobrir limites de mudança de dia e o fuso horário adotado pelo projeto.

### RF-07 — Frequência semanal por contagem (BUG-007 e SUS-008 relacionado ao comportamento, sem incorporar a investigação geral)

- Para `N vezes por semana`, considerar o número de conclusões registradas na semana ISO atual.
- Qualquer dia da semana pode contar para a meta; não há dias predefinidos.
- O hábito permanece elegível até alcançar `N` conclusões.
- Ao alcançar a meta, exibir progresso completo e não oferecer nova conclusão naquela semana.
- A contagem reinicia na semana seguinte sem apagar o histórico.
- A Home e a aba Hábitos devem apresentar o mesmo estado de elegibilidade e progresso.
- A conclusão rápida não pode registrar uma ocorrência em dia em que a meta semanal já foi atingida.
- A regra deve funcionar para diferentes valores de `N` suportados pelo domínio, não apenas `3`.

### RF-08 — Erro visível para nome vazio (BUG-008, P2)

- Salvar com nome vazio ou contendo apenas espaços exibe erro associado ao campo Nome.
- O diálogo permanece aberto e nenhum hábito é persistido.
- Ao corrigir o campo, o erro é removido de forma coerente.

### RF-09 — Frequência selecionada visível (BUG-009, P2)

- A opção atualmente selecionada deve ter aparência distinta das opções não selecionadas.
- A seleção visual acompanha imediatamente a mudança de estado.
- A frequência salva corresponde à opção visualmente selecionada.
- O estado selecionado deve ser acessível por tecnologias assistivas.

### RF-10 — Estado de conclusão na aba Hábitos (BUG-010, P2)

- O cartão recebe/observa informação suficiente para identificar a conclusão aplicável ao dia ou à semana.
- Hábitos diários concluídos exibem estado inequívoco, como `Concluído hoje`.
- Hábitos `N vezes por semana` exibem progresso, por exemplo `2/3`, e estado completo ao atingir a meta.
- A UI atualiza imediatamente após concluir ou desfazer.
- Deve existir ação explícita para desfazer a conclusão atual, quando aplicável.
- Desfazer remove/reverte somente a ocorrência correspondente, sem afetar ocorrências anteriores.

### RF-11 — Pausar e retomar hábito pela UI (BUG-011, P2)

- Hábitos ativos oferecem ação de pausar alcançável, sem remover a ação de conclusão rápida.
- Hábitos pausados aparecem na seção apropriada e oferecem ação de retomar.
- Após pausar, o hábito não deve ser apresentado como elegível para conclusão.
- Após retomar, volta à lista de ativos e segue a regra de frequência vigente.
- Não deve existir ramo de UI inacessível por ordem incorreta de condições.

### RF-12 — Preservar navegação após recriação da Activity (BUG-012, P2)

- A aba atual é restaurada após rotação/recriação da Activity.
- O estado deve sobreviver ao mecanismo normal de salvamento/restauração do Android.
- Não regredir navegação normal entre as cinco abas.
- Se houver subestado de navegação já explicitamente mantido pelo projeto, preservar o comportamento existente; a preservação de subaba de Rotinas citada em SUS-007 não faz parte desta Spec.

### RF-13 — Chip `3x/sem` legível (BUG-013, P3)

- O rótulo é exibido em uma única linha em larguras suportadas.
- O chip mantém tamanho e alinhamento coerentes com as demais opções.
- Não há corte ou sobreposição do texto em tamanhos de fonte suportados.

### RF-14 — Editar, arquivar e alterar frequência (BUG-014, P2)

- **Editar:** permitir alterar nome e descrição de um hábito existente; reutilizar o formulário de criação quando adequado.
- **Alterar frequência:** disponibilizar ação na UI e conectar ao fluxo de atualização existente (`onUpdateFrequency`), com confirmação antes de aplicar.
- **Preservação:** ocorrências anteriores permanecem intactas após edição ou mudança de frequência.
- **Arquivar:** disponibilizar ação de arquivamento para hábitos ativos e pausados.
- **Estado arquivado:** hábito deixa as listas de ativos/pausados e não pode receber novas conclusões.
- **Persistência:** alterações continuam presentes após fechar e reabrir o aplicativo.
- **Feedback:** sucesso, cancelamento e falha são comunicados ao usuário.
- Não implementar exclusão física como substituto do arquivamento.

## 7. Requisitos não funcionais

- **RNF-01 — Estabilidade:** nenhum dos fluxos descritos deve causar crash.
- **RNF-02 — Integridade:** nenhuma correção pode apagar ocorrências ou histórico já persistidos.
- **RNF-03 — Compatibilidade:** manter suporte declarado a API 24 em diante.
- **RNF-04 — Consistência:** regras de domínio devem ficar na camada apropriada, sem duplicação divergente entre telas.
- **RNF-05 — Responsividade:** estados de carregamento, sucesso e erro devem atualizar sem exigir reiniciar a tela.
- **RNF-06 — Testabilidade:** regras de datas, duração e frequência devem ser testáveis com relógio/data controláveis, evitando dependência do relógio real nos testes.

## 8. Restrições técnicas

- Preservar Kotlin, Jetpack Compose, Room e a organização de camadas existente.
- Evitar reescritas arquiteturais amplas.
- Não introduzir dependências sem justificativa técnica.
- Não remover validações do domínio para resolver crashes.
- Não usar `REPLACE` ou alterar estratégia de persistência como parte desta Spec sem necessidade demonstrada; SUS-001 será tratado separadamente.
- Se uma migração Room for necessária, ela deve preservar registros existentes e incluir teste de migração.
- Centralizar regras de data/fuso e semana para evitar diferenças entre Home, Hábitos e Histórico.

## 9. Estratégia de testes e validação

### 9.1 Testes automatizados

- Testes unitários de domínio para duração de sessão, pausa/retomada, atraso de tarefas e frequência semanal.
- Testes de ViewModel para entradas inválidas, feedback de erro, conclusão/desfazer, pausa/retomada, edição, arquivamento e alteração de frequência.
- Testes de repositório/DAO para persistência e preservação das ocorrências.
- Testes de UI ou instrumentados para histórico de treinos, formulários de hábito, ações dos cartões e restauração da navegação, conforme a infraestrutura existente.
- Testes de migração Room caso o schema seja alterado.

### 9.2 Cenários mínimos de regressão

1. Abrir histórico sem treinos, com um treino e com vários treinos.
2. Finalizar treino e abrir histórico.
3. Tentar salvar hábito com nome vazio, 81 caracteres e descrição com 251 caracteres.
4. Criar hábito válido pela Home e pela aba Hábitos.
5. Rodar `lint`, `test` e `assembleDebug`.
6. Pausar por um intervalo conhecido, retomar e finalizar treino; confirmar que a pausa não entrou na duração.
7. Criar tarefa com prazo hoje e verificar que só atrasa no dia seguinte.
8. Completar hábito 3x/sem em quaisquer três dias; verificar progresso e bloqueio após 3/3; verificar reinício na semana seguinte.
9. Concluir e desfazer hábito e verificar Home, Hábitos e Histórico.
10. Pausar e retomar hábito pela interface.
11. Rotacionar o aparelho em cada uma das cinco abas e verificar restauração.
12. Editar, alterar frequência e arquivar hábito; fechar/reabrir app e verificar persistência e histórico.

### 9.3 Comandos de qualidade

Executar a partir do projeto Android, usando o wrapper Gradle:

```powershell
.\gradlew.bat test
.\gradlew.bat lint
.\gradlew.bat assembleDebug
```

Executar também os testes instrumentados disponíveis no repositório e registrar claramente qualquer limitação de ambiente.

## 10. Critérios de conclusão da Spec

A Spec 10 só pode ser considerada concluída quando:

- RF-01 a RF-14 estiverem implementados.
- Todos os critérios de aceitação aplicáveis tiverem evidência de teste.
- `test`, `lint` e `assembleDebug` passarem.
- Os fluxos manuais de regressão tiverem sido executados.
- Não houver perda de dados existentes causada pelas correções.
- O relatório final listar arquivos alterados, decisões técnicas, testes executados, resultados e limitações restantes.
- SUS-001 a SUS-008 permanecerem explicitamente fora do escopo, sem serem apresentados como resolvidos.

## 11. Rastreabilidade

| Bug | Requisito |
|---|---|
| BUG-001 | RF-01 |
| BUG-002 | RF-02 |
| BUG-003 | RF-03 |
| BUG-004 | RF-04 |
| BUG-005 | RF-05 |
| BUG-006 | RF-06 |
| BUG-007 | RF-07 |
| BUG-008 | RF-08 |
| BUG-009 | RF-09 |
| BUG-010 | RF-10 |
| BUG-011 | RF-11 |
| BUG-012 | RF-12 |
| BUG-013 | RF-13 |
| BUG-014 | RF-14 |

## 12. Referências

- `FIX.md` — Relatório de QA (auditoria Specs 1–8), 14/11/2026 a 16/11/2026.
- Commit auditado: `7aa20d4`.
