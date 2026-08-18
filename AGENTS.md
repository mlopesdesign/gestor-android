# AGENTS — App Android `Gestor de Demandas`

> App Android que consome a **API REST do plugin WP `gestor-api`** (hospedada em `tools.mlopesdesign.com.br`).
> Apresenta **primeiro no Android Studio do Marcio** (emulador ou device). **Play Store é fase futura, NÃO agora.**
> Lido integralmente **antes** de qualquer alteração. Vinculante.

---

## 0. Relação com o AGENTS.md raiz

Este arquivo **complementa** `E:\Projetos\LOPES FOCUS\AGENTS.md` §9 (Projetos irmãos).
Em conflito, prevalece §9 + o briefing do Marcio no chat de origem (session atual).
Em silêncio deste, vale a seção 9 do AGENTS.md raiz e o `coder` system prompt.

**REGRA DE FERRO herdada do raiz (AGENTS.md §9.1):** **NÃO MEXER no Gestor desktop v0.2.22.**
Este app conversa com o **plugin WP**, não com o desktop. A fase de mexer no desktop vem depois que o Marcio liberar.

---

## 1. Identidade imutável do app

| Atributo | Valor | Observação |
|---|---|---|
| Application ID | `com.mlopes.gestor` | Package name Android (preservado em updates) |
| App name (PT-BR) | `Gestor de Demandas` | Label em `strings.xml` |
| App name (EN) | `Gestor de Demandas` | Mesmo PT (não i18n agora) |
| Versão atual | `0.1.0` (versionCode 1) | SemVer. Bump em todo build |
| minSdk | 26 (Android 8.0 Oreo) | Cobre 95%+ dos devices BR em 2026 |
| targetSdk | 35 (Android 15) | Exigido pela Play Store quando for |
| compileSdk | 35 | Igual target |
| Namespace do build | `com.mlopes.gestor` | Igual applicationId |
| URL da API | `https://tools.mlopesdesign.com.br/wp-json/gestor/v1/` | Em construção, mas usável |
| Repo destino | `https://github.com/mlopesdesign/gestor-android` | A criar quando Marcio pedir |

> Mudou qualquer um destes → quebra update, dados locais, link com o plugin WP.
> Conferir antes de cada release.

---

## 2. Stack do app (fixa, sem negociação)

| Camada | Tecnologia | Versão | Justificativa |
|---|---|---|---|
| Linguagem | **Kotlin** | 2.0+ (K2 compiler) | Padrão Android em 2026, mais conciso que Java |
| UI | **Jetpack Compose** | BOM 2024.12+ | Declarativo, Material 3, padrão atual |
| Design | **Material 3** | 1.3+ | Tema moderno, dark/light automático |
| Banco local | **Room** | 2.6+ | Espelho do MySQL, offline-first |
| HTTP | **Retrofit 2** | 2.11+ | Padrão Android, com OkHttp 4.x |
| Serialização | **kotlinx.serialization** | 1.7+ | JSON rápido, integrado ao Retrofit |
| DI | **Hilt** | 2.51+ | Padrão Google, baseado em Dagger |
| Async | **Coroutines + Flow** | 1.8+ | Padrão Android, sem RxJava |
| Navegação | **Navigation Compose** | 2.8+ | Type-safe routes, padrão Google |
| Build | **Gradle (Kotlin DSL)** | 8.10+ | Padrão Android Studio 2024+ |
| KSP | **KSP 2** | 2.0+ | Pra Hilt + Room, mais rápido que kapt |
| Testes | **JUnit 4 + MockK + Turbine** | latest | Padrão Android test stack |
| Cobertura | **Kover** | 0.8+ | Reporta %, integração Gradle |
| IDE | **Android Studio** | Ladybug 2024.2+ | Que o Marcio já tem |
| Emulador | **AVD padrão** (Pixel 7, API 35) | — | Marcio roda local |

### 2.1 PROIBIDO neste app

