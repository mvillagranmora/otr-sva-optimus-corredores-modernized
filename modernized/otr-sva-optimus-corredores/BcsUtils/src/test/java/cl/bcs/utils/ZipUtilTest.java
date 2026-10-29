package cl.bcs.utils;

import cl.bcs.utils.equivalencia.Golden;
import cl.bcs.utils.equivalencia.Golden.Caso;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static cl.bcs.utils.equivalencia.Archivos.entradas;
import static cl.bcs.utils.equivalencia.Archivos.escribir;
import static cl.bcs.utils.equivalencia.Golden.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * ZipUtil (legacy ZipUtil.java:35-154) and Utils.fileToZipFile (Utils.java:1040-1056), in temp dirs.
 * UTIL-53 generateZip, UTIL-54 generateMultipleZip, UTIL-55 fileToZipFile, UTIL-56 getFirstFileEntry, UTIL-57 isRar.
 * Zips are compared by entries (name, dir flag, size, sha256, method); entry times are wall clock and not compared.
 */
class ZipUtilTest {

    static final String AREA = "zip";
    /**
     * UTIL-56: the legacy never returns when the FIRST entry is a directory (it calls zipFile.entries() anew on every
     * loop turn, ZipUtil.java:118-119). APPROVED deviation (decision 4, 2026-10-16): return the first non-directory
     * entry (the javadoc's stated intent). Asserted in {@link #util56_desviacionAprobada()}, not as equivalence.
     */
    static final Set<String> DESVIACION_APROBADA = Set.of("ZIP-027");

    @TempDir
    static Path base;
    static final AtomicInteger SEQ = new AtomicInteger();

    static Path dir() throws Exception {
        return Files.createDirectories(base.resolve("c" + SEQ.incrementAndGet()));
    }

    static List<String> lista(JsonElement e) {
        List<String> l = new ArrayList<>();
        for (JsonElement x : e.getAsJsonArray()) l.add(x.getAsString());
        return l;
    }

