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

## Deploy
`docker compose up --build` sobe a API e o front (nginx, porta 8081, com proxy `/api`). Em hospedagem separada, builde o front com `VITE_API_URL=https://sua-api` e ajuste `RM_CORS_ORIGENS`. A API é stateless: escala horizontalmente atrás de um balanceador.

## Segurança
JWT stateless, BCrypt(12), CPF/CNPJ validados por dígito verificador, isolamento por produtor (404 em recurso alheio), travas de linha no estoque, RLS ligado no Supabase, erros em ProblemDetail sem vazar stack. Limitações em `CLAUDE.md`.

## Status
Front verificado (build, testes, navegador). **Backend Java ainda não compilado** — veja `CLAUDE.md` para os primeiros passos.
