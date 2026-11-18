# RuralManager — guia para o Claude Code

Sistema de gestão agrícola para pequenos e médios produtores (safras, culturas, colheitas, gastos, vendas, estoque, relatórios).
Nasceu como trabalho de Banco de Dados I (IFC Rio do Sul, Davi Bianchi Ayres e Geovana Blasius, prof.ª Patricia Blini Estivalete) e virou aplicação real.
Contexto completo: `docs/CONTEXTO.md`. Material da disciplina: `docs/academico/`.

## Stack
- Backend: Spring Boot 3.3.5, Java 21, JPA (`ddl-auto: validate`), Flyway, Spring Security stateless JWT (HS256), BCrypt.
- Banco: Postgres (Supabase, projeto `ybzkckwdxpryfqefplbu`). Migrations `V1..V8` **já aplicadas** lá; Flyway usa `baseline-version: 8`.
- Frontend: `frontend/` — React 18 + Vite + TypeScript + TanStack Query, PWA instalável. CRUD genérico guiado por `src/resources/definicoes.ts`.
- Pacote Java: `br.com.ruralmanager` (nomes de domínio em português; manter).

## ESTADO REAL (leia primeiro)
- O Java compila e roda. `mvn test` (precisa do Docker ligado) executa testes unitários e de integração com Testcontainers/Postgres real: isolamento entre produtores, estoque e concorrência, regras de data, login/recuperação/verificação de e-mail, perfis, contrato do relatório e carga de demonstração.
- Migrations `V1..V12` existem; o Flyway aplica as novas na subida da API (V9 em diante já foram aplicadas no Supabase).
- Frontend: `tsc`, `vite build` e vitest passam. Regras de negócio documentadas em `docs/REGRAS.md`.
- Cadastro novo exige confirmar código de 6 dígitos por e-mail (`RM_EXIGIR_VERIFICACAO_EMAIL`, padrão true); sem `RM_RESEND_API_KEY` o cadastro e a recuperação respondem 503. Nos testes, `EmailServico` é substituído por um falso que expõe o código enviado (`codigoEnviadoPara`).

## Como trabalhar
1. `mvn test` e `cd frontend && npm run build && npm test` antes de cada commit.
2. `.env` a partir de `.env.example` (nunca commitar). Rodar `mvn spring-boot:run`; testar `/actuator/health/readiness`.
3. Front: `cd frontend && npm i && npm run dev` (proxy `/api` → :8080).

## Regras que não podem quebrar
- Produtor vem **somente do JWT**; todo repositório filtra por dono; recurso de outro produtor responde **404**, nunca 403.
- Estoque: `ColheitaRepository.bloquear` (PESSIMISTIC_WRITE); `FaturaServico.gravarItens` ordena por colheitaId antes de travar (evita deadlock).
- Regras de data/negócio que o banco não cobre ficam na camada de serviço (plantio dentro da safra e não futuro; colheita ≥ plantio; saída/venda ≥ colheita; gasto coerente).
- Relatórios são SQL agregado (JdbcClient) — não trocar por laços com N+1. Listagens paginadas (máx. 100); `sort` do cliente é ignorado de propósito.
- Banco: RLS ligado sem policies (fecha a API REST pública do Supabase; o JDBC com role dono ignora). Não criar policies sem pensar.

## Commits
Nunca adicionar linha de atribuição ao Claude (`Co-Authored-By: Claude ...` ou similar) em commits ou PRs deste repositório. A Geovana pediu para tirar isso; o histórico já foi reescrito uma vez por causa disso.

## Limitações conhecidas (decisões a revisar)
- Sem verificação de e-mail no cadastro (dá para "ocupar" um CPF de outra pessoa).
- Token JWT em localStorage (mitigado por expiração de 8 h e CSP no nginx).
- Limitador de tentativas de login é por instância; para escala horizontal use rate limit no gateway.
- Listas de opções de colheita limitadas a 100; regra de janela do plantio é por ano da safra.

## Segurança — AÇÃO DA GEOVANA
O repositório público antigo (`GeovanaBlasius/RuralManager`) tem `config.properties` com **senha de banco vazada no histórico**. Troque essa senha e, ao subir este projeto, ponha o protótipo antigo em `legacy/` ou em outra branch e nunca commite `.env`.
