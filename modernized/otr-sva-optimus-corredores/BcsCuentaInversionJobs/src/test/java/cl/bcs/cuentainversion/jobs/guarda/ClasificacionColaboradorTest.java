package cl.bcs.cuentainversion.jobs.guarda;

import cl.bcs.cuentainversion.infrastructure.adapter.ColaboradoresReportesAdapter;
import cl.bcs.cuentainversion.jobs.guarda.ClasificacionColaborador.Categoria;
import cl.bcs.cuentainversion.service.JavaCallDispatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** Which collaborator names the guard protects (inventory JOBS_H1_H4_INVENTORY.md A.2, verified against what this module and its library send). */
class ClasificacionColaboradorTest {

    private static final Pattern LLAMADA = Pattern.compile("(?<![\\w.])((?:\\w*[Cc]olaborador\\w*\\s*\\.\\s*)?)invocar\\(\\s*([^,)]+?)\\s*[,)]");
    private static final Pattern CONSTANTE = Pattern.compile("static\\s+final\\s+String\\s+(\\w+)\\s*=\\s*\"([A-Za-z0-9_.]+)\"");
    private static final Pattern DEFINICION = Pattern.compile("String|[\\w<>\\[\\]?]+\\s+\\w+"); // a declaration, or the type in a Javadoc link {@link #invocar(String, ...)}
    /** Library files that forward their own {@code servicio} parameter (a private helper of the reporting adapter, the typed overload of the port). */
    private static final Set<String> REENVIADORES = Set.of("ColaboradoresReportesAdapter.java", "Colaboradores.java");

    private static Set<String> de(Categoria c) {
        return ClasificacionColaborador.todas().entrySet().stream().filter(e -> e.getValue() == c)
                .map(Map.Entry::getKey).collect(Collectors.toCollection(TreeSet::new));
    }

    /** CLASE of every class of {@code domain.colaborador} of the library (what its services send by {@code X.CLASE}), by simple class name. */
    private static Map<String, String> clasesDeLaBiblioteca() throws Exception {
        Map<String, String> porTipo = new HashMap<>();
        for (Resource r : new PathMatchingResourcePatternResolver().getResources("classpath*:cl/bcs/cuentainversion/domain/colaborador/*.class")) {
            String simple = r.getFilename().replace(".class", "");
            if (simple.contains("$")) {
                continue;
            }
            Class<?> tipo = Class.forName("cl.bcs.cuentainversion.domain.colaborador." + simple);
            try {
                porTipo.put(simple, (String) tipo.getField("CLASE").get(null));
            } catch (NoSuchFieldException ausente) {
                // not a collaborator contract
            }
        }
        return porTipo;
    }

    /** The name of every {@code invocar(...)} call of the text; an argument that is not a literal or a resolvable constant goes to {@code sinResolver}. */
    static Set<String> nombres(String fuente, Map<String, String> biblioteca, List<String> sinResolver) {
        Map<String, String> locales = new HashMap<>();
        Matcher c = CONSTANTE.matcher(fuente);
        while (c.find()) {
            locales.put(c.group(1), c.group(2));
        }
        Set<String> nombres = new TreeSet<>();
        Matcher m = LLAMADA.matcher(fuente);
        while (m.find()) {
            String arg = m.group(2);
            if (DEFINICION.matcher(arg).matches()) {
                continue; // a method declaration (String servicio ...)
            }
            String valor = arg.startsWith("\"") && arg.endsWith("\"") ? arg.substring(1, arg.length() - 1)
                    : arg.endsWith(".CLASE") && biblioteca.containsKey(arg.substring(0, arg.length() - 6)) ? biblioteca.get(arg.substring(0, arg.length() - 6))
                    : locales.get(arg);
            if (valor == null) {
                sinResolver.add(arg);
            } else {
                nombres.add(valor);
            }
        }
        return nombres;
    }

