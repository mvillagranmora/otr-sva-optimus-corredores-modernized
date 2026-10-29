package cl.bcs.reports.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * The single place that knows where the sidecar writes its temporary files (uploaded/read templates "*.rpt" and
 * resized pictures "*.png"). The legacy used the relative path "tmp/" (resolved against whatever the working directory
 * was); here it is absolute: system property {@code bcs.reports.tmpdir} or, by default, {@code <user.dir>/tmp}
 * (in the container /app/tmp, which compose mounts as a size-limited tmpfs).
 */
public final class TempFiles {

    private static final Logger log = LoggerFactory.getLogger(TempFiles.class);

    private TempFiles() {
    }

    public static File dir() {
        String configured = System.getProperty("bcs.reports.tmpdir");
        File d = configured != null && !configured.trim().isEmpty()
                ? new File(configured.trim())
                : new File(System.getProperty("user.dir"), "tmp");
        d = d.getAbsoluteFile();
        d.mkdirs();
        return d;
    }

    /** A new, not yet existing file name in the temp dir, e.g. {@code <dir>/<uuid>.rpt}. */
    public static File newFile(String extension) {
        return new File(dir(), java.util.UUID.randomUUID() + "." + extension);
    }

    /** Deletes the file if it exists; never throws. */
    public static void deleteQuietly(File f) {
        if (f == null) {
            return;
        }
        try {
            if (f.exists() && !f.delete()) {
                log.warn("could not delete temp file {}", f.getName());
            }
        } catch (RuntimeException e) {
            log.warn("could not delete temp file {}: {}", f.getName(), e.toString());
        }
    }

    /** Startup sweep: removes *.rpt and *.png older than {@code maxAgeMillis} left by a crashed/killed process. */
    public static int sweep(long maxAgeMillis) {
        File[] files = dir().listFiles((d, n) -> n.endsWith(".rpt") || n.endsWith(".png"));
        int removed = 0;
        if (files == null) {
            return 0;
        }
        long limit = System.currentTimeMillis() - maxAgeMillis;
        for (File f : files) {
            if (f.isFile() && f.lastModified() < limit) {
                if (f.delete()) {
                    removed++;
                }
            }
        }
        if (removed > 0) {
            log.info("temp sweep: removed {} stale file(s) from {}", removed, dir());
        }
        return removed;
    }
}