- **Java** (só Kotlin)
- **XML layouts** (só Compose, exceto `AndroidManifest.xml`, `themes.xml`, `colors.xml`, `strings.xml`)
- **RxJava** (só Coroutines + Flow)
- **Dagger 2 puro** (só Hilt)
- **kapt** (só KSP)
- **Glide / Picasso** (só **Coil 3** pra imagens)
- **Firebase / Google Mobile Ads / Crashlytics** (custo zero, self-hosted, sem dependência Google)
- **Play Services Location / Maps** (sem mapa por enquanto)
- **Tailwind / qualquer framework CSS** (não se aplica, é nativo)
- **JSON manual parsing** (só kotlinx.serialization)
- **GlobalScope** (sempre viewModelScope ou applicationScope)
- **`!!`** (sempre `?.let { }`, `requireNotNull`, ou `sealed class`)
- **Hardcoded strings** (sempre `R.string.*`)
- **Hardcoded colors** (sempre `MaterialTheme.colorScheme.*`)

---

## 3. Estrutura de arquivos (Clean Architecture)

```
E:\Projetos\LOPES FOCUS\android-app\
├── AGENTS.md                                      ← este arquivo
├── README.md                                      ← como abrir no Android Studio
├── build.gradle.kts                               ← root
├── settings.gradle.kts                            ← inclui :app
├── gradle.properties                              ← JVM args, AndroidX, Kotlin
├── gradle/
│   ├── libs.versions.toml                         ← version catalog (centraliza deps)
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── docs/
│   ├── MANUAL-ANDROID.md                          ← como abrir + usar
│   ├── MANUAL-ANDROID.pdf
│   ├── GUIA-API.md                                ← endpoints consumidos
│   └── GUIA-API.pdf
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/mlopes/gestor/
        │   │   ├── GestorApp.kt                  ← @HiltAndroidApp
        │   │   ├── MainActivity.kt               ← single activity, NavHost
        │   │   ├── data/
        │   │   │   ├── remote/
        │   │   │   │   ├── api/
        │   │   │   │   │   ├── GestorApi.kt      ← interface Retrofit
        │   │   │   │   │   ├── AuthApi.kt
        │   │   │   │   │   ├── TarefasApi.kt
        │   │   │   │   │   ├── ProjetosApi.kt
        │   │   │   │   │   ├── ClientesApi.kt
        │   │   │   │   │   ├── AreasApi.kt
        │   │   │   │   │   └── SyncApi.kt
        │   │   │   │   ├── dto/
        │   │   │   │   │   ├── AuthDto.kt        ← LoginRequest, LoginResponse
        │   │   │   │   │   ├── TarefaDto.kt
        │   │   │   │   │   └── SyncDto.kt        ← MudancaDto, PushRequest
        │   │   │   │   ├── interceptor/
        │   │   │   │   │   ├── AuthInterceptor.kt ← injeta Bearer token
        │   │   │   │   │   └── LoggingInterceptor.kt
        │   │   │   │   └── TokenStorage.kt        ← EncryptedSharedPreferences
        │   │   │   ├── local/
        │   │   │   │   ├── db/
        │   │   │   │   │   ├── GestorDatabase.kt  ← @Database
        │   │   │   │   │   └── Converters.kt      ← List<String>, Date, JSON
        │   │   │   │   ├── dao/
        │   │   │   │   │   ├── TarefaDao.kt
        │   │   │   │   │   ├── ProjetoDao.kt
        │   │   │   │   │   ├── ClienteDao.kt
        │   │   │   │   │   ├── AreaDao.kt
        │   │   │   │   │   └── PendingOpDao.kt    ← fila offline
        │   │   │   │   └── entity/
        │   │   │   │       ├── TarefaEntity.kt
        │   │   │   │       ├── ProjetoEntity.kt
        │   │   │   │       ├── ClienteEntity.kt
        │   │   │   │       ├── AreaEntity.kt
        │   │   │   │       └── PendingOpEntity.kt
        │   │   │   └── repository/
        │   │   │       ├── AuthRepository.kt
        │   │   │       ├── TarefaRepository.kt
        │   │   │       ├── ProjetoRepository.kt
        │   │   │       ├── ClienteRepository.kt
        │   │   │       ├── AreaRepository.kt
        │   │   │       └── SyncRepository.kt
        │   │   ├── domain/
        │   │   │   ├── model/
        │   │   │   │   ├── Tarefa.kt
        │   │   │   │   ├── Projeto.kt
        │   │   │   │   ├── Cliente.kt
        │   │   │   │   ├── Area.kt
        │   │   │   │   ├── StatusTarefa.kt
        │   │   │   │   └── Prioridade.kt
        │   │   │   └── usecase/
        │   │   │       ├── LoginUseCase.kt
        │   │   │       ├── ListarTarefasUseCase.kt
        │   │   │       ├── CriarTarefaUseCase.kt
        │   │   │       ├── ConcluirTarefaUseCase.kt
        │   │   │       ├── ExcluirTarefaUseCase.kt
        │   │   │       └── SincronizarUseCase.kt
        │   │   ├── ui/
        │   │   │   ├── theme/
        │   │   │   │   ├── Color.kt              ← paleta ML Lopes (amarelo #F0A000, preto, branco)
        │   │   │   │   ├── Theme.kt              ← GestorTheme
        │   │   │   │   └── Type.kt               ← tipografia
        │   │   │   ├── nav/
        │   │   │   │   ├── NavGraph.kt           ← define rotas
        │   │   │   │   └── Routes.kt             ← sealed class de destinos
        │   │   │   ├── components/
        │   │   │   │   ├── TarefaCard.kt
        │   │   │   │   ├── PrioridadeChip.kt
        │   │   │   │   ├── EmptyState.kt
        │   │   │   │   ├── ErrorState.kt
        │   │   │   │   └── LoadingIndicator.kt
        │   │   │   ├── auth/
        │   │   │   │   ├── LoginScreen.kt
        │   │   │   │   └── LoginViewModel.kt
        │   │   │   ├── tarefas/
        │   │   │   │   ├── TarefasScreen.kt      ← lista (Hoje / Pendentes / Concluídas)
        │   │   │   │   ├── TarefasViewModel.kt
        │   │   │   │   ├── TarefaDetalheScreen.kt
        │   │   │   │   ├── TarefaDetalheViewModel.kt
        │   │   │   │   ├── TarefaEditarScreen.kt
        │   │   │   │   └── TarefaEditarViewModel.kt
        │   │   │   ├── projetos/
        │   │   │   │   ├── ProjetosScreen.kt
        │   │   │   │   └── ProjetosViewModel.kt
        │   │   │   ├── clientes/
        │   │   │   │   ├── ClientesScreen.kt
        │   │   │   │   └── ClientesViewModel.kt
        │   │   │   └── areas/
        │   │   │       ├── AreasScreen.kt
        │   │   │       └── AreasViewModel.kt
        │   │   └── di/
        │   │       ├── NetworkModule.kt
        │   │       ├── DatabaseModule.kt
        │   │       └── RepositoryModule.kt
        │   └── res/
        │       ├── values/
        │       │   ├── strings.xml               ← TUDO em pt-BR
        │       │   ├── colors.xml                ← paleta Material 3
        │       │   └── themes.xml                ← AppTheme (Material 3 dynamic)
        │       ├── drawable/
        │       │   └── ic_launcher_foreground.xml
        │       ├── mipmap-anydpi-v26/
        │       │   ├── ic_launcher.xml
        │       │   └── ic_launcher_round.xml
        │       └── xml/
        │           ├── backup_rules.xml
        │           └── data_extraction_rules.xml
        └── test/
            └── java/com/mlopes/gestor/
                ├── data/
                │   └── repository/
                │       ├── TarefaRepositoryTest.kt
                │       └── AuthRepositoryTest.kt
                ├── domain/
                │   └── usecase/
                │       └── ListarTarefasUseCaseTest.kt
                └── ui/
                    ├── auth/
                    │   └── LoginViewModelTest.kt
                    └── tarefas/
                        └── TarefasViewModelTest.kt
```

