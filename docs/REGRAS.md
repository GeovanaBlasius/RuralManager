# Regras de negócio: do enunciado ao código

Mapa das regras de `docs/academico/avaliacao1_enunciado.txt` (RN01 a RN14, numeradas no
próprio enunciado) e das demais regras de validação descritas na mesma seção, numeradas
aqui a partir de RN15 para ficarem rastreáveis. Para cada uma: onde o banco garante
(migração/constraint), onde o serviço Java garante, e qual teste cobre.

Legenda de teste: **✅** coberto hoje: `❌` sem teste ainda (ver item de testes de
integração — Testcontainers).

## Regras de relacionamento (RN01–RN14, numeração do enunciado)

| RN | Resumo | Garantido no banco | Garantido no serviço | Teste |
|---|---|---|---|---|
| RN01 | Produtor 1:N Safra | `fk_safra_produtor` (V4) | `SafraServico` filtra tudo por `contexto.produtorId()` | ✅ `IsolamentoEntreProdutoresIT` |
| RN02 | Safra 1:N Cultura | `fk_cultura_safra` (V4) | `CulturaServico.buscar/criar` | ✅ indireto: `IsolamentoEntreProdutoresIT` (monta o cenário pela API) |
| RN03 | Safra 1:N Gasto | `fk_gasto_safra` (V5) | `GastoServico.criar` exige `safraId` | ✅ indireto: `IsolamentoEntreProdutoresIT` (monta o cenário pela API) |
| RN04 | Produto N:M Safra via Cultura | `fk_cultura_produto` + `fk_cultura_safra` (V4) | `CulturaServico.criar` | ✅ indireto: `IsolamentoEntreProdutoresIT` (monta o cenário pela API) |
| RN05 | Cultura 1:N Gasto (opcional) | `fk_gasto_cultura` (V5), `cultura_id` nulo permitido | `GastoServico.vinculos` | ✅ indireto: `IsolamentoEntreProdutoresIT` (monta o cenário pela API) |
| RN06 | Cultura 1:N Colheita (opcional) | `fk_colheita_cultura` (V4) | `ColheitaServico.criar` | ✅ indireto: `IsolamentoEntreProdutoresIT` (monta o cenário pela API) |
| RN07 | UnidadeMedida 1:N Colheita | `fk_colheita_unidade` (V4) | `ColheitaServico.unidade` | ✅ indireto: `IsolamentoEntreProdutoresIT` (monta o cenário pela API) |
| RN08 | Categoria 1:N Gasto | `fk_gasto_categoria` (V5) | `GastoServico.categoria` | ✅ indireto: `IsolamentoEntreProdutoresIT` (monta o cenário pela API) |
| RN09 | Colheita 1:N ItemFatura | `fk_item_colheita` (V6) | `FaturaServico.gravarItens` | ✅ indireto: `IsolamentoEntreProdutoresIT` (monta o cenário pela API) |
| RN10 | Colheita 1:N Saída; vendido + destinado ≤ colhido | sem CHECK agregando linhas (Postgres puro não expressa `SUM` entre tabelas sem gatilho); `ColheitaRepository.bloquear` (`PESSIMISTIC_WRITE`) evita corrida | `FaturaServico.conferir` e `SaidaServico` somam vendido+destinado antes de aceitar | ✅ `EstoqueEConcorrenciaIT` (inclui duas vendas concorrentes: só uma passa) |
| RN11 | Colheita 1:N Gasto | `fk_gasto_colheita` (V5) + `fk_gasto_cultura_colheita` composta (V11) | `GastoServico.vinculos` | ❌ |
| RN12 | Destino 1:N Saída | `fk_saida_destino` (V5); destino em uso não pode ser removido: sem `ON DELETE CASCADE`, `CatalogoNomeadoServico.excluir` dá `flush()` e a API devolve 409 | `DestinoServico extends CatalogoNomeadoServico` | ❌ |
| RN13 | Cliente 1:N Fatura | `fk_fatura_cliente` (V6) | `FaturaServico.criar` | ✅ indireto: `IsolamentoEntreProdutoresIT` (monta o cenário pela API) |
| RN14 | Fatura 1:N ItemFatura | `fk_item_fatura` + `uk_item_fatura_unico` (V6) | `FaturaServico.gravarItens` | ✅ indireto: `IsolamentoEntreProdutoresIT` (monta o cenário pela API) |

Além das 14 relações, o enunciado pede duas chaves estrangeiras **compostas** que o MySQL
do trabalho tinha e que a V11 reproduziu no Postgres: `gasto(safra_id, cultura_id) →
cultura(safra_id, id)` e `gasto(cultura_id, colheita_id) → colheita(cultura_id, id)`, as
duas com `MATCH SIMPLE` (permite `cultura_id`/`colheita_id` nulos). Antes eram garantidas
só pelo serviço (`GastoServico.vinculos`); agora o banco também garante.

