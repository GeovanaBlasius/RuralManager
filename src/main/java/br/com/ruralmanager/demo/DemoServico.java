package br.com.ruralmanager.demo;

import br.com.ruralmanager.comum.AcessoProibidoException;
import br.com.ruralmanager.comum.RegraDeNegocioException;
import br.com.ruralmanager.seguranca.ContextoUsuario;
import br.com.ruralmanager.usuario.PerfilUsuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carrega na conta do produtor logado os dados de exemplo do trabalho de Banco de Dados
 * (docs/academico/ruralmanager_completo.sql): 10 culturas, colheitas, gastos, saidas,
 * 5 faturas e 5 clientes, para a demonstracao ficar igual ao trabalho.
 *
 * Os cinco produtores do trabalho viram um so (a conta que esta carregando), entao as safras
 * de mesmo periodo se juntam em uma. Fica desligado por padrao (rm.demo.habilitado) e exige
 * confirmacao explicita. E idempotente: o cliente "Cooperativa Agricola do Alto Vale" serve de
 * marca de que a carga ja foi feita nesta conta.
 */
@Service
public class DemoServico {

    /** CNPJ do primeiro cliente de exemplo; presenca dele na conta = carga ja feita. */
    static final String MARCA = "11222333000181";

    public record Estado(boolean habilitado, boolean jaCarregado) {
    }

    public record Resultado(boolean carregou, String mensagem) {
    }

    private final JdbcClient jdbc;
    private final ContextoUsuario contexto;
    private final boolean habilitado;

    public DemoServico(JdbcClient jdbc, ContextoUsuario contexto,
                       @Value("${rm.demo.habilitado:false}") boolean habilitado) {
        this.jdbc = jdbc;
        this.contexto = contexto;
        this.habilitado = habilitado;
    }

    @Transactional(readOnly = true)
    public Estado estado() {
        Long pid = contexto.atual().produtorId();
        return new Estado(habilitado, habilitado && pid != null && jaCarregado(pid));
    }

