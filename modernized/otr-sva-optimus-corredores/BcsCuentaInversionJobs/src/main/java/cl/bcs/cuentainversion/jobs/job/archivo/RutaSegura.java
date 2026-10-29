package cl.bcs.cuentainversion.jobs.job.archivo;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Review M-2 (step 5): a generated file must stay under its configured base folder. Failures are {@link IllegalArgumentException}s, i.e.
 * a job error like any other failure of a run. Same LENIENT rule as the one of BcsTesoreriaJobs (own copy, not shared): the legacy builds
 * the path by plain concatenation {@code carpeta + nombre}, so a folder that does not end in a separator is not a directory prefix
 * ("/out" + "file" = "/outfile") and its parent is the limit; a path that cannot be parsed is not judged here (opening it fails as in the legacy).
 */
public final class RutaSegura {

    private RutaSegura() {
    }

    /** The NAME part of a file: no path separators and no '..' (the text itself is otherwise free: legacy allows spaces and accents). */
    public static String nombre(String nombre) {
        if (nombre != null && (nombre.contains("/") || nombre.contains("\\") || nombre.contains(".."))) {
            throw new IllegalArgumentException("The output file name must not contain path separators or '..'");
        }
        return nombre;
    }

    /** {@code ruta = carpeta + nombre} must stay under {@code carpeta} (or under its parent when it does not end in a separator). */
    public static void bajoCarpeta(String carpeta, String ruta) {
        if (carpeta == null) {
            return;
        }
        if (carpeta.endsWith("/") || carpeta.endsWith("\\")) {
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

    /** {@code ruta} (a file or a folder) must stay under {@code base} after normalization. */
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
            return;
        }
        if (!destino.startsWith(raiz)) {
            throw new IllegalArgumentException("The output path is outside the configured base directory");
        }
    }
}
