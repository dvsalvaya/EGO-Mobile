# Spec 10 — Estabilização, Correção de Bugs e Integridade Funcional

- **Status:** `DRAFT`
- **Base:** [FIX.md](../../../FIX.md), auditoria histórica das Specs 1–8 no commit `7aa20d4`
- **Plataforma:** Android — Kotlin, Jetpack Compose, Room
- **Escopo:** recuperação do build e correções funcionais revalidadas. RF-06 adota data civil, conforme a decisão confirmada no planejamento da V1. RF-09 e RF-13 ficam com a nova UI.
- **Fora do escopo:** SUS-001 a SUS-008 (investigação reservada para uma Spec futura)

---

## 1. Objetivo

Restaurar um build instalável, corrigir os problemas funcionais que ainda se aplicam e preservar o histórico existente. A base estabilizada deve permitir implementar o redesenho e repetir a regressão nas telas novas.

A implementação deve preservar os dados existentes e a arquitetura atual do projeto. Não faz parte desta Spec redesenhar a identidade visual ou antecipar o trabalho de UI/UX da Spec 9, exceto pelos ajustes visuais estritamente necessários para corrigir os bugs descritos aqui.

## 2. Contexto e diagnóstico

A auditoria serve de referência para reprodução. Alguns componentes mudaram depois do commit auditado. Na verificação mais recente, `testDebugUnitTest assembleDebug` parou em `:app:compileDebugKotlin` com erros em navegação, tema e telas. Recuperar o build é pré-requisito para confirmar o estado dos fluxos atuais.

O relatório também registra oito suspeitas não confirmadas. Elas não devem ser corrigidas ou incorporadas automaticamente nesta Spec; devem permanecer registradas para investigação separada.

## 3. Objetivos específicos

1. Eliminar o crash causado por listas roláveis aninhadas no histórico de treinos.
2. Impedir que entradas inválidas de hábitos derrubem o aplicativo.
3. Fazer `gradlew lint` passar mantendo `minSdk = 24`.
4. Fazer o cronômetro excluir o tempo em que o treino permaneceu pausado.
5. Aplicar a regra de vencimento por data civil compartilhada com a Spec 9.
6. Definir frequência `N vezes por semana` por quantidade de conclusões, sem dias fixos.
7. Tornar explícitos os erros de validação do formulário de hábitos.
8. Levar a indicação visual e acessível da frequência selecionada para a Spec 9.
9. Exibir o estado de conclusão do hábito na aba Hábitos e permitir desfazer a conclusão do dia.
10. Tornar acessíveis as ações de pausar e retomar hábitos.
11. Preservar a aba atual durante recriação da Activity, incluindo rotação.
12. Levar a legibilidade do rótulo de meta semanal para a Spec 9.
13. Implementar edição, arquivamento e alteração de frequência de hábitos, incluindo confirmação quando a frequência mudar.

## 4. Escopo

### 4.1 Incluído

- RF-00 e os requisitos funcionais desta fase, com evidência atual por item. Um achado histórico que já esteja resolvido pode ser encerrado com evidência de regressão.
- Ajustes de domínio, ViewModels, repositórios, DAOs, UI Compose e configuração Gradle necessários para os bugs.
- Testes unitários, testes de integração disponíveis no projeto e testes manuais/instrumentados dos fluxos afetados.
- Migrações Room somente se forem indispensáveis às correções desta Spec.
- Verificação de regressão de persistência dos dados já existentes.

### 4.2 Excluído

- Investigação ou correção de SUS-001 a SUS-008.
- Redesenho amplo da UI/UX ou da identidade visual da Spec 9.
- Implementação de RF-09 e RF-13, atribuída ao formulário da nova UI na Spec 9.
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
- A Spec 10 entrega a meta por contagem. A Spec 9 V1 permite salvar dias preferidos para apresentação, mantendo a conclusão elegível em outros dias.
- A conclusão rápida não deve permitir gerar ocorrências adicionais além da meta semanal, salvo se o fluxo de desfazer remover primeiro uma ocorrência válida.
- Contar por data civil da ocorrência em `America/Sao_Paulo`, inclusive em registros retroativos permitidos pela Spec 3. Uma conclusão retroativa pertence à semana da data registrada, mesmo quando feita em outra semana. A ação rápida registra no máximo uma conclusão por hábito e data.
- Aplicar o limite na conclusão rápida com verificação e gravação na mesma transação. Contagens históricas que já excedam a meta continuam preservadas. Para registros retroativos em semanas de uma frequência anterior, preservar o fluxo da Spec 3 e aplicar as regras da frequência vigente no período.
- Usar a mesma regra de progresso e elegibilidade na Home e em Hábitos. O histórico e a grade de consistência continuam a exibir eventos efetivamente registrados segundo seus próprios critérios.

A Spec 9 V1 permite corrigir um registro factual no Histórico mesmo se a correção elevar a contagem acima da meta. O limite continua valendo para a conclusão rápida.

