# EGO Mobile — Documentação do Projeto

**Status:** documentação de produto e arquitetura para o MVP  
**Plataforma:** Android  
**Linguagem:** Kotlin  
**UI:** Jetpack Compose (decisão inicial; confirmar suporte da IDE/toolchain escolhida)  
**Persistência:** local, sem conta ou sincronização no MVP  
**Fuso horário de referência:** `America/Sao_Paulo`

## Documentos

1. [`01-PRD.md`](01-PRD.md) — visão do produto, escopo e requisitos.
2. [`02-USER-FLOWS.md`](02-USER-FLOWS.md) — navegação e regras dos fluxos.
3. [`03-SDD.md`](03-SDD.md) — arquitetura técnica e decisões de implementação.
4. [`04-ROADMAP.md`](04-ROADMAP.md) — etapas incrementais e critérios de conclusão.

## Diretriz de tecnologia

O projeto passa a usar **Kotlin como linguagem principal**. Java fica fora do escopo de estudo ativo neste momento. Não manter implementações paralelas em Java.

A troca de linguagem não altera automaticamente os requisitos de produto. As funcionalidades e regras registradas nos documentos continuam sendo a referência do MVP.

## Como usar estes documentos

- Trate requisitos explicitamente definidos como decisões aprovadas.
- Não implemente funcionalidades fora do MVP sem atualizar primeiro o PRD e o roadmap.
- Se uma decisão técnica depender de suporte da IDE, versão do Android Gradle Plugin ou compatibilidade de biblioteca, confira a documentação oficial e a configuração real do projeto antes de codar.
- Prefira a implementação mais simples que preserve as regras de negócio.
- Não considere uma funcionalidade pronta apenas porque a tela foi construída: persistência, estados vazios, erros e testes também precisam funcionar.
