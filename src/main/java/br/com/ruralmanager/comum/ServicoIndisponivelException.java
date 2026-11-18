package br.com.ruralmanager.comum;

/** Funcionalidade que depende de um servico externo nao configurado ou fora do ar (ex.: envio de e-mail). */
public class ServicoIndisponivelException extends RuntimeException {
    public ServicoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
