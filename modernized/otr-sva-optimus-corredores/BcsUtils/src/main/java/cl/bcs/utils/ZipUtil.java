package cl.bcs.utils;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * UTIL-53..57: zip helpers (legacy {@code ZipUtil.java:35-154}).
 *
 * <p>Files are opened with {@code java.io} streams on purpose: callers (and the golden records) see
 * {@link java.io.FileNotFoundException} for a missing file, a missing parent folder or a directory.
 */
public final class ZipUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ZipUtil.class);
    private static final String MISMA_RUTA = "La ruta del archivo de salida no puede ser igual a la ruta del archivo de entrada";

    private ZipUtil() {
    }

    /**
     * UTIL-53: zips one file into {@code outputPath} (entry = the file name). Input and output paths equal ignoring case
     * throw {@link BadFilePathException} before anything is opened; a missing input creates no output.
     */
    public static void generateZip(String inputPath, String outputPath) throws IOException {
        if (inputPath.equalsIgnoreCase(outputPath)) {
            throw new BadFilePathException(MISMA_RUTA);
        }
        try (InputStream in = new FileInputStream(inputPath);
             ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(outputPath))) {
            agregar(zip, FilenameUtils.getName(inputPath), in);
        }
    }

    /**
     * UTIL-54: zips several files into {@code outputPath}. Legacy behavior kept as pinned: EVERY error (missing file,
     * duplicate entry name, an input equal to the output, a {@code null} list, an unwritable output) is swallowed and
     * reported as {@code false} (logged at WARN instead of the legacy printStackTrace), keeping the entries written so
     * far. {@code throws IOException} is kept only for source compatibility with legacy callers.
     */
    public static boolean generateMultipleZip(List<String> inputPaths, String outputPath) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(outputPath))) {
            for (String inputPath : inputPaths) {
                try (InputStream in = new FileInputStream(inputPath)) {
                    if (inputPath.equalsIgnoreCase(outputPath)) {
                        throw new BadFilePathException(MISMA_RUTA);
                    }
                    agregar(zip, FilenameUtils.getName(inputPath), in);
                }
            }
            return true;
        } catch (Exception e) {
            LOG.warn("generateMultipleZip({}) failed; the zip keeps the entries written so far", outputPath, e);
            return false;
        }
    }

    /**
     * UTIL-56 (APPROVED DEVIATION, 2026-10-16): the first entry that is not a directory, or {@code null}. The legacy
     * re-created the enumeration on every loop turn (ZipUtil.java:118-119) and never returned when the first entry was
     * a directory.
     */
    public static ZipEntry getFirstFileEntry(ZipFile zipFile) {
        return zipFile.stream()
                .filter(e -> !e.isDirectory())
                .findFirst()
                .orElse(null);
    }

    /** UTIL-57: the first text line (UTF-8, lenient decoding) starts with {@code "Rar!"}; case-sensitive, no BOM skipping. */
    public static boolean isRar(String filepath) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8))) {
            String linea = br.readLine();
            return linea != null && linea.startsWith("Rar!");
        }
    }

    static void agregar(ZipOutputStream zip, String nombre, InputStream in) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));
        in.transferTo(zip);
        zip.closeEntry();
    }
}