    @Transactional
    public Resultado carregar(boolean confirmar) {
        if (!habilitado) {
            throw new AcessoProibidoException("A carga de dados de demonstracao esta desativada neste ambiente.");
        }
        contexto.exigirPerfil(PerfilUsuario.PRODUTOR);
        Long pid = contexto.produtorId();
        if (!confirmar) {
            throw new RegraDeNegocioException("Confirme que quer carregar os dados de demonstracao nesta conta.");
        }
        if (jaCarregado(pid)) {
            return new Resultado(false, "Os dados de demonstracao ja estao nesta conta. Nada foi alterado.");
        }

        Map<Integer, Long> clientes = new HashMap<>();
        clientes.put(1, cliente(pid, "Cooperativa Agricola do Alto Vale", MARCA, "(47) 3333-1000", "Rio do Sul - SC"));
        clientes.put(2, cliente(pid, "Cerealista Vale Verde Ltda", "11444777000161", "(47) 3333-2000", "Ituporanga - SC"));
        clientes.put(3, cliente(pid, "Antonio Pereira", "52998224725", "(47) 99933-4455", "Aurora - SC"));
        clientes.put(4, cliente(pid, "Agroindustria Vale do Itajai Ltda", "45997418000153", "(47) 3333-3000", "Rio do Sul - SC"));
        clientes.put(5, cliente(pid, "Marcia Fernandes Beduschi", "39053344705", "(47) 99977-8899", "Laurentino - SC"));

        long s2425 = safra(pid, 2024, 2025);
        long s2526 = safra(pid, 2025, 2026);
        long s2627 = safra(pid, 2026, 2027);

        Map<Integer, Long> cultura = new HashMap<>();
        cultura.put(1, cultura(s2526, "Soja", "BMX Potencia RR", "18.50", "2025-10-12"));
        cultura.put(2, cultura(s2526, "Soja", "TMG 7062 IPRO", "12.00", "2025-10-20"));
        cultura.put(3, cultura(s2526, "Milho", "AG 9025 PRO3", "9.75", "2025-09-28"));
        cultura.put(4, cultura(s2627, "Soja", "BMX Potencia RR", "22.00", "2026-09-18"));
        cultura.put(5, cultura(s2627, "Milho", "AG 9025 PRO3", "8.40", "2026-09-22"));
        cultura.put(6, cultura(s2526, "Cebola", "Bola Precoce", "3.20", "2025-05-15"));
        cultura.put(7, cultura(s2526, "Alho", "Roxo Caxiense", "1.80", "2025-04-10"));
        cultura.put(8, cultura(s2526, "Feijao", "Carioca Precoce", "2.50", "2025-09-10"));
        cultura.put(9, cultura(s2526, "Arroz", "IRGA 424", "5.00", "2025-10-05"));
        cultura.put(10, cultura(s2425, "Fumo", "Virginia 1", "1.40", "2024-10-15"));

        Map<Integer, Long> colheita = new HashMap<>();
        colheita.put(1, colheita(cultura.get(1), "sc60", "620.00", "2026-03-14"));
        colheita.put(2, colheita(cultura.get(1), "sc60", "485.00", "2026-03-22"));
        colheita.put(3, colheita(cultura.get(2), "sc60", "710.00", "2026-03-28"));
        colheita.put(4, colheita(cultura.get(3), "sc50", "940.00", "2026-02-18"));
        colheita.put(5, colheita(cultura.get(6), "kg", "8600.00", "2025-09-12"));
        colheita.put(6, colheita(cultura.get(7), "cx20", "210.00", "2025-08-25"));

        gasto(s2526, cultura.get(1), null, "Sementes", "Compra de sementes de soja", "4500.00", "2025-10-05");
        gasto(s2526, cultura.get(1), null, "Fertilizantes", "Compra de fertilizantes para soja", "6800.00", "2025-10-10");
        gasto(s2526, cultura.get(2), null, "Defensivos", "Compra de defensivos agricolas", "2300.00", "2025-10-18");
        gasto(s2526, cultura.get(3), null, "Sementes", "Compra de sementes de milho", "1800.00", "2025-09-20");
        gasto(s2526, null, null, "Combustivel", "Combustivel para maquinas agricolas", "1200.00", "2025-11-10");
        gasto(s2526, null, null, "Mao de obra", "Pagamento de mao de obra", "2500.00", "2025-12-05");
        gasto(s2526, null, null, "Energia eletrica", "Energia eletrica da propriedade", "450.00", "2026-01-10");
        gasto(s2526, null, null, "Transporte", "Transporte de insumos agricolas", "700.00", "2025-10-15");
        gasto(s2526, cultura.get(1), colheita.get(1), "Mao de obra", "Mao de obra na primeira colheita de soja", "950.00", "2026-03-14");
        gasto(s2526, cultura.get(3), colheita.get(4), "Combustivel", "Combustivel utilizado na colheita do milho", "680.00", "2026-02-18");
        gasto(s2627, cultura.get(4), null, "Fertilizantes", "Fertilizantes para a nova safra de soja", "7200.00", "2026-09-20");
        gasto(s2627, null, null, "Arrendamento", "Arrendamento de area agricola", "3000.00", "2026-09-25");
        gasto(s2526, cultura.get(6), null, "Defensivos", "Defensivos para a cultura de cebola", "850.00", "2025-06-10");

        saida(colheita.get(6), "Semente guardada", "40.00", "2025-08-26", "Alho reservado para o plantio seguinte");
        saida(colheita.get(6), "Consumo proprio", "15.00", "2025-09-05", "Consumo da familia");
        saida(colheita.get(4), "Alimentacao animal", "120.00", "2026-02-20", "Milho para o trato dos animais");
        saida(colheita.get(4), "Perda apos a colheita", "35.00", "2026-04-10", "Grao perdido por umidade no paiol");
        saida(colheita.get(5), "Consumo proprio", "200.00", "2025-09-15", "Cebola para consumo da familia");

        long f1 = fatura(clientes.get(1), "2026-03-20");
        long f2 = fatura(clientes.get(2), "2026-04-15");
        long f3 = fatura(clientes.get(1), "2026-04-02");
        long f4 = fatura(clientes.get(3), "2025-09-20");
        long f5 = fatura(clientes.get(4), "2025-09-25");
        item(f1, colheita.get(1), "400.00", "128.50");
        item(f2, colheita.get(1), "220.00", "134.00");
        item(f2, colheita.get(2), "485.00", "134.00");
        item(f3, colheita.get(3), "710.00", "130.75");
        item(f3, colheita.get(4), "785.00", "74.20");
        item(f4, colheita.get(5), "8400.00", "2.35");
        item(f5, colheita.get(6), "100.00", "12.00");

        return new Resultado(true, "Dados de demonstracao carregados: 3 safras, 10 culturas, 6 colheitas, "
                + "13 gastos, 5 saidas, 5 clientes e 5 vendas.");
    }

    // ---------- apoio ----------

    private boolean jaCarregado(Long pid) {
        return jdbc.sql("select count(*) from cliente where produtor_id = :p and cpf_cnpj = :d")
                .param("p", pid).param("d", MARCA).query(Long.class).single() > 0;
    }

    private long cliente(Long pid, String nome, String documento, String telefone, String endereco) {
        Optional<Long> existente = jdbc.sql("select id from cliente where produtor_id = :p and cpf_cnpj = :d")
                .param("p", pid).param("d", documento).query(Long.class).optional();
        if (existente.isPresent()) {
            return existente.get();
        }
        return jdbc.sql("insert into cliente (produtor_id, nome, cpf_cnpj, telefone, endereco, created_at, updated_at) "
                        + "values (:p, :n, :d, :t, :e, now(), now()) returning id")
                .param("p", pid).param("n", nome).param("d", documento).param("t", telefone).param("e", endereco)
                .query(Long.class).single();
    }

