package cl.bcs.utils.equivalencia;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static cl.bcs.utils.equivalencia.Golden.map;

/** File fixtures and zip rendering, identical to the legacy oracle's FileOracle helpers. */
public final class Archivos {

    public static byte[] contenido(String nombre) {
        switch (nombre) {
            case "vacio": return new byte[0];
            case "hola": return "hola".getBytes(StandardCharsets.UTF_8);
            case "utf8": return "ñandú – 😀\n".getBytes(StandardCharsets.UTF_8);
            case "bytes256": {
                byte[] b = new byte[256];
                for (int i = 0; i < 256; i++) b[i] = (byte) i;
                return b;
            }
            case "1MiB": {
                byte[] b = new byte[1 << 20];
                for (int i = 0; i < b.length; i++) b[i] = (byte) (i * 31 + (i >> 8));
                return b;
            }
            case "1025": {
                byte[] b = new byte[1025];
                Arrays.fill(b, (byte) 'z');
                return b;
            }
            case "rar5": return new byte[]{'R', 'a', 'r', '!', 0x1A, 0x07, 0x01, 0x00};
            case "rar4": return new byte[]{'R', 'a', 'r', '!', 0x1A, 0x07, 0x00, (byte) 0xCF};
            case "rarTexto": return "Rar!".getBytes(StandardCharsets.US_ASCII);
            case "rarMinuscula": return "rar!xyz".getBytes(StandardCharsets.US_ASCII);
            case "rarCorto": return "Rar".getBytes(StandardCharsets.US_ASCII);
            case "rarSegundaLinea": return "\nRar!".getBytes(StandardCharsets.US_ASCII);
            case "rarConEspacio": return " Rar!".getBytes(StandardCharsets.US_ASCII);
            case "zip": return new byte[]{'P', 'K', 3, 4, 0, 0};
            case "bomRar": return new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'R', 'a', 'r', '!'};
            default: throw new IllegalArgumentException(nombre);
        }
    }

    public static Path escribir(Path d, String rel, String nombreContenido) throws Exception {
        Path p = d.resolve(rel);
        Files.createDirectories(p.getParent());
        Files.write(p, contenido(nombreContenido));
        return p;
    }

    public static String sha256(byte[] b) throws Exception {
        StringBuilder s = new StringBuilder();
        for (byte x : MessageDigest.getInstance("SHA-256").digest(b)) s.append(String.format("%02x", x));
        return s.toString();
    }

    /** Entries of a zip: name, directory flag, size, content sha256, method. Entry times are never compared. */
    public static Object entradas(Path zip) throws Exception {
        if (!Files.exists(zip)) return "NO_EXISTE";
        if (Files.size(zip) == 0) return map("bytes", 0L);
        List<Object> l = new ArrayList<>();
        try (ZipFile z = new ZipFile(zip.toFile(), StandardCharsets.UTF_8)) {
            Enumeration<? extends ZipEntry> en = z.entries();
            while (en.hasMoreElements()) {
                ZipEntry e = en.nextElement();
                byte[] b;
                try (InputStream is = z.getInputStream(e)) {
                    b = is.readAllBytes();
                }
                l.add(map("name", e.getName(), "dir", e.isDirectory(), "size", (long) b.length, "sha256", sha256(b), "method", e.getMethod()));
            }
            return map("entries", l, "comment", z.getComment());
        } catch (java.util.zip.ZipException ze) {
            return map("bytes", Files.size(zip), "zipException", true);
        }
    }

    public static String rel(Path d, File f) {
        String a = d.toFile().getAbsolutePath();
        String b = f.getAbsolutePath();
        return b.startsWith(a) ? b.substring(a.length()).replace(File.separatorChar, '/') : "OUTSIDE:" + f.getName();
    }

    private Archivos() {
    }
}
