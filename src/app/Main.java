package app;

import database.InserirDadosExemplo;
import service.MenuService;
import database.ConnectionFactory;

public class Main {
    public static void main(String[] args) {
        System.out.println("🌱 =========================================");
        System.out.println("🌱       RURALMANAGER - SISTEMA DE GESTÃO");
        System.out.println("🌱 =========================================");
        System.out.println("🚀 Iniciando sistema...");

        inicializarBancoDados();

        MenuService menuService = new MenuService();
        menuService.executarSistema();

        System.out.println("\n👋 Encerrando RuralManager...");
        System.out.println("Obrigado por usar nosso sistema!");
    }

    private static void inicializarBancoDados() {
        System.out.println("🔧 Configurando banco de dados...");
        ConnectionFactory.criarBancoSeNaoExistir();
        System.out.println("✅ Banco de dados configurado!");
    }
}