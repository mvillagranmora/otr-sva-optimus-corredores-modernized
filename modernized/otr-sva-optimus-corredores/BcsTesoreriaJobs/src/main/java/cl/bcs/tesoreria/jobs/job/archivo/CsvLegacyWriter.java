package cl.bcs.tesoreria.jobs.job.archivo;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * CSV writer with the observable behavior of opencsv 3.7, the version the legacy runtime shipped (legacy JOBMAN.gradle:
 * {@code libs/opencsv-3.7.jar}). The parent pom carries opencsv 5.9, which removed the constructors the legacy used and
 * changed the output (it ESCAPES the separator and line breaks when there is no quote character, and {@code close()} throws
 * on a closed writer), so this small class reproduces 3.7 for the cases the legacy uses: {@code new CSVWriter(w, ';')},
 * {@code new CSVWriter(w, ';', NO_QUOTE_CHARACTER)}, {@code writeNext(String[])}, {@code close()}. Reproduced from the 3.7
 * source, NOT run against the real jar (no network); the goldens CM-41, CG-02, CG-35, PG-05 and PG-40 pin it.
 *
 * <ul>
 *   <li>{@code writeNext(String[])} quotes every field (applyQuotesToAll); a field that contains the quote character, the
 *       escape character or a line break has them doubled; a field that only contains the separator is NOT special;</li>
 *   <li>a {@code null} element writes nothing (the separators stay); lines end with {@code "\n"};</li>
 *   <li>writes go through a {@link PrintWriter}, which swallows {@link IOException}: {@code close()} after the underlying
 *       writer was closed does not throw.</li>
 * </ul>
 */
public final class CsvLegacyWriter implements Closeable {

    public static final char ESCAPE_POR_DEFECTO = '"';
    public static final char COMILLA_POR_DEFECTO = '"';
    public static final char SIN_COMILLA = '\u0000';
    private static final String FIN_DE_LINEA = "\n";

    private final Writer escritor;
    private final PrintWriter pw;
    private final char separador;
    private final char comilla;
    private final char escape;

    public CsvLegacyWriter(Writer escritor, char separador) {
        this(escritor, separador, COMILLA_POR_DEFECTO);
    }

    public CsvLegacyWriter(Writer escritor, char separador, char comilla) {
        this.escritor = escritor;
        this.pw = new PrintWriter(escritor);
        this.separador = separador;
        this.comilla = comilla;
        this.escape = ESCAPE_POR_DEFECTO;
    }

    /** {@code writeNext(String[])}: every field is quoted. */
    public void escribirLinea(String[] campos) {
        escribirLinea(campos, true);
    }

    /** {@code writeNext(String[], applyQuotesToAll)}: with {@code false} only the special fields are quoted. */
    public void escribirLinea(String[] campos, boolean comillasATodos) {
        if (campos == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i != 0) {
                sb.append(separador);
            }
            String campo = campos[i];
            if (campo == null) {
                continue;
            }
            boolean especial = campo.indexOf(comilla) != -1 || campo.indexOf(escape) != -1
                    || campo.contains("\n") || campo.contains("\r");
            boolean entrecomillar = (comillasATodos || especial) && comilla != SIN_COMILLA;
            if (entrecomillar) {
                sb.append(comilla);
            }
            if (especial) {
                for (int j = 0; j < campo.length(); j++) {
                    char c = campo.charAt(j);
                    if (c == comilla || c == escape) {
                        sb.append(escape);
                    }
                    sb.append(c);
                }
            } else {
                sb.append(campo);
            }
            if (entrecomillar) {
                sb.append(comilla);
            }
        }
        sb.append(FIN_DE_LINEA);
        pw.write(sb.toString());
    }

    public void flush() {
        pw.flush();
    }

    @Override
    public void close() throws IOException {
        pw.flush();
        pw.close();
        escritor.close();
    }
}