---

## 4. Configuração Gradle (Kotlin DSL)

### 4.1 `gradle/libs.versions.toml` (version catalog — centraliza versões)

```toml
[versions]
agp = "8.7.3"                          # Android Gradle Plugin
kotlin = "2.0.21"
ksp = "2.0.21-1.0.27"
hilt = "2.51.1"
room = "2.6.1"
retrofit = "2.11.0"
okhttp = "4.12.0"
kotlinxSerialization = "1.7.3"
kotlinxCoroutines = "1.9.0"
composeBom = "2024.12.01"
navigation = "2.8.5"
coil = "3.0.4"
junit = "4.13.2"
mockk = "1.13.13"
turbine = "1.2.0"
kover = "0.9.1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.15.0" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version = "1.9.3" }
androidx-lifecycle = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version = "2.8.7" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version = "2.8.7" }
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons = { group = "androidx.compose.material", name = "material-icons-extended" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
retrofit-core = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-kotlinx-serialization = { group = "com.squareup.retrofit2", name = "converter-kotlinx-serialization", version.ref = "retrofit" }
okhttp-core = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinxCoroutines" }
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }
coil-network = { group = "io.coil-kt.coil3", name = "coil-network-okhttp", version.ref = "coil" }
security-crypto = { group = "androidx.security", name = "security-crypto", version = "1.1.0-alpha06" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version = "1.1.1" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
```

