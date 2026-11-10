# RuralManager — Consultas SQL

Banco: `RURALMANAGER` — treze tabelas, oitenta e tantos registros, conforme entregue na 2ª avaliação.
Todas as consultas abaixo foram executadas e as respostas são a saída real do MySQL.

---

## a) Funções de agregação

### a1 — COUNT

**Pergunta:** Quantas lavouras foram cadastradas em cada safra?

```sql
SELECT S.SAF_CODE, CONCAT(S.SAF_ANO_INICIO,'/',S.SAF_ANO_FIM) AS SAFRA,
       COUNT(C.CUL_CODE) AS TOTAL_LAVOURAS
FROM SAFRA S, CULTURA C
WHERE S.SAF_CODE = C.SAF_CODE
GROUP BY S.SAF_CODE, S.SAF_ANO_INICIO, S.SAF_ANO_FIM;
```

| SAF_CODE | SAFRA | TOTAL_LAVOURAS |
|---|---|---|
| 1 | 2025/2026 | 3 |
| 2 | 2026/2027 | 2 |
| 3 | 2025/2026 | 2 |
| 4 | 2025/2026 | 1 |
| 5 | 2025/2026 | 1 |
| 6 | 2024/2025 | 1 |

### a2 — SUM

**Pergunta:** Qual o custo total de cada safra?

```sql
SELECT S.SAF_CODE, CONCAT(S.SAF_ANO_INICIO,'/',S.SAF_ANO_FIM) AS SAFRA,
       SUM(G.GAS_VALOR) AS CUSTO_TOTAL
FROM SAFRA S, GASTO G
WHERE S.SAF_CODE = G.SAF_CODE
GROUP BY S.SAF_CODE, S.SAF_ANO_INICIO, S.SAF_ANO_FIM;
```

| SAF_CODE | SAFRA | CUSTO_TOTAL |
|---|---|---|
| 1 | 2025/2026 | 21880.00 |
| 2 | 2026/2027 | 10200.00 |
| 3 | 2025/2026 | 850.00 |

### a3 — MAX

**Pergunta:** Qual a maior área plantada em uma única lavoura?

```sql
SELECT MAX(CUL_AREA_HA) AS MAIOR_AREA FROM CULTURA;
```

| MAIOR_AREA |
|---|
| 22.00 |

### a4 — MIN

**Pergunta:** Qual o menor valor de gasto registrado?

```sql
SELECT MIN(GAS_VALOR) AS MENOR_GASTO FROM GASTO;
```

| MENOR_GASTO |
|---|
| 450.00 |

### a5 — AVG

**Pergunta:** Qual o preço médio pago por unidade em cada produto comercializado?

```sql
SELECT P.PRO_NOME, AVG(I.ITE_PRECO_UNITARIO) AS PRECO_MEDIO
FROM ITEM_FATURA I, COLHEITA CO, CULTURA C, PRODUTO P
WHERE I.COL_CODE = CO.COL_CODE
  AND CO.CUL_CODE = C.CUL_CODE
  AND C.PRO_CODE = P.PRO_CODE
GROUP BY P.PRO_NOME;
```

| PRO_NOME | PRECO_MEDIO |
|---|---|
| Alho | 12.000000 |
| Cebola | 2.350000 |
| Milho | 74.200000 |
| Soja | 131.812500 |

---

## b) Junção interna

### b1 — Método antigo (vírgula e WHERE)

**Pergunta:** Quais lavouras foram plantadas e de qual produto é cada uma?

```sql
SELECT C.CUL_CODE, P.PRO_NOME, C.CUL_VARIEDADE, C.CUL_AREA_HA
FROM CULTURA C, PRODUTO P
WHERE C.PRO_CODE = P.PRO_CODE;
```

| CUL_CODE | PRO_NOME | CUL_VARIEDADE | CUL_AREA_HA |
|---|---|---|---|
| 1 | Soja | BMX Potencia RR | 18.50 |
| 2 | Soja | TMG 7062 IPRO | 12.00 |
| 3 | Milho | AG 9025 PRO3 | 9.75 |
| 4 | Soja | BMX Potencia RR | 22.00 |
| 5 | Milho | AG 9025 PRO3 | 8.40 |
| 6 | Cebola | Bola Precoce | 3.20 |
| 7 | Alho | Roxo Caxiense | 1.80 |
| 8 | Feijao | Carioca Precoce | 2.50 |
| 9 | Arroz | IRGA 424 | 5.00 |
| 10 | Fumo | Virginia 1 | 1.40 |

