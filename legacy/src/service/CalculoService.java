package service;

import model.*;
import java.util.List;

public class CalculoService {

    public static double calcularReceitaCultura(Cultura cultura) {
        return cultura.getColheitas().stream()
                .filter(Colheita::isVendida)
                .mapToDouble(col -> col.getQuantidade() * col.getPrecoVenda())
                .sum();
    }

    public static double calcularLucroPrejuizoCultura(Cultura cultura) {
        double receita = calcularReceitaCultura(cultura);
        double gastos = cultura.calcularTotalGastos();
        return receita - gastos;
    }

    public static double calcularLucroPrejuizoSafra(Safra safra) {
        return safra.getCulturas().stream()
                .mapToDouble(CalculoService::calcularLucroPrejuizoCultura)
                .sum();
    }

    public static double calcularProdutividade(Cultura cultura) {
        if (cultura.getAreaPlantada() <= 0) return 0;
        double totalColhido = cultura.calcularTotalColhido();
        return totalColhido / cultura.getAreaPlantada();
    }

    public static String gerarIndicadoresFinanceiros(Produtor produtor) {
        double totalGastos = 0;
        double totalReceita = 0;

        for (Safra safra : produtor.getSafras()) {
            totalGastos += safra.calcularTotalGastos();
            totalReceita += safra.calcularTotalReceita();
        }

        double resultado = totalReceita - totalGastos;
        double margem = totalReceita > 0 ? (resultado / totalReceita) * 100 : 0;

        return String.format(
                "📊 INDICADORES FINANCEIROS - %s\n" +
                        "Gastos Totais: R$ %.2f\n" +
                        "Receita Total: R$ %.2f\n" +
                        "Resultado: R$ %.2f\n" +
                        "Margem: %.1f%%\n" +
                        "Status: %s",
                produtor.getNome(),
                totalGastos,
                totalReceita,
                resultado,
                margem,
                resultado >= 0 ? "✅ Lucrativo" : "❌ Prejuízo"
        );
    }

    public static double calcularROI(Cultura cultura) {
        double investimento = cultura.calcularTotalGastos();
        double retorno = calcularReceitaCultura(cultura);

        if (investimento <= 0) return 0;
        return ((retorno - investimento) / investimento) * 100;
    }

    public static double calcularCustoPorHectare(Cultura cultura) {
        if (cultura.getAreaPlantada() <= 0) return 0;
        return cultura.calcularTotalGastos() / cultura.getAreaPlantada();
    }

    public static double calcularReceitaPorHectare(Cultura cultura) {
        if (cultura.getAreaPlantada() <= 0) return 0;
        return calcularReceitaCultura(cultura) / cultura.getAreaPlantada();
    }
}