### 5.2 Prazo de tarefa

**Decisão confirmada na V1.** A regra abaixo substitui a comparação de instantes discutida anteriormente nas Specs 4 e 9.

Uma tarefa cuja data de vencimento seja hoje não está atrasada durante o dia de hoje. Ela passa a estar atrasada a partir do próximo dia civil, se continuar incompleta.

A comparação deve usar a data civil no fuso horário definido pelo projeto, e não comparar diretamente meia-noite do prazo com o timestamp atual.

### 5.3 Treino pausado

O tempo pausado não integra a duração efetiva do treino. O cronômetro deve congelar ao pausar e continuar do mesmo valor ao retomar. Histórico e estatísticas usam o mesmo cálculo de duração efetiva. Sessões antigas sem dados de pausa mantêm a duração histórica disponível.

### 5.4 Hábitos: conclusão, pausa e arquivamento

- Um hábito concluído hoje deve ter estado visual inequívoco na aba Hábitos.
- Deve existir uma ação para desfazer a conclusão do dia, sem duplicar ocorrências.
- Pausar e retomar devem ser ações alcançáveis na interface.
- Arquivar deve preservar o histórico e as ocorrências já registradas.
- Hábitos arquivados não devem aparecer entre hábitos ativos ou pausados nem ser elegíveis para novas conclusões.

### 5.5 Alteração de frequência

Ao alterar a frequência de um hábito, exibir confirmação explicando que as ocorrências anteriores serão preservadas e que a nova frequência será aplicada dali em diante. Não apagar nem reescrever ocorrências históricas.

Oferecer “Aplicar a partir de hoje” e “Cancelar alteração”, como na Spec 3. Como regra proposta para a transição, as conclusões já registradas na semana corrente entram no progresso quando o usuário passa para uma meta semanal durante essa semana.

## 6. Requisitos funcionais e critérios de aceitação

### RF-00 — Recuperar o build atual

- Corrigir os erros de compilação que impedem `assembleDebug` e a execução da suíte, preservando as funcionalidades e migrações existentes.
- Registrar o resultado de `testDebugUnitTest`, `lint` e `assembleDebug`. Falhas de comportamento ou de lint identificadas após a compilação entram na triagem desta fase.

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

### RF-06 — Prazo hoje não é atraso (referência BUG-006)

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
- Cobrir conclusão retroativa na semana atual e em semana anterior, concorrência pelo último registro elegível e preservação de históricos já acima da meta.

### RF-08 — Erro visível para nome vazio (BUG-008, P2)

- Salvar com nome vazio ou contendo apenas espaços exibe erro associado ao campo Nome.
- O diálogo permanece aberto e nenhum hábito é persistido.
- Ao corrigir o campo, o erro é removido de forma coerente.

### RF-09 — Frequência selecionada visível (BUG-009, Spec 9)

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
- Validar os destinos efetivos do build estabilizado. Repetir a restauração da tela com a navegação da nova UI, incluindo o Histórico interno quando estiver integrado.

### RF-13 — Rótulo da meta semanal legível (BUG-013, Spec 9)

- O rótulo da meta semanal permanece legível em larguras compactas e com fonte ampliada.
- O controle mantém alinhamento coerente com as demais opções e pode adaptar seu layout à largura disponível.
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
7. Criar tarefa com prazo hoje e verificar que ela só fica atrasada no dia seguinte em America/Sao_Paulo.
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

- RF-00 e os requisitos funcionais desta fase estiverem atendidos, com evidência para os achados já resolvidos. RF-06 deve estar validado na Home e em Tarefas. RF-09 e RF-13 devem constar como critérios para a nova UI.
- Todos os critérios de aceitação aplicáveis tiverem evidência de teste.
- `test`, `lint` e `assembleDebug` passarem.
- Os fluxos manuais de regressão tiverem sido executados.
- Não houver perda de dados existentes causada pelas correções.
- O relatório final listar arquivos alterados, decisões técnicas, testes executados, resultados e limitações restantes.
- SUS-001 a SUS-008 permanecerem explicitamente fora do escopo, sem serem apresentados como resolvidos.

## 11. Rastreabilidade

| Bug | Requisito |
|---|---|
| Falha de compilação atual | RF-00 |
| BUG-001 | RF-01 |
| BUG-002 | RF-02 |
| BUG-003 | RF-03 |
| BUG-004 | RF-04 |
| BUG-005 | RF-05 |
| BUG-006 | RF-06, vencimento por data civil |
| BUG-007 | RF-07 |
| BUG-008 | RF-08 |
| BUG-009 | RF-09, nova UI |
| BUG-010 | RF-10 |
| BUG-011 | RF-11 |
| BUG-012 | RF-12 |
| BUG-013 | RF-13, nova UI |
| BUG-014 | RF-14 |

## 12. Referências

- `FIX.md` — Relatório de QA (auditoria Specs 1–8), 14/11/2026 a 16/11/2026.
- Commit auditado: `7aa20d4`.
