package cl.bcs.fondomutuo.certificacionporfiltros;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescatePort;

/**
 * F3 W3 T6-A, structure and security of what the step adds. The decision of the user: T6-A is IN-PROCESS ONLY (a job of another module calls it): (1) NO JavaCall handler class exists, its FQN is NOT in the
 * allow-list of the dispatcher (which stays at 13), NO list was added; (2) the port is an interface, the facade holds only final fields and no mutable static state; (3) the adapter of the select only READS:
 * the SQL is the verbatim legacy text (SHA-256) and has no INSERT / UPDATE / DELETE, and the adapter source calls only the query method; (4) every file the step writes and every new production source uses LF
 * line endings, and RULE-076: nothing carries a real id, URL or credential and nothing calls the legacy {@code main()}. Patterns are assembled from pieces so that this file does not match itself.
 *
 * <p>RED BEFORE CODE: the tests first demand the production classes ("T6-A not implemented"); the parts about the test files do not and pass today.
 */
class CerPfEstructuraTest {

    private static final Path TEST_PF = Path.of("src/test/java/cl/bcs/fondomutuo/certificacionporfiltros");
    private static final Path TEST_CER = Path.of("src/test/java/cl/bcs/fondomutuo/certificacion");
    private static final Path MAIN = Path.of("src/main/java/cl/bcs/fondomutuo");
    private static final Path PRODUCCION_PF = MAIN.resolve("certificacionporfiltros");

    private static final List<Path> ARCHIVOS_DEL_PASO = List.of(TEST_PF.resolve("ApiT6a.java"), TEST_PF.resolve("API_ASSUMED_T6A.md"), TEST_PF.resolve("README.md"),
            TEST_PF.resolve("CerPfEquivalenciaTest.java"), TEST_PF.resolve("CerPfDeterminismoTest.java"), TEST_PF.resolve("CerPfReglasTest.java"), TEST_PF.resolve("CerPfPuertoTest.java"),
            TEST_PF.resolve("CerPfEstructuraTest.java"), TEST_PF.resolve("CerPfCableadoTest.java"), TEST_CER.resolve("CasoCerPf.java"), TEST_CER.resolve("CerCosturaPf.java"));

    private static List<Path> fuentesNuevas() {
        return List.of(PRODUCCION_PF.resolve("CertificarPorFiltrosContrato.java"), PRODUCCION_PF.resolve("CertificarPorFiltrosPort.java"), PRODUCCION_PF.resolve("CertificarPorFiltrosDatos.java"),
                PRODUCCION_PF.resolve("CertificarPorFiltrosDatosJdbc.java"), PRODUCCION_PF.resolve("CertificarPorFiltrosFachada.java"), PRODUCCION_PF.resolve("CertificarPorFiltrosConfiguracion.java"));
    }

