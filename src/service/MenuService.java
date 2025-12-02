package service;

import model.Produtor;
import model.Safra;
import util.FormatacaoUtil;
import util.ValidacaoUtil;
import database.LoggerUtil;
import java.util.Scanner;

public class MenuService {
    private final Scanner scanner = new Scanner(System.in);
    private final CadastroService cadastroService;
    private final RelatorioService relatorioService;
    private final ConfiguracaoService configuracaoService;
    private final OperacoesService operacoesService;

    private Produtor produtorAtual = null;
    private Safra safraAtual = null;

    public MenuService() {
        this.cadastroService = new CadastroService();
        this.relatorioService = new RelatorioService(cadastroService);
        this.configuracaoService = new ConfiguracaoService();
        this.operacoesService = new OperacoesService(cadastroService);
    }

    public void executarSistema() {
        while (true) {
            exibirMenuInicial();
            int opcao = lerInt();

            switch (opcao) {
                case 1 -> selecionarProdutor();
                case 2 -> cadastrarProdutor();
                case 3 -> {
                    if (produtorAtual != null) operacoesService.executarMenuOperacoes(produtorAtual, this);
                    else System.out.println("❌ Primeiro selecione um produtor!");
                }
                case 4 -> {
                    if (produtorAtual != null) relatorioService.exibirMenuRelatoriosProdutor(produtorAtual);
                    else System.out.println("❌ Primeiro selecione um produtor!");
                }
                case 5 -> {
                    if (produtorAtual != null) cadastroService.editarProdutor();
                    else System.out.println("❌ Primeiro selecione um produtor!");
                }
                case 6 -> configuracaoService.exibirMenuConfiguracoes();
                case 0 -> {
                    scanner.close();
                    return;
                }
                default -> System.out.println("❌ Opção inválida!");
            }
        }
    }

    private void exibirMenuInicial() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🌱 RURALMANAGER - MENU INICIAL");
        System.out.println("=".repeat(50));

        if (produtorAtual != null) {
            System.out.println("👤 PRODUTOR ATUAL: " + produtorAtual.getNome());
            System.out.println("📞 Telefone: " + FormatacaoUtil.formatarTelefone(produtorAtual.getTelefone()));
            System.out.println("📅 Safras cadastradas: " + produtorAtual.getSafras().size());
            System.out.println("-".repeat(50));
        }

        System.out.println("1. 👤 Selecionar/Trocar Produtor");
        System.out.println("2. ➕ Cadastrar Novo Produtor");

        if (produtorAtual != null) {
            System.out.println("3. 📋 Menu de Operações do Produtor");
            System.out.println("4. 📊 Relatórios do Produtor");
            System.out.println("5. 🔄 Alterar Dados do Produtor");
        }

