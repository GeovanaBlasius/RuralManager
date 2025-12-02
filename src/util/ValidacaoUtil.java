package util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ValidacaoUtil {

    public static void validarNaoNegativo(double valor, String campo) {
        if (valor < 0) {
            throw new IllegalArgumentException(campo + " não pode ser negativo");
        }
    }

    public static void validarPositivo(double valor, String campo) {
        if (valor <= 0) {
            throw new IllegalArgumentException(campo + " deve ser maior que zero");
        }
    }

    public static void validarStringNaoVazia(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " não pode ser vazio");
        }
    }

    public static void validarCPF(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF não pode ser vazio");
        }

        String cpfLimpo = cpf.replaceAll("\\D", "");
        if (cpfLimpo.length() != 11) {
            throw new IllegalArgumentException("CPF deve conter 11 dígitos");
        }

        // Validação de dígitos repetidos
        if (cpfLimpo.matches("(\\d)\\1{10}")) {
            throw new IllegalArgumentException("CPF inválido (dígitos repetidos)");
        }
    }

    private static boolean validarDigitosCPF(String cpf) {
        // Algoritmo de validação de CPF
        int soma = 0;
        int peso = 10;

        // Validar primeiro dígito
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * peso;
            peso--;
        }

        int resto = soma % 11;
        int digito1 = (resto < 2) ? 0 : 11 - resto;

        if (Character.getNumericValue(cpf.charAt(9)) != digito1) {
            return false;
        }

        soma = 0;
        peso = 11;

        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * peso;
            peso--;
        }

        resto = soma % 11;
        int digito2 = (resto < 2) ? 0 : 11 - resto;

        return Character.getNumericValue(cpf.charAt(10)) == digito2;
    }

    public static void validarAnoSafra(int ano) {
        int anoAtual = LocalDate.now().getYear();
        if (ano < 2000 || ano > anoAtual + 1) {
            throw new IllegalArgumentException("Ano da safra deve estar entre 2000 e " + (anoAtual + 1));
        }
    }

    public static void validarAno(int ano) {
        validarAnoSafra(ano); // Reutiliza a mesma validação
    }

    public static void validarDataNaoFutura(LocalDate data, String campo) {
        if (data == null) {
            throw new IllegalArgumentException(campo + " não pode ser nula");
        }

        if (data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(campo + " não pode ser uma data futura");
        }
    }

    public static LocalDate validarData(String dataStr, String campo) {
        try {
            LocalDate data = LocalDate.parse(dataStr);

            if (data.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException(campo + " não pode ser uma data futura");
            }

            if (data.isBefore(LocalDate.of(2000, 1, 1))) {
                throw new IllegalArgumentException(campo + " não pode ser anterior a 2000");
            }

            return data;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(campo + " inválida. Use o formato AAAA-MM-DD");
        }
    }

    public static void validarAreaPlantada(double area) {
        if (area <= 0) {
            throw new IllegalArgumentException("Área plantada deve ser maior que zero");
        }
        if (area > 1000) {
            throw new IllegalArgumentException("Área plantada muito grande (máximo: 1000 ha)");
        }
    }

    public static void validarQuantidade(double quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if (quantidade > 1000000) {
            throw new IllegalArgumentException("Quantidade muito grande (máximo: 1.000.000 kg)");
        }
    }

    public static void validarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new IllegalArgumentException("Telefone não pode ser vazio");
        }

        String telefoneLimpo = telefone.replaceAll("\\D", "");

        if (telefoneLimpo.length() < 10 || telefoneLimpo.length() > 11) {
            throw new IllegalArgumentException("Telefone deve ter 10 ou 11 dígitos");
        }
    }

    public static void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser vazio");
        }

        if (nome.trim().length() < 3) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 3 caracteres");
        }

        if (nome.trim().length() > 100) {
            throw new IllegalArgumentException("Nome muito longo (máximo: 100 caracteres)");
        }
    }

    public static void validarValorMonetario(double valor, String campo) {
        validarNaoNegativo(valor, campo);
        if (valor > 1000000) { // Limite razoável
            throw new IllegalArgumentException(campo + " muito alto (máximo: R$ 1.000.000)");
        }
    }

    public static void validarValorNaoNegativo(double valor, String campo) {
        validarNaoNegativo(valor, campo);
    }

    public static int validarInteiro(String valorStr, String campo) {
        try {
            int valor = Integer.parseInt(valorStr);
            if (valor < 0) {
                throw new IllegalArgumentException(campo + " não pode ser negativo");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " deve ser um número inteiro válido");
        }
    }

    public static double validarDecimal(String valorStr, String campo) {
        try {
            double valor = Double.parseDouble(valorStr.replace(",", "."));
            if (valor < 0) {
                throw new IllegalArgumentException(campo + " não pode ser negativo");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " deve ser um número válido");
        }
    }
}