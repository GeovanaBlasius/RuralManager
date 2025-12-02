package model;

import java.util.ArrayList;
import java.util.List;

public class Safra {
    private Integer id;
    private int ano;
    private String descricao;
    private Integer produtorId;
    private List<Cultura> culturas;

    public Safra() {
        this.culturas = new ArrayList<>();
    }

    public Safra(int ano) {
        this();
        setAno(ano);
    }

    public Safra(Integer id, int ano, String descricao, Integer produtorId) {
        this();
        this.id = id;
        setAno(ano);
        setDescricao(descricao);
        setProdutorId(produtorId);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        int limiteSuperior = java.time.Year.now().getValue() + 1;
        if (ano < 2000 || ano > limiteSuperior) throw new IllegalArgumentException("Ano da safra inválido!");
        this.ano = ano;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getProdutorId() {
        return produtorId;
    }

    public void setProdutorId(Integer produtorId) {
        this.produtorId = produtorId;
    }

    public List<Cultura> getCulturas() {
        return culturas;
    }

    public void adicionarCultura(Cultura cultura) {
        if (cultura == null) throw new IllegalArgumentException("Cultura inválida!");
        if (buscarCulturaPorNome(cultura.getNome()) != null) throw new IllegalArgumentException("Cultura já cadastrada nesta safra!");
        this.culturas.add(cultura);
    }

    public Cultura buscarCulturaPorNome(String nomeCultura) {
        return culturas.stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nomeCultura))
                .findFirst()
                .orElse(null);
    }

    public boolean removerCultura(String nomeCultura) {
        return culturas.removeIf(c -> c.getNome().equalsIgnoreCase(nomeCultura));
    }

    public double calcularTotalGastos() {
        return culturas.stream().mapToDouble(Cultura::calcularTotalGastos).sum();
    }

    public double calcularTotalReceita() {
        return culturas.stream().mapToDouble(Cultura::calcularReceita).sum();
    }

    public double calcularLucroPrejuizoSafra() {
        return calcularTotalReceita() - calcularTotalGastos();
    }

    @Override
    public String toString() {
        return String.format("Safra [ID: %d] %d | Produtor ID: %d | Culturas: %d | Lucro/Prejuízo: R$ %.2f",
                id, ano, produtorId, culturas.size(), calcularLucroPrejuizoSafra());
    }
}