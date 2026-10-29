package cl.bcs.contabilidad.jobs.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/** File helpers. Names are read from the directory listing, so case is exact even on Windows. */
public final class Archivos {

    private Archivos() {
    }

    /** Every regular file below {@code raiz}, as a '/'-separated relative path, sorted. Empty if raiz is missing. */
    public static List<String> arbol(Path raiz) {
        if (!Files.isDirectory(raiz)) return List.of();
        try (Stream<Path> s = Files.walk(raiz)) {
            return s.filter(Files::isRegularFile)
                    .map(p -> raiz.relativize(p).toString().replace('\\', '/'))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** File names (exact case) directly inside {@code dir}, sorted. */
    public static List<String> nombres(Path dir) {
        if (!Files.isDirectory(dir)) return List.of();
        try (Stream<Path> s = Files.list(dir)) {
            return s.map(p -> p.getFileName().toString()).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String leer(Path p) {
        try {
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("cannot read " + p + ": " + e, e);
        }
    }

    public static String leerLatin1(Path p) {
        try {
            return Files.readString(p, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            throw new AssertionError("cannot read " + p + ": " + e, e);
        }
    }

    /**
     * Lines of a legacy {@code cl.bcs.contabilidad.Log} file ("TIPO - mensaje\r\n" each, Log.java:33), without the
     * per-file lines the legacy {@code Crea*} generators also wrote ("INF - Creando Archivo: ...",
     * "INF - Fin de creacion de Archivo..."): those belong to BcsContabilidadServicios, not to the job shell.
     */
    public static List<String> lineasJob(Path log) {
        String txt = leer(log);
        if (!txt.isEmpty() && !txt.endsWith("\r\n")) {
            throw new AssertionError("legacy Log lines end with CRLF (Log.java:33): " + log);
        }
        return Arrays.stream(txt.split("\r\n", -1))
                .filter(l -> !l.isEmpty())
                .filter(l -> !l.startsWith("INF - Creando Archivo") && !l.startsWith("INF - Fin de creacion de Archivo"))
                .toList();
    }

    public static void escribir(Path p, String contenido) {
        try {
            Files.createDirectories(p.getParent());
            Files.writeString(p, contenido, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