    private static boolean inmutable(Class<?> t) {
        return t.isPrimitive() || t == String.class || t == Boolean.class || t == Integer.class || t == Long.class;
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // in-process only: no handler, not in the allow-list, no list
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T6-A in-process only: NO JavaCall handler class of CertificarPorFiltros exists (not by class name, not in the production package, not in any source of the module)")
    void noExisteUnHandlerJavaCall() throws IOException {
        ApiT6a.cargar(ApiT6a.PUERTO);   // red before code: the port must exist for the negative checks to mean anything
        for (String nombre : List.of("CertificarPorFiltrosJavaCallHandler", "CertificarPorFiltrosSolicitudAporteRescateJavaCallHandler", "CertificarPorFiltrosHandler")) {
            assertThatThrownBy(() -> Class.forName(ApiT6a.PAQUETE + nombre)).as(nombre).isInstanceOf(ClassNotFoundException.class);
        }
        List<String> hallazgos = new ArrayList<>();
        try (Stream<Path> archivos = Files.walk(MAIN)) {
            for (Path p : archivos.filter(f -> f.toString().endsWith(".java")).toList()) {
                String s = Files.readString(p, StandardCharsets.UTF_8);
                boolean delPaquete = p.startsWith(PRODUCCION_PF);
                if (delPaquete && Pattern.compile("(implements|extends)\\s+[^{]*JavaCallHandler").matcher(s).find()) hallazgos.add(p + " implements JavaCallHandler");
                if (delPaquete && s.contains("JavaCallDispatcher")) hallazgos.add(p + " touches the dispatcher");
                boolean andamio = p.startsWith(MAIN.resolve("adapter/in/web")) || p.startsWith(MAIN.resolve("service"));   // the old stubs: excluded from the Spring context (BcsFondoMutuoApplication.STUBS), see CerPfCableadoTest
                if (!andamio && s.contains("\"" + ApiT6a.FQN_LEGACY + "\"")) hallazgos.add(p + " names the legacy FQN as a literal");
            }
        }
        assertThat(hallazgos).isEmpty();
        // no bean of the module that is a JavaCallHandler for it: the classes of the package of production (the Spring wiring is in CerPfCableadoTest)
        for (Class<?> c : List.of(ApiT6a.cargar(ApiT6a.FACHADA), ApiT6a.cargar(ApiT6a.DATOS_JDBC), ApiT6a.cargar(ApiT6a.CONFIGURACION))) {
            assertThat(JavaCallHandler.class.isAssignableFrom(c)).as(c.getSimpleName()).isFalse();
        }
    }

    @Test
    @DisplayName("T6-A in-process only: the legacy FQN is NOT in JavaCallDispatcher.LISTA_BLANCA (still exactly 13), NOT in CLASES_LECTURA (still 8), and no constant of the dispatcher has its value")
    void elFqnNoEstaEnLaListaBlanca() throws Exception {
        ApiT6a.cargar(ApiT6a.PUERTO);
        assertThat(JavaCallDispatcher.LISTA_BLANCA).doesNotContain(ApiT6a.FQN_LEGACY).hasSize(13);
        assertThat(JavaCallDispatcher.CLASES_LECTURA).doesNotContain(ApiT6a.FQN_LEGACY).hasSize(8);
        for (Field f : JavaCallDispatcher.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == String.class) {
                f.setAccessible(true);
                assertThat((String) f.get(null)).as(f.getName()).isNotEqualTo(ApiT6a.FQN_LEGACY);
            }
        }
        assertThat(JavaCallDispatcher.LISTA_BLANCA).noneMatch(f -> f.contains("PorFiltros"));
    }

