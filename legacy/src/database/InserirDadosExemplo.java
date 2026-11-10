package database;

import java.sql.*;

public class InserirDadosExemplo {

    public static void main(String[] args) {
        System.out.println("📊 Inserindo dados de exemplo no banco...");

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            System.out.println("🗑️  Limpando dados existentes...");
            stmt.execute("DELETE FROM colheita");
            stmt.execute("DELETE FROM gasto");
            stmt.execute("DELETE FROM cultura");
            stmt.execute("DELETE FROM safra");
            stmt.execute("DELETE FROM produtor");

            System.out.println("👤 Inserindo produtores...");
            String[] produtores = {
                    "INSERT INTO produtor (nome, cpf, telefone) VALUES ('João da Silva', '12345678901', '11999999999')",
                    "INSERT INTO produtor (nome, cpf, telefone) VALUES ('Maria Oliveira', '98765432109', '21988888888')",
                    "INSERT INTO produtor (nome, cpf, telefone) VALUES ('Carlos Pereira', '45612378902', '31977777777')",
                    "INSERT INTO produtor (nome, cpf, telefone) VALUES ('Ana Santos', '32165498703', '41966666666')",
                    "INSERT INTO produtor (nome, cpf, telefone) VALUES ('Pedro Costa', '65498732104', '51955555555')"
            };

            for (String sql : produtores) {
                stmt.executeUpdate(sql);
            }

            System.out.println("📅 Inserindo safras...");
            String[] safras = {
                    "INSERT INTO safra (produtor_id, ano, descricao) VALUES (1, 2023, 'Safra de verão 2023 - Soja e Milho')",
                    "INSERT INTO safra (produtor_id, ano, descricao) VALUES (1, 2024, 'Safra 2024 - Rotação de culturas')",
                    "INSERT INTO safra (produtor_id, ano, descricao) VALUES (2, 2023, 'Café arábica - Safra principal')",
                    "INSERT INTO safra (produtor_id, ano, descricao) VALUES (3, 2023, 'Milho - Plantio direto')"
            };

            for (String sql : safras) {
                stmt.executeUpdate(sql);
            }

            System.out.println("🌱 Inserindo culturas...");
            String[] culturas = {
                    "INSERT INTO cultura (safra_id, nome, variedade, area_plantada) VALUES (1, 'Soja', 'BMX Potência RR', 50.00)",
                    "INSERT INTO cultura (safra_id, nome, variedade, area_plantada) VALUES (1, 'Milho', 'AG 1051', 30.00)",
                    "INSERT INTO cultura (safra_id, nome, variedade, area_plantada) VALUES (2, 'Soja', 'BMX Lança', 45.00)",
                    "INSERT INTO cultura (safra_id, nome, variedade, area_plantada) VALUES (2, 'Feijão', 'BRS Estilo', 15.00)",
                    "INSERT INTO cultura (safra_id, nome, variedade, area_plantada) VALUES (3, 'Café', 'Arábica Catuaí', 20.00)",
                    "INSERT INTO cultura (safra_id, nome, variedade, area_plantada) VALUES (4, 'Milho', 'Pioneer 30F53', 80.00)"
            };

            for (String sql : culturas) {
                stmt.executeUpdate(sql);
            }

            System.out.println("💰 Inserindo gastos...");
            String[] gastos = {
                    "INSERT INTO gasto (cultura_id, descricao, categoria, valor, data) VALUES (1, 'Sementes Soja Potência RR', 'Sementes', 12500.00, '2023-10-15')",
                    "INSERT INTO gasto (cultura_id, descricao, categoria, valor, data) VALUES (1, 'Adubo NPK 08-28-16', 'Fertilizantes', 18000.00, '2023-10-20')",
                    "INSERT INTO gasto (cultura_id, descricao, categoria, valor, data) VALUES (1, 'Herbicida Glifosato', 'Defensivos', 8500.00, '2023-10-25')",
                    "INSERT INTO gasto (cultura_id, descricao, categoria, valor, data) VALUES (2, 'Sementes Milho AG 1051', 'Sementes', 9000.00, '2023-11-05')",
                    "INSERT INTO gasto (cultura_id, descricao, categoria, valor, data) VALUES (2, 'Adubação de cobertura', 'Fertilizantes', 12000.00, '2023-11-20')"
            };

            for (String sql : gastos) {
                stmt.executeUpdate(sql);
            }

            System.out.println("🌾 Inserindo colheitas...");
            String[] colheitas = {
                    "INSERT INTO colheita (cultura_id, quantidade, preco_venda, vendida, data_colheita) VALUES (1, 3750.00, 160.00, TRUE, '2024-03-15')",
                    "INSERT INTO colheita (cultura_id, quantidade, preco_venda, vendida, data_colheita) VALUES (1, 1250.00, 165.00, TRUE, '2024-03-20')",
                    "INSERT INTO colheita (cultura_id, quantidade, preco_venda, vendida, data_colheita) VALUES (2, 2400.00, 85.00, TRUE, '2024-06-10')",
                    "INSERT INTO colheita (cultura_id, quantidade, preco_venda, vendida, data_colheita) VALUES (2, 600.00, 82.00, TRUE, '2024-06-15')",
                    "INSERT INTO colheita (cultura_id, quantidade, preco_venda, vendida, data_colheita) VALUES (3, 1800.00, 170.00, TRUE, '2024-03-25')",
                    "INSERT INTO colheita (cultura_id, quantidade, preco_venda, vendida, data_colheita) VALUES (5, 1500.00, 1200.00, TRUE, '2023-07-20')"
            };

            for (String sql : colheitas) {
                stmt.executeUpdate(sql);
            }

            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            System.out.println("✅ Dados de exemplo inseridos com sucesso!");
            System.out.println("\n📊 RESUMO:");
            System.out.println("• 5 produtores");
            System.out.println("• 4 safras");
            System.out.println("• 6 culturas");
            System.out.println("• 5 gastos");
            System.out.println("• 6 colheitas");

        } catch (SQLException e) {
            System.err.println("❌ Erro ao inserir dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
}