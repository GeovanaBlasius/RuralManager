package model;

import java.time.LocalDate;

public class Colheita {
    private Integer id;
    private double quantidade;
    private double precoVenda;
    private boolean vendida;
    private LocalDate dataColheita;
    private Integer culturaId;

    public Colheita() {}

    public Colheita(double quantidade, LocalDate dataColheita) {
        setQuantidade(quantidade);
        setDataColheita(dataColheita);
        this.vendida = false;
        this.precoVenda = 0;
    }

    public Colheita(Integer id, double quantidade, double precoVenda, boolean vendida, LocalDate dataColheita, Integer culturaId) {
        this();
        this.id = id;
        setQuantidade(quantidade);
        setPrecoVenda(precoVenda);
        setVendida(vendida);
        setDataColheita(dataColheita);
        setCulturaId(culturaId);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade colhida não pode ser negativa");
        this.quantidade = quantidade;
    }

    public double getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(double precoVenda) {
        if (precoVenda < 0) throw new IllegalArgumentException("Preço de venda não pode ser negativo");
        this.precoVenda = precoVenda;
    }

    public boolean isVendida() {
        return vendida;
    }

    public void setVendida(boolean vendida) {
        this.vendida = vendida;
        if (!vendida) {
            this.precoVenda = 0;
        } else if (precoVenda <= 0) {
            throw new IllegalArgumentException("Colheita vendida deve ter preço de venda maior que zero!");
        }
    }

    public LocalDate getDataColheita() {
        return dataColheita;
    }

    public void setDataColheita(LocalDate dataColheita) {
        if (dataColheita == null) throw new IllegalArgumentException("Data de colheita inválida!");
        if (dataColheita.isAfter(LocalDate.now())) throw new IllegalArgumentException("Data da colheita não pode ser futura!");
        this.dataColheita = dataColheita;
    }

    public Integer getCulturaId() {
        return culturaId;
    }

    public void setCulturaId(Integer culturaId) {
        this.culturaId = culturaId;
    }

    public double calcularValorVenda() {
        return vendida ? quantidade * precoVenda : 0;
    }

    @Override
    public String toString() {
        return String.format("Colheita [ID: %d] %.2f kg | Data: %s | Cultura ID: %d | %s",
                id, quantidade, dataColheita, culturaId,
                vendida ? String.format("Vendida - R$ %.2f", calcularValorVenda()) : "Não vendida");
    }
}