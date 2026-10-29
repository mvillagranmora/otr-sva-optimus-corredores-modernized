package cl.bcs.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

/**
 * UTIL-50 / UTIL-51: file helpers (legacy {@code FileUtils.java:11-25}).
 */
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * UTIL-50: creates (with parents) {@code <root absolute path>/<folderName>} and returns it; best effort, as in the
     * legacy: a failure leaves a non-existing {@link File}. The name is not normalised ({@code ".."} stays) and a
     * {@code null} name creates a folder literally named {@code "null"}.
     */
    public static File createFolder(File root, String folderName) {
        File folder = new File(root.getAbsolutePath() + File.separator + folderName);
        folder.mkdirs();
        return folder;
    }

    /** UTIL-51: the file content as standard, unchunked Base64; a missing file throws {@link java.nio.file.NoSuchFileException}. */
    public static String encodeBase64String(File file) throws IOException {
        return Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
    }
}
