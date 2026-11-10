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
- **O Java NUNCA foi compilado** (o ambiente onde foi escrito não alcançava o Maven Central). Só foi checado sintaxe com `javac`. Espere alguns erros de compilação/import/anotação.
- Verificado de verdade: SQL no Supabase (testes com rollback), TypeScript (`tsc`, `vite build`, vitest 5/5, Playwright sem erros de console), `Documentos` (CPF/CNPJ) executado isoladamente.
- Testes Java escritos mas nunca rodados: `src/test/java/...` (Documentos, Datas, Safra, RelatorioCalculos).

## Primeiras tarefas
1. `mvn -q compile` → corrigir tudo que aparecer (sem mudar regras de negócio).
2. `mvn test` → corrigir testes/código.
3. Criar `.env` a partir de `.env.example` (RM_DB_URL/RM_DB_PASSWORD do Supabase; `RM_JWT_SECRET` com 32+ chars). Rodar `mvn spring-boot:run`; testar `/actuator/health/readiness`.
4. Subir o front: `cd frontend && npm i && npm run dev` (proxy `/api` → :8080). Fluxo: cadastro → safra → cultura → colheita → gasto → venda → painel.
5. Escrever testes de integração (Testcontainers/Postgres) para: isolamento entre produtores (404 em recurso alheio), estoque (vendido+destinado ≤ colhido) e concorrência de vendas.

## Regras que não podem quebrar
- Produtor vem **somente do JWT**; todo repositório filtra por dono; recurso de outro produtor responde **404**, nunca 403.
- Estoque: `ColheitaRepository.bloquear` (PESSIMISTIC_WRITE); `FaturaServico.gravarItens` ordena por colheitaId antes de travar (evita deadlock).
- Regras de data/negócio que o banco não cobre ficam na camada de serviço (plantio dentro da safra e não futuro; colheita ≥ plantio; saída/venda ≥ colheita; gasto coerente).
- Relatórios são SQL agregado (JdbcClient) — não trocar por laços com N+1. Listagens paginadas (máx. 100); `sort` do cliente é ignorado de propósito.
- Banco: RLS ligado sem policies (fecha a API REST pública do Supabase; o JDBC com role dono ignora). Não criar policies sem pensar.

## Limitações conhecidas (decisões a revisar)
- Sem verificação de e-mail no cadastro (dá para "ocupar" um CPF de outra pessoa).
- Token JWT em localStorage (mitigado por expiração de 8 h e CSP no nginx).
- Limitador de tentativas de login é por instância; para escala horizontal use rate limit no gateway.
- Listas de opções de colheita limitadas a 100; regra de janela do plantio é por ano da safra.

## Segurança — AÇÃO DA GEOVANA
O repositório público antigo (`GeovanaBlasius/RuralManager`) tem `config.properties` com **senha de banco vazada no histórico**. Troque essa senha e, ao subir este projeto, ponha o protótipo antigo em `legacy/` ou em outra branch e nunca commite `.env`.
