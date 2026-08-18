# GUIA API — Endpoints consumidos pelo app Android

> O app consome a mesma API REST documentada em `E:\Projetos\LOPES FOCUS\wp-api\gestor-api\docs\GUIA-API.md`.
> Este arquivo e apenas um espelho minimo. Para a documentacao completa, consulte o original.

## URL base

```
https://tools.mlopesdesign.com.br/wp-json/gestor/v1/
```

Definida em `app/build.gradle.kts` via `buildConfigField("String", "API_BASE_URL", ...)`.

## Endpoints usados

### Autenticacao

- `POST /auth/login` — login com e-mail + senha, retorna token de 30 dias
- `POST /auth/refresh` — renova o token
- `POST /auth/logout` — revoga o token
- `GET /auth/me` — dados do usuario logado

### Tarefas (CRUD padrao)

- `GET /tarefas` — lista todas (com paginacao `since`, `limit`, `offset`)
- `GET /tarefas/hoje` — tarefas com vencimento hoje
- `GET /tarefas/atrasadas` — tarefas atrasadas
- `GET /tarefas/{id}` — busca por ID
- `POST /tarefas` — cria
- `PUT /tarefas/{id}` — atualiza (requer `versao_base`)
- `DELETE /tarefas/{id}` — soft-delete
- `POST /tarefas/{id}/concluir` — marca como CONCLUIDA
- `POST /tarefas/{id}/reabrir` — volta pra PENDENTE

### Projetos / Clientes / Areas

Mesmo padrao CRUD: `GET /<entidade>`, `GET /<entidade>/{id}`, `POST`, `PUT`, `DELETE`.

### Sync

- `GET /sync/pull?dispositivo_id=...&since=...` — deltas desde o cursor
- `POST /sync/push` — envia batch de mutations locais
- `GET /sync/conflitos` — lista conflitos pendentes

## Erros

Codigos HTTP padronizados (200, 201, 204, 400, 401, 403, 404, 409, 429, 500).
Resposta de erro no formato:

```json
{
  "code": "slug_do_erro",
  "message": "Mensagem legivel",
  "data": { "status": 400 }
}
```

## Documentacao completa

Para a documentacao completa (request/response detalhados, exemplos curl, painel admin WP), veja:

```
E:\Projetos\LOPES FOCUS\wp-api\gestor-api\docs\GUIA-API.md
```

---

*ML Lopes Design · Marcio · 2026-08-17*
