package service;

import model.*;
import database.LoggerUtil;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class RelatorioService {
    private final CadastroService cadastroService;
    private final CalculoService calculoService;
    private final Scanner scanner;

    public RelatorioService(CadastroService cadastroService) {
        this.cadastroService = cadastroService;
        this.calculoService = new CalculoService();
        this.scanner = new Scanner(System.in);
    }

    public void exibirMenuRelatoriosProdutor(Produtor produtor) {
        System.out.println("\n=== RELATÓRIOS DO PRODUTOR ===");
        System.out.println("Produtor: " + produtor.getNome());
        System.out.println("-".repeat(40));

        while (true) {
            System.out.println("\nSelecione o tipo de relatório:");
            System.out.println("1. 📊 Resumo Financeiro do Produtor");
            System.out.println("2. 📈 Análise por Safra");
            System.out.println("3. 🌱 Produtividade por Cultura");
            System.out.println("4. 💰 Gastos Detalhados");
            System.out.println("5. 📄 Exportar Relatório em TXT");
            System.out.println("0. 🔙 Voltar");
            System.out.print("Opção: ");

            int opcao = lerInt();

            switch (opcao) {
                case 1 -> gerarResumoFinanceiroProdutor(produtor);
                case 2 -> gerarAnaliseSafra(produtor);
                case 3 -> gerarProdutividadeCulturas(produtor);
                case 4 -> gerarRelatorioGastosDetalhados(produtor);
                case 5 -> exportarRelatorioProdutor(produtor);
                case 0 -> { return; }
                default -> System.out.println("❌ Opção inválida!");
            }
        }
    }

    private void gerarRelatorioGastosTotais() {
        System.out.println("\n=== RELATÓRIO DE GASTOS TOTAIS POR PRODUTOR ===");

        List<Produtor> produtores = cadastroService.getProdutores();
        if (produtores.isEmpty()) {
            System.out.println("❌ Nenhum produtor cadastrado!");
            return;
        }

        System.out.printf("\n%-5s %-30s %-15s %-20s%n",
                "ID", "PRODUTOR", "TOTAL SAFRAS", "GASTOS TOTAIS (R$)");
        System.out.println("-".repeat(75));

        for (Produtor produtor : produtores) {
            double totalGastos = 0;
            for (Safra safra : produtor.getSafras()) {
                totalGastos += safra.calcularTotalGastos();
            }

            System.out.printf("%-5d %-30s %-15d R$ %-15.2f%n",
                    produtor.getId(),
                    produtor.getNome(),
                    produtor.getSafras().size(),
                    totalGastos);
        }

        LoggerUtil.logOperacao("RELATORIO GASTOS TOTAIS",
                "Produtores analisados: " + produtores.size());
    }

    private void gerarRelatorioGastosDetalhados(Produtor produtor) {
        System.out.println("\n=== RELATÓRIO DE GASTOS DETALHADOS ===");
        System.out.println("Produtor: " + produtor.getNome());
        System.out.println("CPF: " + produtor.getCpf());
        System.out.println("=".repeat(60));

        double totalGeral = 0;

        for (Safra safra : produtor.getSafras()) {
            System.out.println("\nSAFRA " + safra.getAno() + ":");
            System.out.println("-".repeat(30));

            for (Cultura cultura : safra.getCulturas()) {
                System.out.println("  Cultura: " + cultura.getNome() +
                        " (" + cultura.getVariedade() + ")");
                System.out.println("  Área: " + cultura.getAreaPlantada() + " ha");

                if (cultura.getGastos().isEmpty()) {
                    System.out.println("  Nenhum gasto registrado");
                    continue;
                }

                double totalCultura = 0;
                for (Gasto gasto : cultura.getGastos()) {
                    System.out.printf("    • %s - %s: R$ %.2f (%s)%n",
                            gasto.getData(),
                            gasto.getDescricao(),
                            gasto.getValor(),
                            gasto.getCategoria());
                    totalCultura += gasto.getValor();
                }

                System.out.println("  Total da cultura: R$ " +
                        String.format("%.2f", totalCultura));
                totalGeral += totalCultura;
            }

            double totalSafra = safra.calcularTotalGastos();
            System.out.println("\n  TOTAL SAFRA " + safra.getAno() +
                    ": R$ " + String.format("%.2f", totalSafra));
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("TOTAL GERAL DO PRODUTOR: R$ " +
                String.format("%.2f", totalGeral));

        LoggerUtil.logOperacao("RELATORIO GASTOS DETALHADOS",
                "Produtor: " + produtor.getNome());
    }

    private void gerarRelatorioAnaliseProducao() {
        System.out.println("\n=== ANÁLISE DE PRODUÇÃO ===");

        List<Produtor> produtores = cadastroService.getProdutores();
        if (produtores.isEmpty()) {
            System.out.println("❌ Nenhum dado disponível!");
            return;
        }

        System.out.println("\n📈 RESUMO DE PRODUÇÃO POR CULTURA:");
        System.out.println("=".repeat(80));

        for (Produtor produtor : produtores) {
            System.out.println("\n🌾 PRODUTOR: " + produtor.getNome().toUpperCase());

            for (Safra safra : produtor.getSafras()) {
                System.out.println("\n  📅 SAFRA " + safra.getAno() + ":");

                for (Cultura cultura : safra.getCulturas()) {
                    double produtividade = CalculoService.calcularProdutividade(cultura);
                    double custoHectare = cultura.getAreaPlantada() > 0 ?
                            cultura.calcularTotalGastos() / cultura.getAreaPlantada() : 0;

                    System.out.println("\n    • " + cultura.getNome().toUpperCase() +
                            " (" + cultura.getVariedade() + ")");
                    System.out.println("      Área plantada: " +
                            String.format("%.2f", cultura.getAreaPlantada()) + " ha");
                    System.out.println("      Total colhido: " +
                            String.format("%.2f", cultura.calcularTotalColhido()) + " kg");
                    System.out.println("      Produtividade: " +
                            String.format("%.2f", produtividade) + " kg/ha");
                    System.out.println("      Custo por hectare: R$ " +
                            String.format("%.2f", custoHectare));

                    String classificacao;
                    if (produtividade > 10000) classificacao = "⭐ EXCELENTE";
                    else if (produtividade > 5000) classificacao = "✅ BOA";
                    else if (produtividade > 2000) classificacao = "⚠️  REGULAR";
                    else classificacao = "❌ BAIXA";

                    System.out.println("      Classificação: " + classificacao);
                }
            }
        }

        LoggerUtil.logOperacao("RELATORIO ANALISE PRODUCAO",
                "Produtores analisados: " + produtores.size());
    }

    private void gerarRelatorioGeralCadastros() {
        System.out.println("\n=== RELATÓRIO GERAL DE CADASTROS ===");

        List<Produtor> produtores = cadastroService.getProdutores();
        if (produtores.isEmpty()) {
            System.out.println("❌ Nenhum cadastro no sistema!");
            return;
        }

        int totalProdutores = produtores.size();
        int totalSafras = 0;
        int totalCulturas = 0;
        int totalGastos = 0;
        int totalColheitas = 0;
        double valorTotalGastos = 0;
        double valorTotalReceita = 0;

        System.out.println("\n📊 ESTATÍSTICAS DO SISTEMA:");
        System.out.println("=".repeat(50));

        for (Produtor produtor : produtores) {
            totalSafras += produtor.getSafras().size();

            for (Safra safra : produtor.getSafras()) {
                totalCulturas += safra.getCulturas().size();
                valorTotalGastos += safra.calcularTotalGastos();
                valorTotalReceita += safra.calcularTotalReceita();

                for (Cultura cultura : safra.getCulturas()) {
                    totalGastos += cultura.getGastos().size();
                    totalColheitas += cultura.getColheitas().size();
                }
            }
        }

        System.out.println("👥 Total de produtores: " + totalProdutores);
        System.out.println("📅 Total de safras: " + totalSafras);
        System.out.println("🌱 Total de culturas: " + totalCulturas);
        System.out.println("💰 Total de gastos registrados: " + totalGastos);
        System.out.println("🌾 Total de colheitas registradas: " + totalColheitas);
        System.out.println("💸 Valor total em gastos: R$ " +
                String.format("%.2f", valorTotalGastos));
        System.out.println("💵 Valor total em receita: R$ " +
                String.format("%.2f", valorTotalReceita));

        double resultadoTotal = valorTotalReceita - valorTotalGastos;
        System.out.println("📈 Resultado total: R$ " +
                String.format("%.2f", resultadoTotal));
        System.out.println("🎯 Situação: " +
                (resultadoTotal >= 0 ? "✅ LUCRO" : "❌ PREJUÍZO"));

        if (totalCulturas > 0) {
            double mediaGastosPorCultura = valorTotalGastos / totalCulturas;
            double mediaReceitaPorCultura = valorTotalReceita / totalCulturas;
            System.out.println("📊 Média de gastos por cultura: R$ " +
                    String.format("%.2f", mediaGastosPorCultura));
            System.out.println("📊 Média de receita por cultura: R$ " +
                    String.format("%.2f", mediaReceitaPorCultura));
        }

        LoggerUtil.logOperacao("RELATORIO GERAL CADASTROS",
                "Total produtores: " + totalProdutores);
    }

    private void gerarAnaliseFinanceiraCultura() {
        System.out.println("\n=== ANÁLISE FINANCEIRA POR CULTURA ===");

        try {
            System.out.print("CPF do produtor: ");
            String cpf = scanner.nextLine();

            Produtor produtor = cadastroService.buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            System.out.print("Ano da safra: ");
            int ano = Integer.parseInt(scanner.nextLine());

            Safra safra = produtor.buscarSafraPorAno(ano);
            if (safra == null) {
                System.out.println("❌ Safra não encontrada!");
                return;
            }

            System.out.print("Nome da cultura: ");
            String nomeCultura = scanner.nextLine();

            Cultura cultura = safra.buscarCulturaPorNome(nomeCultura);
            if (cultura == null) {
                System.out.println("❌ Cultura não encontrada!");
                return;
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("💰 ANÁLISE FINANCEIRA - " +
                    cultura.getNome().toUpperCase());
            System.out.println("=".repeat(60));

            double totalGastos = cultura.calcularTotalGastos();
            double receita = CalculoService.calcularReceitaCultura(cultura);
            double lucroPrejuizo = CalculoService.calcularLucroPrejuizoCultura(cultura);
            double roi = CalculoService.calcularROI(cultura);
            double produtividade = CalculoService.calcularProdutividade(cultura);

            System.out.println("\n📊 DADOS BÁSICOS:");
            System.out.println("  Variedade: " + cultura.getVariedade());
            System.out.println("  Área plantada: " +
                    String.format("%.2f", cultura.getAreaPlantada()) + " ha");
            System.out.println("  Total colhido: " +
                    String.format("%.2f", cultura.calcularTotalColhido()) + " kg");
            System.out.println("  Produtividade: " +
                    String.format("%.2f", produtividade) + " kg/ha");

            System.out.println("\n💰 ANÁLISE FINANCEIRA:");
            System.out.println("  Gastos totais: R$ " +
                    String.format("%.2f", totalGastos));
            System.out.println("  Receita total: R$ " +
                    String.format("%.2f", receita));
            System.out.println("  Resultado: R$ " +
                    String.format("%.2f", lucroPrejuizo));
            System.out.println("  ROI (Retorno sobre Investimento): " +
                    String.format("%.1f", roi) + "%");

            System.out.println("\n📈 INDICADORES POR HECTARE:");
            if (cultura.getAreaPlantada() > 0) {
                System.out.println("  Custo por hectare: R$ " +
                        String.format("%.2f", totalGastos / cultura.getAreaPlantada()));
                System.out.println("  Receita por hectare: R$ " +
                        String.format("%.2f", receita / cultura.getAreaPlantada()));
                System.out.println("  Lucro por hectare: R$ " +
                        String.format("%.2f", lucroPrejuizo / cultura.getAreaPlantada()));
            }

            System.out.println("\n🎯 RECOMENDAÇÕES:");
            if (lucroPrejuizo > 0) {
                System.out.println("  ✅ Cultura rentável - considere expandir área");
                if (roi > 50) {
                    System.out.println("  ⭐ ROI excelente - ótimo investimento");
                }
            } else {
                System.out.println("  ⚠️  Cultura com prejuízo - analise custos");
                if (totalGastos > receita * 2) {
                    System.out.println("  ❌ Custos muito altos - revise práticas");
                }
            }

            LoggerUtil.logOperacao("ANALISE FINANCEIRA CULTURA",
                    "Cultura: " + cultura.getNome() + ", ROI: " + roi + "%");

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    private void gerarRelatorioProdutividadeSafra() {
        System.out.println("\n=== PRODUTIVIDADE POR SAFRA ===");

        List<Produtor> produtores = cadastroService.getProdutores();
        if (produtores.isEmpty()) {
            System.out.println("❌ Nenhum dado disponível!");
            return;
        }

        System.out.printf("\n%-30s %-10s %-20s %-20s %-15s%n",
                "PRODUTOR", "SAFRA", "CULTURA", "PRODUTIVIDADE", "CLASSIFICAÇÃO");
        System.out.println("-".repeat(100));

        for (Produtor produtor : produtores) {
            for (Safra safra : produtor.getSafras()) {
                for (Cultura cultura : safra.getCulturas()) {
                    double produtividade = CalculoService.calcularProdutividade(cultura);

                    String classificacao;
                    if (produtividade > 10000) classificacao = "EXCELENTE";
                    else if (produtividade > 5000) classificacao = "BOA";
                    else if (produtividade > 2000) classificacao = "REGULAR";
                    else classificacao = "BAIXA";

                    System.out.printf("%-30s %-10d %-20s %-20.2f kg/ha %-15s%n",
                            produtor.getNome(),
                            safra.getAno(),
                            cultura.getNome(),
                            produtividade,
                            classificacao);
                }
            }
        }
    }

    private void gerarIndicadoresFinanceiros() {
        System.out.println("\n=== INDICADORES FINANCEIROS CONSOLIDADOS ===");

        List<Produtor> produtores = cadastroService.getProdutores();
        if (produtores.isEmpty()) {
            System.out.println("❌ Nenhum dado disponível!");
            return;
        }

        System.out.println("\n📊 RESUMO FINANCEIRO DO SISTEMA:");
        System.out.println("=".repeat(60));

        double totalGastosSistema = 0;
        double totalReceitaSistema = 0;
        int produtoresLucrativos = 0;
        int produtoresPrejuizo = 0;

        for (Produtor produtor : produtores) {
            double totalGastosProdutor = 0;
            double totalReceitaProdutor = 0;

            for (Safra safra : produtor.getSafras()) {
                totalGastosProdutor += safra.calcularTotalGastos();
                totalReceitaProdutor += safra.calcularTotalReceita();
            }

            totalGastosSistema += totalGastosProdutor;
            totalReceitaSistema += totalReceitaProdutor;

            double resultadoProdutor = totalReceitaProdutor - totalGastosProdutor;
            if (resultadoProdutor >= 0) {
                produtoresLucrativos++;
            } else {
                produtoresPrejuizo++;
            }

            System.out.println("\n👤 " + produtor.getNome().toUpperCase() + ":");
            System.out.println("  Gastos: R$ " + String.format("%.2f", totalGastosProdutor));
            System.out.println("  Receita: R$ " + String.format("%.2f", totalReceitaProdutor));
            System.out.println("  Resultado: R$ " + String.format("%.2f", resultadoProdutor));
            System.out.println("  Status: " + (resultadoProdutor >= 0 ? "✅ Lucrativo" : "❌ Prejuízo"));
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("📈 RESUMO GERAL DO SISTEMA:");
        System.out.println("  Total de produtores: " + produtores.size());
        System.out.println("  Produtores lucrativos: " + produtoresLucrativos);
        System.out.println("  Produtores com prejuízo: " + produtoresPrejuizo);
        System.out.println("  Taxa de sucesso: " +
                String.format("%.1f", (produtoresLucrativos * 100.0 / produtores.size())) + "%");
        System.out.println("  Gastos totais do sistema: R$ " +
                String.format("%.2f", totalGastosSistema));
        System.out.println("  Receita total do sistema: R$ " +
                String.format("%.2f", totalReceitaSistema));

        double resultadoSistema = totalReceitaSistema - totalGastosSistema;
        System.out.println("  Resultado do sistema: R$ " +
                String.format("%.2f", resultadoSistema));
        System.out.println("  Status geral: " +
                (resultadoSistema >= 0 ? "✅ Sistema Lucrativo" : "❌ Sistema com Prejuízo"));
    }

    private void gerarResumoFinanceiroProdutor(Produtor produtor) {
        System.out.println("\n📊 RESUMO FINANCEIRO - " + produtor.getNome().toUpperCase());
        System.out.println("=".repeat(60));

        double totalGastos = produtor.calcularTotalGastosProdutor();
        double totalReceita = produtor.calcularTotalReceitaProdutor();
        double lucroPrejuizo = produtor.calcularLucroPrejuizoProdutor();

        System.out.println("Total de safras: " + produtor.getSafras().size());
        System.out.println("Gastos totais: R$ " + String.format("%.2f", totalGastos));
        System.out.println("Receita total: R$ " + String.format("%.2f", totalReceita));
        System.out.println("Resultado: R$ " + String.format("%.2f", lucroPrejuizo));

        if (totalReceita > 0) {
            double margem = (lucroPrejuizo / totalReceita) * 100;
            System.out.println("Margem: " + String.format("%.1f", margem) + "%");
        }

        System.out.println("Status: " + (lucroPrejuizo >= 0 ? "✅ LUCRO" : "❌ PREJUÍZO"));
    }

    private void gerarAnaliseSafra(Produtor produtor) {
        System.out.println("\n📈 ANÁLISE POR SAFRA");
        System.out.println("=".repeat(60));

        for (Safra safra : produtor.getSafras()) {
            System.out.println("\nSAFRA " + safra.getAno() + ":");
            System.out.println("-".repeat(20));
            System.out.println("Culturas: " + safra.getCulturas().size());
            System.out.println("Gastos: R$ " +
                    String.format("%.2f", safra.calcularTotalGastos()));
            System.out.println("Receita: R$ " +
                    String.format("%.2f", safra.calcularTotalReceita()));
            System.out.println("Lucro/Prejuízo: R$ " +
                    String.format("%.2f", safra.calcularLucroPrejuizoSafra()));
            Cultura maisRentavel = null;
            double maiorLucro = Double.NEGATIVE_INFINITY;

            for (Cultura cultura : safra.getCulturas()) {
                double lucro = cultura.calcularLucroPrejuizo();
                if (lucro > maiorLucro) {
                    maiorLucro = lucro;
                    maisRentavel = cultura;
                }
            }

            if (maisRentavel != null) {
                System.out.println("Cultura mais rentável: " + maisRentavel.getNome() +
                        " (R$ " + String.format("%.2f", maiorLucro) + ")");
            }
        }
    }

    private void gerarProdutividadeCulturas(Produtor produtor) {
        System.out.println("\n🌱 PRODUTIVIDADE POR CULTURA");
        System.out.println("=".repeat(60));

        for (Safra safra : produtor.getSafras()) {
            System.out.println("\nSAFRA " + safra.getAno() + ":");

            for (Cultura cultura : safra.getCulturas()) {
                double produtividade = cultura.calcularProdutividade();
                System.out.println("  • " + cultura.getNome() +
                        ": " + String.format("%.2f", produtividade) + " kg/ha");
            }
        }
    }

    private void exportarRelatorioProdutor(Produtor produtor) {
        try {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "relatorio_" + produtor.getNome().replace(" ", "_") +
                    "_" + timestamp + ".txt";

            try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
                writer.println("RELATÓRIO DO PRODUTOR - RURALMANAGER");
                writer.println("Data: " + java.time.LocalDate.now());
                writer.println("=".repeat(60));
                writer.println();
                writer.println("PRODUTOR: " + produtor.getNome());
                writer.println("CPF: " + produtor.getCpf());
                writer.println("Telefone: " + produtor.getTelefone());
                writer.println();

                double totalGastos = produtor.calcularTotalGastosProdutor();
                double totalReceita = produtor.calcularTotalReceitaProdutor();
                double resultado = totalReceita - totalGastos;

                writer.println("RESUMO FINANCEIRO:");
                writer.println("Gastos totais: R$ " + String.format("%.2f", totalGastos));
                writer.println("Receita total: R$ " + String.format("%.2f", totalReceita));
                writer.println("Resultado: R$ " + String.format("%.2f", resultado));
                writer.println();

                writer.println("DETALHAMENTO POR SAFRA:");
                writer.println("=".repeat(40));

                for (Safra safra : produtor.getSafras()) {
                    writer.println();
                    writer.println("SAFRA " + safra.getAno() + ":");
                    writer.println("Culturas: " + safra.getCulturas().size());
                    writer.println("Gastos: R$ " +
                            String.format("%.2f", safra.calcularTotalGastos()));
                    writer.println("Receita: R$ " +
                            String.format("%.2f", safra.calcularTotalReceita()));
                    writer.println("Lucro/Prejuízo: R$ " +
                            String.format("%.2f", safra.calcularLucroPrejuizoSafra()));
                }

                System.out.println("✅ Relatório exportado: " + filename);
                LoggerUtil.logOperacao("EXPORTAR RELATORIO",
                        "Arquivo: " + filename + ", Produtor: " + produtor.getNome());
            }

        } catch (Exception e) {
            System.out.println("❌ Erro ao exportar: " + e.getMessage());
        }
    }

    private void exportarRelatorioTXT() {
        System.out.println("\n=== EXPORTAR RELATÓRIO ===");

        System.out.println("\nSelecione o tipo de relatório:");
        System.out.println("1 - Relatório Geral de Cadastros");
        System.out.println("2 - Análise Financeira por Produtor");
        System.out.println("3 - Relatório de Produção Completo");
        System.out.print("Opção: ");

        try {
            int tipo = Integer.parseInt(scanner.nextLine());
            String filename = "";

            switch (tipo) {
                case 1:
                    filename = exportarRelatorioGeral();
                    break;
                case 2:
                    filename = exportarAnaliseFinanceira();
                    break;
                case 3:
                    filename = exportarRelatorioProducao();
                    break;
                default:
                    System.out.println("❌ Opção inválida!");
                    return;
            }

            if (!filename.isEmpty()) {
                System.out.println("✅ Relatório exportado com sucesso: " + filename);
                LoggerUtil.logOperacao("EXPORTAR RELATORIO", "Arquivo: " + filename);
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    private String exportarRelatorioGeral() {
        String filename = "relatorio_geral_" + System.currentTimeMillis() + ".txt";

        try (FileWriter writer = new FileWriter(filename)) {
            List<Produtor> produtores = cadastroService.getProdutores();

            writer.write("RELATÓRIO GERAL - RURALMANAGER\n");
            writer.write("Data: " + java.time.LocalDate.now() + "\n");
            writer.write("=".repeat(60) + "\n\n");

            writer.write("RESUMO DO SISTEMA:\n");
            writer.write("-".repeat(30) + "\n");

            int totalSafras = 0;
            int totalCulturas = 0;
            double totalGastos = 0;
            double totalReceita = 0;

            for (Produtor produtor : produtores) {
                totalSafras += produtor.getSafras().size();

                for (Safra safra : produtor.getSafras()) {
                    totalCulturas += safra.getCulturas().size();
                    totalGastos += safra.calcularTotalGastos();
                    totalReceita += safra.calcularTotalReceita();
                }
            }

            writer.write("Total de produtores: " + produtores.size() + "\n");
            writer.write("Total de safras: " + totalSafras + "\n");
            writer.write("Total de culturas: " + totalCulturas + "\n");
            writer.write("Gastos totais: R$ " + String.format("%.2f", totalGastos) + "\n");
            writer.write("Receita total: R$ " + String.format("%.2f", totalReceita) + "\n");

            double resultado = totalReceita - totalGastos;
            writer.write("Resultado: R$ " + String.format("%.2f", resultado) + "\n");
            writer.write("Situação: " + (resultado >= 0 ? "LUCRO" : "PREJUÍZO") + "\n\n");

            writer.write("DETALHAMENTO POR PRODUTOR:\n");
            writer.write("=".repeat(40) + "\n\n");

            for (Produtor produtor : produtores) {
                writer.write("PRODUTOR: " + produtor.getNome().toUpperCase() + "\n");
                writer.write("CPF: " + produtor.getCpf() + "\n");
                writer.write("Telefone: " + produtor.getTelefone() + "\n");
                writer.write("Safras: " + produtor.getSafras().size() + "\n");

                for (Safra safra : produtor.getSafras()) {
                    writer.write("  SAFRA " + safra.getAno() + ":\n");
                    writer.write("    Culturas: " + safra.getCulturas().size() + "\n");
                    writer.write("    Gastos: R$ " +
                            String.format("%.2f", safra.calcularTotalGastos()) + "\n");
                    writer.write("    Receita: R$ " +
                            String.format("%.2f", safra.calcularTotalReceita()) + "\n\n");
                }
                writer.write("\n");
            }

            return filename;

        } catch (IOException e) {
            System.out.println("❌ Erro ao exportar: " + e.getMessage());
            return "";
        }
    }

    private String exportarAnaliseFinanceira() {
        System.out.print("\nCPF do produtor para análise: ");
        String cpf = scanner.nextLine();

        Produtor produtor = cadastroService.buscarProdutorPorCPF(cpf);
        if (produtor == null) {
            System.out.println("❌ Produtor não encontrado!");
            return "";
        }

        String filename = "analise_financeira_" + produtor.getNome().replace(" ", "_") +
                "_" + System.currentTimeMillis() + ".txt";

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("ANÁLISE FINANCEIRA - " + produtor.getNome().toUpperCase() + "\n");
            writer.write("CPF: " + produtor.getCpf() + "\n");
            writer.write("Data: " + java.time.LocalDate.now() + "\n");
            writer.write("=".repeat(60) + "\n\n");

            double totalGastos = 0;
            double totalReceita = 0;

            for (Safra safra : produtor.getSafras()) {
                writer.write("SAFRA " + safra.getAno() + ":\n");
                writer.write("-".repeat(20) + "\n");

                double gastosSafra = safra.calcularTotalGastos();
                double receitaSafra = safra.calcularTotalReceita();
                double resultadoSafra = receitaSafra - gastosSafra;

                writer.write("Gastos: R$ " + String.format("%.2f", gastosSafra) + "\n");
                writer.write("Receita: R$ " + String.format("%.2f", receitaSafra) + "\n");
                writer.write("Resultado: R$ " + String.format("%.2f", resultadoSafra) + "\n");
                writer.write("Status: " + (resultadoSafra >= 0 ? "LUCRO" : "PREJUÍZO") + "\n\n");

                totalGastos += gastosSafra;
                totalReceita += receitaSafra;

                for (Cultura cultura : safra.getCulturas()) {
                    writer.write("  " + cultura.getNome().toUpperCase() + ":\n");
                    writer.write("    Área: " + cultura.getAreaPlantada() + " ha\n");
                    writer.write("    Gastos: R$ " +
                            String.format("%.2f", cultura.calcularTotalGastos()) + "\n");
                    writer.write("    Receita: R$ " +
                            String.format("%.2f", CalculoService.calcularReceitaCultura(cultura)) + "\n");
                    writer.write("    Produtividade: " +
                            String.format("%.2f", CalculoService.calcularProdutividade(cultura)) + " kg/ha\n\n");
                }
            }

            writer.write("\n" + "=".repeat(60) + "\n");
            writer.write("RESUMO GERAL DO PRODUTOR:\n");
            writer.write("Gastos totais: R$ " + String.format("%.2f", totalGastos) + "\n");
            writer.write("Receita total: R$ " + String.format("%.2f", totalReceita) + "\n");

            double resultadoTotal = totalReceita - totalGastos;
            writer.write("Resultado total: R$ " + String.format("%.2f", resultadoTotal) + "\n");
            writer.write("Status geral: " + (resultadoTotal >= 0 ? "LUCRO" : "PREJUÍZO") + "\n");

            if (totalReceita > 0) {
                double margem = (resultadoTotal / totalReceita) * 100;
                writer.write("Margem de lucro: " + String.format("%.1f", margem) + "%\n");
            }

            return filename;

        } catch (IOException e) {
            System.out.println("❌ Erro ao exportar: " + e.getMessage());
            return "";
        }
    }

    private String exportarRelatorioProducao() {
        String filename = "relatorio_producao_" + System.currentTimeMillis() + ".txt";

        try (FileWriter writer = new FileWriter(filename)) {
            List<Produtor> produtores = cadastroService.getProdutores();

            writer.write("RELATÓRIO DE PRODUÇÃO COMPLETO - RURALMANAGER\n");
            writer.write("Data: " + java.time.LocalDate.now() + "\n");
            writer.write("=".repeat(70) + "\n\n");

            for (Produtor produtor : produtores) {
                writer.write("PRODUTOR: " + produtor.getNome().toUpperCase() + "\n");
                writer.write("CPF: " + produtor.getCpf() + "\n");
                writer.write("=".repeat(40) + "\n\n");

                for (Safra safra : produtor.getSafras()) {
                    writer.write("SAFRA " + safra.getAno() + ":\n");
                    writer.write("-".repeat(20) + "\n");

                    for (Cultura cultura : safra.getCulturas()) {
                        writer.write(cultura.getNome().toUpperCase() +
                                " (" + cultura.getVariedade() + ")\n");
                        writer.write("  Área: " + cultura.getAreaPlantada() + " ha\n");
                        writer.write("  Produção total: " +
                                String.format("%.2f", cultura.calcularTotalColhido()) + " kg\n");
                        writer.write("  Produtividade: " +
                                String.format("%.2f", CalculoService.calcularProdutividade(cultura)) + " kg/ha\n");
                        writer.write("  Gastos: " + cultura.getGastos().size() + " registros\n");
                        writer.write("  Colheitas: " + cultura.getColheitas().size() + " registros\n\n");
                    }
                    writer.write("\n");
                }
                writer.write("\n" + "=".repeat(70) + "\n\n");
            }

            return filename;

        } catch (IOException e) {
            System.out.println("❌ Erro ao exportar: " + e.getMessage());
            return "";
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