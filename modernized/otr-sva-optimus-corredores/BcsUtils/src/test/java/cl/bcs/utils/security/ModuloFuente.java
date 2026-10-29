package cl.bcs.utils.security;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The production side of THIS module, as files: {@code src/main/java}, {@code src/main/resources} and the compiled
 * {@code target/classes}. Every accessor fails (never returns an empty list) when the tree is missing or does not hold
 * the target API classes, so a scan can never pass vacuously.
 */
final class ModuloFuente {

    /** The target API of the pure library (see src/test/java/README.md). */
    static final List<String> CLASES_OBLIGATORIAS = List.of(
            "cl/bcs/utils/RutUtil", "cl/bcs/utils/StringUtils", "cl/bcs/utils/Utils", "cl/bcs/utils/DateUtil",
            "cl/bcs/utils/Fecha", "cl/bcs/utils/Timer", "cl/bcs/utils/FileUtils", "cl/bcs/utils/ZipUtil",
            "cl/bcs/utils/JavaCall", // JsonWebToken: not ported (decision 5, 2026-10-16)
            "cl/bcs/utils/clave/PoliticaClave", "cl/bcs/utils/clave/ValidadorClave",
            "cl/bcs/utils/sesion/TokenSesion", "cl/bcs/utils/sesion/PoliticaInactividad", "cl/bcs/utils/sesion/RelojUtcCacheado",
            "cl/bcs/utils/security/HybridPasswordEncoder", "cl/bcs/utils/report/CrystalReportsRestClient");

    private ModuloFuente() {
    }

    static Path base() {
        return Path.of(System.getProperty("basedir", System.getProperty("user.dir"))).toAbsolutePath().normalize();
    }

    static List<Path> fuentesJava() {
        List<Path> l = listar(base().resolve("src/main/java"), ".java");
        for (String c : CLASES_OBLIGATORIAS) {
            assertThat(base().resolve("src/main/java/" + c + ".java")).as("scan must cover the target API sources").exists();
        }
        return l;
    }

    static List<Path> recursos() {
        Path r = base().resolve("src/main/resources");
        if (!Files.isDirectory(r)) return List.of();
        try (Stream<Path> s = Files.walk(r)) {
            return s.filter(Files::isRegularFile).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static List<Path> clasesCompiladas() {
        List<Path> l = listar(base().resolve("target/classes"), ".class");
        for (String c : CLASES_OBLIGATORIAS) {
            assertThat(base().resolve("target/classes/" + c + ".class")).as("scan must cover the compiled target API classes").exists();
        }
        return l;
    }

    static List<Path> goldens() {
        return listar(base().resolve("src/test/resources/golden"), ".json");
    }

    static String nombreClase(Path clase) {
        String rel = base().resolve("target/classes").relativize(clase).toString().replace('\\', '/');
        return rel.substring(0, rel.length() - ".class".length()).replace('/', '.');
    }

    static String texto(Path p) {
        try {
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            try {
                return Files.readString(p, StandardCharsets.ISO_8859_1);
            } catch (IOException e2) {
                throw new UncheckedIOException(e2);
            }
        }
    }

    static byte[] bytes(Path p) {
        try {
            return Files.readAllBytes(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static boolean contiene(byte[] datos, byte[] patron) {
        outer:
        for (int i = 0; i <= datos.length - patron.length; i++) {
            for (int j = 0; j < patron.length; j++) if (datos[i + j] != patron[j]) continue outer;
            return true;
        }
        return false;
    }

    private static List<Path> listar(Path raiz, String sufijo) {
        if (!Files.isDirectory(raiz)) {
            throw new AssertionError(raiz + " does not exist: nothing to scan is a FAILURE, not a pass");
        }
        try (Stream<Path> s = Files.walk(raiz)) {
            List<Path> l = s.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(sufijo)).sorted().toList();
            if (l.isEmpty()) throw new AssertionError("no " + sufijo + " file under " + raiz);
            return l;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