### b2 — NATURAL JOIN

**Pergunta:** Quais safras pertencem a cada produtor?

```sql
SELECT PRD_NOME, SAF_CODE, SAF_ANO_INICIO, SAF_ANO_FIM
FROM PRODUTOR NATURAL JOIN SAFRA;
```

A junção natural funciona aqui porque `PRD_CPF` é a única coluna de mesmo nome nas duas tabelas, e o SGBD a usa automaticamente como critério.

| PRD_NOME | SAF_CODE | SAF_ANO_INICIO | SAF_ANO_FIM |
|---|---|---|---|
| Pedro Henrique Schmitt | 4 | 2025 | 2026 |
| Joao Carlos Bianchi | 1 | 2025 | 2026 |
| Joao Carlos Bianchi | 2 | 2026 | 2027 |
| Ana Lucia Hoffmann | 5 | 2025 | 2026 |
| Valdir Kruger | 6 | 2024 | 2025 |
| Maria Aparecida Souza | 3 | 2025 | 2026 |

### b3 — JOIN USING

**Pergunta:** Em que unidade de medida cada colheita foi expressa?

```sql
SELECT COL_CODE, COL_QUANTIDADE, UNI_SIGLA, UNI_NOME
FROM COLHEITA JOIN UNIDADE_MEDIDA USING (UNI_CODE);
```

| COL_CODE | COL_QUANTIDADE | UNI_SIGLA | UNI_NOME |
|---|---|---|---|
| 1 | 620.00 | sc60 | Saca de 60 quilos |
| 2 | 485.00 | sc60 | Saca de 60 quilos |
| 3 | 710.00 | sc60 | Saca de 60 quilos |
| 4 | 940.00 | sc50 | Saca de 50 quilos |
| 5 | 8600.00 | kg | Quilograma |
| 6 | 210.00 | cx20 | Caixa de 20 quilos |

### b4 — JOIN ON

**Pergunta:** Como os gastos se distribuem entre as categorias?

```sql
SELECT G.GAS_CODE, T.CAT_NOME, G.GAS_DESCRICAO, G.GAS_VALOR
FROM GASTO G JOIN CATEGORIA T ON G.CAT_CODE = T.CAT_CODE
ORDER BY G.GAS_CODE;
```

| GAS_CODE | CAT_NOME | GAS_DESCRICAO | GAS_VALOR |
|---|---|---|---|
| 1 | Sementes | Compra de sementes de soja | 4500.00 |
| 2 | Fertilizantes | Compra de fertilizantes para soja | 6800.00 |
| 3 | Defensivos | Compra de defensivos agricolas | 2300.00 |
| 4 | Sementes | Compra de sementes de milho | 1800.00 |
| 5 | Combustivel | Combustivel para maquinas agricolas | 1200.00 |
| 6 | Mao de obra | Pagamento de mao de obra | 2500.00 |

*(mostradas as seis primeiras das treze linhas)*

---

## c) Junção externa

### c1 — LEFT JOIN

**Pergunta:** Quais colheitas cada lavoura teve, incluindo as lavouras que ainda não foram colhidas?

```sql
SELECT C.CUL_CODE, P.PRO_NOME, C.CUL_VARIEDADE, CO.COL_CODE, CO.COL_QUANTIDADE
FROM CULTURA C
JOIN PRODUTO P ON P.PRO_CODE = C.PRO_CODE
LEFT JOIN COLHEITA CO ON CO.CUL_CODE = C.CUL_CODE
ORDER BY C.CUL_CODE;
```

