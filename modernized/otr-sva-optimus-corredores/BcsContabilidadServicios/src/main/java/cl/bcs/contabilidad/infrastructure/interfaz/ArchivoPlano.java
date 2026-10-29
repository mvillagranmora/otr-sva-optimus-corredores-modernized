package cl.bcs.contabilidad.infrastructure.interfaz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Fixed-width text primitives shared by the interface generators (CONT-A-24..27, CONT-A-35).
 *
 * <p>Legacy: {@code String.format("%1$-Ns", value)} per column, {@code SimpleDateFormat("yyyyMMdd")} for dates and
 * {@code FileUtils.writeStringToFile(file, text, "ISO-8859-1", true)} to write. Those semantics are kept:
 * values are left-aligned and padded but never truncated, {@code null} prints as {@code null}, amounts use
 * {@link BigDecimal#toString()}, rows go out in reverse query order, files are appended to (never truncated),
 * lines end with LF and unmappable characters become {@code ?}.
 */
final class ArchivoPlano {

    /** Encoding of every interface file except B1_DES_CC_PROD.TXT. */
    static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE;
    private static final Logger log = LoggerFactory.getLogger(ArchivoPlano.class);

    private ArchivoPlano() {
    }

    /** {@code String.format("%1$-<width>s", value)}: left aligned, space padded, not truncated. */
    static String col(Object value, int width) {
        return String.format(Locale.ROOT, "%-" + width + "s", value);
    }

    /** {@code yyyyMMdd} of a SAP date column; a null date aborts the file, as the legacy NPE did. */
    static String fecha(LocalDate value, String archivo, String campo) {
        if (value == null) {
            throw new InterfazDatoInvalidoException(archivo, campo);
        }
        return value.format(YYYYMMDD);
    }

    /** Running total: {@code total.add(value)}; a null amount aborts the file, as the legacy NPE did. */
    static BigDecimal sumar(BigDecimal total, BigDecimal value, String archivo, String campo) {
        if (value == null) {
            throw new InterfazDatoInvalidoException(archivo, campo);
        }
        return total.add(value);
    }

    /** Rows in reverse query order (legacy {@code listIterator(size)} + {@code hasPrevious}). */
    static <T> List<T> invertido(List<T> rows) {
        return rows.reversed();
    }

    /** Appends {@code text} to {@code file}, creating parent directories (legacy {@code FileUtils} append). */
    static void anexar(Path file, String text, Charset charset) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, text.getBytes(charset), StandardOpenOption.CREATE, StandardOpenOption.APPEND,
                StandardOpenOption.WRITE);
    }

    /** Run-log line written before a file is built (legacy {@code "Creando Archivo: " + nombre}). */
    static String creando(String nombre) {
        return "Creando Archivo: " + nombre;
    }

    /** Run-log line after a data file is written (legacy {@code "Fin de creacion de Archivo: " + nombre}). */
    static String finDatos(String nombre) {
        return "Fin de creacion de Archivo: " + nombre;
    }

    /**
     * Run-log line after a control file is written. The legacy CTAS, MOV_CONT, SALD_CONT and CUENTAS_BPC generators
     * omitted the ": " ({@code "Fin de creacion de Archivo" + nombreArchivoCtr}, e.g. CreaBroadCuentas.java:201);
     * kept. CreaMovimientoBPC.java:247 has it and uses {@link #finDatos}.
     */
    static String finControlSinSeparador(String nombre) {
        return "Fin de creacion de Archivo" + nombre;
    }

    /**
     * Writes one file of a family and reports it to the run log. A write failure is logged and swallowed so that
     * the next file of the family is still attempted, as the legacy {@code catch (IOException)} around each write
     * did; the legacy logged {@code "ERR - " + e.getMessage()} there instead of the "Fin" line.
     */
    static void escribir(Path file, String text, Charset charset, InterfazBitacora bitacora, String lineaFin) {
        try {
            anexar(file, text, charset);
            log.info("Interfaz file written: {}", file.getFileName());
            bitacora.info(lineaFin);
        } catch (IOException e) {
            log.error("Cannot write interfaz file {}: {}", file, e.toString());
            bitacora.error(e.getMessage());
        }
    }
}