    private long safra(Long pid, int inicio, int fim) {
        Optional<Long> existente = jdbc.sql("select id from safra where produtor_id = :p and ano_inicio = :i and ano_fim = :f")
                .param("p", pid).param("i", inicio).param("f", fim).query(Long.class).optional();
        if (existente.isPresent()) {
            return existente.get();
        }
        return jdbc.sql("insert into safra (produtor_id, ano_inicio, ano_fim, created_at, updated_at) "
                        + "values (:p, :i, :f, now(), now()) returning id")
                .param("p", pid).param("i", inicio).param("f", fim).query(Long.class).single();
    }

    private long cultura(long safraId, String produto, String variedade, String area, String plantio) {
        long produtoId = catalogo("produto", produto);
        Optional<Long> existente = jdbc.sql("select id from cultura where safra_id = :s and produto_id = :p and variedade = :v")
                .param("s", safraId).param("p", produtoId).param("v", variedade).query(Long.class).optional();
        if (existente.isPresent()) {
            return existente.get();
        }
        return jdbc.sql("insert into cultura (safra_id, produto_id, variedade, area_ha, data_plantio, created_at, updated_at) "
                        + "values (:s, :p, :v, :a, :d, now(), now()) returning id")
                .param("s", safraId).param("p", produtoId).param("v", variedade)
                .param("a", new BigDecimal(area)).param("d", LocalDate.parse(plantio)).query(Long.class).single();
    }

    private long colheita(long culturaId, String siglaUnidade, String quantidade, String data) {
        long unidadeId = jdbc.sql("select id from unidade_medida where sigla = :s").param("s", siglaUnidade)
                .query(Long.class).optional()
                .orElseThrow(() -> new RegraDeNegocioException("Unidade de medida " + siglaUnidade + " nao cadastrada."));
        return jdbc.sql("insert into colheita (cultura_id, unidade_medida_id, quantidade, data_colheita, created_at, updated_at) "
                        + "values (:c, :u, :q, :d, now(), now()) returning id")
                .param("c", culturaId).param("u", unidadeId).param("q", new BigDecimal(quantidade))
                .param("d", LocalDate.parse(data)).query(Long.class).single();
    }

    private void gasto(long safraId, Long culturaId, Long colheitaId, String categoria, String descricao, String valor, String data) {
        long categoriaId = catalogo("categoria_gasto", categoria);
        jdbc.sql("insert into gasto (safra_id, cultura_id, colheita_id, categoria_gasto_id, descricao, valor, data_gasto, created_at, updated_at) "
                        + "values (:s, :c, :h, :k, :d, :v, :t, now(), now())")
                .param("s", safraId).param("c", culturaId).param("h", colheitaId).param("k", categoriaId)
                .param("d", descricao).param("v", new BigDecimal(valor)).param("t", LocalDate.parse(data)).update();
    }

    private void saida(long colheitaId, String destino, String quantidade, String data, String observacao) {
        long destinoId = catalogo("destino", destino);
        jdbc.sql("insert into saida (colheita_id, destino_id, quantidade, data_saida, observacao, created_at, updated_at) "
                        + "values (:c, :d, :q, :t, :o, now(), now())")
                .param("c", colheitaId).param("d", destinoId).param("q", new BigDecimal(quantidade))
                .param("t", LocalDate.parse(data)).param("o", observacao).update();
    }

    private long fatura(long clienteId, String data) {
        return jdbc.sql("insert into fatura (cliente_id, data_compra, created_at, updated_at) values (:c, :d, now(), now()) returning id")
                .param("c", clienteId).param("d", LocalDate.parse(data)).query(Long.class).single();
    }

    private void item(long faturaId, long colheitaId, String quantidade, String preco) {
        jdbc.sql("insert into item_fatura (fatura_id, colheita_id, quantidade, preco_unitario, created_at, updated_at) "
                        + "values (:f, :c, :q, :p, now(), now())")
                .param("f", faturaId).param("c", colheitaId).param("q", new BigDecimal(quantidade))
                .param("p", new BigDecimal(preco)).update();
    }

    /** Catalogo compartilhado (produto, categoria_gasto, destino): reusa pelo nome ou cria se faltar. */
    private long catalogo(String tabela, String nome) {
        Optional<Long> existente = jdbc.sql("select id from " + tabela + " where lower(nome) = lower(:n)")
                .param("n", nome).query(Long.class).optional();
        if (existente.isPresent()) {
            return existente.get();
        }
        return jdbc.sql("insert into " + tabela + " (nome, created_at, updated_at) values (:n, now(), now()) returning id")
                .param("n", nome).query(Long.class).single();
    }
}