    @Test
    @DisplayName("T6-A in-process only: NO list was added (the wiring declares no LISTA constant; the lists of the Manejador, of the UI reads and of the UI writes are unchanged and do not mention the class)")
    void noSeAgregoUnaLista() throws Exception {
        Class<?> configuracion = ApiT6a.cargar(ApiT6a.CONFIGURACION);
        for (Field f : configuracion.getDeclaredFields()) {
            assertThat(f.getName()).as("static constant of the wiring").isNotIn("LISTA", "LISTAS");
        }
        Class<?> manejador = Class.forName("cl.bcs.fondomutuo.manejador.ManejadorConfiguracion");
        for (String nombre : List.of("LISTAS_LECTURA", "LISTAS_ESCRITURA_UI")) {
            Field f = manejador.getField(nombre);
            @SuppressWarnings("unchecked")
            java.util.Set<String> listas = (java.util.Set<String>) f.get(null);
            assertThat(listas).as(nombre).noneMatch(l -> l.toUpperCase().contains("FILTROS"));
        }
        assertThat(((java.util.Set<?>) manejador.getField("LISTAS_ESCRITURA_UI").get(null))).as("CARGAR_VALOR_CUOTA and CERTIFICAR_SOLICITUD_APORTE_RESCATE only").hasSize(2);
        assertThat(((java.util.Set<?>) manejador.getField("LISTAS_LECTURA").get(null))).hasSize(8);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // shape: port = interface, facade = final fields, adapter = reads only
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T6-A: the port and the read port are INTERFACES with ONE method each: logica(Entrada) throws Exception, and solicitudes(InFiltros, String, Long) returning List<InSolicitudes>")
    void losPuertosSonInterfaces() throws Exception {
        Class<?> puerto = ApiT6a.cargar(ApiT6a.PUERTO);
        Class<?> datos = ApiT6a.cargar(ApiT6a.DATOS);
        assertThat(puerto.isInterface()).isTrue();
        assertThat(datos.isInterface()).isTrue();
        assertThat(puerto.getMethods()).hasSize(1);
        Method logica = puerto.getMethod("logica", ApiT6a.cargar(ApiT6a.ENTRADA));
        assertThat(logica.getReturnType()).isEqualTo(ApiT6a.cargar(ApiT6a.SALIDA));
        assertThat(logica.getExceptionTypes()).contains(Exception.class);
        assertThat(datos.getMethods()).hasSize(1);
        Method solicitudes = datos.getMethods()[0];
        assertThat(solicitudes.getParameterTypes()).containsExactly(ApiT6a.cargar(ApiT6a.FILTRO), String.class, Long.class);
        assertThat(List.class.isAssignableFrom(solicitudes.getReturnType())).isTrue();
        assertThat(solicitudes.getGenericReturnType().getTypeName()).contains("InSolicitudes");
        assertThat(solicitudes.getExceptionTypes()).as("the select raises what the JDBC layer raises (unchecked): the port declares no checked exception").isEmpty();
    }

    @Test
    @DisplayName("T6-A: the facade implements the port, has ONE public constructor (CertificarAporteRescatePort, CertificarPorFiltrosDatos), only final instance fields and no mutable static state; the adapter implements the read port over final fields")
    void laFachadaYElAdaptadorNoTienenEstado() {
        Class<?> fachada = ApiT6a.cargar(ApiT6a.FACHADA);
        Class<?> jdbc = ApiT6a.cargar(ApiT6a.DATOS_JDBC);
        assertThat(ApiT6a.cargar(ApiT6a.PUERTO).isAssignableFrom(fachada)).isTrue();
        assertThat(ApiT6a.cargar(ApiT6a.DATOS).isAssignableFrom(jdbc)).isTrue();
        assertThat(fachada.getConstructors()).hasSize(1);
        assertThat(fachada.getConstructors()[0].getParameterTypes()).containsExactly(CertificarAporteRescatePort.class, ApiT6a.cargar(ApiT6a.DATOS));
        assertThat(jdbc.getConstructors()).hasSize(1);
        assertThat(jdbc.getConstructors()[0].getParameterTypes()).containsExactly(ConexionUnidadDeTrabajo.class);
        for (Class<?> c : List.of(fachada, jdbc, ApiT6a.cargar(ApiT6a.CONFIGURACION))) {
            for (Field f : c.getDeclaredFields()) {
                if (f.isSynthetic()) continue;
                if (Modifier.isStatic(f.getModifiers())) {
                    assertThat(Modifier.isFinal(f.getModifiers()) && inmutable(f.getType())).as(c.getSimpleName() + ": static field " + f + " must be a final String / primitive constant").isTrue();
                } else if (c != ApiT6a.cargar(ApiT6a.CONFIGURACION)) {
                    assertThat(Modifier.isFinal(f.getModifiers())).as(c.getSimpleName() + ": instance field " + f + " must be final").isTrue();
                }
            }
        }
    }

    @Test
    @DisplayName("T6-A: the adapter of the select only READS: its SQL is the verbatim legacy text (SHA-256 of the legacy file), has no INSERT / UPDATE / DELETE / MERGE, and the adapter source calls only the query method")
    void elAdaptadorSoloLee() throws Exception {
        ApiT6a.cargar(ApiT6a.DATOS_JDBC);
        String sql = SqlRecursos.modulo(ApiT6a.SQL_SELECT);
        String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(sql.getBytes(StandardCharsets.UTF_8)));
        assertThat(sha).as("the SQL of the module is the verbatim legacy file").isEqualTo(ApiT6a.SQL_SELECT_SHA256);
        String palabras = Pattern.compile("(?i)\\b(insert|update|delete|merge|truncate|upsert|drop|alter|create)\\b").matcher(sql).results().map(r -> r.group()).toList().toString();
        assertThat(palabras).as("writing keywords in the SQL").isEqualTo("[]");
        assertThat(sql).as("verbatim: no ORDER BY added (the order of the rows is the engine's, as in the legacy)").doesNotContainIgnoringCase("order by");
        assertThat(sql).startsWith("SELECT DISTINCT \"Solicitudes\".\"IdSolicitudAporteRescate\"");
        String fuente = Files.readString(PRODUCCION_PF.resolve("CertificarPorFiltrosDatosJdbc.java"), StandardCharsets.UTF_8);
        assertThat(fuente).doesNotContain("actualizar").doesNotContain("actualizarLote").doesNotContain("executeUpdate").doesNotContain("batchUpdate").doesNotContain("commit").doesNotContain("rollback");
        assertThat(Pattern.compile("(?i)\\b(insert into|update\\s+\"|delete from)\\b").matcher(fuente).find()).isFalse();
        assertThat(fuente).contains("SqlNombrado.consultar");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // files
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T6-A: the files of the step use LF line endings (tests, bridge, harness, assumed API; and the new production sources once they exist)")
    void finesDeLineaLf() throws IOException {
        List<String> conCr = new ArrayList<>();
        for (Path p : ARCHIVOS_DEL_PASO) {
            assertThat(Files.exists(p)).as("missing file of the step: " + p).isTrue();
            if (Files.readString(p, StandardCharsets.UTF_8).indexOf('\r') >= 0) conCr.add(p.toString());
        }
        for (Path p : fuentesNuevas()) {
            if (Files.exists(p) && Files.readString(p, StandardCharsets.UTF_8).indexOf('\r') >= 0) conCr.add(p.toString());
        }
        assertThat(conCr).as("CR found (use LF)").isEmpty();
        ApiT6a.cargar(ApiT6a.PUERTO);   // red before code: the production sources must exist for this check to mean anything
        for (Path p : fuentesNuevas()) assertThat(Files.exists(p)).as("missing production source: " + p).isTrue();
    }

    @Test
    @DisplayName("T6-A RULE-076: no URL with a scheme, no credential-like literal, no connection string and no call to the legacy main() in the files of the step or in the new production sources")
    void sinSecretosNiMain() throws IOException {
        String esquema = "ht" + "tps?:" + "//";
        List<Pattern> prohibidos = List.of(Pattern.compile(esquema), Pattern.compile("(?i)(pass" + "word|pass" + "wd|secr" + "et|api[_-]?k" + "ey)\\s*[=:]\\s*\"[^\"]{4,}"), Pattern.compile("Bear" + "er\\s+[A-Za-z0-9._-]{8,}"),
                Pattern.compile("jd" + "bc:(?!h2:mem)"), Pattern.compile("\\.ma" + "in\\s*\\(\\s*(new\\s+String|args|null)"));
        List<String> hallazgos = new ArrayList<>();
        List<Path> todos = new ArrayList<>(ARCHIVOS_DEL_PASO);
        for (Path p : fuentesNuevas()) if (Files.exists(p)) todos.add(p);
        for (Path p : todos) {
            String s = Files.readString(p, StandardCharsets.UTF_8);
            for (Pattern pat : prohibidos) {
                if (pat.matcher(s).find()) hallazgos.add(p.getFileName() + " ~ " + pat);
            }
        }
        assertThat(hallazgos).as("RULE-076: no real id / URL / credential and no legacy main()").isEmpty();
    }
}