| CUL_CODE | PRO_NOME | CUL_VARIEDADE | COL_CODE | COL_QUANTIDADE |
|---|---|---|---|---|
| 1 | Soja | BMX Potencia RR | 1 | 620.00 |
| 1 | Soja | BMX Potencia RR | 2 | 485.00 |
| 2 | Soja | TMG 7062 IPRO | 3 | 710.00 |
| 3 | Milho | AG 9025 PRO3 | 4 | 940.00 |
| 4 | Soja | BMX Potencia RR | NULL | NULL |
| 5 | Milho | AG 9025 PRO3 | NULL | NULL |
| 6 | Cebola | Bola Precoce | 5 | 8600.00 |
| 7 | Alho | Roxo Caxiense | 6 | 210.00 |
| 8 | Feijao | Carioca Precoce | NULL | NULL |
| 9 | Arroz | IRGA 424 | NULL | NULL |
| 10 | Fumo | Virginia 1 | NULL | NULL |

Os cinco NULL são as lavouras cujo ciclo ainda não se encerrou. Com junção interna elas desapareceriam do resultado.

### c2 — RIGHT JOIN

**Pergunta:** Quais unidades de medida cadastradas nunca foram utilizadas em uma colheita?

```sql
SELECT CO.COL_CODE, CO.COL_QUANTIDADE, U.UNI_CODE, U.UNI_NOME, U.UNI_SIGLA
FROM COLHEITA CO
RIGHT JOIN UNIDADE_MEDIDA U ON CO.UNI_CODE = U.UNI_CODE
ORDER BY U.UNI_CODE;
```

| COL_CODE | COL_QUANTIDADE | UNI_CODE | UNI_NOME | UNI_SIGLA |
|---|---|---|---|---|
| 3 | 710.00 | 1 | Saca de 60 quilos | sc60 |
| 2 | 485.00 | 1 | Saca de 60 quilos | sc60 |
| 1 | 620.00 | 1 | Saca de 60 quilos | sc60 |
| 4 | 940.00 | 2 | Saca de 50 quilos | sc50 |
| 5 | 8600.00 | 3 | Quilograma | kg |
| NULL | NULL | 4 | Tonelada | t |
| 6 | 210.00 | 5 | Caixa de 20 quilos | cx20 |
| NULL | NULL | 6 | Unidade | un |
| NULL | NULL | 7 | Duzia | dz |

Tonelada, unidade e dúzia estão cadastradas mas nenhuma colheita as utilizou.

---

## d) Subconsulta

### d1 — No WHERE

**Pergunta:** Quais lavouras têm área plantada acima da média geral?

```sql
SELECT CUL_CODE, CUL_VARIEDADE, CUL_AREA_HA
FROM CULTURA
WHERE CUL_AREA_HA > (SELECT AVG(CUL_AREA_HA) FROM CULTURA);
```

| CUL_CODE | CUL_VARIEDADE | CUL_AREA_HA |
|---|---|---|
| 1 | BMX Potencia RR | 18.50 |
| 2 | TMG 7062 IPRO | 12.00 |
| 3 | AG 9025 PRO3 | 9.75 |
| 4 | BMX Potencia RR | 22.00 |

### d2 — Com IN

**Pergunta:** Quais produtores possuem safra referente ao período 2025/2026?

```sql
SELECT PRD_CPF, PRD_NOME
FROM PRODUTOR
WHERE PRD_CPF IN (
    SELECT PRD_CPF FROM SAFRA
    WHERE SAF_ANO_INICIO = 2025 AND SAF_ANO_FIM = 2026
);
```

| PRD_CPF | PRD_NOME |
|---|---|
| 11122233355 | Pedro Henrique Schmitt |
| 12345678901 | Joao Carlos Bianchi |
| 22233344466 | Ana Lucia Hoffmann |
| 98765432100 | Maria Aparecida Souza |

### d3 — No HAVING

**Pergunta:** Quais safras acumularam custo total superior ao maior gasto individual do sistema?

```sql
SELECT SAF_CODE, SUM(GAS_VALOR) AS CUSTO_TOTAL
FROM GASTO
GROUP BY SAF_CODE
HAVING SUM(GAS_VALOR) > (SELECT MAX(GAS_VALOR) FROM GASTO);
```

| SAF_CODE | CUSTO_TOTAL |
|---|---|
| 1 | 21880.00 |
| 2 | 10200.00 |

O maior gasto individual é de 7200,00. A safra 3, com 850,00, fica de fora.

### d4 — Com ALL

**Pergunta:** Qual lavoura tem área maior que todas as lavouras da safra 1?