    static Path zipCon(Path d, List<String> entradas) throws Exception {
        Path zp = d.resolve("z.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zp.toFile()))) {
            for (String s : entradas) {
                zos.putNextEntry(new ZipEntry(s.substring(2)));
                if (s.startsWith("f:")) zos.write("x".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        return zp;
    }

    static String primera(ZipFile zf) throws Exception {
        AtomicReference<Object> r = new AtomicReference<>("TIMEOUT");
        Thread t = new Thread(() -> {
            try {
                ZipEntry e = ZipUtil.getFirstFileEntry(zf);
                r.set(e == null ? "null" : e.getName());
            } catch (Throwable th) {
                r.set("throws:" + th.getClass().getName());
            }
        });
        t.setDaemon(true);
        t.start();
        t.join(3000);
        return (String) r.get();
    }

    Callable<Object> moderno(Caso c) {
        String fn = c.s("fn");
        String in = c.s("input");
        return switch (fn) {
            case "generateZip" -> () -> {
                if (in == null) {
                    ZipUtil.generateZip(null, "x.zip");
                    return "no exception";
                }
                Path d = dir();
                if (in.startsWith("sub/entrada.txt(")) {
                    String cont = in.substring("sub/entrada.txt(".length(), in.length() - 1);
                    ZipUtil.generateZip(escribir(d, "sub/entrada.txt", cont).toString(), d.resolve("salida.zip").toString());
                    return entradas(d.resolve("salida.zip"));
                }
                if (in.equals("ñandú.txt(hola)")) {
                    ZipUtil.generateZip(escribir(d, "ñandú.txt", "hola").toString(), d.resolve("s.zip").toString());
                    return entradas(d.resolve("s.zip"));
                }
                if (in.equals("missing")) {
                    Exception ex = null;
                    try {
                        ZipUtil.generateZip(d.resolve("no-existe.txt").toString(), d.resolve("s.zip").toString());
                    } catch (Exception e) {
                        ex = e;
                    }
                    return map("throws", ex == null ? null : ex.getClass().getName(), "output", entradas(d.resolve("s.zip")));
                }
                Path a = escribir(d, "a.txt", "hola");
                String out = c.s("output");
                if (out.equals("a.txt (same)")) {
                    ZipUtil.generateZip(a.toString(), a.toString());
                    return "no exception";
                }
                if (out.startsWith("A.TXT")) {
                    Exception ex = null;
                    try {
                        ZipUtil.generateZip(a.toString(), a.toString().toUpperCase());
                    } catch (Exception e) {
                        ex = e;
                    }
                    return map("throws", ex == null ? null : ex.getClass().getName(), "inputStillHola", Files.readString(a));
                }
                ZipUtil.generateZip(a.toString(), d.resolve("dir/inexistente/s.zip").toString());
                return "no exception";
            };
            case "generateMultipleZip" -> () -> {
                JsonElement ins = c.input().get("inputs");
                if (ins.isJsonNull()) return ZipUtil.generateMultipleZip(null, dir().resolve("m.zip").toString());
                Path d = dir();
                if (c.es("output", "no/hay/m.zip")) return ZipUtil.generateMultipleZip(List.of(escribir(d, "a.txt", "hola").toString()), d.resolve("no/hay/m.zip").toString());
                Path salida = d.resolve("multi.zip");
                List<String> rutas = new ArrayList<>();
                for (String r : lista(ins)) {
                    if (r.equals("OUT")) rutas.add(salida.toString());
                    else if (r.equals("missing.txt")) rutas.add(d.resolve(r).toString());
                    else rutas.add(escribir(d, r, r.startsWith("vacio") ? "vacio" : "hola").toString());
                }
                boolean ok = ZipUtil.generateMultipleZip(rutas, salida.toString());
                return map("returned", ok, "output", entradas(salida));
            };
            case "fileToZipFile" -> () -> {
                Path d = dir();
                boolean del = c.input().get("deleteSrc").getAsBoolean();
                if ("missing".equals(in)) {
                    Exception ex = null;
                    try {
                        Utils.fileToZipFile(d.resolve("no.txt").toString(), d.resolve("f.zip").toString(), del);
                    } catch (Exception e) {
                        ex = e;
                    }
                    Path z = d.resolve("f.zip");
                    return map("throws", ex == null ? null : ex.getClass().getName(), "output", entradas(z), "outputBytes", Files.exists(z) ? Files.size(z) : -1L);
                }
                if (c.es("output", "f.txt (same)")) {
                    Path f = escribir(d, "f.txt", "hola");
                    Exception ex = null;
                    try {
                        Utils.fileToZipFile(f.toString(), f.toString(), false);
                    } catch (Exception e) {
                        ex = e;
                    }
                    return map("throws", ex == null ? null : ex.getClass().getName(), "fileNow", entradas(f));
                }
                Path f = escribir(d, "sub/f.txt", "1MiB");
                Utils.fileToZipFile(f.toString(), d.resolve("f.zip").toString(), del);
                return map("output", entradas(d.resolve("f.zip")), "sourceExists", Files.exists(f));
            };
            case "getFirstFileEntry" -> () -> {
                if (c.input().get("entries").isJsonNull()) return ZipUtil.getFirstFileEntry(null);
                try (ZipFile zf = new ZipFile(zipCon(dir(), lista(c.input().get("entries"))).toFile())) {
                    return primera(zf);
                }
            };
            case "isRar" -> () -> {
                String cont = c.s("content");
                if (cont.equals("missing")) return ZipUtil.isRar(dir().resolve("no").toString());
                if (cont.equals("directory")) return ZipUtil.isRar(dir().toString());
                return ZipUtil.isRar(escribir(dir(), "f", cont).toString());
            };
            default -> throw new AssertionError("unmapped golden fn " + fn + " in " + c.id());
        };
    }

    @TestFactory
    @DisplayName("UTIL-53..57 equivalence: ZipUtil / fileToZipFile == legacy (temp dirs)")
    Stream<DynamicTest> util53a57_equivalencia() {
        List<Caso> casos = Golden.casos("any", AREA);
        assertThat(casos).extracting(Caso::id).containsAll(DESVIACION_APROBADA);
        return casos.stream().filter(c -> !DESVIACION_APROBADA.contains(c.id()))
                .map(c -> DynamicTest.dynamicTest(c.nombre(), () -> {
                    // ZIP-024 (input == output): the entry content is the zip's own local header, which embeds the wall
                    // clock -> compare name/size/method, not sha256
                    if (c.id().equals("ZIP-024"))
                        Golden.igualNormalizado(AREA, c, o -> o.getAsJsonObject("value").getAsJsonObject("fileNow").getAsJsonArray("entries")
                                .forEach(e -> e.getAsJsonObject().remove("sha256")), moderno(c));
                    else Golden.igual(AREA, c, moderno(c));
                }));
    }

    @AfterAll
    static void reporte() {
        System.out.println("equivalence cases executed [" + AREA + "]: " + Golden.ejecutados(AREA) + " (approved deviation: " + DESVIACION_APROBADA + ")");
    }

    @Test
    @DisplayName("UTIL-56 APPROVED DEVIATION (decision 4, 2026-10-16): directory-first zip -> legacy hangs (golden TIMEOUT); new returns the first file entry")
    void util56_desviacionAprobada() throws Exception {
        Caso legado = Golden.casos("any", AREA).stream().filter(c -> c.id().equals("ZIP-027")).findFirst().orElseThrow();
        assertThat(legado.valor().getAsString()).as("legacy record").isEqualTo("TIMEOUT");
        try (ZipFile zf = new ZipFile(zipCon(dir(), List.of("d:carpeta/", "f:carpeta/x.txt")).toFile())) {
            ZipEntry e = assertTimeoutPreemptively(Duration.ofSeconds(3), () -> ZipUtil.getFirstFileEntry(zf));
            assertThat(e.getName()).isEqualTo("carpeta/x.txt");
        }
        Golden.contar(AREA + "-deviation");
    }

    @Test
    @DisplayName("UTIL-54 legacy behavior pinned: generateMultipleZip swallows every error and returns false, keeping the entries written so far")
    void util54_tragaErrores() throws Exception {
        Path d = dir();
        Path a = escribir(d, "a.txt", "hola");
        assertThat(ZipUtil.generateMultipleZip(List.of(a.toString(), d.resolve("nada.txt").toString()), d.resolve("m.zip").toString())).isFalse();
        try (ZipFile z = new ZipFile(d.resolve("m.zip").toFile())) {
            assertThat(z.size()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("UTIL-53: same input and output path (ignoring case) -> BadFilePathException before anything is opened")
    void util53_mismaRuta() throws Exception {
        Path a = escribir(dir(), "a.txt", "hola");
        assertThatThrownBy(() -> ZipUtil.generateZip(a.toString(), a.toString().toUpperCase())).isInstanceOf(BadFilePathException.class);
        assertThat(Files.readString(a)).isEqualTo("hola");
        assertThat(BadFilePathException.class).hasSuperclass(RuntimeException.class);
    }

    @Test
    @DisplayName("UTIL-57: isRar checks that the first text line starts with \"Rar!\" (case-sensitive, no BOM skipping)")
    void util57_isRar() throws Exception {
        assertThat(ZipUtil.isRar(escribir(dir(), "r", "rar5").toString())).isTrue();
        assertThat(ZipUtil.isRar(escribir(dir(), "r", "rarMinuscula").toString())).isFalse();
        assertThat(ZipUtil.isRar(escribir(dir(), "r", "bomRar").toString())).isFalse();
    }
}
