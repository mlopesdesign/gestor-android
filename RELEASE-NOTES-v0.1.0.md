# Gestor de Demandas — Android v0.1.0

App Android nativo do **Gestor Inteligente de Demandas**, fala com o plugin WP via REST.

## Stack

- Kotlin 2.0.21
- Jetpack Compose (BOM 2024.12.01) + Material 3
- Room 2.6.1 (offline-first)
- Retrofit 2.11.0 + OkHttp 4.12.0
- Hilt 2.51.1 (DI)
- KSP 2.0.21-1.0.27
- Gradle 8.10.2, AGP 8.7.3
- minSdk 26, targetSdk 35

**Custo: R$ 0,00** — sem Firebase, sem Crashlytics, sem Play Services, sem certificado pago, sem assinatura paga.

## Identidade imutavel

- `applicationId`: `com.mlopes.gestor`
- Package: `com.mlopes.gestor`
- Version: 0.1.0 (versionCode 1)
- App name: "Gestor de Demandas"

## O que tem nesta versao

### Telas (9)

- **Login** — email + senha, rate limit 5/15min
- **Tarefas** (Home) — lista com filtros (Todas, Pendentes, Concluídas, Hoje) + busca
- **Tarefa Detalhe** — ver, concluir, reabrir, editar, **excluir** (com confirm)
- **Tarefa Editar/Criar** — formulário com validação
- **Projetos** — lista CRUD
- **Clientes** — lista com busca CRUD
- **Áreas** — lista CRUD
- **Configurações** — URL da API, sincronizar, logout
- **Sobre** — versão, autor

### Funcionalidades

- **Offline-first**: Room guarda snapshot local; mutations ficam em fila `pending_ops` e sobem no próximo push
- **Sync bidirecional** com plugin WP (`/sync/pull` + `/sync/push`)
- **Detecção de conflito** por versão (last-write-wins com log)
- **Token persistente** (DataStore + Tink encryption)
- **Network monitor** (sincroniza quando volta online)
- **CRUD completo** de todas as entidades

### Design

- Paleta ML Lopes: **amarelo `#F0A000`** como cor de marca
- Topbar preta, fundo claro
- Tipografia: Roboto Flex
- **Sem emojis** (só o `+` de nova tarefa, que é ícone padrão Material)
- Material 3 com cantos arredondados

## Como instalar e testar

### 1. Pré-requisitos
- Android Studio (qualquer versão recente)
- Emulador Pixel 8 (API 35) ou dispositivo físico com Android 8.0+

### 2. Instalar o APK release
```bash
adb install -r app-release.apk
```

### 3. Garantir que o plugin WP está rodando
URL: `https://tools.mlopesdesign.com.br/wp-json/gestor/v1/`
- Usuário: o que você criou no admin WP
- Senha: a definida no admin WP

### 4. Login
- Abra o app "Gestor de Demandas"
- Tela de login: informe email + senha do WP
- O app guarda o token localmente (criptografado)

### 5. CRUD
- Toque `+` pra criar tarefa
- Toque numa tarefa pra ver detalhe
- Toque `Excluir` (com confirmação) pra remover

### 6. Sync
- Toque "Sincronizar agora" em Configurações
- Sem rede: mutations ficam na fila local
- Com rede: pull delta + push batch

## Arquivos

- `app-release.apk` — 2,5 MB, assinado com debug keystore (Custo Zero)
- `MANUAL-ANDROID.pdf` — manual completo do usuário

## Proximas versoes

- v0.2.0: anexos em tarefas (foto, áudio)
- v0.3.0: notificações push (FCM — custo zero até 10K users)
- v0.4.0: modo offline com FTS4 no Room
- v1.0.0: Play Store (certificado pago, ~$25)