### 4.2 `app/build.gradle.kts` (trechos-chave)

```kotlin
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
}

android {
  namespace = "com.mlopes.gestor"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.mlopes.gestor"
    minSdk = 26
    targetSdk = 35
    versionCode = 1
    versionName = "0.1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    // URL da API (production; em dev pode usar staging)
    buildConfigField("String", "API_BASE_URL", "\"https://tools.mlopesdesign.com.br/wp-json/gestor/v1/\"")
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug {
      applicationIdSuffix = ".debug"
      isDebuggable = true
    }
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.graphics)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.compose.material3)
  implementation(libs.compose.material.icons)
  implementation(libs.navigation.compose)
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
  implementation(libs.hilt.navigation.compose)
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  ksp(libs.room.compiler)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.kotlinx.serialization)
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.logging)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.coil.compose)
  implementation(libs.coil.network)
  implementation(libs.security.crypto)
  implementation(libs.datastore.preferences)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.turbine)
  testImplementation(libs.coroutines.test)
}
```

---

## 5. Telas (UX / Material 3)

### 5.1 Paleta (PT-BR, sem emoji, sem ícone decorativo)

| Token | Light | Dark | Uso |
|---|---|---|---|
| `primary` | `#F0A000` (amarelo ML Lopes) | `#F0A000` | Botões primários, FAB, links |
| `onPrimary` | `#FFFFFF` | `#FFFFFF` | Texto sobre primary |
| `secondary` | `#1F1F1F` | `#E0E0E0` | Botões secundários |
| `surface` | `#FFFFFF` | `#121212` | Fundo de cards |
| `background` | `#F5F5F5` | `#0A0A0A` | Fundo da tela |
| `error` | `#B3261E` | `#F2B8B5` | Erros, botão excluir |
| `priority.Alta` | `#D32F2F` | `#F2B8B5` | Chip de prioridade ALTA/CRÍTICA |
| `priority.Normal` | `#1976D2` | `#90CAF9` | Chip de prioridade NORMAL |
| `priority.Baixa` | `#388E3C` | `#A5D6A7` | Chip de prioridade BAIXA |

### 5.2 Tipografia

- **Display:** Inter 32sp (títulos)
- **Headline:** Inter 24sp (subtítulos)
- **Title:** Inter 20sp (títulos de card)
- **Body:** Inter 16sp (texto)
- **Label:** Inter 14sp (botões, chips)

> **SEM** icones decorativos (Marcio odeia). Só ícones funcionais (lápis=editar, lixeira=excluir, check=concluir, + = criar).

### 5.3 Telas (MVP do MVP — escopo confirmado pelo Marcio)

