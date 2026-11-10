package database;

import java.sql.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Scanner;

public class ConnectionFactory {
    private static final String PROPERTIES_FILE = "config.properties";
    private static final String BACKUP_DIR = "backups/";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            criarDiretorios();
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL não encontrado!");
            System.exit(1);
        }
    }

    private static void criarDiretorios() {
        new File(BACKUP_DIR).mkdirs();
        new File("logs").mkdirs();
    }

    public static Connection getConnection() throws SQLException {
        try {
            Properties props = loadProperties();
            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            Connection conn = DriverManager.getConnection(url, user, password);
            return conn;
        } catch (IOException e) {
            throw new SQLException("Erro ao carregar configurações: " + e.getMessage(), e);
        }
    }

    public static void testConnection() {
        System.out.println("\n🔧 Testando conexão com o banco de dados...");

        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1")) {

                    if (rs.next()) {
                        System.out.println("✅ Conexão estabelecida com sucesso!");
                        System.out.println("📊 Banco: " + conn.getMetaData().getDatabaseProductName());
                        System.out.println("📈 Versão: " + conn.getMetaData().getDatabaseProductVersion());
                        System.out.println("🔗 URL: " + conn.getMetaData().getURL());

                        testarTabelas(conn);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Falha na conexão: " + e.getMessage());
        }
    }

    private static void testarTabelas(Connection conn) {
        String[] tabelas = {"produtor", "safra", "cultura", "gasto", "colheita"};
        int tabelasOk = 0;

        try (Statement stmt = conn.createStatement()) {
            for (String tabela : tabelas) {
                try {
                    stmt.execute("SELECT 1 FROM " + tabela + " LIMIT 1");
                    tabelasOk++;
                    System.out.println("   ✅ Tabela '" + tabela + "' existe");
                } catch (SQLException e) {
                    System.out.println("   ⚠️  Tabela '" + tabela + "' não existe ou está vazia");
                }
            }

            if (tabelasOk == tabelas.length) {
                System.out.println("\n🎉 Todas as tabelas estão prontas para uso!");
            } else {
                System.out.println("\n⚠️  Execute 'criarBancoSeNaoExistir()' para criar as tabelas faltantes");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao testar tabelas: " + e.getMessage());
        }
    }

    public static void criarBancoSeNaoExistir() {
        System.out.println("\n🔧 Iniciando configuração do banco de dados...");

        try {
            Properties props = loadProperties();
            String dbName = props.getProperty("db.name", "ruralmanager");
            String baseUrl = props.getProperty("db.baseurl", "jdbc:mysql://localhost:3306/");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            try (Connection conn = DriverManager.getConnection(baseUrl, user, password);
                 Statement stmt = conn.createStatement()) {

                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName +
                        " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                System.out.println("✅ Banco de dados '" + dbName + "' verificado/criado");

                stmt.executeUpdate("USE " + dbName);

                criarTabelasCompatíveis(stmt);

                criarViews(stmt);

                System.out.println("\n🎉 Banco de dados configurado com sucesso!");
                System.out.println("📊 Banco: " + dbName);
                System.out.println("📋 Tabelas: 5 principais");
                System.out.println("👁️  Views: 2 de análise");

            }

        } catch (SQLException | IOException e) {
            System.err.println("❌ Erro ao configurar banco: " + e.getMessage());
        }
    }

    public static void inserirDadosExemplo() {
        System.out.println("\n📊 Inserindo dados de exemplo...");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("👤 Inserindo produtores...");

            String[] inserts = {
                    "INSERT IGNORE INTO produtor (nome, cpf, telefone, endereco) VALUES ('João da Silva', '12345678901', '11999999999', 'Fazenda São João, km 12 - São Paulo/SP')",
                    "INSERT IGNORE INTO produtor (nome, cpf, telefone, endereco) VALUES ('Maria Oliveira', '98765432109', '21988888888', 'Sítio Flores, Estrada da Serra - Rio de Janeiro/RJ')",
                    "INSERT IGNORE INTO produtor (nome, cpf, telefone, endereco) VALUES ('Carlos Pereira', '45612378902', '31977777777', 'Chácara Primavera, Zona Rural - Belo Horizonte/MG')",
                    "INSERT IGNORE INTO produtor (nome, cpf, telefone, endereco) VALUES ('Ana Santos', '32165498703', '41966666666', 'Estância Verde, Rodovia PR-445 - Curitiba/PR')",
                    "INSERT IGNORE INTO produtor (nome, cpf, telefone, endereco) VALUES ('Pedro Costa', '65498732104', '51955555555', 'Rancho do Vale, BR-116 - Porto Alegre/RS')",

                    "INSERT IGNORE INTO safra (produtor_id, ano, descricao) VALUES (1, 2023, 'Safra de verão 2023 - Principal plantio de soja')",
                    "INSERT IGNORE INTO safra (produtor_id, ano, descricao) VALUES (1, 2024, 'Safra 2024 - Rotação soja/milho')",
                    "INSERT IGNORE INTO safra (produtor_id, ano, descricao) VALUES (2, 2023, 'Safra de café arábica')",
                    "INSERT IGNORE INTO safra (produtor_id, ano, descricao) VALUES (2, 2024, 'Safra experimental de café especial')",
                    "INSERT IGNORE INTO safra (produtor_id, ano, descricao) VALUES (3, 2023, 'Plantio direto de milho')",

                    "INSERT IGNORE INTO cultura (safra_id, nome, variedade, area_plantada) VALUES (1, 'Soja', 'BMX Potência RR', 50.00)",
                    "INSERT IGNORE INTO cultura (safra_id, nome, variedade, area_plantada) VALUES (1, 'Milho', 'AG 1051', 30.00)",
                    "INSERT IGNORE INTO cultura (safra_id, nome, variedade, area_plantada) VALUES (3, 'Café', 'Arábica Catuaí', 15.00)",
                    "INSERT IGNORE INTO cultura (safra_id, nome, variedade, area_plantada) VALUES (4, 'Café Especial', 'Arábica Bourbon', 5.00)",
                    "INSERT IGNORE INTO cultura (safra_id, nome, variedade, area_plantada) VALUES (5, 'Milho', 'Pioneer 30F53', 80.00)",

                    "INSERT IGNORE INTO gasto (cultura_id, descricao, categoria, valor, data) VALUES (1, 'Sementes Soja Potência RR', 'Sementes', 12500.00, '2023-10-15')",
                    "INSERT IGNORE INTO gasto (cultura_id, descricao, categoria, valor, data) VALUES (1, 'Adubo NPK 08-28-16', 'Fertilizantes', 18000.00, '2023-10-20')",
                    "INSERT IGNORE INTO gasto (cultura_id, descricao, categoria, valor, data) VALUES (3, 'Adubação café', 'Fertilizantes', 8500.00, '2023-03-10')",
                    "INSERT IGNORE INTO gasto (cultura_id, descricao, categoria, valor, data) VALUES (3, 'Colheita manual café', 'Mão de obra', 12500.00, '2023-06-15')",

                    "INSERT IGNORE INTO colheita (cultura_id, quantidade, preco_venda, vendida, data_colheita) VALUES (1, 3750.00, 160.00, TRUE, '2024-03-15')",
                    "INSERT IGNORE INTO colheita (cultura_id, quantidade, preco_venda, vendida, data_colheita) VALUES (1, 1250.00, 165.00, TRUE, '2024-03-20')",
                    "INSERT IGNORE INTO colheita (cultura_id, quantidade, preco_venda, vendida, data_colheita) VALUES (3, 4500.00, 1200.00, TRUE, '2023-07-20')",
                    "INSERT IGNORE INTO colheita (cultura_id, quantidade, preco_venda, vendida, data_colheita) VALUES (3, 600.00, 1800.00, TRUE, '2023-07-25')",
                    "INSERT IGNORE INTO colheita (cultura_id, quantidade, preco_venda, vendida, data_colheita) VALUES (5, 9600.00, 85.00, TRUE, '2024-06-10')"
            };

            int sucessos = 0;
            for (String sql : inserts) {
                try {
                    stmt.executeUpdate(sql);
                    sucessos++;
                } catch (SQLException e) {
                    System.out.println("⚠️  Erro ao inserir: " + e.getMessage());
                }
            }

            System.out.println("✅ Dados de exemplo inseridos: " + sucessos + " registros");
            System.out.println("\n📊 RESUMO:");
            System.out.println("• 5 produtores");
            System.out.println("• 5 safras");
            System.out.println("• 5 culturas");
            System.out.println("• 4 gastos");
            System.out.println("• 5 colheitas");
            System.out.println("\n💡 Agora você pode testar listagens e relatórios!");

        } catch (SQLException e) {
            System.err.println("❌ Erro ao inserir dados: " + e.getMessage());
        }
    }

    private static void criarTabelasCompatíveis(Statement stmt) throws SQLException {
        System.out.println("\n📋 Criando tabelas...");

        String sqlProdutor = """
            CREATE TABLE IF NOT EXISTS produtor (
                id INT AUTO_INCREMENT PRIMARY KEY,
                nome VARCHAR(100) NOT NULL,
                cpf VARCHAR(14) UNIQUE NOT NULL,
                telefone VARCHAR(15) NOT NULL,
                endereco VARCHAR(200),
                data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_cpf (cpf)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        stmt.executeUpdate(sqlProdutor);
        System.out.println("   ✅ Tabela 'produtor'");

        String sqlSafra = """
            CREATE TABLE IF NOT EXISTS safra (
                id INT AUTO_INCREMENT PRIMARY KEY,
                produtor_id INT NOT NULL,
                ano INT NOT NULL,
                descricao TEXT,
                data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY uk_produtor_ano (produtor_id, ano),
                FOREIGN KEY (produtor_id) REFERENCES produtor(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        stmt.executeUpdate(sqlSafra);
        System.out.println("   ✅ Tabela 'safra'");

        String sqlCultura = """
            CREATE TABLE IF NOT EXISTS cultura (
                id INT AUTO_INCREMENT PRIMARY KEY,
                safra_id INT NOT NULL,
                nome VARCHAR(100) NOT NULL,
                variedade VARCHAR(100) NOT NULL,
                area_plantada DECIMAL(10,2) NOT NULL,
                data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY uk_safra_nome (safra_id, nome),
                FOREIGN KEY (safra_id) REFERENCES safra(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        stmt.executeUpdate(sqlCultura);
        System.out.println("   ✅ Tabela 'cultura'");

        String sqlGasto = """
            CREATE TABLE IF NOT EXISTS gasto (
                id INT AUTO_INCREMENT PRIMARY KEY,
                cultura_id INT NOT NULL,
                descricao VARCHAR(100) NOT NULL,
                categoria VARCHAR(50) NOT NULL,
                valor DECIMAL(10,2) NOT NULL,
                data DATE NOT NULL,
                data_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (cultura_id) REFERENCES cultura(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        stmt.executeUpdate(sqlGasto);
        System.out.println("   ✅ Tabela 'gasto'");

        String sqlColheita = """
            CREATE TABLE IF NOT EXISTS colheita (
                id INT AUTO_INCREMENT PRIMARY KEY,
                cultura_id INT NOT NULL,
                quantidade DECIMAL(10,2) NOT NULL,
                preco_venda DECIMAL(10,2) DEFAULT 0,
                vendida BOOLEAN DEFAULT FALSE,
                data_colheita DATE NOT NULL,
                data_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (cultura_id) REFERENCES cultura(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        stmt.executeUpdate(sqlColheita);
        System.out.println("   ✅ Tabela 'colheita'");
    }

    public static void resetarBanco() {
        System.out.println("\n⚠️ ⚠️ ⚠️  ATENÇÃO: Esta ação irá APAGAR TODOS os dados! ⚠️ ⚠️ ⚠️");
        System.out.println("Todos os registros serão PERDIDOS permanentemente!");
        System.out.print("\nDigite 'RESETAR' para confirmar: ");

        Scanner scanner = new Scanner(System.in);
        String confirmacao = scanner.nextLine().trim();

        if (!"RESETAR".equals(confirmacao)) {
            System.out.println("❌ Operação cancelada.");
            return;
        }

        System.out.println("\n🗑️  Iniciando reset do banco de dados...");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            String[] tabelas = {
                    "colheita",
                    "gasto",
                    "cultura",
                    "safra",
                    "produtor"
            };

            int tabelasExcluidas = 0;
            for (String tabela : tabelas) {
                try {
                    stmt.execute("DROP TABLE IF EXISTS " + tabela);
                    System.out.println("   ✅ Tabela excluída: " + tabela);
                    tabelasExcluidas++;
                } catch (SQLException e) {
                    System.err.println("   ⚠️  Não foi possível excluir tabela " + tabela + ": " + e.getMessage());
                }
            }

            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            System.out.println("\n✅ Banco resetado com sucesso!");
            System.out.println("🗑️  Tabelas excluídas: " + tabelasExcluidas + " de " + tabelas.length);
            System.out.println("\n💡 Execute 'criarBancoSeNaoExistir()' para recriar a estrutura.");

        } catch (SQLException e) {
            System.err.println("❌ Erro ao resetar banco: " + e.getMessage());
        }
    }

    public static void realizarBackup() {
        try {
            Properties props = loadProperties();
            String dbName = props.getProperty("db.name", "ruralmanager");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = LocalDateTime.now().format(dtf);
            String backupFile = BACKUP_DIR + "backup_" + dbName + "_" + timestamp + ".sql";

            System.out.println("\n💾 Iniciando backup do banco: " + dbName);

            try {
                String[] command = {
                        "mysqldump",
                        "-u", user,
                        "-p" + password,
                        "--databases", dbName,
                        "--routines",
                        "--events",
                        "--single-transaction"
                };

                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectOutput(new File(backupFile));
                Process process = pb.start();

                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorLine;
                StringBuilder errors = new StringBuilder();
                while ((errorLine = errorReader.readLine()) != null) {
                    errors.append(errorLine).append("\n");
                }

                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    File file = new File(backupFile);
                    long sizeKB = file.length() / 1024;

                    System.out.println("✅ Backup realizado com sucesso!");
                    System.out.println("📁 Arquivo: " + backupFile);
                    System.out.println("📏 Tamanho: " + sizeKB + " KB");

                    LoggerUtil.logInfo("Backup realizado: " + backupFile + " (" + sizeKB + " KB)");
                } else {
                    System.out.println("❌ Falha no backup (código: " + exitCode + ")");
                    System.out.println("⚠️  Dica: Certifique-se de que o mysqldump está instalado");
                    System.out.println("Erro: " + errors.toString());
                    LoggerUtil.logErro("Falha no backup: " + errors.toString());
                }

            } catch (IOException e) {
                System.out.println("❌ Erro ao executar mysqldump: " + e.getMessage());
                System.out.println("⚠️  O backup será feito via SQL nativo...");
                realizarBackupSQL(dbName, backupFile);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Erro ao realizar backup: " + e.getMessage());
            LoggerUtil.logErro("Erro no backup: " + e.getMessage());
        }
    }

    private static void realizarBackupSQL(String dbName, String backupFile) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             PrintWriter writer = new PrintWriter(new FileWriter(backupFile))) {

            writer.println("-- Backup do banco de dados: " + dbName);
            writer.println("-- Data: " + LocalDateTime.now());
            writer.println("-- Gerado por RuralManager");
            writer.println();

            String[] tabelas = {"produtor", "safra", "cultura", "gasto", "colheita"};

            for (String tabela : tabelas) {
                writer.println("\n-- Backup da tabela: " + tabela);
                writer.println("DROP TABLE IF EXISTS " + tabela + ";");

                try (ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + tabela)) {
                    if (rs.next()) {
                        String createSQL = rs.getString(2);
                        writer.println(createSQL + ";");
                    }
                }

                try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + tabela)) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();

                    while (rs.next()) {
                        writer.print("INSERT INTO " + tabela + " VALUES (");
                        for (int i = 1; i <= columnCount; i++) {
                            if (i > 1) writer.print(", ");
                            Object value = rs.getObject(i);

                            if (value == null) {
                                writer.print("NULL");
                            } else if (value instanceof String || value instanceof java.sql.Date || value instanceof java.sql.Timestamp) {
                                writer.print("'" + value.toString().replace("'", "''") + "'");
                            } else {
                                writer.print(value);
                            }
                        }
                        writer.println(");");
                    }
                }
            }

            writer.println("\n-- Fim do backup");
            writer.flush();

            File file = new File(backupFile);
            long sizeKB = file.length() / 1024;

            System.out.println("✅ Backup via SQL realizado com sucesso!");
            System.out.println("📁 Arquivo: " + backupFile);
            System.out.println("📏 Tamanho: " + sizeKB + " KB");
            LoggerUtil.logInfo("Backup SQL realizado: " + backupFile + " (" + sizeKB + " KB)");

        } catch (SQLException | IOException e) {
            System.err.println("❌ Erro no backup SQL: " + e.getMessage());
            LoggerUtil.logErro("Erro no backup SQL: " + e.getMessage());
        }
    }

    private static void criarViews(Statement stmt) throws SQLException {
        System.out.println("\n👁️  Criando views de análise...");

        try {
            String viewResumo = """
                CREATE OR REPLACE VIEW vw_resumo_produtor AS
                SELECT 
                    p.id,
                    p.nome,
                    p.cpf,
                    COUNT(DISTINCT s.id) as total_safras,
                    COUNT(DISTINCT c.id) as total_culturas,
                    COALESCE(SUM(g.valor), 0) as total_gastos,
                    COALESCE(SUM(col.quantidade), 0) as total_colhido,
                    COALESCE(SUM(CASE WHEN col.vendida THEN col.quantidade * col.preco_venda ELSE 0 END), 0) as receita_total
                FROM produtor p
                LEFT JOIN safra s ON p.id = s.produtor_id
                LEFT JOIN cultura c ON s.id = c.safra_id
                LEFT JOIN gasto g ON c.id = g.cultura_id
                LEFT JOIN colheita col ON c.id = col.cultura_id
                GROUP BY p.id, p.nome, p.cpf
                """;
            stmt.executeUpdate(viewResumo);
            System.out.println("   ✅ View 'vw_resumo_produtor' criada");
        } catch (SQLException e) {
            System.out.println("   ⚠️  Não foi possível criar view 'vw_resumo_produtor': " + e.getMessage());
        }

        try {
            String viewAnalise = """
                CREATE OR REPLACE VIEW vw_analise_cultura AS
                SELECT 
                    c.id,
                    p.nome as produtor,
                    s.ano,
                    c.nome as cultura,
                    c.variedade,
                    c.area_plantada,
                    COALESCE(SUM(g.valor), 0) as total_gastos,
                    COALESCE(SUM(col.quantidade), 0) as total_colhido,
                    COALESCE(SUM(CASE WHEN col.vendida THEN col.quantidade * col.preco_venda ELSE 0 END), 0) as receita,
                    CASE 
                        WHEN c.area_plantada > 0 THEN COALESCE(SUM(col.quantidade), 0) / c.area_plantada 
                        ELSE 0 
                    END as produtividade_kg_ha,
                    COALESCE(SUM(CASE WHEN col.vendida THEN col.quantidade * col.preco_venda ELSE 0 END), 0) - COALESCE(SUM(g.valor), 0) as resultado_financeiro
                FROM cultura c
                JOIN safra s ON c.safra_id = s.id
                JOIN produtor p ON s.produtor_id = p.id
                LEFT JOIN gasto g ON c.id = g.cultura_id
                LEFT JOIN colheita col ON c.id = col.cultura_id
                GROUP BY c.id, p.nome, s.ano, c.nome, c.variedade, c.area_plantada
                """;
            stmt.executeUpdate(viewAnalise);
            System.out.println("   ✅ View 'vw_analise_cultura' criada");
        } catch (SQLException e) {
            System.out.println("   ⚠️  Não foi possível criar view 'vw_analise_cultura': " + e.getMessage());
        }
    }

    private static Properties loadProperties() throws IOException {
        File configFile = new File(PROPERTIES_FILE);

        if (!configFile.exists()) {
            criarConfigPadrao();
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
        }
        return props;
    }

    private static void criarConfigPadrao() {
        try (FileOutputStream fos = new FileOutputStream(PROPERTIES_FILE)) {
            String config = """
                # Configurações do Banco de Dados
                db.url=jdbc:mysql://localhost:3306/ruralmanager?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
                db.baseurl=jdbc:mysql://localhost:3306/
                db.user=root
                db.password=30112005
                db.name=ruralmanager
                
                # Configurações da Aplicação
                app.name=Rural Manager
                app.version=1.0.0
                """;
            fos.write(config.getBytes());
            System.out.println("📄 Arquivo de configuração criado: " + PROPERTIES_FILE);
        } catch (IOException e) {
            System.err.println("❌ Erro ao criar arquivo de configuração: " + e.getMessage());
        }
    }
}