```sql
SELECT CUL_CODE, SAF_CODE, CUL_VARIEDADE, CUL_AREA_HA
FROM CULTURA
WHERE CUL_AREA_HA > ALL (SELECT CUL_AREA_HA FROM CULTURA WHERE SAF_CODE = 1);
```

| CUL_CODE | SAF_CODE | CUL_VARIEDADE | CUL_AREA_HA |
|---|---|---|---|
| 4 | 2 | BMX Potencia RR | 22.00 |

### d5 — No FROM (tabela derivada)

**Pergunta:** Qual a média de lavouras por safra?

```sql
SELECT ROUND(AVG(T.TOTAL), 2) AS MEDIA_LAVOURAS_POR_SAFRA
FROM (SELECT SAF_CODE, COUNT(*) AS TOTAL FROM CULTURA GROUP BY SAF_CODE) T;
```

| MEDIA_LAVOURAS_POR_SAFRA |
|---|
| 1.67 |

### d6 — Na lista de atributos

**Pergunta:** Como a área de cada lavoura se compara à área média do sistema?

```sql
SELECT C.CUL_CODE, C.CUL_VARIEDADE, C.CUL_AREA_HA,
       (SELECT ROUND(AVG(CUL_AREA_HA), 2) FROM CULTURA) AS MEDIA_GERAL
FROM CULTURA C;
```

| CUL_CODE | CUL_VARIEDADE | CUL_AREA_HA | MEDIA_GERAL |
|---|---|---|---|
| 1 | BMX Potencia RR | 18.50 | 8.46 |
| 2 | TMG 7062 IPRO | 12.00 | 8.46 |
| 3 | AG 9025 PRO3 | 9.75 | 8.46 |
| 4 | BMX Potencia RR | 22.00 | 8.46 |
| 5 | AG 9025 PRO3 | 8.40 | 8.46 |

*(mostradas as cinco primeiras das dez linhas)*

### d7 — Correlacionada

**Pergunta:** Quais lavouras superam a área média da própria safra?

```sql
SELECT C.CUL_CODE, C.SAF_CODE, C.CUL_VARIEDADE, C.CUL_AREA_HA
FROM CULTURA C
WHERE C.CUL_AREA_HA > (
    SELECT AVG(C2.CUL_AREA_HA) FROM CULTURA C2 WHERE C2.SAF_CODE = C.SAF_CODE
);
```

A subconsulta é correlacionada porque referencia `C.SAF_CODE`, da consulta externa: ela é recalculada para cada linha avaliada.

| CUL_CODE | SAF_CODE | CUL_VARIEDADE | CUL_AREA_HA |
|---|---|---|---|
| 1 | 1 | BMX Potencia RR | 18.50 |
| 4 | 2 | BMX Potencia RR | 22.00 |
| 6 | 3 | Bola Precoce | 3.20 |

---

## e) Funções de data

Observação sobre nomenclatura: `DATEADD` é a forma do SQL Server e `TO_CHAR` e `TO_DATE` são do Oracle e do PostgreSQL. No MySQL as funções equivalentes são `DATE_ADD`, `DATE_FORMAT` e `STR_TO_DATE`, utilizadas abaixo.

### e1 — YEAR

**Pergunta:** Quantas colheitas foram realizadas em cada ano?

```sql
SELECT YEAR(COL_DATA) AS ANO, COUNT(*) AS TOTAL_COLHEITAS
FROM COLHEITA
GROUP BY YEAR(COL_DATA);
```

| ANO | TOTAL_COLHEITAS |
|---|---|
| 2025 | 2 |
| 2026 | 4 |

### e2 — MONTH

**Pergunta:** Em quais meses ocorreram os gastos da safra 1 e quanto foi lançado em cada um?

```sql
SELECT MONTH(GAS_DATA) AS MES, COUNT(*) AS LANCAMENTOS, SUM(GAS_VALOR) AS TOTAL
FROM GASTO
WHERE SAF_CODE = 1
GROUP BY MONTH(GAS_DATA)
ORDER BY MES;
```

| MES | LANCAMENTOS | TOTAL |
|---|---|---|
| 1 | 1 | 450.00 |
| 2 | 1 | 680.00 |
| 3 | 1 | 950.00 |
| 9 | 1 | 1800.00 |
| 10 | 4 | 14300.00 |
| 11 | 1 | 1200.00 |
| 12 | 1 | 2500.00 |

