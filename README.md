# RuralManager

Gestão agrícola para pequenos e médios produtores: safras, culturas, colheitas, gastos, vendas (faturas), estoque e relatórios de lucro, custo por hectare e produtividade. Site responsivo **instalável (PWA)**.

## Estrutura
```
pom.xml, src/          API Spring Boot (br.com.ruralmanager)
src/main/resources/db/migration   V1..V8 (Postgres)
frontend/              PWA React + Vite + TypeScript
docs/                  CONTEXTO.md e material acadêmico
Dockerfile, docker-compose.yml, .env.example
```

## Rodar localmente
Requisitos: JDK 21, Maven 3.9+, Node 20+.
```bash
cp .env.example .env        # preencha RM_DB_URL, RM_DB_PASSWORD, RM_JWT_SECRET
set -a; source .env; set +a
mvn spring-boot:run          # API em :8080
cd frontend && npm install && npm run dev   # app em :5173
```
Testes: `mvn test` (API) e `cd frontend && npm test`.

## Variáveis de ambiente
| Variável | Descrição |
|---|---|
| RM_DB_URL | JDBC do Postgres/Supabase (`sslmode=require`) |
| RM_DB_USER / RM_DB_PASSWORD | credenciais do banco |
| RM_JWT_SECRET | segredo HS256, 32+ caracteres |
| RM_JWT_MINUTOS | validade do token (padrão 480) |
| RM_CORS_ORIGENS | origens do front, separadas por vírgula |
| RM_OPENAPI | `true` liga o Swagger em `/swagger-ui.html` |
| RM_RESEND_API_KEY | chave da API do [Resend](https://resend.com) para o e-mail de recuperação de senha; em branco, só registra no log |
| RM_EMAIL_REMETENTE | remetente dos e-mails, ex. `RuralManager <onboarding@resend.dev>` |
| RM_ADMIN_EMAIL / RM_ADMIN_SENHA | cria o administrador inicial na subida (senha com 12+ caracteres); em branco, nenhum é criado |

## Deploy

**Local / VPS único:** `docker compose up --build` sobe a API e o front (nginx, porta 8081, com proxy `/api`).

**Render.com (hospedagem gratuita, sem domínio próprio):** o repositório já tem um `render.yaml` pronto.
1. Crie uma conta grátis em [render.com](https://render.com).
2. **New > Blueprint**, conecte este repositório no GitHub — o Render lê o `render.yaml` e propõe dois serviços: `ruralmanager-api` e `ruralmanager-web`.
3. Preencha os campos secretos que ele pedir (os mesmos valores do seu `.env` local: `RM_DB_URL`, `RM_DB_PASSWORD`, `RM_JWT_SECRET`, `RM_RESEND_API_KEY`, `RM_ADMIN_EMAIL`, `RM_ADMIN_SENHA`).
4. Aguarde o build dos dois serviços e abra a URL do serviço `ruralmanager-web` (o Render mostra no painel; se os nomes `ruralmanager-api`/`ruralmanager-web` já estiverem em uso, ele acrescenta um sufixo, ex. `ruralmanager-web-w8cf.onrender.com` — nesse caso ajuste `RM_CORS_ORIGENS` e `VITE_API_URL` no `render.yaml` para os nomes finais).

No plano gratuito, cada serviço "dorme" depois de 15 min sem acesso e demora alguns segundos para acordar na próxima visita.

Em qualquer outra hospedagem separada (front e API em domínios diferentes), builde o front com `VITE_API_URL=https://sua-api` e ajuste `RM_CORS_ORIGENS` na API para a URL do front. A API é stateless: escala horizontalmente atrás de um balanceador.

## Segurança
JWT stateless, BCrypt(12), CPF/CNPJ validados por dígito verificador, isolamento por produtor (404 em recurso alheio), travas de linha no estoque, RLS ligado no Supabase, erros em ProblemDetail sem vazar stack. Limitações em `CLAUDE.md`.

## Status
Backend e front verificados de ponta a ponta (compilação, testes, Supabase real, navegador): cadastro, safra, cultura, colheita, gasto, venda, painel, suporte (relatar problemas), recuperação de senha por e-mail e painel de administrador.
