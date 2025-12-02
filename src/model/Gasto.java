package model;

import java.time.LocalDate;

public class Gasto {
    private Integer id;
    private String descricao;
    private String categoria;
    private double valor;
    private LocalDate data;
    private Integer culturaId;

    public Gasto() {}

    public Gasto(String descricao, String categoria, double valor, LocalDate data) {
        setDescricao(descricao);
        setCategoria(categoria);
        setValor(valor);
        setData(data);
    }

    public Gasto(Integer id, String descricao, String categoria, double valor, LocalDate data, Integer culturaId) {
        this();
        this.id = id;
        setDescricao(descricao);
        setCategoria(categoria);
        setValor(valor);
        setData(data);
        setCulturaId(culturaId);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) throw new IllegalArgumentException("Descrição não pode ser vazia!");
        this.descricao = descricao.trim();
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) throw new IllegalArgumentException("Categoria não pode ser vazia!");
        this.categoria = categoria.trim();
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        if (valor < 0) throw new IllegalArgumentException("Valor do gasto não pode ser negativo!");
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        if (data == null) throw new IllegalArgumentException("Data inválida!");
        if (data.isAfter(LocalDate.now())) throw new IllegalArgumentException("Data do gasto não pode ser futura!");
        this.data = data;
    }

    public Integer getCulturaId() {
        return culturaId;
    }

    public void setCulturaId(Integer culturaId) {
        this.culturaId = culturaId;
    }

    @Override
    public String toString() {
        return String.format("Gasto [ID: %d] %s | Categoria: %s | Valor: R$ %.2f | Data: %s | Cultura ID: %d",
                id, descricao, categoria, valor, data, culturaId);
    }

    public String gerarLinhaRelatorio() {
        return descricao + ";" + categoria + ";" + String.format("%.2f", valor) + ";" + data;
    }
}