## Demais regras de validação (descritas no enunciado, não numeradas lá)

| RN | Resumo | Garantido no banco | Garantido no serviço | Teste |
|---|---|---|---|---|
| RN15 | Produtor: CPF único e validado quanto ao formato | `uk_produtor_cpf`, `ck_produtor_cpf` (V1) | `Documentos.cpfValido`, `AuthService.cadastrar` | ✅ `DocumentosTest` |
| RN16 | Safra: ano início obrigatório ≥ 1900; ano fim obrigatório, ≥ início, ≤ início + 1 | sem CHECK (ficou só no serviço) | `SafraServico.validarPeriodo` | ✅ `SafraTest` |
| RN17 | Cultura: área plantada > 0 | `ck_cultura_area` (V4) | `CulturaServico.validarCampos` | ❌ |
| RN18 | Cultura: plantio obrigatório, não futuro, dentro do período da safra | sem CHECK (depende da safra relacionada) | `Datas.naoFutura`, `CulturaServico.validarCampos` | ✅ `RegrasDeDataIT` (fora do período e plantio futuro) |
| RN19 | Cultura: não repete produto + variedade na mesma safra | `uk_cultura_natural` (V4) | `CulturaServico.criar/atualizar` (mensagem amigável antes do banco reclamar) | ❌ |
| RN20 | UnidadeMedida: fator de conversão para quilos é opcional | coluna `fator_kg` nula permitida (V3) | — | ❌ |
| RN21 | Categoria: nome único; em uso não pode ser removida | `UNIQUE(nome)`; sem cascade + `flush()` → 409 | `CatalogoNomeadoServico` | ❌ |
| RN22 | Gasto: valor > 0; data não futura | `ck_gasto_valor` (V5) | `GastoServico.validarData` | ❌ |
| RN23 | Colheita: quantidade > 0; data não futura e não anterior ao plantio | `ck_colheita_qtd` (V4) | `ColheitaServico.validarData` | ✅ `RegrasDeDataIT` (colheita antes do plantio) |
| RN24 | Destino: nome único; em uso não pode ser removido | `UNIQUE(nome)`; sem cascade + `flush()` → 409 | `CatalogoNomeadoServico` | ❌ |
| RN25 | Saída: quantidade > 0; data não futura e não anterior à colheita | `ck_saida_qtd` (V5) | `SaidaServico` | ✅ `RegrasDeDataIT` (saída antes da colheita) |
| RN26 | Cliente: CPF/CNPJ único e validado quanto ao formato | `uk_cliente_documento`, `ck_cliente_documento` (V6) | `Documentos.cpfOuCnpjValido`, `ClienteServico.documento` | ✅ `DocumentosTest` |
| RN27 | Fatura: data da compra não futura | sem CHECK (regra de serviço) | `FaturaServico.criar` | ❌ |
| RN28 | Item da fatura: quantidade e preço unitário > 0 | `ck_item_quantidade`, `ck_item_preco` (V6) | — | ❌ |

### Diferença proposital da RN26 em relação ao enunciado

O enunciado original pede CPF/CNPJ do cliente **único no sistema inteiro** (ele não previa
múltiplos produtores usando o mesmo banco). Na aplicação real, cada produtor é uma conta
isolada (multi-tenant), e dois produtores diferentes podem muito bem vender para o mesmo
comprador sem que um veja o cadastro do outro. Por isso a unicidade virou **por produtor**
(`UNIQUE(produtor_id, cpf_cnpj)`, não `UNIQUE(cpf_cnpj)`). Essa mudança é intencional e
seguida das demais diferenças de schema, listadas no README.

## Cobertura de testes

Suíte atual: 17 testes unitários (`Documentos`, `Datas`, `Safra`, `RelatorioCalculos`) e 27 de
integração com Testcontainers/Postgres real (`*IT`): isolamento entre produtores
(`IsolamentoEntreProdutoresIT`), estoque e venda concorrente (`EstoqueEConcorrenciaIT`),
regras de data (`RegrasDeDataIT`), login, recuperação de senha e limitador de tentativas
(`AutenticacaoIT`), perfis ADMIN/PRODUTOR/OPERADOR (`PerfisIT`) e o contrato do relatório com os
números do trabalho (`RelatorioContratoIT`: soja 18,5 ha, 620 sc60, 2.010,81 kg/ha). `mvn test`
roda tudo (precisa do Docker ligado).

Ainda sem teste dedicado: RN11 (chaves compostas da V11, hoje só exercitadas indiretamente),
RN12/RN21/RN24 (catálogo em uso não é removido: 409), RN17, RN19, RN20, RN22, RN27 e RN28.
