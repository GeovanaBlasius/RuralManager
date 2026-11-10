package br.com.ruralmanager.comum;

/** Violacao de uma regra do dominio, com mensagem pensada para o agricultor ler. */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