| # | Tela | Rota | Conteúdo | Estado |
|---|---|---|---|---|
| 1 | **Login** | `login` | Email + Senha + Botão Entrar + Erro inline + Loading | Primeira tela se sem token |
| 2 | **Home (Tarefas Hoje)** | `home` | Lista de tarefas com vencimento HOJE, agrupadas por projeto, com FAB "+" pra criar | Default após login |
| 3 | **Tarefas (todas)** | `tarefas` | Filtros: Todas / Pendentes / Concluídas, busca por texto | — |
| 4 | **Tarefa Detalhe** | `tarefa/{id}` | Título, descrição, status, prioridade, vencimento, projeto, cliente, botões Concluir / Editar / Excluir | — |
| 5 | **Tarefa Editar/Criar** | `tarefa/editar/{id?}` | Form completo: título, descrição, projeto (dropdown), cliente (dropdown), prioridade (chips), status, vencimento (date picker) | — |
| 6 | **Projetos** | `projetos` | Lista de projetos com status (PLANEJADO/EM_ANDAMENTO/CONCLUÍDO) e progresso | — |
| 7 | **Clientes** | `clientes` | Lista de clientes, busca, status | — |
| 8 | **Áreas** | `areas` | Lista de áreas com cor | — |
| 9 | **Configurações** | `config` | Sair, URL da API (avançado), Sobre | — |

### 5.4 Navegação

```kotlin
sealed class Route(val path: String) {
  data object Login : Route("login")
  data object Home : Route("home")
  data object Tarefas : Route("tarefas")
  data object TarefaDetalhe : Route("tarefa/{id}") {
    fun create(id: String) = "tarefa/$id"
  }
  data object TarefaEditar : Route("tarefa/editar?id={id}") {
    fun create(id: String? = null) = "tarefa/editar?id=$id"
  }
  data object Projetos : Route("projetos")
  data object Clientes : Route("clientes")
  data object Areas : Route("areas")
  data object Config : Route("config")
}
```

Bottom navigation: **Tarefas (Home) | Projetos | Clientes | Áreas | Config** (5 abas, padrão Material 3).

---

## 6. Camada de dados

### 6.1 Repositórios (data/repository/)

Cada repository expõe **Flow<List<T>>** do Room e dispara sync em background.
Padrão offline-first: lê do Room, se tem rede → busca API → atualiza Room → Room re-emite.

```kotlin
@Singleton
class TarefaRepository @Inject constructor(
  private val api: TarefasApi,
  private val dao: TarefaDao,
  private val sync: SyncRepository,
  private val networkMonitor: NetworkMonitor,
) {
  fun observarTarefas(status: StatusTarefa? = null): Flow<List<Tarefa>> =
    if (status == null) dao.observarTodas() else dao.observarPorStatus(status)

  suspend fun refresh(): Result<Unit> = runCatching {
    if (!networkMonitor.connected()) return@runCatching
    val response = api.listar(since = sync.ultimoCursor("tarefas"))
    dao.substituir(response.items.map { it.toEntity() })
    sync.atualizarCursor("tarefas", response.nextCursor)
  }

  suspend fun criar(input: TarefaInput): Result<Tarefa> = runCatching {
    val dto = api.criar(input.toDto())
    dao.upsert(dto.toEntity())
    sync.atualizarCursor("tarefas", null)  // forçar refresh na próxima
    dto.toDomain()
  }

  suspend fun concluir(id: String): Result<Unit> = runCatching {
    api.concluir(id)
    val tarefa = dao.buscarPorId(id) ?: return@runCatching
    dao.upsert(tarefa.copy(status = "CONCLUIDA", concluidaEm = Instant.now(), versao = tarefa.versao + 1))
  }

  // ... atualizar, excluir, etc.
}
```

### 6.2 Fila offline (`pending_ops`)

Quando o app tá offline, mutations vão pra `pending_ops` table (operação + payload + timestamp).
`SyncRepository.flushPending()` é chamado em:
- Conexão volta (`NetworkMonitor`)
- App volta do background (`LifecycleObserver`)
- Usuário puxa pra baixo (`PullToRefresh`)
- Botão "Sincronizar" em Configurações

### 6.3 AuthInterceptor (injeta Bearer)

```kotlin
class AuthInterceptor @Inject constructor(
  private val tokenStorage: TokenStorage,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val token = tokenStorage.buscar()
    val authenticated = request.newBuilder().apply {
      if (token != null) addHeader("Authorization", "Bearer $token")
      addHeader("Accept", "application/json")
      addHeader("User-Agent", "GestorAndroid/0.1.0")
    }.build()
    return chain.proceed(authenticated)
  }
}
```

