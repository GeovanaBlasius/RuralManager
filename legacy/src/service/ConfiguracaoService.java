package service;

import database.ConnectionFactory;
import database.LoggerUtil;
import java.util.Scanner;

public class ConfiguracaoService {
    private final Scanner scanner = new Scanner(System.in);

    public void exibirMenuConfiguracoes() {
        while (true) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("⚙️  CONFIGURAÇÕES DO SISTEMA");
            System.out.println("=".repeat(40));
            System.out.println("1. 🔧 Testar Conexão com Banco");
            System.out.println("2. 💾 Realizar Backup do Banco");
            System.out.println("3. 📜 Visualizar Logs do Sistema");
            System.out.println("4. ⚠️  Resetar Banco de Dados");
            System.out.println("5. 📊 Inserir Dados de Exemplo");
            System.out.println("0. 🔙 Voltar");
            System.out.println("-".repeat(40));
            System.out.print("Opção: ");

            int op = lerInt();

            switch (op) {
                case 1 -> testarConexao();
                case 2 -> realizarBackup();
                case 3 -> visualizarLogs();
                case 4 -> resetarBanco();
                case 5 -> inserirDadosExemplo();
                case 0 -> { return; }
                default -> System.out.println("❌ Opção inválida!");
            }
        }
    }

    private void inserirDadosExemplo() {
        System.out.println("\n=== INSERIR DADOS DE EXEMPLO ===");
        System.out.print("Esta ação irá adicionar 24 registros de teste. Continuar? (s/n): ");
        String resposta = scanner.nextLine();

        if ("s".equalsIgnoreCase(resposta)) {
            ConnectionFactory.inserirDadosExemplo();
        } else {
            System.out.println("❌ Operação cancelada.");
        }
    }

    private void testarConexao() {
        System.out.println("\n=== TESTAR CONEXÃO ===");
        ConnectionFactory.testConnection();
    }

    private void realizarBackup() {
        System.out.println("\n=== REALIZAR BACKUP ===");
        ConnectionFactory.realizarBackup();
    }

    private void visualizarLogs() {
        System.out.println("\n=== VISUALIZAR LOGS ===");
        LoggerUtil.visualizarLogs();
    }

    private void resetarBanco() {
        System.out.println("\n=== RESETAR BANCO DE DADOS ===");
        ConnectionFactory.resetarBanco();
    }

    private int lerInt() {
        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return -1;
            return Integer.parseInt(input);
        } catch (Exception e) {
            return -1;
        }
    }
}