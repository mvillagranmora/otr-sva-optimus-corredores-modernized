package cl.bcs.contabilidad.jobs.guarda;

import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.port.out.ProcesoBatchPort;
import cl.bcs.contabilidad.jobs.guarda.ClasificacionColaborador.Categoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Which collaborator names the guard protects. This module has no router: it reaches TomEE through two typed ports whose HTTP adapters live in
 * BcsContabilidadServicios ({@code infrastructure/plataforma}). The tests read those sources, so a new HTTP path, a new port method or a new HTTP
 * client without a classification fails here.
 */
class ClasificacionColaboradorTest {

    private static final Pattern RUTA = Pattern.compile("\"/api/[A-Za-z0-9_]+/([A-Za-z0-9_]+)\"");
    private static final Pattern CLIENTE_HTTP = Pattern.compile("RestClient|RestTemplate|WebClient|HttpClient|HttpURLConnection|URLConnection");
    private static final Pattern HILOS = Pattern.compile("new Thread\\b|ExecutorService|Executors\\.|@Async|CompletableFuture|parallelStream|ForkJoinPool|"
            + "@Scheduled|TaskExecutor|sendAsync|new Timer\\b");

    private static Path modulo() {
        return Path.of(System.getProperty("basedir", System.getProperty("user.dir"))).toAbsolutePath().normalize();
    }

    private static Path fuentesServicios() {
        Path p = modulo().resolve("../BcsContabilidadServicios/src/main/java").normalize();
        assertThat(p.resolve("cl/bcs/contabilidad/infrastructure/plataforma/TomEePlataformaClient.java")).as("scan must cover the HTTP adapters").exists();
        return p;
    }