Se 401 → limpa token + navega pra Login.

### 6.4 TokenStorage (EncryptedSharedPreferences)

```kotlin
@Singleton
class TokenStorage @Inject constructor(
  @ApplicationContext private val context: Context,
) {
  private val prefs by lazy {
    val masterKey = MasterKey.Builder(context)
      .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
      .build()
    EncryptedSharedPreferences.create(
      context, "gestor_secure", masterKey,
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
  }

  fun salvar(token: String, expiraEm: Instant) {
    prefs.edit().putString("token", token).putString("expira_em", expiraEm.toString()).apply()
  }
  fun buscar(): String? = prefs.getString("token", null)
  fun expirou(): Boolean {
    val str = prefs.getString("expira_em", null) ?: return true
    return Instant.parse(str).isBefore(Instant.now())
  }
  fun limpar() = prefs.edit().clear().apply()
}
```

---

## 7. DTOs (kotlinx.serialization)

Exemplo `TarefaDto.kt`:

```kotlin
@Serializable
data class TarefaDto(
  val id: String,
  val titulo: String,
  val descricao: String? = null,
  val status: String,
  val prioridade: String,
  val nivelCobranca: String = "PERSISTENTE",
  val areaId: String? = null,
  val projetoId: String? = null,
  val clienteId: String? = null,
  val inicioEm: String? = null,            // ISO 8601
  val vencimentoEm: String? = null,
  val duracaoEstimadaMin: Int? = null,
  val duracaoRealizadaMin: Int = 0,
  val etiquetas: List<String> = emptyList(),
  val responsavel: String? = null,
  val origem: String = "MANUAL",
  val concluidaEm: String? = null,
  val entregueEm: String? = null,
  val criadaEm: String,
  val atualizadaEm: String,
  val versao: Int,
  val deletadoEm: String? = null,
)

@Serializable
data class TarefaListResponse(
  val items: List<TarefaDto>,
  val nextCursor: String? = null,
  val hasMore: Boolean = false,
)

@Serializable
data class TarefaInputDto(
  val titulo: String,
  val descricao: String? = null,
  val status: String? = null,
  val prioridade: String? = null,
  val projetoId: String? = null,
  val clienteId: String? = null,
  val vencimentoEm: String? = null,
)
```

---

## 8. ViewModel + State (padrão MVI leve)

```kotlin
data class TarefasUiState(
  val carregando: Boolean = false,
  val tarefas: List<Tarefa> = emptyList(),
  val filtro: FiltroTarefa = FiltroTarefa.Hoje,
  val erro: String? = null,
)

enum class FiltroTarefa { Hoje, Pendentes, Concluidas, Todas }

@HiltViewModel
class TarefasViewModel @Inject constructor(
  private val listar: ListarTarefasUseCase,
  private val concluir: ConcluirTarefaUseCase,
) : ViewModel() {
  private val _state = MutableStateFlow(TarefasUiState())
  val state: StateFlow<TarefasUiState> = _state.asStateFlow()

  init {
    listar(filtro = FiltroTarefa.Hoje).onEach { tarefas ->
      _state.update { it.copy(carregando = false, tarefas = tarefas, erro = null) }
    }.catch { e ->
      _state.update { it.copy(carregando = false, erro = e.message) }
    }.launchIn(viewModelScope)
  }

  fun setFiltro(filtro: FiltroTarefa) { _state.update { it.copy(filtro = filtro) } }
  fun concluir(id: String) {
    viewModelScope.launch { concluir.invoke(id) }
  }
}
```

---

## 9. Testes (JUnit + MockK + Turbine)

| Suite | Cobre | Mínimo |
|---|---|---|
| `LoginViewModelTest` | Login OK, login email inválido, login senha errada, loading state | 4 testes |
| `TarefasViewModelTest` | Carrega lista, muda filtro, conclui tarefa, trata erro | 4 testes |
| `TarefaRepositoryTest` | Lê do Room, refresh da API, criar offline vai pra pending, flush quando online | 4 testes |
| `AuthRepositoryTest` | Salva token, busca token expirado retorna null, logout limpa | 3 testes |
| `ListarTarefasUseCaseTest` | Filtra por status, ordena por prioridade, agrupa por projeto | 3 testes |

