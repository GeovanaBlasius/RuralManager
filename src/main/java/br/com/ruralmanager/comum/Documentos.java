package br.com.ruralmanager.comum;

/** Validacao de CPF e CNPJ pelos digitos verificadores, e nao so pelo tamanho. */
public final class Documentos {

    private Documentos() {
    }

    public static String somenteDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    public static boolean cpfValido(String cpf) {
        String d = somenteDigitos(cpf);
        if (d == null || d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return digito(d, 9, 10) == d.charAt(9) - '0' && digito(d, 10, 11) == d.charAt(10) - '0';
    }

    public static boolean cnpjValido(String cnpj) {
        String d = somenteDigitos(cnpj);
        if (d == null || d.length() != 14 || d.chars().distinct().count() == 1) {
            return false;
        }
        int[] p1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] p2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        return digitoCnpj(d, p1) == d.charAt(12) - '0' && digitoCnpj(d, p2) == d.charAt(13) - '0';
    }

    /** CPF (11) ou CNPJ (14), conforme o tamanho. */
    public static boolean cpfOuCnpjValido(String doc) {
        String d = somenteDigitos(doc);
        if (d == null) {
            return false;
        }
        return d.length() == 11 ? cpfValido(d) : d.length() == 14 && cnpjValido(d);
    }

    private static int digito(String d, int qtd, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < qtd; i++) {
            soma += (d.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private static int digitoCnpj(String d, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (d.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