### e3 — DAY

**Pergunta:** Em que dia do mês cada compra foi realizada?

```sql
SELECT FAT_CODE, FAT_DATA_COMPRA, DAY(FAT_DATA_COMPRA) AS DIA FROM FATURA;
```

| FAT_CODE | FAT_DATA_COMPRA | DIA |
|---|---|---|
| 1 | 2026-03-20 | 20 |
| 2 | 2026-04-15 | 15 |
| 3 | 2026-04-02 | 2 |
| 4 | 2025-09-20 | 20 |
| 5 | 2025-09-25 | 25 |

### e4 — DATE_ADD (equivalente ao DATEADD)

**Pergunta:** Qual o vencimento de cada fatura, considerando prazo de trinta dias?

```sql
SELECT FAT_CODE, FAT_DATA_COMPRA,
       DATE_ADD(FAT_DATA_COMPRA, INTERVAL 30 DAY) AS VENCIMENTO
FROM FATURA;
```

| FAT_CODE | FAT_DATA_COMPRA | VENCIMENTO |
|---|---|---|
| 1 | 2026-03-20 | 2026-04-19 |
| 2 | 2026-04-15 | 2026-05-15 |
| 3 | 2026-04-02 | 2026-05-02 |
| 4 | 2025-09-20 | 2025-10-20 |
| 5 | 2025-09-25 | 2025-10-25 |

### e5 — DATEDIFF

**Pergunta:** Quantos dias durou o ciclo de cada lavoura, do plantio até a colheita?

```sql
SELECT C.CUL_CODE, P.PRO_NOME, C.CUL_DATA_PLANTIO, CO.COL_DATA,
       DATEDIFF(CO.COL_DATA, C.CUL_DATA_PLANTIO) AS DIAS_DE_CICLO
FROM CULTURA C
JOIN PRODUTO P ON P.PRO_CODE = C.PRO_CODE
JOIN COLHEITA CO ON CO.CUL_CODE = C.CUL_CODE
ORDER BY DIAS_DE_CICLO;
```

| CUL_CODE | PRO_NOME | CUL_DATA_PLANTIO | COL_DATA | DIAS_DE_CICLO |
|---|---|---|---|---|
| 6 | Cebola | 2025-05-15 | 2025-09-12 | 120 |
| 7 | Alho | 2025-04-10 | 2025-08-25 | 137 |
| 3 | Milho | 2025-09-28 | 2026-02-18 | 143 |
| 1 | Soja | 2025-10-12 | 2026-03-14 | 153 |
| 2 | Soja | 2025-10-20 | 2026-03-28 | 159 |
| 1 | Soja | 2025-10-12 | 2026-03-22 | 161 |

### e6 — DATE_FORMAT (equivalente ao TO_CHAR)

**Pergunta:** Como exibir a data da colheita no formato brasileiro?

```sql
SELECT COL_CODE, COL_DATA, DATE_FORMAT(COL_DATA, '%d/%m/%Y') AS DATA_BR
FROM COLHEITA;
```

| COL_CODE | COL_DATA | DATA_BR |
|---|---|---|
| 1 | 2026-03-14 | 14/03/2026 |
| 2 | 2026-03-22 | 22/03/2026 |
| 3 | 2026-03-28 | 28/03/2026 |
| 4 | 2026-02-18 | 18/02/2026 |
| 5 | 2025-09-12 | 12/09/2025 |
| 6 | 2025-08-25 | 25/08/2025 |

### e7 — STR_TO_DATE (equivalente ao TO_DATE)

**Pergunta:** Quais colheitas ocorreram a partir de 01/03/2026, informando a data como texto?

```sql
SELECT COL_CODE, COL_DATA, COL_QUANTIDADE
FROM COLHEITA
WHERE COL_DATA >= STR_TO_DATE('01/03/2026', '%d/%m/%Y');
```

| COL_CODE | COL_DATA | COL_QUANTIDADE |
|---|---|---|
| 1 | 2026-03-14 | 620.00 |
| 2 | 2026-03-22 | 485.00 |
| 3 | 2026-03-28 | 710.00 |

---

## f) Funções numéricas

### f1 — ABS

