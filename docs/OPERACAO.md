# Operação: backup, restauração e checagens de produção

Este guia é para quem mantém o RuralManager no ar (Render + Supabase). Os dados dos produtores
(CPF, valores de venda, clientes) ficam no Postgres do Supabase: **perder o banco é perder o sistema**.
O código está no GitHub; os dados, não.

## 1. Backup do banco

### 1.1 O que o Supabase já oferece (confira no seu plano)

No painel do Supabase: **Database > Backups**.

- Planos pagos têm backup diário automático (retenção depende do plano) e a opção de **PITR**
  (Point in Time Recovery), que permite voltar o banco a qualquer minuto dentro da janela contratada.
- O plano gratuito **não garante** backup restaurável por você. Se o projeto está no plano gratuito,
  trate o backup manual abaixo como obrigatório, não opcional.

Confirme no painel o que o seu plano entrega hoje antes de confiar nele: isso muda com o tempo.

### 1.2 Backup manual com `pg_dump` (funciona em qualquer plano)

Pré-requisito: cliente do Postgres instalado (`pg_dump` e `pg_restore`, versão igual ou maior que a do
servidor; o Supabase usa Postgres 15 ou 17, veja em Project Settings > Infrastructure).

Use a conexão **direta** ou o **Session pooler** (porta 5432). Evite o Transaction pooler (6543) para dump.

```bash
# senha em variável de ambiente, nunca na linha de comando
export PGPASSWORD='<senha do banco>'
pg_dump --host=<HOST> --port=5432 --username=postgres --dbname=postgres \
        --format=custom --no-owner --no-privileges \
        --schema=public \
        --file=ruralmanager-$(date +%Y%m%d-%H%M).dump
```

No Windows há o script pronto [`scripts/backup-banco.ps1`](../scripts/backup-banco.ps1): lê o `.env`
(`RM_DB_URL` e `RM_DB_PASSWORD`), gera o `.dump` na pasta `backups/` (ignorada pelo Git) e apaga os
mais antigos que 14 dias. Para rodar todo dia, agende no **Agendador de Tarefas** do Windows:

```powershell
schtasks /Create /SC DAILY /ST 02:00 /TN "RuralManager backup" `
  /TR "powershell -NoProfile -ExecutionPolicy Bypass -File C:\caminho\RuralManager\scripts\backup-banco.ps1"
```

### 1.3 Onde guardar o arquivo

- **Nunca** no repositório do GitHub (ele é público e o dump contém CPF, clientes e valores) nem como
  artefato de um workflow público.
- Guarde cópia fora do computador onde ele foi gerado (disco externo ou nuvem pessoal) e, de preferência,
  **criptografada** (ex.: `7z a -p -mhe=on backup.7z arquivo.dump`, ou `age`/`gpg`).
- Mantenha pelo menos: 7 diários, 4 semanais, 3 mensais.
- O dump contém dados pessoais: trate-o como trata o próprio banco (LGPD).

## 2. Restauração

Restaure **sempre primeiro num banco vazio de teste** para conferir o arquivo. Backup que nunca foi
restaurado é só uma esperança.

### 2.1 Testar o backup num Postgres local (descartável)

```bash
docker run --rm -d --name pg-teste -e POSTGRES_PASSWORD=teste -p 5433:5432 postgres:17
# espere uns segundos
pg_restore --host=localhost --port=5433 --username=postgres --dbname=postgres \
           --no-owner --no-privileges --exit-on-error ruralmanager-AAAAMMDD-HHMM.dump
psql -h localhost -p 5433 -U postgres -c "select count(*) from produtor;"
docker stop pg-teste
```

Confira que as tabelas e contagens fazem sentido e que `select version from flyway_schema_history
order by installed_rank desc limit 1;` mostra a última migration (V12 ou maior).

### 2.2 Restaurar de verdade (desastre)

1. **Pare a escrita**: no Render, suspenda o serviço `ruralmanager-api` (Settings > Suspend), para ninguém
   gravar durante a restauração.
2. Se for restaurar **no mesmo projeto** com dados estragados: faça antes um dump do estado atual (mesmo
   estragado), para poder voltar atrás. Depois:
   ```bash
   pg_restore --host=<HOST> --port=5432 --username=postgres --dbname=postgres \
              --clean --if-exists --no-owner --no-privileges ruralmanager-AAAAMMDD-HHMM.dump
   ```
   `--clean` **apaga** as tabelas atuais antes de recriar: só rode depois de confirmar o arquivo (2.1).
3. Se for restaurar **num projeto novo** do Supabase: crie o projeto, rode o `pg_restore` sem `--clean`,
   e troque `RM_DB_URL` / `RM_DB_PASSWORD` no Render para o banco novo.
4. O RLS (Row Level Security) ligado sem policies **vem junto no dump** do schema `public`. Confirme com
   `select relname, relrowsecurity from pg_class where relname in ('safra','gasto','usuario');`
   (deve ser `true`): é ele que fecha a API REST pública do Supabase.
5. Religue o serviço no Render e teste `/actuator/health/readiness` e um login.
6. O Flyway não reaplica nada se `flyway_schema_history` veio no dump, o que é o esperado.

### 2.3 PITR (planos que têm)

No painel, **Database > Backups > Point in Time**, escolha o instante e confirme. Isso substitui o banco
inteiro pelo estado daquele momento: avise os usuários e suspenda a API antes.

## 3. Checagens de produção

### 3.1 O que a API expõe (verificado por teste automático)

[`SuperficieExpostaIT`](../src/test/java/br/com/ruralmanager/SuperficieExpostaIT.java) falha se algo disso mudar:

- Único endpoint público fora do login/cadastro/recuperação: `GET /actuator/health/**` (sem detalhes:
  só `UP`/`DOWN`). Nenhum outro actuator (`env`, `beans`, `heapdump`, `metrics`, `loggers`...) responde,
  nem anônimo nem logado: `management.endpoints.web.exposure.include=health`.
- Swagger e `/v3/api-docs` ficam **desligados** (`RM_OPENAPI=false`, padrão). Só ligue em desenvolvimento.
  Mesmo ligado, qualquer rota fora de `/api/**` e do health cai em `denyAll`.
- Erros não vazam rastro de pilha: o detalhe vai para o log do servidor.

Confira você mesmo em produção (todos devem dar 401/403/404, nunca 200):

```bash
for p in /actuator /actuator/env /actuator/beans /v3/api-docs /swagger-ui.html; do
  curl -s -o /dev/null -w "$p %{http_code}\n" https://SUA-API.onrender.com$p
done
```

### 3.2 Cabeçalhos de segurança

A API responde com `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`,
`Referrer-Policy: no-referrer`, `Content-Security-Policy: default-src 'none'; frame-ancestors 'none'` e,
quando a requisição chega por HTTPS (o proxy do Render manda `X-Forwarded-Proto: https`),
`Strict-Transport-Security: max-age=31536000; includeSubDomains`. O front (nginx) tem os seus.
Teste: `curl -sI https://SUA-API.onrender.com/actuator/health/readiness`.

### 3.3 Rotina sugerida

| Quando | O quê |
|---|---|
| Diário | backup (1.2); conferir que o arquivo novo existe e tem tamanho razoável |
| Mensal | restaurar o backup mais recente num Postgres local (2.1) e conferir contagens |
| Ao trocar de senha do banco | atualizar `RM_DB_PASSWORD` no Render e no `.env`, redeploy, testar login |
| Após cada deploy | `/actuator/health/readiness` e um login de teste |
