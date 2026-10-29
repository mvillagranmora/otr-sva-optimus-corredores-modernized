package cl.bcs.tesoreria.jobs.support;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * File-capture helpers shared by the legacy oracle runner and the modern tests. The jobs write real files below a per-case
 * temporary directory; {@code ${RUTA_BASE}} in a case (config, parameters, collaborator scripts, seed) is replaced by that
 * directory. A capture lists every file as {@code {bytes, sha256, texto}} (byte-exact: the sha256 and the length are
 * compared, the escaped text is what a human reads in a diff) and every directory.
 *
 * <p>Masks (documented in the README): a wall-clock file name part {@code yyyyMMdd_HHmmss} ({@link #TIMESTAMP}) becomes
 * {@code <TS>} in names AND contents; the temporary base path becomes {@code ${RUTA_BASE}} in error messages.
 */
public final class Archivos {

    public static final Pattern TIMESTAMP = Pattern.compile("\\d{8}_\\d{6}");
    public static final String PLACEHOLDER = "${RUTA_BASE}";

    private Archivos() {
    }

    public static String mascara(String s) {
        return s == null ? null : TIMESTAMP.matcher(s).replaceAll("<TS>");
    }

    /** Creates the per-case base directory with the given sub directories. */
    public static Path nuevaRaiz(Iterable<String> subdirectorios) {
        try {
            Path base = Files.createTempDirectory("tesjobs-");
            for (String d : subdirectorios) {
                Files.createDirectories(base.resolve(d));
            }
            return base;
        } catch (IOException e) {
            throw new IllegalStateException("cannot create the file-capture directory", e);
        }
    }

    /** Text with ${RUTA_BASE} replaced by the directory path (forward slashes, no trailing slash). */
    public static String sustituir(String texto, Path base) {
        return texto.replace(PLACEHOLDER, base.toAbsolutePath().toString().replace('\\', '/'));
    }

    /** The reverse, for messages: the base path (both slash forms) becomes ${RUTA_BASE}. */
    public static String enmascararRuta(String s, Path base) {
        if (s == null || base == null) {
            return s;
        }
        String abs = base.toAbsolutePath().toString();
        return s.replace(abs, PLACEHOLDER).replace(abs.replace('\\', '/'), PLACEHOLDER).replace(PLACEHOLDER + "\\", PLACEHOLDER + "/");
    }

    /** {"archivos": {"<rel path>": {bytes, sha256, texto}}, "directorios": ["<rel dir>"]}; deletes the directory. */
    public static JsonObject capturar(Path base) {
        JsonObject files = new JsonObject();
        JsonArray dirs = new JsonArray();
        try (Stream<Path> s = Files.walk(base)) {
            for (Path p : s.sorted().toList()) {
                if (p.equals(base)) {
                    continue;
                }
                String rel = base.relativize(p).toString().replace('\\', '/');
                if (Files.isDirectory(p)) {
                    dirs.add(mascara(rel));
                } else {
                    byte[] b = Files.readAllBytes(p);
                    JsonObject f = new JsonObject();
                    f.addProperty("bytes", b.length);
                    f.addProperty("sha256", sha256(mascaraBytes(b)));
                    f.addProperty("texto", escapar(mascara(texto(b))));
                    files.add(mascara(rel), f);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("cannot read captured files", e);
        }
        borrar(base);
        JsonObject out = new JsonObject();
        out.add("archivos", files);
        out.add("directorios", dirs);
        return out;
    }

    /** the sha256 is taken after the timestamp mask so a wall-clock part of the content does not change it (same length, same mask). */
    private static byte[] mascaraBytes(byte[] b) {
        return mascara(new String(b, StandardCharsets.ISO_8859_1)).getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String sha256(byte[] b) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(b);
            StringBuilder sb = new StringBuilder();
            for (byte x : d) {
                sb.append(String.format("%02x", x));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String texto(byte[] b) {
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(b)).toString();
        } catch (CharacterCodingException e) {
            return new String(b, StandardCharsets.ISO_8859_1);
        }
    }

    /** Control characters and the BOM are made visible ({@code \r \n \t \xHH ﻿}); a backslash is doubled. */
    static String escapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else if (c < 0x20 || c == 0x7f) {
                sb.append(String.format("\\x%02X", (int) c));
            } else if (c == '﻿') {
                sb.append("\\uFEFF");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static void borrar(Path base) {
        try (Stream<Path> s = Files.walk(base)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException e) {
            // best effort
        }
    }
}
