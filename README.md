# Gestor de Demandas — App Android

App Android nativo do **Gestor Inteligente de Demandas**. Fala com o plugin WP (`mlopesdesign/gestor-api`) via REST API.

## Stack

- **Kotlin 2.0.21** idiomático, sem `!!`, sem `runBlocking` em produção
- **Jetpack Compose** (BOM 2024.12.01) + Material 3
- **Room 2.6.1** — offline-first
- **Retrofit 2.11.0** + OkHttp 4.12.0 + kotlinx-serialization
- **Hilt 2.51.1** — DI
- **KSP 2.0.21-1.0.27** — code generation
- **Gradle 8.10.2** + AGP 8.7.3
- **minSdk 26** (Android 8.0), **targetSdk 35** (Android 15)

**Custo: R$ 0,00** — sem Firebase, sem Crashlytics, sem Play Services, sem certificado pago.

## Identidade imutável

| Atributo | Valor |
|---|---|
| `applicationId` | `com.mlopes.gestor` |
| Package | `com.mlopes.gestor` |
| Version | 0.1.0 (versionCode 1) |
| App name | "Gestor de Demandas" |
| API base | `https://tools.mlopesdesign.com.br/wp-json/gestor/v1/` |

## Como rodar

### Opção 1: Android Studio

1. Abra o Android Studio (Hedgehog ou mais recente).
2. `File → Open` → selecione a pasta `android-app/`.
3. Aguarde o Gradle sync.
4. Crie/selecione um emulador (Pixel 8, API 35 recomendado).
5. Run ▶ (Shift+F10).

### Opção 2: APK release (sem abrir o Android Studio)

```bash
adb install -r gestor-android-0.1.0.apk
```

O APK release está em [`releases/latest`](https://github.com/mlopesdesign/gestor-android/releases/latest).

## Telas

| Tela | Descrição |
|---|---|
| Login | email + senha, rate limit 5/15min |
| Tarefas | lista com filtros + busca + criar |
| Tarefa Detalhe | ver, concluir, reabrir, editar, excluir |
| Tarefa Editar | formulário com validação |
| Projetos | CRUD |
| Clientes | CRUD com busca |
| Áreas | CRUD |
| Configurações | URL API, sync, logout |

## Arquitetura

```
app/src/main/java/com/mlopes/gestor/
├── data/
│   ├── remote/         Retrofit + DTOs + interceptor
│   ├── local/          Room (DB, DAO, Entity)
│   └── repository/     Auth, Tarefa, Projeto, Cliente, Area, Sync, NetworkMonitor
├── domain/
│   ├── model/          Entidades de domínio
│   └── usecase/        7 use cases
├── ui/
│   ├── theme/          Material 3 theme
│   ├── components/     Componentes Compose reutilizáveis
│   ├── auth/           Tela de login
│   ├── tarefas/        Lista + detalhe + editar
│   ├── projetos/       Lista
│   ├── clientes/       Lista
│   ├── areas/          Lista
│   ├── config/         Configurações
│   └── nav/            Navigation Compose
├── di/                 Hilt modules (Network, Database, Repository)
├── GestorApp.kt        @HiltAndroidApp
└── MainActivity.kt     NavHost
```

## Sync

- **Pull**: `GET /sync/pull?since=ISO&dispositivo_id=X` retorna deltas
- **Push**: `POST /sync/push` envia batch de mutations
- **Conflito**: last-write-wins por `versao` (log em `sync_conflitos`)
- **Offline-first**: mutations ficam em `pending_ops` no Room e sobem quando voltar online

## Documentação

- [`AGENTS.md`](./AGENTS.md) — governança do app
- [`RELEASE-NOTES-v0.1.0.md`](./RELEASE-NOTES-v0.1.0.md) — notas da versão
- Manual do usuário: ver `docs/MANUAL-ANDROID.pdf` na última release

## Licença

Proprietária — ML Lopes Design.