    private static List<Path> java(Path raiz) {
        try (Stream<Path> s = Files.walk(raiz)) {
            return s.filter(f -> f.toString().endsWith(".java")).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String leer(Path f) {
        try {
            return Files.readString(f, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Names in {@code "/api/<owner>/<Name>"} literals of the sources. */
    static Set<String> nombresHttp(Iterable<String> fuentes) {
        Set<String> nombres = new TreeSet<>();
        for (String f : fuentes) {
            Matcher m = RUTA.matcher(f);
            while (m.find()) {
                nombres.add(m.group(1));
            }
        }
        return nombres;
    }

    private static Set<String> metodos(Class<?> puerto) {
        return Stream.of(puerto.getDeclaredMethods()).map(Method::getName).map(n -> puerto.getSimpleName() + "." + n)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static Set<String> de(Categoria c) {
        return ClasificacionColaborador.todas().entrySet().stream().filter(e -> e.getValue() == c).map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Test
    @DisplayName("G6-C01: every method of the two ports is mapped to a classified name (a new port method fails here)")
    void todoMetodoDePuertoEstaClasificado() {
        Set<String> metodos = new TreeSet<>(metodos(PlataformaPort.class));
        metodos.addAll(metodos(ProcesoBatchPort.class));

        assertThat(metodos).hasSize(8).isEqualTo(ClasificacionColaborador.puertos().keySet());
        assertThat(ClasificacionColaborador.puertos().values()).allSatisfy(n -> assertThat(ClasificacionColaborador.de(n)).as(n).isNotNull());
    }

    @Test
    @DisplayName("G6-C02: every /api/<owner>/<Name> the Servicios adapters post to is classified (a new HTTP name fails here)")
    void todaRutaHttpEstaClasificada() {
        List<String> fuentes = java(fuentesServicios()).stream().map(ClasificacionColaboradorTest::leer).toList();

        Set<String> nombres = nombresHttp(fuentes);

        assertThat(nombres).containsExactlyInAnyOrder("ObtenerDiaAnterior", "ConsultarClearingFinanciero", "InscribirProcesoBatch");
        assertThat(nombres).allSatisfy(n -> assertThat(ClasificacionColaborador.de(n)).as(n).isNotNull());
        // the names the ports map to are exactly the HTTP ones (plus the JDBC marker)
        Set<String> deLosPuertos = new TreeSet<>(ClasificacionColaborador.puertos().values());
        deLosPuertos.remove(ClasificacionColaborador.EN_CONEXION);
        assertThat(deLosPuertos).isEqualTo(nombres);
    }

    @Test
    @DisplayName("G6-C03: the scanner does see an unclassified name (the test above is not vacuous)")
    void elEscanerDetectaUnNombreNuevo() {
        Set<String> nombres = nombresHttp(List.of("http.post(\"/api/plataforma/AgregarEvento\", x); http.post(\"/api/tesoreria/CrearOrdenes\", y);"));

        assertThat(nombres).containsExactly("AgregarEvento", "CrearOrdenes");
        assertThat(nombres).anySatisfy(n -> assertThat(ClasificacionColaborador.de(n)).isNull());
    }

    @Test
    @DisplayName("G6-C04: HTTP clients: only PlataformaHttp (TomEE) and SapB1Gateway (SAP B1 Service Layer, not a TomEE collaborator) exist; the HTTP adapters are two")
    void soloDosAdaptadoresHttp() {
        Set<String> conCliente = new TreeSet<>();
        Set<String> conPlataformaHttp = new TreeSet<>();
        for (Path f : java(fuentesServicios())) {
            String fuente = leer(f);
            if (CLIENTE_HTTP.matcher(fuente).find()) {
                conCliente.add(f.getFileName().toString());
            }
            if (fuente.contains("new PlataformaHttp(")) {
                conPlataformaHttp.add(f.getFileName().toString());
            }
        }
        for (Path f : java(modulo().resolve("src/main/java"))) {
            assertThat(CLIENTE_HTTP.matcher(leer(f)).find()).as("HTTP client in the jobs module: " + f.getFileName()).isFalse();
        }

        assertThat(conCliente).containsExactly("PlataformaHttp.java", "SapB1Gateway.java");
        assertThat(conPlataformaHttp).containsExactly("TomEePlataformaClient.java", "TomEeProcesoBatchClient.java");
    }

    @Test
    @DisplayName("G6-C05: the module starts no other thread (the guard scope is a thread-local of the thread that runs the job)")
    void sinOtrosHilos() {
        Map<String, String> encontrados = new TreeMap<>();
        for (Path raiz : List.of(modulo().resolve("src/main/java"), fuentesServicios())) {
            for (Path f : java(raiz)) {
                String codigo = leer(f).replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\\n]*", ""); // comments may name what is NOT used
                Matcher m = HILOS.matcher(codigo);
                if (m.find()) {
                    encontrados.put(f.getFileName().toString(), m.group());
                }
            }
        }

        assertThat(encontrados).isEmpty();
    }

    @Test
    @DisplayName("G6-C06: guarded = InscribirProcesoBatch only; reads (ObtenerDiaAnterior), the migrated clearing read and the JDBC marker are not")
    void categorias() {
        assertThat(de(Categoria.NO_REPETIBLE)).containsExactly("InscribirProcesoBatch");
        assertThat(de(Categoria.MIGRADO_ESCRITOR)).isEmpty();
        assertThat(ClasificacionColaborador.todas().keySet().stream().filter(ClasificacionColaborador::guardada)).containsExactly("InscribirProcesoBatch");
        assertThat(ClasificacionColaborador.de("ObtenerDiaAnterior")).isEqualTo(Categoria.SOLO_LECTURA);
        assertThat(ClasificacionColaborador.de("ConsultarClearingFinanciero")).isEqualTo(Categoria.MIGRADO_LECTURA);
        assertThat(ClasificacionColaborador.guardada("AgregarLog")).isFalse(); // append-only, not sent by this module
        assertThat(ClasificacionColaborador.de("NombreQueNoExiste")).isNull();
        assertThat(ClasificacionColaborador.guardada(null)).isFalse();
    }
}
