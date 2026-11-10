package model;

import java.util.ArrayList;
import java.util.List;

public class Produtor {
    private Integer id;
    private String nome;
    private String cpf;
    private String telefone;
    private String endereco;
    private List<Safra> safras;

    public Produtor() {
        this.safras = new ArrayList<>();
    }

    public Produtor(String nome, String cpf, String telefone) {
        this();
        setNome(nome);
        setCpf(cpf);
        setTelefone(telefone);
    }

    public Produtor(Integer id, String nome, String cpf, String telefone, String endereco) {
        this();
        this.id = id;
        setNome(nome);
        setCpf(cpf);
        setTelefone(telefone);
        setEndereco(endereco);
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
        if (nome == null || nome.trim().isEmpty()) throw new IllegalArgumentException("Nome não pode ser vazio!");
        this.nome = nome.trim();
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) throw new IllegalArgumentException("CPF não pode ser vazio!");
        this.cpf = cpf.trim();
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) throw new IllegalArgumentException("Telefone não pode ser vazio!");
        this.telefone = telefone.trim();
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public List<Safra> getSafras() {
        return safras;
    }

    public void adicionarSafra(Safra safra) {
        if (safra == null) throw new IllegalArgumentException("Safra inválida!");
        if (buscarSafraPorAno(safra.getAno()) != null) throw new IllegalArgumentException("Safra já cadastrada para este ano!");
        this.safras.add(safra);
    }

    public Safra buscarSafraPorAno(int ano) {
        return safras.stream().filter(s -> s.getAno() == ano).findFirst().orElse(null);
    }

    public boolean removerSafra(int ano) {
        return safras.removeIf(s -> s.getAno() == ano);
    }

    public double calcularTotalGastosProdutor() {
        return safras.stream().mapToDouble(Safra::calcularTotalGastos).sum();
    }

    public double calcularTotalReceitaProdutor() {
        return safras.stream().mapToDouble(Safra::calcularTotalReceita).sum();
    }

    public double calcularLucroPrejuizoProdutor() {
        return calcularTotalReceitaProdutor() - calcularTotalGastosProdutor();
    }

    @Override
    public String toString() {
        return String.format("Produtor [ID: %d] %s | CPF: %s | Telefone: %s",
                id, nome, cpf, telefone);
    }
}