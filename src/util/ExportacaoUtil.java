package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportacaoUtil {
    public static void exportarLinhasParaTxt(String caminho, List<String> linhas) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminho))) {
            for (String linha : linhas) {
                bw.write(linha);
                bw.newLine();
            }
        }
    }
}