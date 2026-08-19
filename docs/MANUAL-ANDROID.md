# Manual do Usuário — Gestor de Demandas (Android v0.1.0)

> App Android do **Gestor Inteligente de Demandas** — fala com o plugin WP via REST API.
> Stack: Kotlin + Jetpack Compose + Material 3.

---

## 1. Instalação

### Opção A: Instalar pelo Android Studio (recomendado para dev)

1. Abra o **Android Studio** (Hedgehog 2023.1.1 ou mais recente).
2. Vá em **File → Open** e selecione a pasta `E:\Projetos\LOPES FOCUS\android-app`.
3. Aguarde o **Gradle sync** terminar (pode demorar 5–10 min na primeira vez).
4. Crie/selecione um **emulador Pixel 8** (API 35 recomendado) em **Device Manager**.
5. Clique **Run ▶** (Shift+F10).
6. O app abre direto na tela de **Login**.

### Opção B: Instalar o APK release (sem Android Studio)

```bash
adb install -r gestor-android-0.1.0.apk
```

O ícone **"Gestor de Demandas"** aparece na gaveta de apps.

---

## 2. Configuração inicial

O app precisa de **3 coisas** antes de funcionar:

1. **Plugin WP `gestor-api` instalado e ativo** em `https://tools.mlopesdesign.com.br/wp-admin/`
2. **Usuário criado** com email + senha (via menu lateral **Gestor API → Criar usuário**)
3. **URL da API** configurada (default: `https://tools.mlopesdesign.com.br/wp-json/gestor/v1/`)

Se a URL do seu WP for diferente, vá em **Configurações → URL da API** antes de logar.

---

## 3. Telas

### 3.1 Login

- Informe **E-mail** e **Senha** (os mesmos do plugin WP).
- Toque **Entrar**.
- O app guarda o token localmente (criptografado com Tink).
- Em caso de erro:
  - **E-mail ou senha incorretos** → revise as credenciais
  - **Muitas tentativas** → aguarde 15 minutos (rate limit 5 tentativas / 15 min por IP)
  - **Sem conexão** → verifique sua internet

### 3.2 Tarefas (Home)

Lista principal, mostra todas as tarefas do usuário.

- **Filtros** (no topo): **Todas** | **Pendentes** | **Concluídas** | **Hoje**
- **Buscar** (🔍): busca por título
- **Atualizar** (↻): força pull de novas tarefas
- **+ Nova tarefa** (canto inferior direito): abre formulário de criação

Tocar numa tarefa abre o **Detalhe**.

### 3.3 Detalhe da Tarefa

Mostra todas as informações da tarefa:

- **Título** e **Descrição**
- **Status** (Pendente / Em andamento / Concluída / Cancelada / Arquivada)
- **Prioridade** (Baixa / Normal / Alta / Crítica)
- **Projeto** (se vinculado)
- **Cliente** (se vinculado)
- **Vencimento** (se definido)
- **Criada em** / **Atualizada em**

Ações:
- **Concluir** / **Reabrir** (alterna status)
- **Editar** (abre formulário)
- **Excluir** (com confirmação; soft-delete — pode ser restaurada)

### 3.4 Editar / Criar Tarefa

Formulário com:
- **Título** (obrigatório, máx 200 chars)
- **Descrição** (opcional)
- **Projeto** (dropdown, opcional)
- **Cliente** (dropdown, opcional)
- **Prioridade** (Baixa/Normal/Alta/Crítica)
- **Status** (Pendente/Em andamento/Concluída/Cancelada/Arquivada)
- **Vencimento** (date picker, opcional)

Toque **Salvar** pra criar/editar.

### 3.5 Projetos

Lista de projetos do usuário. Toque num projeto pra ver detalhes (em breve). Use **+** pra criar.

### 3.6 Clientes

Lista de clientes com **busca** no topo. Toque num cliente pra ver detalhes (em breve). Use **+** pra criar.

### 3.7 Áreas

Lista de áreas (categorias). Use **+** pra criar.

### 3.8 Configurações

- **URL da API** — altera o endpoint do WP
- **Sincronizar agora** — força pull + push
- **Sair** — encerra a sessão (vai pra tela de Login)

---

## 4. Sincronização

O app é **offline-first**: tudo o que você faz sem internet fica numa fila local e sobe quando voltar online.

- **Pull** (servidor → app): baixa tarefas/projetos/clientes/áreas novos ou alterados desde a última sincronização
- **Push** (app → servidor): envia mutations locais (criar/editar/excluir) que ainda não subiram

Indicador de status:
- **Sincronizando** (com spinner)
- **Sincronização concluída** (verde)
- **Falha na sincronização** (vermelho — tente de novo quando tiver internet)

### Conflito de sync

Se a mesma tarefa foi editada no app E no WP entre sincronizações, o servidor aplica **last-write-wins** (a versão mais recente vence) e registra o conflito em `wp_gestor_sync_conflitos` pra auditoria. **Nada é sobrescrito silenciosamente** — o conflito é sempre logado.

---

## 5. Permissões

- **Internet** — obrigatória pra sync
- **Acesso ao estado da rede** — pra detectar offline/online e mostrar indicador

Nenhuma permissão sensível (câmera, localização, contatos, etc).

---

## 6. Solução de problemas

### "Sem conexão com a internet"
- Verifique Wi-Fi / dados móveis
- Tente **Sincronizar agora** em Configurações

### "E-mail ou senha incorretos"
- Verifique se está usando as credenciais **do plugin WP**, não as do WP admin
- Abra o admin WP → menu lateral **Gestor API** → tabela **Usuários** → confirme email

### "Muitas tentativas. Aguarde 15 minutos."
- Rate limit: 5 tentativas / 15 min por IP
- Aguarde 15 min ou peça pro admin resetar em **Gestor API → Revogar todos os tokens**

### "Falha na sincronização"
- Sem internet → conecte e tente de novo
- URL da API errada → **Configurações → URL da API**
- Token expirado (30 dias) → **Sair** e logar de novo

### App abre e fecha sozinho
- Reinstale: `adb uninstall com.mlopes.gestor` → `adb install gestor-android-0.1.0.apk`
- Os dados locais (Room) serão perdidos, mas no servidor estão intactos

---

## 7. Atalhos de teclado (emulador)

- **Tab** — próximo campo
- **Enter** — submete formulário / abre item selecionado
- **Esc** — fecha modal / volta

---

## 8. Próximas versões

- **v0.2.0** — Anexos (foto, áudio) em tarefas
- **v0.3.0** — Notificações push (FCM, custo zero até 10K users)
- **v0.4.0** — Busca FTS4 no Room (busca local instantânea)
- **v1.0.0** — Publicação na Play Store (certificado pago ~$25)

---

## 9. Suporte

- **Plugin WP**: https://github.com/mlopesdesign/gestor-api/issues
- **App Android**: https://github.com/mlopesdesign/gestor-android/issues
- **Gestor desktop**: https://github.com/mlopesdesign/gestor-inteligente-de-demandas/issues
- **Email**: mlopesdesign@gmail.com

---

*ML Lopes Design · Marcio · 2026-08-18 · v0.1.0 · Custo R$ 0,00*
