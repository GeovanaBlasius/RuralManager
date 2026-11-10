package br.com.ruralmanager.catalogo;

/** Catalogo simples, identificado so por um nome unico: produto, categoria de gasto, destino. */
public interface ItemNomeado {
    Long getId();

    String getNome();

    void setNome(String nome);
}
