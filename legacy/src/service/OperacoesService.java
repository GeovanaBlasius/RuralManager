package service;

import model.*;
import util.ValidacaoUtil;
import database.LoggerUtil;
import java.time.LocalDate;
import java.util.Scanner;

public class OperacoesService {
    private final CadastroService cadastroService;
    private final Scanner scanner = new Scanner(System.in);

    public OperacoesService(CadastroService cadastroService) {
        this.cadastroService = cadastroService;
    }

    public void executarMenuOperacoes(Produtor produtor, MenuService menuService) {
        Safra safraAtual = menuService.getSafraAtual();

        while (true) {
            exibirMenuOperacoes(produtor, safraAtual);
            int opcao = lerInt();

            switch (opcao) {
                case 1 -> cadastrarSafra(produtor);
                case 2 -> listarSafrasProdutor(produtor);
                case 3 -> menuService.setSafraAtual(selecionarSafra(produtor));
                case 4 -> {
                    if (safraAtual != null) cadastrarCultura(produtor, safraAtual);
                    else System.out.println("❌ Primeiro selecione uma safra!");
                }
                case 5 -> {
                    if (safraAtual != null) listarCulturasSafra(safraAtual);
                    else System.out.println("❌ Primeiro selecione uma safra!");
                }
                case 6 -> {
                    if (safraAtual != null) excluirCultura(produtor, safraAtual);
                    else System.out.println("❌ Primeiro selecione uma safra!");
                }
                case 7 -> {
                    if (safraAtual != null) registrarGasto(produtor, safraAtual);
                    else System.out.println("❌ Primeiro selecione uma safra!");
                }
                case 8 -> {
                    if (safraAtual != null) listarGastos(produtor, safraAtual);
                    else System.out.println("❌ Primeiro selecione uma safra!");
                }
                case 9 -> {
                    if (safraAtual != null) registrarColheita(produtor, safraAtual);
                    else System.out.println("❌ Primeiro selecione uma safra!");
                }
                case 10 -> {
                    if (safraAtual != null) listarColheitas(produtor, safraAtual);
                    else System.out.println("❌ Primeiro selecione uma safra!");
                }
                case 11 -> {
                    if (safraAtual != null) alterarDadosSafra();
                    else System.out.println("❌ Primeiro selecione uma safra!");
                }
                case 12 -> {
                    if (safraAtual != null) {
                        excluirSafra(produtor, safraAtual);
                        menuService.setSafraAtual(null);
                    } else {
                        System.out.println("❌ Primeiro selecione uma safra!");
                    }
                }
                case 0 -> {
                    menuService.setSafraAtual(null);
                    return;
                }
                default -> System.out.println("❌ Opção inválida!");
            }

            safraAtual = menuService.getSafraAtual();
        }
    }

    private void exibirMenuOperacoes(Produtor produtor, Safra safraAtual) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📋 MENU DE OPERAÇÕES - " + produtor.getNome().toUpperCase());
        System.out.println("=".repeat(50));

        if (safraAtual != null) {
            System.out.println("📅 SAFRA SELECIONADA: " + safraAtual.getAno());
            System.out.println("-".repeat(50));
        }

        System.out.println("📅 GERENCIAR SAFRAS");
        System.out.println("  1. ➕ Cadastrar Nova Safra");
        System.out.println("  2. 📋 Listar Todas as Safras");
        System.out.println("  3. 🔍 Selecionar Safra para Trabalhar");

        if (safraAtual != null) {
            System.out.println("");
            System.out.println("🌱 GERENCIAR CULTURAS DA SAFRA " + safraAtual.getAno());
            System.out.println("  4. ➕ Cadastrar Nova Cultura");
            System.out.println("  5. 📋 Listar Culturas");
            System.out.println("  6. ❌ Excluir Cultura");
            System.out.println("");
            System.out.println("💰 REGISTROS FINANCEIROS");
            System.out.println("  7. 💰 Registrar Gasto");
            System.out.println("  8. 📋 Listar Gastos");
            System.out.println("  9. 🌾 Registrar Colheita");
            System.out.println("  10. 📋 Listar Colheitas");
            System.out.println("");
            System.out.println("11. 🔄 Alterar Dados da Safra");
            System.out.println("12. ❌ Excluir Safra");
        }

