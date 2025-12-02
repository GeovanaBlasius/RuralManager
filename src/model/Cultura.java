package model;

import java.util.ArrayList;
import java.util.List;

public class Cultura {
    private Integer id;
    private String nome;
    private String variedade;
    private double areaPlantada;
    private Integer safraId;
    private List<Gasto> gastos;
    private List<Colheita> colheitas;

    public Cultura() {
        this.gastos = new ArrayList<>();
        this.colheitas = new ArrayList<>();
    }

    public Cultura(String nome, String variedade, double areaPlantada) {
        this();
        setNome(nome);
        setVariedade(variedade);
        setAreaPlantada(areaPlantada);
    }

    public Cultura(Integer id, String nome, String variedade, double areaPlantada, Integer safraId) {
        this();
        this.id = id;
        setNome(nome);
        setVariedade(variedade);
        setAreaPlantada(areaPlantada);
        setSafraId(safraId);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) throw new IllegalArgumentException("Nome da cultura não pode ser vazio!");
        this.nome = nome.trim();
    }

    public String getVariedade() {
        return variedade;
    }

    public void setVariedade(String variedade) {
        if (variedade == null || variedade.trim().isEmpty()) throw new IllegalArgumentException("Variedade não pode ser vazia!");
        this.variedade = variedade.trim();
    }

    public double getAreaPlantada() {
        return areaPlantada;
    }

    public void setAreaPlantada(double areaPlantada) {
        if (areaPlantada <= 0) throw new IllegalArgumentException("Área plantada deve ser maior que zero!");
        this.areaPlantada = areaPlantada;
    }

    public Integer getSafraId() {
        return safraId;
    }

    public void setSafraId(Integer safraId) {
        this.safraId = safraId;
    }

    public List<Gasto> getGastos() {
        return gastos;
    }

    public List<Colheita> getColheitas() {
        return colheitas;
    }

    public void adicionarGasto(Gasto gasto) {
        if (gasto == null) throw new IllegalArgumentException("Gasto inválido!");
        this.gastos.add(gasto);
    }

    public void adicionarColheita(Colheita colheita) {
        if (colheita == null) throw new IllegalArgumentException("Colheita inválida!");
        if (colheita.isVendida() && colheita.getPrecoVenda() <= 0) {
            throw new IllegalArgumentException("Colheita vendida deve ter preço de venda > 0!");
        }
        this.colheitas.add(colheita);
    }

    public double calcularTotalGastos() {
        return gastos.stream().mapToDouble(Gasto::getValor).sum();
    }

    public double calcularTotalColhido() {
        return colheitas.stream().mapToDouble(Colheita::getQuantidade).sum();
    }

    public double calcularReceita() {
        return colheitas.stream()
                .filter(Colheita::isVendida)
                .mapToDouble(c -> c.getQuantidade() * c.getPrecoVenda())
                .sum();
    }

    public double calcularLucroPrejuizo() {
        return calcularReceita() - calcularTotalGastos();
    }

    public double calcularProdutividade() {
        return areaPlantada > 0 ? calcularTotalColhido() / areaPlantada : 0;
    }

    @Override
    public String toString() {
        return String.format(
                "Cultura [ID: %d] %s - %s | Área: %.2f ha | Produtividade: %.2f kg/ha | Gastos: R$ %.2f | Receita: R$ %.2f | Lucro/Prejuízo: R$ %.2f",
                id, nome, variedade, areaPlantada, calcularProdutividade(),
                calcularTotalGastos(), calcularReceita(), calcularLucroPrejuizo()
        );
    }
}