**Pergunta:** De quanto a área de cada lavoura se afasta da média, para mais ou para menos?

```sql
SELECT CUL_CODE, CUL_VARIEDADE, CUL_AREA_HA,
       ROUND((SELECT AVG(CUL_AREA_HA) FROM CULTURA), 2) AS AREA_MEDIA,
       ROUND(ABS(CUL_AREA_HA - (SELECT AVG(CUL_AREA_HA) FROM CULTURA)), 2) AS DESVIO
FROM CULTURA
ORDER BY DESVIO DESC;
```

| CUL_CODE | CUL_VARIEDADE | CUL_AREA_HA | AREA_MEDIA | DESVIO |
|---|---|---|---|---|
| 4 | BMX Potencia RR | 22.00 | 8.46 | 13.55 |
| 1 | BMX Potencia RR | 18.50 | 8.46 | 10.05 |
| 10 | Virginia 1 | 1.40 | 8.46 | 7.06 |
| 7 | Roxo Caxiense | 1.80 | 8.46 | 6.66 |
| 8 | Carioca Precoce | 2.50 | 8.46 | 5.96 |
| 6 | Bola Precoce | 3.20 | 8.46 | 5.26 |
| 2 | TMG 7062 IPRO | 12.00 | 8.46 | 3.55 |
| 9 | IRGA 424 | 5.00 | 8.46 | 3.46 |
| 3 | AG 9025 PRO3 | 9.75 | 8.46 | 1.30 |
| 5 | AG 9025 PRO3 | 8.40 | 8.46 | 0.06 |

Sem a função, as lavouras abaixo da média apareceriam com valor negativo e a ordenação não faria sentido.

### f2 — ROUND

**Pergunta:** Qual a produtividade de cada lavoura em quilos por hectare?

```sql
SELECT C.CUL_CODE, P.PRO_NOME, C.CUL_AREA_HA,
       ROUND(SUM(CO.COL_QUANTIDADE * U.UNI_FATOR_KG) / C.CUL_AREA_HA, 2) AS KG_POR_HECTARE
FROM CULTURA C
JOIN PRODUTO P ON P.PRO_CODE = C.PRO_CODE
JOIN COLHEITA CO ON CO.CUL_CODE = C.CUL_CODE
JOIN UNIDADE_MEDIDA U ON U.UNI_CODE = CO.UNI_CODE
GROUP BY C.CUL_CODE, P.PRO_NOME, C.CUL_AREA_HA;
```

| CUL_CODE | PRO_NOME | CUL_AREA_HA | KG_POR_HECTARE |
|---|---|---|---|
| 1 | Soja | 18.50 | 3583.78 |
| 2 | Soja | 12.00 | 3550.00 |
| 3 | Milho | 9.75 | 4820.51 |
| 6 | Cebola | 3.20 | 2687.50 |
| 7 | Alho | 1.80 | 2333.33 |

### f3 — CEILING

**Pergunta:** Quantas viagens de caminhão com capacidade de mil quilos cada colheita exige?

```sql
SELECT CO.COL_CODE, P.PRO_NOME,
       CO.COL_QUANTIDADE * U.UNI_FATOR_KG AS TOTAL_KG,
       CEILING(CO.COL_QUANTIDADE * U.UNI_FATOR_KG / 1000) AS VIAGENS
FROM COLHEITA CO
JOIN UNIDADE_MEDIDA U ON U.UNI_CODE = CO.UNI_CODE
JOIN CULTURA C ON C.CUL_CODE = CO.CUL_CODE
JOIN PRODUTO P ON P.PRO_CODE = C.PRO_CODE;
```

| COL_CODE | PRO_NOME | TOTAL_KG | VIAGENS |
|---|---|---|---|
| 1 | Soja | 37200.00000 | 38 |
| 2 | Soja | 29100.00000 | 30 |
| 3 | Soja | 42600.00000 | 43 |
| 4 | Milho | 47000.00000 | 47 |
| 5 | Cebola | 8600.00000 | 9 |
| 6 | Alho | 4200.00000 | 5 |

O arredondamento para cima é o correto aqui: a sobra de 200 quilos da primeira colheita ainda exige uma viagem inteira.

### f4 — FLOOR

