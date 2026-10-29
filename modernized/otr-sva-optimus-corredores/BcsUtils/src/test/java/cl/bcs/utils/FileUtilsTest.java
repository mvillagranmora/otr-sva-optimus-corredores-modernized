package cl.bcs.utils;

import cl.bcs.utils.equivalencia.Golden;
import cl.bcs.utils.equivalencia.Golden.Caso;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static cl.bcs.utils.equivalencia.Archivos.escribir;
import static cl.bcs.utils.equivalencia.Archivos.rel;
import static cl.bcs.utils.equivalencia.Archivos.sha256;
import static cl.bcs.utils.equivalencia.Golden.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure file operations in temp dirs: UTIL-50 FileUtils.createFolder (legacy FileUtils.java:11-18), UTIL-51
 * FileUtils.encodeBase64String (:20-25, commons-codec unchunked Base64), UTIL-52 Utils.touch (Utils.java:1088-1093).
 * Golden: golden/any/files.json.
 */
class FileUtilsTest {

    static final String AREA = "files";
    @TempDir
    static Path base;
    static final AtomicInteger SEQ = new AtomicInteger();

    static Path dir() throws Exception {
        return Files.createDirectories(base.resolve("c" + SEQ.incrementAndGet()));
    }

    static Object carpeta(Path d, File f) {
        return map("relPath", rel(d, f), "name", f.getName(), "exists", f.exists(), "isDirectory", f.isDirectory());
    }

    Callable<Object> moderno(Caso c) {
        String fn = c.s("fn");
        return switch (fn) {
            case "createFolder" -> () -> {
                String root = c.s("root");
                if (root == null) return FileUtils.createFolder(null, c.s("folderName"));
                Path d = dir();
                return switch (root) {
                    case "<tmp>" -> {
                        if (c.es("folderName", "ya (exists)")) {
                            Files.createDirectories(d.resolve("ya"));
                            File f = FileUtils.createFolder(d.toFile(), "ya");
                            yield map("relPath", rel(d, f), "exists", f.exists(), "isDirectory", f.isDirectory());
                        }
                        yield carpeta(d, FileUtils.createFolder(d.toFile(), c.s("folderName")));
                    }
                    case "<tmp>/no/existe/aun" -> {
                        File f = FileUtils.createFolder(d.resolve("no/existe/aun").toFile(), "hija");
                        yield map("relPath", rel(d, f), "exists", f.exists(), "isDirectory", f.isDirectory());
                    }
                    case "<tmp>/soy-archivo (a regular file)" -> {
                        File f = FileUtils.createFolder(escribir(d, "soy-archivo", "hola").toFile(), "hija");
                        yield map("relPath", rel(d, f), "exists", f.exists(), "isDirectory", f.isDirectory());
                    }
                    default -> throw new AssertionError(root);
                };
            };
            case "encodeBase64String" -> () -> {
                String cont = c.s("content");
                if (cont == null) return FileUtils.encodeBase64String(null);
                if (cont.equals("missing file")) return FileUtils.encodeBase64String(dir().resolve("no-existe.bin").toFile());
                String b64 = FileUtils.encodeBase64String(escribir(dir(), "f.bin", cont).toFile());
                boolean salto = b64.contains("\n") || b64.contains("\r");
                return b64.length() > 400 ? map("length", b64.length(), "sha256", sha256(b64.getBytes(StandardCharsets.US_ASCII)), "head", b64.substring(0, 76), "hasLineBreak", salto)
                        : map("value", b64, "hasLineBreak", salto);
            };
            case "touch" -> () -> {
                Path d = dir();
                long ts = c.l("timestamp");
                switch (c.s("file")) {
                    case "new":
                        if (ts < 0) {
                            File f = d.resolve("neg.txt").toFile();
                            Utils.touch(f, ts);
                            return map("exists", f.exists());
                        }
                        File f = d.resolve("nuevo.txt").toFile();
                        Utils.touch(f, ts);
                        return map("exists", f.exists(), "length", f.length(), "lastModified", f.lastModified());
                    case "existing(hola)":
                        File e = escribir(d, "existe.txt", "hola").toFile();
                        Utils.touch(e, ts);
                        return map("exists", e.exists(), "content", Files.readString(e.toPath()), "lastModified", e.lastModified());
                    case "missing parent dir":
                        Utils.touch(d.resolve("sin/padre.txt").toFile(), ts);
                        return "no exception";
                    default:
                        throw new AssertionError(c.s("file"));
                }
            };
            default -> throw new AssertionError("unmapped golden fn " + fn + " in " + c.id());
        };
    }

    @TestFactory
    @DisplayName("UTIL-50..52 equivalence: FileUtils / Utils.touch == legacy (temp dirs)")
    Stream<DynamicTest> util50a52_equivalencia() {
        // createFolder(root, ""): the result IS the case temp dir, whose name is a test artifact -> not compared
        return Golden.casos("any", AREA).stream().map(c -> DynamicTest.dynamicTest(c.nombre(), () -> {
            if (c.es("folderName", "")) Golden.igualNormalizado(AREA, c, o -> o.getAsJsonObject("value").remove("name"), moderno(c));
            else Golden.igual(AREA, c, moderno(c));
        }));
    }

    @AfterAll
    static void reporte() {
        System.out.println("equivalence cases executed [" + AREA + "]: " + Golden.ejecutados(AREA));
    }

    @Test
    @DisplayName("UTIL-50 legacy quirk pinned: createFolder(root, null) creates a folder literally named \"null\"; \"..\" is not normalised")
    void util50_nombreNulo() throws Exception {
        Path d = dir();
        assertThat(FileUtils.createFolder(d.toFile(), null)).isDirectory().hasName("null");
        assertThat(FileUtils.createFolder(d.toFile(), "..").getPath()).endsWith("..");
    }

    @Test
    @DisplayName("UTIL-51: Base64 output is unchunked (no line breaks) and a missing file raises NoSuchFileException")
    void util51_base64() throws Exception {
        Path d = dir();
        assertThat(FileUtils.encodeBase64String(escribir(d, "h", "hola").toFile())).isEqualTo("aG9sYQ==");
        assertThatThrownBy(() -> FileUtils.encodeBase64String(d.resolve("x").toFile())).isInstanceOf(java.nio.file.NoSuchFileException.class);
    }
}
