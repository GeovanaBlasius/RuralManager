package br.com.ruralmanager.comum;

/** Login com senha correta, mas o e-mail da conta ainda nao foi confirmado. */
public class EmailNaoVerificadoException extends RuntimeException {
    public EmailNaoVerificadoException() {
        super("Confirme seu e-mail para entrar. Enviamos um codigo de 6 digitos para o endereco cadastrado.");
    }
}