        System.out.println("");
        System.out.println("0. 🔙 Voltar ao Menu Inicial");
        System.out.println("-".repeat(50));
        System.out.print("📍 Escolha uma opção: ");
    }

    private void cadastrarSafra(Produtor produtor) {
        try {
            System.out.print("Ano da safra: ");
            int ano = lerInt();
            ValidacaoUtil.validarAno(ano);

            cadastroService.cadastrarSafraParaProdutor(produtor.getCpf(), ano);

            Produtor atualizado = cadastroService.buscarProdutorPorCPF(produtor.getCpf());
            if (atualizado != null) {
                System.out.println("✅ Safra cadastrada com sucesso!");
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    private void listarSafrasProdutor(Produtor produtor) {
        System.out.println("\n=== SAFRAS DO PRODUTOR " + produtor.getNome().toUpperCase() + " ===");

        if (produtor.getSafras().isEmpty()) {
            System.out.println("Nenhuma safra cadastrada.");
            System.out.println("💡 Use a opção 'Cadastrar Nova Safra' para começar!");
            return;
        }

        for (Safra safra : produtor.getSafras()) {
            System.out.println("\n📅 SAFRA " + safra.getAno() + ":");
            System.out.println("  Culturas: " + safra.getCulturas().size());
            System.out.println("  Gastos totais: R$ " +
                    String.format("%.2f", safra.calcularTotalGastos()));
            System.out.println("  Receita total: R$ " +
                    String.format("%.2f", safra.calcularTotalReceita()));
            System.out.println("  Lucro/Prejuízo: R$ " +
                    String.format("%.2f", safra.calcularLucroPrejuizoSafra()));
        }
    }

    private Safra selecionarSafra(Produtor produtor) {
        if (produtor.getSafras().isEmpty()) {
            System.out.println("Nenhuma safra cadastrada.");
            System.out.println("💡 Cadastre uma safra primeiro!");
            return null;
        }

        System.out.println("\n=== SELECIONAR SAFRA ===");
        System.out.println("\n📋 SAFRAS DISPONÍVEIS:");
        System.out.println("-".repeat(30));

        for (int i = 0; i < produtor.getSafras().size(); i++) {
            Safra safra = produtor.getSafras().get(i);
            System.out.printf("%d. Safra %d - %d culturas\n",
                    i + 1, safra.getAno(), safra.getCulturas().size());
        }

        System.out.println("\n0. 🔙 Cancelar");
        System.out.println("-".repeat(30));
        System.out.print("Selecione o número da safra: ");

        int opcao = lerInt();

        if (opcao == 0) {
            return null;
        }

        if (opcao > 0 && opcao <= produtor.getSafras().size()) {
            Safra safraSelecionada = produtor.getSafras().get(opcao - 1);
            System.out.println("\n✅ Safra " + safraSelecionada.getAno() + " selecionada!");
            System.out.println("🌱 Culturas: " + safraSelecionada.getCulturas().size());
            return safraSelecionada;
        } else {
            System.out.println("❌ Opção inválida!");
            return null;
        }
    }

    private void cadastrarCultura(Produtor produtor, Safra safra) {
        System.out.println("\n=== CADASTRAR CULTURA (Safra " + safra.getAno() + ") ===");

        try {
            System.out.print("Nome da cultura: ");
            String nome = scanner.nextLine();
            ValidacaoUtil.validarStringNaoVazia(nome, "Nome da cultura");

            System.out.print("Variedade: ");
            String variedade = scanner.nextLine();
            ValidacaoUtil.validarStringNaoVazia(variedade, "Variedade");

            System.out.print("Área plantada (hectares): ");
            double area = Double.parseDouble(scanner.nextLine().replace(",", "."));
            ValidacaoUtil.validarAreaPlantada(area);

            cadastroService.cadastrarCulturaParaSafra(
                    produtor.getCpf(),
                    safra.getAno(),
                    nome,
                    variedade,
                    area
            );

            System.out.println("✅ Cultura cadastrada com sucesso!");

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    private void listarCulturasSafra(Safra safra) {
        System.out.println("\n🌱 CULTURAS DA SAFRA " + safra.getAno() + ":");
        System.out.println("-".repeat(40));

        if (safra.getCulturas().isEmpty()) {
            System.out.println("Nenhuma cultura cadastrada.");
            return;
        }

        for (Cultura cultura : safra.getCulturas()) {
            System.out.println("• " + cultura.getNome() +
                    " (" + cultura.getVariedade() + ")");
            System.out.println("  Área: " + cultura.getAreaPlantada() + " ha");
            System.out.println("  Gastos: " + cultura.getGastos().size() + " registros (R$ " +
                    String.format("%.2f", cultura.calcularTotalGastos()) + ")");
            System.out.println("  Colheitas: " + cultura.getColheitas().size() + " registros");
            System.out.println("  Produtividade: " +
                    String.format("%.2f", cultura.calcularProdutividade()) + " kg/ha");
            System.out.println();
        }
    }

    private void excluirCultura(Produtor produtor, Safra safra) {
        System.out.println("\n=== EXCLUIR CULTURA (Safra " + safra.getAno() + ") ===");

        if (safra.getCulturas().isEmpty()) {
            System.out.println("Nenhuma cultura para excluir.");
            return;
        }

        System.out.println("\n📋 CULTURAS DISPONÍVEIS:");
        for (int i = 0; i < safra.getCulturas().size(); i++) {
            Cultura cultura = safra.getCulturas().get(i);
            System.out.printf("%d. %s (%s)\n", i + 1, cultura.getNome(), cultura.getVariedade());
        }

        System.out.print("\nSelecione o número da cultura: ");
        int opcao = lerInt();

        if (opcao > 0 && opcao <= safra.getCulturas().size()) {
            Cultura cultura = safra.getCulturas().get(opcao - 1);

            System.out.println("\n⚠️  ATENÇÃO: Esta exclusão será em CASCATA!");
            System.out.println("Serão removidos:");
            System.out.println("  • " + cultura.getGastos().size() + " gastos registrados");
            System.out.println("  • " + cultura.getColheitas().size() + " colheitas registradas");
            System.out.println("  • A cultura: " + cultura.getNome());

            System.out.print("\nConfirma exclusão? (digite 'SIM' para confirmar): ");
            String confirmacao = scanner.nextLine();

            if ("SIM".equalsIgnoreCase(confirmacao)) {
                cadastroService.excluirCulturaDaSafra(
                        produtor.getCpf(),
                        safra.getAno(),
                        cultura.getNome()
                );

                System.out.println("✅ Cultura excluída com sucesso!");
            } else {
                System.out.println("❌ Exclusão cancelada.");
            }
        } else {
            System.out.println("❌ Opção inválida!");
        }
    }

    private void registrarGasto(Produtor produtor, Safra safra) {
        System.out.println("\n=== REGISTRAR GASTO (Safra " + safra.getAno() + ") ===");

        try {
            if (safra.getCulturas().isEmpty()) {
                System.out.println("❌ Nenhuma cultura cadastrada nesta safra!");
                System.out.println("💡 Cadastre uma cultura primeiro.");
                return;
            }

            System.out.println("\n📋 Selecione a cultura:");
            for (int i = 0; i < safra.getCulturas().size(); i++) {
                Cultura cultura = safra.getCulturas().get(i);
                System.out.printf("%d. %s\n", i + 1, cultura.getNome());
            }

            System.out.print("Opção: ");
            int opcaoCultura = lerInt();

            if (opcaoCultura < 1 || opcaoCultura > safra.getCulturas().size()) {
                System.out.println("❌ Cultura inválida!");
                return;
            }

            Cultura cultura = safra.getCulturas().get(opcaoCultura - 1);

            System.out.print("Descrição do gasto: ");
            String descricao = scanner.nextLine();
            ValidacaoUtil.validarStringNaoVazia(descricao, "Descrição");

            System.out.print("Categoria (ex: Sementes, Fertilizantes, Mão de obra): ");
            String categoria = scanner.nextLine();
            ValidacaoUtil.validarStringNaoVazia(categoria, "Categoria");

            System.out.print("Valor (R$): ");
            double valor = Double.parseDouble(scanner.nextLine().replace(",", "."));
            ValidacaoUtil.validarValorNaoNegativo(valor, "Valor");

            System.out.print("Data do gasto (AAAA-MM-DD): ");
            String dataStr = scanner.nextLine();
            LocalDate data = ValidacaoUtil.validarData(dataStr, "Data do gasto");

            if (data.isAfter(LocalDate.now())) {
                System.out.println("❌ Data do gasto não pode ser futura!");
                return;
            }

            cadastroService.registrarGastoParaCultura(
                    produtor.getCpf(),
                    safra.getAno(),
                    cultura.getNome(),
                    descricao,
                    categoria,
                    valor,
                    data
            );

            System.out.println("✅ Gasto registrado com sucesso!");

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    private void listarGastos(Produtor produtor, Safra safra) {
        System.out.println("\n=== GASTOS DA SAFRA " + safra.getAno() + " ===");

        if (safra.getCulturas().isEmpty()) {
            System.out.println("Nenhuma cultura cadastrada.");
            return;
        }

        System.out.println("\n📋 Selecione a cultura:");
        for (int i = 0; i < safra.getCulturas().size(); i++) {
            Cultura cultura = safra.getCulturas().get(i);
            System.out.printf("%d. %s\n", i + 1, cultura.getNome());
        }

        System.out.print("Opção: ");
        int opcaoCultura = lerInt();

        if (opcaoCultura < 1 || opcaoCultura > safra.getCulturas().size()) {
            System.out.println("❌ Cultura inválida!");
            return;
        }

        Cultura cultura = safra.getCulturas().get(opcaoCultura - 1);

        System.out.println("\n💰 Gastos da Cultura: " + cultura.getNome());
        System.out.println("-".repeat(50));

        if (cultura.getGastos().isEmpty()) {
            System.out.println("Nenhum gasto registrado.");
            return;
        }

        double total = 0;
        for (var gasto : cultura.getGastos()) {
            System.out.println("• " + gasto.getData() + " - " + gasto.getDescricao());
            System.out.println("  Categoria: " + gasto.getCategoria());
            System.out.println("  Valor: R$ " + String.format("%.2f", gasto.getValor()));
            System.out.println();
            total += gasto.getValor();
        }

        System.out.println("=".repeat(30));
        System.out.println("TOTAL: R$ " + String.format("%.2f", total));
    }

    private void registrarColheita(Produtor produtor, Safra safra) {
        System.out.println("\n=== REGISTRAR COLHEITA (Safra " + safra.getAno() + ") ===");

        try {
            if (safra.getCulturas().isEmpty()) {
                System.out.println("❌ Nenhuma cultura cadastrada nesta safra!");
                System.out.println("💡 Cadastre uma cultura primeiro.");
                return;
            }

            System.out.println("\n📋 Selecione a cultura:");
            for (int i = 0; i < safra.getCulturas().size(); i++) {
                Cultura cultura = safra.getCulturas().get(i);
                System.out.printf("%d. %s\n", i + 1, cultura.getNome());
            }

            System.out.print("Opção: ");
            int opcaoCultura = lerInt();

            if (opcaoCultura < 1 || opcaoCultura > safra.getCulturas().size()) {
                System.out.println("❌ Cultura inválida!");
                return;
            }

            Cultura cultura = safra.getCulturas().get(opcaoCultura - 1);

            System.out.print("Quantidade colhida (kg): ");
            double quantidade = Double.parseDouble(scanner.nextLine().replace(",", "."));
            ValidacaoUtil.validarQuantidade(quantidade);

            System.out.print("Data da colheita (AAAA-MM-DD): ");
            String dataStr = scanner.nextLine();
            LocalDate data = ValidacaoUtil.validarData(dataStr, "Data da colheita");

            System.out.print("Foi vendida? (s/n): ");
            boolean vendida = scanner.nextLine().equalsIgnoreCase("s");

            double precoVenda = 0;
            if (vendida) {
                System.out.print("Preço de venda por kg (R$): ");
                precoVenda = Double.parseDouble(scanner.nextLine().replace(",", "."));
                ValidacaoUtil.validarValorNaoNegativo(precoVenda, "Preço de venda");

                if (precoVenda <= 0) {
                    System.out.println("❌ Colheita vendida deve ter preço de venda maior que zero!");
                    return;
                }
            }

            cadastroService.registrarColheitaParaCultura(
                    produtor.getCpf(),
                    safra.getAno(),
                    cultura.getNome(),
                    quantidade,
                    data,
                    vendida,
                    precoVenda
            );

            System.out.println("✅ Colheita registrada com sucesso!");

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    private void listarColheitas(Produtor produtor, Safra safra) {
        System.out.println("\n=== COLHEITAS DA SAFRA " + safra.getAno() + " ===");

        if (safra.getCulturas().isEmpty()) {
            System.out.println("Nenhuma cultura cadastrada.");
            return;
        }

        System.out.println("\n📋 Selecione a cultura:");
        for (int i = 0; i < safra.getCulturas().size(); i++) {
            Cultura cultura = safra.getCulturas().get(i);
            System.out.printf("%d. %s\n", i + 1, cultura.getNome());
        }

        System.out.print("Opção: ");
        int opcaoCultura = lerInt();

        if (opcaoCultura < 1 || opcaoCultura > safra.getCulturas().size()) {
            System.out.println("❌ Cultura inválida!");
            return;
        }

        Cultura cultura = safra.getCulturas().get(opcaoCultura - 1);

        System.out.println("\n🌾 Colheitas da Cultura: " + cultura.getNome());
        System.out.println("-".repeat(50));

        if (cultura.getColheitas().isEmpty()) {
            System.out.println("Nenhuma colheita registrada.");
            return;
        }

        double totalColhido = 0;
        double totalVendido = 0;
        double receitaTotal = 0;

        for (var colheita : cultura.getColheitas()) {
            System.out.println("• Data: " + colheita.getDataColheita());
            System.out.println("  Quantidade: " +
                    String.format("%.2f", colheita.getQuantidade()) + " kg");
            System.out.println("  Vendida: " + (colheita.isVendida() ? "✅ Sim" : "❌ Não"));

            if (colheita.isVendida()) {
                double valorVenda = colheita.calcularValorVenda();
                System.out.println("  Preço/kg: R$ " +
                        String.format("%.2f", colheita.getPrecoVenda()));
                System.out.println("  Valor: R$ " + String.format("%.2f", valorVenda));
                totalVendido += colheita.getQuantidade();
                receitaTotal += valorVenda;
            }

            totalColhido += colheita.getQuantidade();
            System.out.println();
        }

        System.out.println("=".repeat(40));
        System.out.println("RESUMO:");
        System.out.println("Total colhido: " +
                String.format("%.2f", totalColhido) + " kg");
        System.out.println("Total vendido: " +
                String.format("%.2f", totalVendido) + " kg");
        System.out.println("Receita total: R$ " +
                String.format("%.2f", receitaTotal));
    }

    private void alterarDadosSafra() {
        System.out.println("\n=== ALTERAR DADOS DA SAFRA ===");
        System.out.println("Atualmente não é possível alterar o ano da safra.");
        System.out.println("Para alterar o ano, exclua a safra e crie uma nova.");
        System.out.println("\nDica: Use a opção 'Cadastrar Nova Safra' para criar uma nova.");
    }

    private void excluirSafra(Produtor produtor, Safra safra) {
        System.out.println("\n=== EXCLUIR SAFRA " + safra.getAno() + " ===");

        System.out.println("\n⚠️  ATENÇÃO: Esta exclusão será em CASCATA!");
        System.out.println("Serão removidos:");
        System.out.println("  • " + safra.getCulturas().size() + " culturas");

        int totalGastos = 0;
        int totalColheitas = 0;
        for (Cultura cultura : safra.getCulturas()) {
            totalGastos += cultura.getGastos().size();
            totalColheitas += cultura.getColheitas().size();
        }

        System.out.println("  • " + totalGastos + " gastos");
        System.out.println("  • " + totalColheitas + " colheitas");

        System.out.print("\nConfirma exclusão? (digite 'SIM' para confirmar): ");
        String confirmacao = scanner.nextLine();

        if ("SIM".equalsIgnoreCase(confirmacao)) {
            cadastroService.excluirSafraDoProdutor(
                    produtor.getCpf(),
                    safra.getAno()
            );

            System.out.println("✅ Safra excluída com sucesso!");
            LoggerUtil.logOperacao("EXCLUIR SAFRA",
                    "Produtor: " + produtor.getNome() + ", Safra: " + safra.getAno());
        } else {
            System.out.println("❌ Exclusão cancelada.");
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

    private double lerDouble() {
        try {
            String input = scanner.nextLine().trim().replace(",", ".");
            if (input.isEmpty()) return -1;
            return Double.parseDouble(input);
        } catch (Exception e) {
            return -1;
        }
    }
}