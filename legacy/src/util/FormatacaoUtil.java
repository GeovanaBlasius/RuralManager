package util;

public class FormatacaoUtil {

    public static String formatarTelefone(String telefone) {
        if (telefone == null || telefone.length() != 11) {
            return telefone;
        }
        return "(" + telefone.substring(0, 2) + ") " +
                telefone.substring(2, 7) + "-" +
                telefone.substring(7);
    }

    public static String formatarCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." +
                cpf.substring(3, 6) + "." +
                cpf.substring(6, 9) + "-" +
                cpf.substring(9);
    }

    public static String formatarNome(String nome) {
        if (nome == null || nome.isEmpty()) {
            return nome;
        }
        String[] palavras = nome.toLowerCase().split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < palavras.length; i++) {
            if (!palavras[i].isEmpty()) {
                if (i > 0) resultado.append(" ");
                resultado.append(palavras[i].substring(0, 1).toUpperCase())
                        .append(palavras[i].substring(1));
            }
        }
        return resultado.toString();
    }
}