package cl.bcs.tesoreria.familia_b.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

/**
 * File-capture helpers shared by the legacy oracle runner and the modern tests (the legacy generator
 * CargaMasivaIngresoEgresoDAPFFMM writes a csv under {@code PLATAFORMA/PLA.RutaBaseArchivos + TESORERIA/TES.RutaSalidaArchivosCargaMasiva}).
 *
 * <ul>
 *   <li>{@code ${RUTA_BASE}} in a case seed/entrada is replaced by a fresh temporary directory (with a pre-created {@code CARGA/} sub
 *       directory) so the code under test writes where the test can read.</li>
 *   <li>File names carry the wall-clock timestamp of the run: {@link #TIMESTAMP} ({@code \d{8}_\d{6}}, yyyyMMdd_HHmmss) is replaced by
 *       {@code <TS>} in every string compared, in the captured file names and in the recorded golden. Nothing else is masked.</li>
 *   <li>Captured contents have line endings normalised to {@code \n} (the legacy uses {@code BufferedWriter.newLine()}, platform
 *       dependent) and are read as UTF-8.</li>
 * </ul>
 */
public final class Archivos {

    /** Documented masking regex for timestamp-bearing file names: yyyyMMdd_HHmmss -> {@code <TS>}. */
    public static final Pattern TIMESTAMP = Pattern.compile("\\d{8}_\\d{6}");
    public static final String PLACEHOLDER = "${RUTA_BASE}";

    private Archivos() {
    }

    public static String mascara(String s) {
        return s == null ? null : TIMESTAMP.matcher(s).replaceAll("<TS>");
    }

    /** Creates the per-case base directory (with the CARGA/ sub directory the configured TES.RutaSalidaArchivosCargaMasiva points to). */
    public static Path nuevaRaiz() {
        try {
            Path base = Files.createTempDirectory("tesb-archivos-");
            Files.createDirectories(base.resolve("CARGA"));
            return base;
        } catch (IOException e) {
            throw new IllegalStateException("cannot create the file-capture directory", e);
        }
    }

    /** Text with ${RUTA_BASE} replaced by the directory path (forward slashes, trailing slash). */
    public static String sustituir(String texto, Path base) {
        String ruta = base.toAbsolutePath().toString().replace('\\', '/') + "/";
        return texto.replace(PLACEHOLDER + "/", ruta).replace(PLACEHOLDER, ruta);
    }

    /** Every regular file under the base directory: {"<relative path, masked>": "<content, \n line endings>"}; deletes the directory. */
    public static JsonObject capturar(Path base) {
        JsonObject out = new JsonObject();
        try (Stream<Path> s = Files.walk(base)) {
            for (Path p : s.filter(Files::isRegularFile).sorted().toList()) {
                String rel = base.relativize(p).toString().replace('\\', '/');
                String contenido = new String(Files.readAllBytes(p), StandardCharsets.UTF_8).replace("\r\n", "\n");
                out.addProperty(mascara(rel), contenido);
            }
        } catch (IOException e) {
            throw new IllegalStateException("cannot read captured files", e);
        }
        borrar(base);
        return out;
    }

    public static void borrar(Path base) {
        try (Stream<Path> s = Files.walk(base)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException e) {
            // best effort
        }
    }
}