        System.out.println("6. ⚙️  Configurações do Sistema");
        System.out.println("0. 🚪 Sair");
        System.out.println("-".repeat(50));
        System.out.print("📍 Escolha uma opção: ");
    }

    private void selecionarProdutor() {
        System.out.println("\n=== SELECIONAR PRODUTOR ===");

        var produtores = cadastroService.getProdutores();
        if (produtores.isEmpty()) {
            System.out.println("Nenhum produtor cadastrado.");
            System.out.println("💡 Cadastre um novo produtor primeiro!");
            return;
        }

        System.out.println("\n📋 PRODUTORES CADASTRADOS:");
        System.out.println("-".repeat(50));

        for (int i = 0; i < produtores.size(); i++) {
            Produtor p = produtores.get(i);
            System.out.printf("%d. %s (CPF: %s) - %d safras\n",
                    i + 1, p.getNome(), FormatacaoUtil.formatarCPF(p.getCpf()), p.getSafras().size());
        }

        System.out.println("\n0. 🔍 Buscar por CPF");
        System.out.println("-".repeat(50));
        System.out.print("Selecione o número do produtor: ");

        int opcao = lerInt();

        if (opcao == 0) {
            buscarProdutorPorCPF();
            return;
        }

        if (opcao > 0 && opcao <= produtores.size()) {
            setProdutorAtual(produtores.get(opcao - 1));
        } else {
            System.out.println("❌ Opção inválida!");
        }
    }

    private void buscarProdutorPorCPF() {
        System.out.print("\nDigite o CPF do produtor (somente números): ");
        String cpf = scanner.nextLine().replaceAll("\\D", "");

        try {
            Produtor produtor = cadastroService.buscarProdutorPorCPF(cpf);
            if (produtor != null) {
                setProdutorAtual(produtor);
            } else {
                System.out.println("❌ Produtor não encontrado!");
                System.out.print("Deseja cadastrar este produtor? (s/n): ");
                String resposta = scanner.nextLine();
                if (resposta.equalsIgnoreCase("s")) {
                    cadastrarProdutorComCPF(cpf);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Erro na busca: " + e.getMessage());
        }
    }

    private void cadastrarProdutor() {
        System.out.println("\n=== CADASTRAR NOVO PRODUTOR ===");

        try {
            System.out.print("Nome completo: ");
            String nome = scanner.nextLine();
            nome = FormatacaoUtil.formatarNome(nome);
            ValidacaoUtil.validarNome(nome);

            System.out.print("CPF (somente números): ");
            String cpf = scanner.nextLine().replaceAll("\\D", "");
            ValidacaoUtil.validarCPF(cpf);

            System.out.print("Telefone (com DDD, somente números): ");
            String telefone = scanner.nextLine().replaceAll("\\D", "");
            ValidacaoUtil.validarTelefone(telefone);

            Produtor produtor = new Produtor(nome, cpf, telefone);

            Produtor produtorProcessado = cadastroService.cadastrarSeNaoExistir(produtor);

            if (produtorProcessado != null) {
                System.out.print("\nDeseja selecionar este produtor agora? (s/n): ");
                String resposta = scanner.nextLine();
                if (resposta.equalsIgnoreCase("s")) {
                    setProdutorAtual(produtorProcessado);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    private void cadastrarProdutorComCPF(String cpfBase) {
        try {
            System.out.println("\n=== CADASTRAR NOVO PRODUTOR ===");
            System.out.println("CPF informado: " + FormatacaoUtil.formatarCPF(cpfBase));

            System.out.print("Nome completo: ");
            String nome = scanner.nextLine();
            nome = FormatacaoUtil.formatarNome(nome);
            ValidacaoUtil.validarNome(nome);

            System.out.print("Telefone (com DDD, somente números): ");
            String telefone = scanner.nextLine().replaceAll("\\D", "");
            ValidacaoUtil.validarTelefone(telefone);

            String cpf = cpfBase;
            ValidacaoUtil.validarCPF(cpf);

            Produtor produtor = new Produtor(nome, cpf, telefone);

            Produtor produtorProcessado = cadastroService.cadastrarSeNaoExistir(produtor);

            if (produtorProcessado != null) {
                setProdutorAtual(produtorProcessado);
                System.out.println("✅ Produtor processado com sucesso!");
            }
        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    public Produtor getProdutorAtual() { return produtorAtual; }
    public Safra getSafraAtual() { return safraAtual; }

    public void setProdutorAtual(Produtor produtor) {
        this.produtorAtual = produtor;
        this.safraAtual = null;
        if (produtor != null) {
            System.out.println("\n✅ Produtor selecionado: " + produtor.getNome());
            System.out.println("📞 Telefone: " + FormatacaoUtil.formatarTelefone(produtor.getTelefone()));
            LoggerUtil.logOperacao("SELECIONAR PRODUTOR",
                    "Produtor: " + produtor.getNome() + " (CPF: " + produtor.getCpf() + ")");
        }
    }

    public void setSafraAtual(Safra safra) {
        this.safraAtual = safra;
        if (safra != null) {
            System.out.println("\n✅ Safra " + safra.getAno() + " selecionada!");
        }
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