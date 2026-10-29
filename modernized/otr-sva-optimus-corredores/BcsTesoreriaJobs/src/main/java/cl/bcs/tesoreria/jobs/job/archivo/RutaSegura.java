package cl.bcs.tesoreria.jobs.job.archivo;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Review M1: request-controlled values that end up in a file name or path must not escape the configured base
 * directory. Failures are {@link IllegalArgumentException}s, i.e. a job error like any other failure of a run.
 */
public final class RutaSegura {

    private static final Pattern NOMBRE = Pattern.compile("^[A-Za-z0-9_.-]{1,60}$");

    private RutaSegura() {
    }

    /**
     * A value used as part of a file name: {@code ^[A-Za-z0-9_.-]{1,60}$} without a '..' sequence. Null and the
     * empty text are harmless in a name (legacy builds "...__<date>.csv" for them) and are returned unchanged.
     */
    public static String nombre(String parametro, String valor) {
        if (valor == null || valor.isEmpty()) {
            return valor;
        }
        if (!NOMBRE.matcher(valor).matches() || valor.contains("..")) {
            throw new IllegalArgumentException("Invalid value for parameter '" + parametro + "'");
        }
        return valor;
    }

    /**
     * For paths the legacy builds by plain concatenation {@code carpeta + nombre}: a {@code carpeta} that does not end
     * in a separator is not a directory prefix (legacy accepted it, e.g. "/out" + "file" = "/outfile"), so then its
     * parent directory is the limit.
     */
    public static void bajoCarpeta(String carpeta, String ruta) {
        if (carpeta == null) {
            return;
        }
        boolean separador = carpeta.endsWith("/") || carpeta.endsWith("\\");
        if (separador) {
            bajoBase(carpeta, ruta);
            return;
        }
        try {
            Path padre = Path.of(carpeta).toAbsolutePath().normalize().getParent();
            bajoBase(padre == null ? carpeta : padre.toString(), ruta);
        } catch (InvalidPathException e) {
            // not a usable path: opening it fails as it did in the legacy
        }
    }

    /** {@code ruta} (a file or folder) must stay under {@code base} after normalization. */
    public static void bajoBase(String base, String ruta) {
        if (base == null || ruta == null) {
            return;
        }
        Path destino;
        Path raiz;
        try {
            raiz = Path.of(base).toAbsolutePath().normalize();
            destino = Path.of(ruta).toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            return; // not a usable path: opening it fails as it did in the legacy
        }
        if (!destino.startsWith(raiz)) {
            throw new IllegalArgumentException("The output path is outside the configured base directory");
        }
    }
}