**Pergunta:** Quantas sacas inteiras de sessenta quilos cada colheita renderia?

```sql
SELECT CO.COL_CODE, P.PRO_NOME,
       CO.COL_QUANTIDADE * U.UNI_FATOR_KG AS TOTAL_KG,
       FLOOR(CO.COL_QUANTIDADE * U.UNI_FATOR_KG / 60) AS SACAS_DE_60
FROM COLHEITA CO
JOIN UNIDADE_MEDIDA U ON U.UNI_CODE = CO.UNI_CODE
JOIN CULTURA C ON C.CUL_CODE = CO.CUL_CODE
JOIN PRODUTO P ON P.PRO_CODE = C.PRO_CODE;
```

| COL_CODE | PRO_NOME | TOTAL_KG | SACAS_DE_60 |
|---|---|---|---|
| 1 | Soja | 37200.00000 | 620 |
| 2 | Soja | 29100.00000 | 485 |
| 3 | Soja | 42600.00000 | 710 |
| 4 | Milho | 47000.00000 | 783 |
| 5 | Cebola | 8600.00000 | 143 |
| 6 | Alho | 4200.00000 | 70 |

Aqui o arredondamento é para baixo, porque uma saca incompleta não conta como saca.

---

## g) Funções de string

### g1 — UPPER e LOWER

**Pergunta:** Como padronizar a exibição dos nomes dos clientes?

```sql
SELECT CLI_CODE, CLI_NOME,
       UPPER(CLI_NOME) AS EM_MAIUSCULA,
       LOWER(CLI_NOME) AS EM_MINUSCULA
FROM CLIENTE;
```

| CLI_CODE | CLI_NOME | EM_MAIUSCULA | EM_MINUSCULA |
|---|---|---|---|
| 1 | Cooperativa Agricola do Alto Vale | COOPERATIVA AGRICOLA DO ALTO VALE | cooperativa agricola do alto vale |
| 2 | Cerealista Vale Verde Ltda | CEREALISTA VALE VERDE LTDA | cerealista vale verde ltda |
| 3 | Antonio Pereira | ANTONIO PEREIRA | antonio pereira |
| 4 | Agroindustria Vale do Itajai Ltda | AGROINDUSTRIA VALE DO ITAJAI LTDA | agroindustria vale do itajai ltda |
| 5 | Marcia Fernandes Beduschi | MARCIA FERNANDES BEDUSCHI | marcia fernandes beduschi |

### g2 — SUBSTRING

**Pergunta:** Como exibir o CPF do produtor parcialmente mascarado, preservando a privacidade?

```sql
SELECT PRD_CPF, PRD_NOME,
       CONCAT(SUBSTRING(PRD_CPF,1,3), '.***.***-', SUBSTRING(PRD_CPF,10,2)) AS CPF_MASCARADO
FROM PRODUTOR;
```

| PRD_CPF | PRD_NOME | CPF_MASCARADO |
|---|---|---|
| 11122233355 | Pedro Henrique Schmitt | 111.***.***-55 |
| 12345678901 | Joao Carlos Bianchi | 123.***.***-01 |
| 22233344466 | Ana Lucia Hoffmann | 222.***.***-66 |
| 33344455577 | Valdir Kruger | 333.***.***-77 |
| 98765432100 | Maria Aparecida Souza | 987.***.***-00 |

### g3 — LENGTH

**Pergunta:** Qual o comprimento do nome de cada produto cadastrado?

```sql
SELECT PRO_CODE, PRO_NOME, LENGTH(PRO_NOME) AS TAMANHO
FROM PRODUTO
ORDER BY TAMANHO DESC;
```

| PRO_CODE | PRO_NOME | TAMANHO |
|---|---|---|
| 3 | Feijao | 6 |
| 6 | Cebola | 6 |
| 2 | Milho | 5 |
| 4 | Arroz | 5 |
| 1 | Soja | 4 |
| 5 | Alho | 4 |
| 7 | Fumo | 4 |

Observação: `LENGTH` conta bytes. Em textos com acentuação, um caractere acentuado ocupa mais de um byte em UTF-8, e a função que conta caracteres é `CHAR_LENGTH`. Como os dados do RuralManager foram cadastrados sem acentos, as duas devolvem o mesmo resultado.
