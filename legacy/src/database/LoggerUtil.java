package database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {
    private static final String LOG_FILE = "logs/sistema.log";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        new File("logs").mkdirs();
    }

    public static synchronized void logInfo(String mensagem) {
        log("INFO", mensagem);
    }

    public static synchronized void logErro(String mensagem) {
        log("ERRO", mensagem);
    }

    public static synchronized void logOperacao(String operacao, String detalhes) {
        log("OPERACAO", operacao + " - " + detalhes);
    }

    public static synchronized void logExclusao(String entidade, int id, String motivo) {
        log("EXCLUSAO", entidade + " ID: " + id + " - Motivo: " + motivo);
    }

    private static void log(String nivel, String mensagem) {
        String timestamp = LocalDateTime.now().format(dtf);
        String logEntry = String.format("[%s] [%s] %s", timestamp, nivel, mensagem);

        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(logEntry);
            System.out.println(logEntry);
        } catch (IOException e) {
            System.err.println("Falha ao escrever no log: " + e.getMessage());
        }
    }

    public static void visualizarLogs() {
        System.out.println("\n=== ULTIMOS LOGS DO SISTEMA ===");
        try {
            java.nio.file.Files.lines(java.nio.file.Paths.get(LOG_FILE))
                    .skip(Math.max(0, java.nio.file.Files.lines(java.nio.file.Paths.get(LOG_FILE)).count() - 20))
                    .forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Nenhum log disponível.");
        }
    }
}