**Cobertura mínima Kover: 70% no domain, 50% no data.** UI testada manualmente (Marcio vai ver no emulador dele).

```bash
cd E:\Projetos\LOPES FOCUS\android-app
./gradlew test          # roda JUnit
./gradlew koverHtmlReport  # gera coverage
```

---

## 10. Permissões Android (AndroidManifest.xml)

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

  <application
    android:name=".GestorApp"
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:label="@string/app_name"
    android:theme="@style/Theme.Gestor"
    android:supportsRtl="true"
    android:allowBackup="true"
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:fullBackupContent="@xml/backup_rules"
    android:usesCleartextTraffic="false">

    <activity
      android:name=".MainActivity"
      android:exported="true"
      android:theme="@style/Theme.Gestor">
      <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
      </intent-filter>
    </activity>

  </application>
</manifest>
```

- `usesCleartextTraffic="false"` (só HTTPS)
- `ACCESS_NETWORK_STATE` (NetworkMonitor)
- Sem `ACCESS_FINE_LOCATION`, sem `CAMERA`, sem `READ_EXTERNAL_STORAGE` no MVP (anexos fora de escopo)

---

## 11. Build & Run

### 11.1 Build APK debug

```powershell
cd E:\Projetos\LOPES FOCUS\android-app
# Sincronizar Gradle (primeira vez demora)
.\gradlew.bat --refresh-dependencies
# Build
.\gradlew.bat assembleDebug
# APK em: app\build\outputs\apk\debug\app-debug.apk
```

### 11.2 Instalar no emulador/dispositivo

```powershell
.\gradlew.bat installDebug
# OU
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### 11.3 Rodar testes

```powershell
.\gradlew.bat test              # JUnit local
.\gradlew.bat connectedAndroidTest   # UI tests no device (não usar no MVP)
```

### 11.4 Lint

```powershell
.\gradlew.bat lint
```

Falha de lint = build falha. Sem `//noinspection` sem motivo.

---

## 12. Como abrir no Android Studio (passo-a-passo pro Marcio)

1. Abre o **Android Studio Ladybug 2024.2+** (ou mais novo)
2. **File → Open** → seleciona `E:\Projetos\LOPES FOCUS\android-app`
3. Espera o **Gradle sync** (primeira vez baixa ~300 MB de dependências)
4. Cria um **AVD** se não tiver: **Tools → Device Manager → Create device → Pixel 7 → API 35**
5. **Run ▶** (Shift+F10) selecionando o AVD
6. App abre na tela de Login
7. Digita: `marcio@gestor.local` / `Ml@2026gestor` (seed do plugin WP, quando existir)
8. Tela Home mostra as tarefas sincronizadas

---

## 13. O que este app **NÃO** faz (escopo MVP)

- ❌ Push notifications (FCM) — fora do MVP
- ❌ Upload de anexos / câmera — fora do MVP
- ❌ Modo offline completo de horas (só leitura do último snapshot) — fora do MVP avançado
- ❌ Widget de home screen — fora do MVP
- ❌ Apple Watch / Wear OS — fora do MVP
- ❌ Sincronização com o Gestor desktop — **BLOQUEADO** até Marcio liberar (AGENTS.md §9.1)
- ❌ Subir pra Play Store — **BLOQUEADO** até Marcio decidir publicar

---

## 14. Histórico de versões

| Versão | Data | Notas |
|---|---|---|
| 0.1.0 | 2026-08-17 | Esqueleto Android Studio: login + lista tarefas + criar/editar/concluir/excluir tarefa, offline-first, projeto de exemplo |

---

*ML Lopes Design · Marcio · mlopesdesign@gmail.com · mlopesdesign.com.br · tools.mlopesdesign.com.br*
*App Android `com.mlopes.gestor` v0.1.0 — gerado em 17/08/2026. Stack: Kotlin 2 + Compose + Material 3 + Room + Retrofit + Hilt. Custo zero, sem Play Store ainda.*