    private static Set<String> enviadosPorLasFuentes(Path raiz, Map<String, String> biblioteca) throws IOException {
        List<String> sinResolver = new ArrayList<>();
        Set<String> nombres = new TreeSet<>();
        try (Stream<Path> s = Files.walk(raiz)) {
            List<Path> java = s.filter(p -> p.toString().endsWith(".java")).toList();
            assertThat(java).as("the scan must see the sources of " + raiz).isNotEmpty();
            for (Path p : java) {
                List<String> deEsteArchivo = new ArrayList<>();
                nombres.addAll(nombres(Files.readString(p, StandardCharsets.UTF_8), biblioteca, deEsteArchivo));
                if (REENVIADORES.contains(p.getFileName().toString())) {
                    deEsteArchivo.removeIf("servicio"::equals); // forwards its own parameter; the names it receives are the constants checked below
                }
                sinResolver.addAll(deEsteArchivo);
            }
        }
        assertThat(sinResolver).as("every invocar( call names its collaborator with a literal or a constant (a computed name would escape the guard)")
                .isEmpty();
        return nombres;
    }

    private static Path base() {
        return Path.of(System.getProperty("basedir", System.getProperty("user.dir"))).toAbsolutePath().normalize();
    }

    @Test
    @DisplayName("G6-C01: every name this module and its service library can send over the collaborator port is classified; a computed name fails the scan")
    void todoNombreEstaClasificado() throws Exception {
        Map<String, String> biblioteca = clasesDeLaBiblioteca();
        assertThat(biblioteca).containsKeys("AgregarLog", "AgregarPrograma", "CalcularParidad", "ConsultarEmpresa");
        Set<String> enviados = new TreeSet<>(biblioteca.values());
        for (Field f : ColaboradoresReportesAdapter.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == String.class) {
                f.setAccessible(true);
                enviados.add((String) f.get(null));
            }
        }
        Set<String> deLosJobs = enviadosPorLasFuentes(base().resolve("src/main/java"), biblioteca);
        assertThat(deLosJobs).containsExactly("cl.bcs.plataforma.bat.InscribirProcesoBatch"); // the PAUTA step; the jobs send nothing else
        enviados.addAll(deLosJobs);
        Path servicios = base().resolveSibling("BcsCuentaInversionServicios/src/main/java");
        assertThat(servicios).as("the sources of the service library next to this module").isDirectory();
        enviados.addAll(enviadosPorLasFuentes(servicios, biblioteca));
        assertThat(enviados).contains("cl.bcs.plataforma.pla.AgregarLog", "cl.bcs.facturacion.CrearMovimientoComprobante", "cl.bcs.plataforma.mer.CalcularParidad",
                "cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion", "cl.bcs.plataforma.bat.InscribirProcesoBatch");

        assertThat(enviados).allSatisfy(n -> assertThat(ClasificacionColaborador.de(n)).as(n).isNotNull());

