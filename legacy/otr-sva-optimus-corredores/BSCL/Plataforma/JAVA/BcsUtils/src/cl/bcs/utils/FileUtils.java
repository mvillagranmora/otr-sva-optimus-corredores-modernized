package cl.bcs.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.codec.binary.Base64;

public final class FileUtils {

    public static File createFolder(
            final File root,
            final String folderName) {
        final String pathname = root.getAbsolutePath() + File.separator + folderName;
        final File folder = new File(pathname);
        folder.mkdirs();
        return folder;
    }

    public static String encodeBase64String(
            final File file)
                    throws IOException {
        final byte[] bytes = Files.readAllBytes(file.toPath());
        return Base64.encodeBase64String(bytes);
    }

    private FileUtils() {
    }
}
