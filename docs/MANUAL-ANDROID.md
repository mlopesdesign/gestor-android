# MANUAL DO APP ANDROID — Gestor de Demandas v0.1.0

> Documento de uso do app Android `com.mlopes.gestor` para o Marcio e futuros usuarios.
> Aplicavel a versao 0.1.0 gerada em 17/08/2026.

---

## 1. O que e este app

Aplicativo Android nativo do **Gestor Inteligente de Demandas**. Ele conversa com a **API REST** hospedada como plugin no WordPress em `tools.mlopesdesign.com.br`. O app nao conversa com o Gestor desktop — isso esta **bloqueado** ate liberacao do Marcio (vide `AGENTS.md` raiz §9.1).

Funcionalidades desta versao:

- Login com e-mail e senha (token salvo de forma criptografada)
- Listagem de tarefas com filtros (Hoje, Pendentes, Concluidas, Todas)
- Detalhe, edicao, criacao, conclusao e exclusao de tarefas
- Listagem de projetos, clientes e areas
- Sincronizacao manual via botao "Sincronizar agora" na aba Config
- Fila offline — se a rede cair, mutations ficam pendentes e sobem quando voltar
- Dark mode automatico conforme o sistema

---

## 2. Instalacao

### 2.1 A partir do Android Studio

1. Abra o Android Studio Ladybug 2024.2+
2. File → Open → selecione `E:\Projetos\LOPES FOCUS\android-app`
3. Espere o Gradle sync (primeira vez demora)
4. Selecione um device ou AVD
5. Run ▶ (Shift+F10)

### 2.2 Via APK debug pre-compilado

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### 2.3 Requisitos do device

- Android 8.0 (API 26) ou superior
- Conexao HTTPS para `tools.mlopesdesign.com.br`

---

## 3. Login

1. Abre o app
2. Tela **Entrar no Gestor** aparece
3. Informe:
   - **E-mail**: o mesmo cadastrado no plugin WP (`marcio@gestor.local` no seed)
   - **Senha**: a senha definida no admin WP
4. Toque **Entrar**

Em caso de erro:

| Mensagem | Causa |
|---|---|
| E-mail ou senha incorretos. | Credenciais invalidas |
| Muitas tentativas. Aguarde 15 minutos. | Rate limit do servidor |
| Sem conexao com a internet. | Sem rede no momento |

O token expira em 30 dias. Apos isso, o app pede login de novo.

---

## 4. Tela Tarefas (Home)

A Home mostra tarefas com vencimento hoje, com prioridade mais alta em cima.

- **Filtros no topo**: Hoje, Pendentes, Concluidas, Todas
- **FAB +** no canto inferior direito: cria uma nova tarefa
- Toque num card para abrir o detalhe
- Icone de **check** no card: marca a tarefa como concluida (com confirmacao visual)

Lista agrupada por prioridade (critica > alta > normal > baixa) e, dentro da mesma prioridade, pela data de vencimento.

---

## 5. Tela Detalhe da Tarefa

Mostra: titulo, descricao, status, prioridade, vencimento, projeto, cliente, datas de criacao e atualizacao.

Botoes:

- **Concluir** (se nao concluida): muda status pra CONCLUIDA
- **Reabrir** (se ja concluida): volta status pra PENDENTE
- **Editar** (lapis): abre a tela de edicao
- **Excluir** (lixeira): pede confirmacao e faz soft-delete

Os botoes de Editar e Excluir ficam no canto superior direito.

---

## 6. Tela Editar / Criar Tarefa

Campos:

| Campo | Obrigatorio | Observacao |
|---|---|---|
| Titulo | sim | — |
| Descricao | nao | — |
| Prioridade | nao (default NORMAL) | 4 chips: Baixa, Normal, Alta, Critica |
| Projeto | nao | Dropdown com lista do servidor |
| Cliente | nao | Dropdown com lista do servidor |
| Vencimento | nao | ISO 8601 (string) |

Botao **Salvar** embaixo. Volta para a tela anterior ao salvar.

---

## 7. Tela Projetos

Lista de projetos cadastrados no plugin WP. Cada card mostra: nome, descricao, status. Atualize puxando pra baixo (pull-to-refresh sera adicionado em versao futura; por enquanto, va em Config → Sincronizar agora).

---

## 8. Tela Clientes

Lista de clientes com **busca** por nome no topo da tela. Mesmo padrao de atualizacao dos Projetos.

---

## 9. Tela Areas

Lista de areas com bolinha colorida representando a cor da area. Ordenado por ordem de exibicao e, depois, por nome.

---

## 10. Tela Configuracoes (Config)

Tres secoes:

- **API**: exibe a URL base da API (somente leitura)
- **Sobre**: versao 0.1.0, autor ML Lopes Design
- **Sincronizar agora**: dispara sync completo (pull + push da fila offline)
- **Sair**: encerra a sessao e volta pra tela de Login (com confirmacao)

---

## 11. Modo offline

O app le **sempre** do banco local (Room). A sincronizacao com o servidor acontece:

1. Automaticamente, quando voce abre uma tela de listagem (em background)
2. Manualmente, via Config → Sincronizar agora
3. Quando a rede volta (via NetworkMonitor)

Mutacoes feitas offline (criar / editar / concluir / excluir) vao pra fila `pending_ops` no Room. Quando a rede volta, sobem em batch via `POST /sync/push`.

Conflitos de versao sao tratados pelo servidor (regra de `versao_base`). O app recebe a resposta com a lista de conflitos e exibe na aba Config (tela dedicada sera adicionada em v0.2.0).

---

## 12. Privacidade e seguranca

- Token guardado em **EncryptedSharedPreferences** (AES-256-GCM)
- Nenhum segredo no bundle — chave de IA (se houver) fica no servidor
- `usesCleartextTraffic="false"` no manifest — so HTTPS
- Backup automatico do Android esta **desativado** para o SharedPreferences seguro (`backup_rules.xml` + `data_extraction_rules.xml`)

---

## 13. Limitacoes desta versao (MVP)

- ❌ Sem upload de anexos / camera
- ❌ Sem push notifications
- ❌ Sem widget de home screen
- ❌ Sincronizacao com o Gestor desktop **bloqueada** (vide AGENTS.md raiz §9.1)
- ❌ Subir pra Play Store **bloqueado** ate Marcio decidir

---

## 14. Onde pedir ajuda

| Canal | Quando |
|---|---|
| `AGENTS.md` deste projeto | Duvida sobre stack, identidade, regras |
| `docs/GUIA-API.md` | Duvida sobre endpoints do servidor |
| `AGENTS.md` raiz do LOPES FOCUS | Duvida sobre o projeto-pai (Gestor desktop) |

---

*ML Lopes Design · Marcio · mlopesdesign@gmail.com · mlopesdesign.com.br · tools.mlopesdesign.com.br*
*Versao 0.1.0 · 2026-08-17*
