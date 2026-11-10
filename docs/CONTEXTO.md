# Contexto do projeto RuralManager

## Origem
Trabalho de **Banco de Dados I** (Instituto Federal Catarinense, Rio do Sul) de Davi Bianchi Ayres e Geovana Blasius; professora Patricia Blini Estivalete. Cenário: pequenos e médios produtores controlam safras, gastos e vendas em cadernos/planilhas; o RuralManager centraliza isso.

## Fases já entregues (material em `docs/academico/`)
1. **1ª avaliação** — enunciado e regras de negócio (`avaliacao1_enunciado.txt`) e DER (`RuralManager - Davi e Geovana.docx`).
2. **2ª avaliação** — implementação em MySQL (`ruralmanager_completo.sql`, enunciado em `avaliacao2_enunciado.txt`).
3. **Consultas SQL** — `RuralManager - Consultas SQL.docx` e `ruralmanager_consultas.md`.
4. **Aplicação real** (esta pasta) — Spring Boot + Postgres/Supabase + PWA.

## Entidades (13 + usuário)
Produtor (CPF único, validado), Usuario (login; ADMIN sem produtor, demais vinculados a um produtor), Safra (ano início ≥ 1900; ano fim = início ou início+1), Produto, Cultura (produto + variedade únicos por safra; área > 0; plantio não futuro e dentro da safra), Colheita, UnidadeMedida, Gasto, CategoriaGasto, Saida, Destino, Cliente (CPF/CNPJ), Fatura + ItemFatura.

## Regras centrais
- Plantio: obrigatório, não futuro, dentro do período da safra.
- Colheita ≥ plantio; saída e venda ≥ colheita.
- Vendido + destinado ≤ colhido (estoque nunca negativo, protegido por trava de linha).
- Divisão por zero nos relatórios devolve 0 (regra do projeto original).
- Relatórios: lucro, margem, custo/ha, receita/ha, produtividade kg/ha, ROI por cultura; painel; estoque por colheita.

## Decisões da conversa de desenvolvimento
- Backend **Spring Boot** (ela testa na IDE); frontend **PWA** (site responsivo instalável; app nativo pode reaproveitar a API JWT depois).
- Produtor = identidade do usuário (multi-tenant); produtor sempre vem do token.
- Banco migrado de MySQL para **Supabase/Postgres**; migrations V1–V8 já aplicadas (o backup MySQL original ficou fora do ZIP; o script acadêmico está em `docs/academico/`).
- Repositório mais recente: `github.com/GeovanaBlasius/RuralManager` (protótipo Java antigo). Contém senha vazada em `config.properties` — rotacionar.

## Diferenças entre o schema acadêmico (MySQL) e o da aplicação (Postgres)
- Tabela `usuario` e `produtor_id` em todas as tabelas de dono; IDENTITY/NUMERIC no lugar de AUTO_INCREMENT/DECIMAL; CHECKs com regex `~`; colunas de auditoria; RLS ligado.
- Regras que triggers faziam no MySQL passaram para a camada de serviço Java onde o banco não consegue expressá-las.

## Pendências
Ver "Primeiras tarefas" e "Limitações" em `CLAUDE.md`. Principal: compilar o Java e corrigir.
