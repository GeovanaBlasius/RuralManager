package br.com.ruralmanager.comum;

/** Usuario autenticado, mas sem permissao para a operacao (ex.: operador tentando gerir usuarios). */
public class AcessoProibidoException extends RuntimeException {
    public AcessoProibidoException(String mensagem) {
        super(mensagem);
    }
}
