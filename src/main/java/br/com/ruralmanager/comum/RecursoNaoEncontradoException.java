package br.com.ruralmanager.comum;

/**
 * Registro inexistente OU pertencente a outro produtor. Os dois casos sao
 * indistinguiveis de proposito: responder 403 para o segundo revelaria que o
 * id existe.
 */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String recurso) {
        super(recurso + " nao encontrado(a).");
    }
}