        List<String> sinResolver = new ArrayList<>();
        nombres("colaboradores.invocar(nombre, x); colaboradores.invocar(Otra.CLASE, x); colaboradores.invocar(\"a.B\", x); "
                + "JsonObject invocar(String servicio, JsonObject e)", biblioteca, sinResolver);
        assertThat(sinResolver).containsExactly("nombre", "Otra.CLASE");
    }

    @Test
    @DisplayName("G6-C02: the non-repeatable TomEE writes of inventory A.2 (group TW) are guarded: programs, events, orders, requests, clients...")
    void escrituraNoRepetible() {
        assertThat(de(Categoria.NO_REPETIBLE)).containsExactlyInAnyOrder("cl.bcs.plataforma.bat.AgregarPrograma", "cl.bcs.plataforma.bat.InscribirProcesoBatch",
                "cl.bcs.plataforma.evt.AgregarEvento", "cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo",
                "cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo", "cl.bcs.plataforma.pla.ValidaEjecucionProcesoRecPAC",
                "cl.bcs.ordenes.CrearOrdenes", "cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal", "cl.bcs.fondomutuo.AnulacionContratoCSC",
                "cl.bcs.fondomutuo.RevocacionContratoCSC", "cl.bcs.portafolio.ModificarPortafolio", "cl.bcs.socionegocio.cli.CrearCliente",
                "cl.bcs.socionegocio.per.CrearPersona", "cl.bcs.socionegocio.per.CrearTelefono", "cl.bcs.socionegocio.per.CrearDireccionElectronica",
                "cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria", "cl.bcs.facturacion.CrearMovimientoComprobante");
        assertThat(de(Categoria.NO_REPETIBLE)).allSatisfy(n -> assertThat(ClasificacionColaborador.guardada(n)).isTrue());
    }

    @Test
    @DisplayName("G6-C03: AgregarLog is the only append-only write and is NOT guarded; group TW = 17 guarded + AgregarLog = 18 clases")
    void appendOnly() {
        assertThat(de(Categoria.APPEND_ONLY)).containsExactly("cl.bcs.plataforma.pla.AgregarLog");
        assertThat(ClasificacionColaborador.guardada("cl.bcs.plataforma.pla.AgregarLog")).isFalse();
        assertThat(de(Categoria.NO_REPETIBLE).size() + de(Categoria.APPEND_ONLY).size()).isEqualTo(18);
    }

    @Test
    @DisplayName("G6-C04: reads, CuentaInversion's own services and the specials are not guarded; the group M writers of APV / Tesoreria are (they commit apart)")
    void restoDeCategorias() {
        assertThat(de(Categoria.MIGRADO_ESCRITOR)).containsExactlyInAnyOrder("cl.bcs.APV.ValidarMandatosPAC", "cl.bcs.APV.CambiarEstadoContratosSAAPV",
                "cl.bcs.tesoreria.CrearIngresoEgreso", "cl.bcs.tesoreria.LiquidarComprobante", "cl.bcs.tesoreria.CrearComprobante",
                "cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco", "cl.bcs.tesoreria.nor.AnularNominasTransferencias", "cl.bcs.tesoreria.AnulacionMandatoPAC",
                "cl.bcs.tesoreria.RevocacionMandatoPAC", "cl.bcs.tesoreria.IngresarMandatoPAC");
        assertThat(de(Categoria.MIGRADO_ESCRITOR)).allSatisfy(n -> assertThat(ClasificacionColaborador.guardada(n)).as(n).isTrue());
        assertThat(de(Categoria.MIGRADO_PROPIO)).containsExactlyInAnyOrder("cl.bcs.cuentaInversion.CrearCargoAbono", "cl.bcs.cuentaInversion.AnularCargoAbono",
                "cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion");
        assertThat(de(Categoria.ESPECIAL)).containsExactlyInAnyOrder("StoredProcedure", "Cloper");
        assertThat(de(Categoria.SOLO_LECTURA)).hasSize(21).contains("cl.bcs.plataforma.mer.CalcularParidad", "cl.bcs.plataforma.pla.utils.ConsultarEmpresa",
                "cl.bcs.plataforma.tas.EntregarPreciosTasas", "cl.bcs.normativa.cartola.MovimientosTitulos");
        for (Categoria c : Set.of(Categoria.SOLO_LECTURA, Categoria.MIGRADO_PROPIO, Categoria.ESPECIAL, Categoria.APPEND_ONLY)) {
            assertThat(de(c)).allSatisfy(n -> assertThat(ClasificacionColaborador.guardada(n)).as(n).isFalse());
        }
        assertThat(ClasificacionColaborador.de("NombreQueNoExiste")).isNull();
        assertThat(ClasificacionColaborador.guardada(null)).isFalse();
    }

    @Test
    @DisplayName("G6-C05: guarded only when the call reaches the HTTP router: no guarded name is answered in process by the dispatcher, every own name is")
    void soloLoQueLlegaAlRouter() {
        JavaCallDispatcher local = new JavaCallDispatcher(null); // only its routing table is used
        ClasificacionColaborador.todas().forEach((nombre, categoria) -> {
            if (categoria.guardada()) {
                assertThat(local.maneja(nombre)).as(nombre + " is guarded, so it must go over HTTP").isFalse();
            }
            if (categoria == Categoria.MIGRADO_PROPIO) {
                assertThat(local.maneja(nombre)).as(nombre + " is CuentaInversion's own, answered in process").isTrue();
            }
        });
    }
}
