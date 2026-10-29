package cl.bcs.utils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ConcurrentArchiver implements Closeable {

    private final ZipOutputStream zos;
    private final Object archiveMutex = new Object();

    public ConcurrentArchiver(
            final String path) throws FileNotFoundException {
        this(path, Deflater.BEST_SPEED);
    }

    public ConcurrentArchiver(
            final String path,
            int compressionLevel) throws FileNotFoundException {
        zos = new ZipOutputStream(new FileOutputStream(path));
        zos.setLevel(compressionLevel);
    }

    public void archive(
            final String name,
            final byte[] bytes)
                    throws IOException {

        synchronized (archiveMutex) {
            try (final InputStream is = new ByteArrayInputStream(bytes)) {
                final ZipEntry ze = new ZipEntry(name);
                zos.putNextEntry(ze);

                final byte[] buffer = new byte[1024];
                int len = is.read(buffer);
                while (len > 0) {
                    zos.write(buffer, 0, len);
                    len = is.read(buffer);
                }

                zos.closeEntry();
            }
        }
    }

    @Override
    public void close()
            throws IOException {
        zos.close();
    }

}
