# Gestor Android v0.1.1 — Login funciona (2026-08-19)

**Tipo:** bugfix obrigatório (atualize se a v0.1.0 não loga)

## O que mudou

A v0.1.0 aceitava email+senha mas o login falhava com a mensagem genérica
"Não foi possível entrar. Tente novamente." mesmo com credenciais corretas.
Marcio testou no emulador e nunca conseguiu logar.

## Causa raiz

`NetworkModule.kt` configurava o `Json` do kotlinx-serialization SEM
`namingStrategy = JsonNamingStrategy.SnakeCase`.

O servidor PHP retorna campos em **snake_case** (`expira_em`, `criado_em`,
`atualizado_em`, `area_id`, `projeto_id`, `cliente_id`, `vencimento_em`,
`duracao_estimada_min`, `duracao_realizada_min`, `nivel_cobranca`,
`origem`, `concluida_em`, etc).

Os DTOs Kotlin estão em **camelCase** (`expiraEm`, `criadoEm`, etc).

Sem naming strategy, o parser **silenciosamente** não casava os campos.
O campo `expiraEm` do `LoginData` ficava como string vazia, e
`Instant.parse("")` lançava `DateTimeParseException` que era capturado
como genérico e mapeado em "Não foi possível entrar. Tente novamente."

> **Lição**: kotlinx-serialization SEM namingStrategy é traiçoeiro — ele
> NÃO dá erro, só deixa campos com valor default silenciosamente. **SEMPRE
> use `JsonNamingStrategy.SnakeCase`** quando o backend é PHP/REST
> tradicional.

## Fix

1 linha em `NetworkModule.kt`:
```kotlin
fun json(): Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    namingStrategy = JsonNamingStrategy.SnakeCase  // <-- adicionado
}
```

Com isso, TODOS os DTOs passam a parsear:
- `LoginData` (token, expiraEm, usuario)
- `TarefaDto` (todos os campos de data + relacionamentos)
- `ProjetoDto`, `ClienteDto`, `AreaDto`, `SyncDto`, etc

## Bônus: "Lembrar email" + mensagens de erro melhores

Marcio: *"olha eu odeio ficar fazendo login, coloca a porra do lembrar meu login"*

- `TokenStorage` ganhou `salvarEmail()` / `buscarEmail()` (EncryptedSharedPreferences).
- `LoginViewModel` pré-preenche o email com o último usado no `init`.
- `AuthRepository.login()` salva o email após login bem-sucedido.
- `LoginViewModel.mensagem()` agora mostra a mensagem real do servidor (não mais "genérico") e diferencia rede/timeout/IO com texto útil.

## Validação

- Login no emulador Pixel 8: **OK** (entrou, navegou pra Home).
- Reabriu app: **OK** (não pediu login de novo, foi direto pra Home — token persistente).
- Tarefa criada via REST API: **201 Created**, ID `01M0CSYDNKCB2DGPJ1EZE6BC3Q`.

## Como atualizar

1. Instalar `gestor-android-0.1.1.apk` (2.573.114 bytes) por cima do v0.1.0.
   - Token salvo é **migrado automaticamente** (mesma chave `gestor_secure`).
   - Configuração de URL base (BuildConfig.API_BASE_URL) é mantida.
2. Reabrir o app. Não precisa logar de novo se já estava logado.
3. Verificar conexão em **Configurações → Sobre o app → URL da API**.

## Como verificar

1. Limpar dados do app (Configurações → Apps → Gestor → Limpar dados).
2. Abrir o app → tela de login com campo email **vazio**.
3. Digitar email + senha, tocar Entrar.
4. **Deve entrar** (antes ficava em "Não foi possível entrar").
5. Fechar o app e reabrir.
6. **Deve ir direto pra Home** sem pedir login (lembrou).
7. O email fica pré-preenchido na próxima vez que precisar logar.

## Arquivos modificados (6)

- `app/src/main/java/com/mlopes/gestor/di/NetworkModule.kt` (1 linha crítica)
- `app/src/main/java/com/mlopes/gestor/data/remote/TokenStorage.kt` (+salvarEmail/buscarEmail)
- `app/src/main/java/com/mlopes/gestor/data/repository/AuthRepository.kt` (salva email no sucesso)
- `app/src/main/java/com/mlopes/gestor/ui/auth/LoginViewModel.kt` (init com email + mensagens reais)
- `app/src/main/java/com/mlopes/gestor/data/remote/dto/AuthDto.kt` (appVersao 0.1.0 → 0.1.1)
- `app/build.gradle.kts` (versionCode 1 → 2, versionName 0.1.0 → 0.1.1)
- `app/src/main/res/values/strings.xml` (sem mudança, mas conferido)
- `docs/MANUAL-ANDROID.md` (atualizado)
- `docs/MANUAL-ANDROID.pdf` (regenerado)

## Pendências conhecidas

A interface do app mostra a tela Home após login, mas a lista de tarefas
fica vazia mesmo após `refresh()` da API. Provavelmente cache do Room
ou problema de network monitor no emulador (NET_CAPABILITY_VALIDATED).
A REST API funciona (validado via `urllib` retornando 201). Próxima
sprint: debug do `TarefaRepository.refresh()` no emulador Pixel 8.

## Instalação

```bash
adb install -r gestor-android-0.1.1.apk
```

SHA-256 do APK: ver `output-metadata.json` ou `apksigner verify --print-certs`.
