# EGO Mobile

EGO é um aplicativo Android nativo para organizar hábitos, tarefas, treinos e cuidados capilares. O MVP funciona localmente: os dados são persistidos no Room no dispositivo, sem conta, nuvem ou conexão de rede.

## Funcionalidades

- **Início:** resumo dos hábitos do dia, tarefas prioritárias e atalhos de criação.
- **Hábitos:** cadastro, conclusão, pausa e retomada.
- **Tarefas:** prazos, conclusão, reagendamento, exclusão e opção de desfazer.
- **Rotinas:** registro de treinos e acompanhamento de cuidados capilares.
- **Histórico:** consulta de atividades concluídas.

## Requisitos

- Android Studio compatível com Android Gradle Plugin 9.3.3.
- Android SDK Platform 37 e Build-Tools 36.0.0.
- Emulador ou dispositivo Android API 24 ou superior; API 37 é usado para os testes instrumentados.
- Android SDK Platform-Tools (ADB) disponível no `PATH` para instalação direta pelo terminal.
- **JDK 25 para o Gradle:** o Gradle Wrapper é 9.5.0 e `gradle/gradle-daemon-jvm.properties` fixa o JDK do daemon em 25. Na primeira sincronização, o Gradle pode provisionar esse JDK conforme a configuração do projeto.
- O bytecode do aplicativo tem alvo **JVM 17** (compatibilidade de origem e destino Java/Kotlin).

Versões principais: Kotlin 2.2.10, Android Gradle Plugin 9.3.3, Gradle 9.5.0, Compose BOM 2026.09.00 e Room 2.8.5.

## Abrir e executar

1. Abra a raiz do repositório no Android Studio e aguarde a sincronização do Gradle.
2. Crie ou selecione um emulador com API 37, ou conecte um dispositivo com depuração USB habilitada.
3. Selecione a configuração `app` e execute **Run** no Android Studio.

No PowerShell, os comandos equivalentes são:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

O APK de depuração é gerado em `app\build\outputs\apk\debug\app-debug.apk`. Para instalar diretamente com ADB:

```powershell
adb devices
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Em macOS/Linux, use `./gradlew` e caminhos com `/`:

```sh
./gradlew :app:assembleDebug
./gradlew :app:installDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Testes

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

`testDebugUnitTest` não exige emulador. `connectedDebugAndroidTest` exige um dispositivo/emulador conectado; confira com `adb devices`.
Em macOS/Linux, os comandos são `./gradlew testDebugUnitTest` e `./gradlew connectedDebugAndroidTest`.

## Arquitetura

O código Android fica em `app/src/main/java/com/ego/mobile`, organizado por responsabilidade:

- `app/navigation`: destinos e navegação inferior.
- `feature`: telas Compose, estado e ViewModels por área.
- `domain`: modelos, contratos de repositório e casos de uso.
- `data/local`: banco Room, entidades, DAOs e mapeadores.
- `data/repository`: implementação dos contratos de domínio usando Room.

Os testes JVM ficam em `app/src/test`; os testes com Room/Compose em `app/src/androidTest`.

## Privacidade e escopo

Os registros do MVP ficam no armazenamento local do aplicativo. O manifesto não solicita acesso à internet, localização ou notificações. Login remoto, telemetria, sincronização em nuvem e gamificação com XP, níveis ou moedas não fazem parte do MVP.
