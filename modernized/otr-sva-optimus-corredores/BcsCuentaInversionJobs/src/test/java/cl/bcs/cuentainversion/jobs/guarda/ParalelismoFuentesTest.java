package cl.bcs.cuentainversion.jobs.guarda;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step 6b, build-time scan. The guard scope ({@link ContextoGuarda}) is a ThreadLocal: a guarded collaborator called from another thread is
 * refused at run time (fail closed, see {@link GuardaSinContextoTest}), so any code of this module or of the sibling Servicios libraries its jobs
 * use that moves work to another thread must be known. This test scans the MAIN sources of all of them for the constructs that do it
 * ({@code parallelStream}, {@code .parallel(}, ForkJoin, CompletableFuture, ExecutorService/Executors, {@code @Async}, {@code new Thread},
 * {@code sendAsync}, {@code StreamUtils.parallel*}) and FAILS on every hit that is not in the explicit allowlist below. An allowlist entry is a file,
 * the exact number of matching lines and the reason why that code is provably NOT on a path that can call a collaborator guarded in this module.
 * A new construct, or one more in an allowlisted file, fails here until somebody audits it. A stale entry (no hit any more) fails too.
 */
class ParalelismoFuentesTest {

    private record Permitido(int lineas, String razon) {
    }

    /** Modules (directories next to this one) whose {@code src/main/java} is scanned: this module and the libraries its jobs run. */
    private static final List<String> RAICES = List.of("BcsCuentaInversionJobs", "BcsUtils", "BcsCuentaInversionServicios");

    /** file (relative to the directory that holds the modules) -> audited verdict. */
    private static final Map<String, Permitido> PERMITIDOS = permitidos();

    private static final Pattern PATRON = Pattern.compile("parallelStream\\s*\\(|\\.parallel\\s*\\(|ForkJoin|CompletableFuture|ExecutorService|\\bExecutors\\b"
            + "|@Async\\b|new\\s+Thread\\b|sendAsync|StreamUtils\\s*\\.\\s*parallel|\\bparallel[A-Z]\\w*\\s*\\(");

    private static Map<String, Permitido> permitidos() {
        Map<String, Permitido> m = new TreeMap<>();
        m.put("BcsUtils/src/main/java/cl/bcs/utils/StreamUtils.java", new Permitido(3,
                "AUDITED (step 6b): the helper parallelFlatmap is DEFINED here and no scanned source calls it (a call anywhere else is itself a hit of this scan, "
                        + "and a collaborator call cannot be inside its pure BiFunction merger unless somebody writes one: that file would then fail here); "
                        + "its lambdas only apply the merger to two in-memory collections."));
        return m;
    }

    private static Path modulo() {
        return Path.of(System.getProperty("basedir", System.getProperty("user.dir"))).toAbsolutePath().normalize();
    }

    private static List<Path> java(Path raiz) {
        try (Stream<Path> s = Files.walk(raiz)) {
            return s.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    @DisplayName("G6b-S01: every scanned source root exists and holds Java sources (the scan cannot pass by looking at nothing)")
    void lasRaicesExisten() {
        Path base = modulo().getParent();
        for (String raiz : RAICES) {
            Path fuentes = base.resolve(raiz).resolve("src/main/java").normalize();
            assertThat(fuentes).as("sources of %s", raiz).isDirectory();
            assertThat(java(fuentes)).as("Java files of %s", raiz).isNotEmpty();
        }
        assertThat(RAICES).contains(modulo().getFileName().toString());
    }

    @Test
    @DisplayName("G6b-S02: the pattern recognises every construct it is meant to find")
    void elPatronReconoce() {
        for (String linea : List.of("lista.parallelStream().map(x -> x)", "IntStream.range(0, 3).parallel()", "new ForkJoinPool(2)", "CompletableFuture.supplyAsync(f)",
                "ExecutorService pool;", "Executors.newFixedThreadPool(2)", "@Async", "new Thread(r).start()", "cliente.sendAsync(req, h)",
                "StreamUtils.parallelFlatmap(a, b, f)", "parallelFlatmap(a, b, f)")) {
            assertThat(PATRON.matcher(linea).find()).as(linea).isTrue();
        }
        for (String linea : List.of("lista.stream().map(x -> x)", "Thread.currentThread().getName()", "// {@code parallelStream} in prose")) {
            assertThat(PATRON.matcher(linea).find()).as(linea).isFalse();
        }
    }

    @Test
    @DisplayName("G6b-S03: no main source of this module or of its sibling libraries starts threads or parallel work, except the audited allowlist")
    void sinParalelismoNoAuditado() {
        Path base = modulo().getParent();
        Map<String, List<String>> aciertos = new TreeMap<>();
        for (String raiz : RAICES) {
            for (Path f : java(base.resolve(raiz).resolve("src/main/java"))) {
                try {
                    List<String> lineas = Files.readAllLines(f, StandardCharsets.UTF_8);
                    for (int i = 0; i < lineas.size(); i++) {
                        if (PATRON.matcher(lineas.get(i)).find()) {
                            aciertos.computeIfAbsent(base.relativize(f).toString().replace('\\', '/'), k -> new ArrayList<>()).add((i + 1) + ": " + lineas.get(i).strip());
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }

        List<String> problemas = new ArrayList<>();
        aciertos.forEach((archivo, lineas) -> {
            Permitido p = PERMITIDOS.get(archivo);
            if (p == null) {
                problemas.add("NOT audited: " + archivo + " " + lineas);
            } else if (p.lineas() != lineas.size()) {
                problemas.add("allowlisted " + archivo + " expects " + p.lineas() + " matching lines but has " + lineas.size() + ": " + lineas);
            }
            // a hit that sits on the same line as a collaborator call is never accepted, whatever the allowlist says
            lineas.stream().filter(l -> l.contains("invocar(") || l.contains("colaboradores")).forEach(l -> problemas.add("collaborator call on a parallel line: " + archivo + " " + l));
        });
        PERMITIDOS.keySet().stream().filter(a -> !aciertos.containsKey(a)).forEach(a -> problemas.add("stale allowlist entry (no hit any more): " + a));

        assertThat(problemas).as("Threads / parallel work found in sources that can run under a job: audit each and allowlist it with a reason, or fix it").isEmpty();
        PERMITIDOS.values().forEach(p -> assertThat(p.razon()).isNotBlank());
    }
}
