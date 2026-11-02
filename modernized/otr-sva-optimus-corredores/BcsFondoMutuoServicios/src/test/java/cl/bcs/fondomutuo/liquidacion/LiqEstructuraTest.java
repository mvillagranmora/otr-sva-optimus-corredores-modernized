package cl.bcs.fondomutuo.liquidacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.certificacion.CrearCargoAbonoPort;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;
import cl.bcs.fondomutuo.tesoreria.ProcesarIngresoEgresoTesoreriaService.CrearIngresoEgresoPuerto;

/**
 * F3 W3 T6-B, structure and security of what the step adds (cards FM-LIQ-02, 08, 18; decisions of the user): (1) the port is an interface, the facade holds only final fields and no mutable static state and reaches
 * the world ONLY through ports (the sequences go through the existing {@code SecuenciaPort}, the three collaborators through {@code CrearIngresoEgresoPuerto}, {@code CrearCargoAbonoPort} and {@code LogPort});
 * (2) the SQL of the class is the VERBATIM legacy text (SHA-256 of the four files of the module and of the APV withholding SQL, which is copied verbatim into {@code src/main/resources}), and the adapter writes
 * only through the batch UPDATE; (3) the handler opens with the G0 gate as its FIRST statement; (4) the wiring declares the list and the two beans, and no replacement of a port that already has one;
 * (5) every file the step writes and every new production source uses LF line endings, and RULE-076: nothing carries a real id, URL or credential and nothing calls the legacy {@code main()}. Patterns are
 * assembled from pieces so that this file does not match itself.
 *
 * <p>RED BEFORE CODE: the tests first demand the production classes ("T6-B not implemented"); the parts about the test files do not and pass today.
 */
class LiqEstructuraTest {

    private static final Path TEST = Path.of("src/test/java/cl/bcs/fondomutuo/liquidacion");
    private static final Path MAIN = Path.of("src/main/java/cl/bcs/fondomutuo");
    private static final Path PRODUCCION = MAIN.resolve("liquidacion");
    private static final Path RECURSOS = Path.of("src/main/resources");

    private static final List<Path> ARCHIVOS_DEL_PASO = List.of(TEST.resolve("ApiT6b.java"), TEST.resolve("API_ASSUMED_T6B.md"), TEST.resolve("README.md"), TEST.resolve("CasoLiq.java"), TEST.resolve("CosturaLiq.java"),
            TEST.resolve("ConexionLiq.java"), TEST.resolve("LiqEquivalenciaTest.java"), TEST.resolve("LiqDeterminismoTest.java"), TEST.resolve("LiqReglasTest.java"), TEST.resolve("LiqPuertoTest.java"),
            TEST.resolve("LiqHandlerTest.java"), TEST.resolve("LiqEstructuraTest.java"), TEST.resolve("LiqCableadoTest.java"));

    private static List<Path> fuentesNuevas() {
        return List.of(PRODUCCION.resolve("LiquidacionContrato.java"), PRODUCCION.resolve("LiquidacionPort.java"), PRODUCCION.resolve("LiquidacionDatos.java"), PRODUCCION.resolve("LiquidacionDatosJdbc.java"),
                PRODUCCION.resolve("LiquidacionFachada.java"), PRODUCCION.resolve("LiquidacionJavaCallHandler.java"), PRODUCCION.resolve("LiquidacionConfiguracion.java"));
    }

    private static boolean inmutable(Class<?> t) {
        return t.isPrimitive() || t == String.class || t == Boolean.class || t == Integer.class || t == Long.class;
    }

    private static String sha256(byte[] b) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // shape: port = interface, facade = final fields, adapter = the SQL of the class
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T6-B: the port is an INTERFACE with ONE method logica(Entrada) throws Exception returning Salida; the data port is an interface with exactly the five methods of the SQL of the class")
    void losPuertosSonInterfaces() throws Exception {
        Class<?> puerto = ApiT6b.cargar(ApiT6b.PUERTO);
        Class<?> datos = ApiT6b.cargar(ApiT6b.DATOS);
        assertThat(puerto.isInterface()).isTrue();
        assertThat(datos.isInterface()).isTrue();
        assertThat(puerto.getMethods()).hasSize(1);
        Method logica = puerto.getMethod("logica", ApiT6b.cargar(ApiT6b.ENTRADA));
        assertThat(logica.getReturnType()).isEqualTo(ApiT6b.cargar(ApiT6b.SALIDA));
        assertThat(logica.getExceptionTypes()).contains(Exception.class);
        assertThat(Arrays.stream(datos.getMethods()).map(Method::getName)).containsExactlyInAnyOrder("porMovimientos", "porFiltro", "modalidadesPago", "configuracionRetencion", "actualizar");
        for (Method m : datos.getMethods()) {
            assertThat(m.getExceptionTypes()).as(m.getName() + ": the JDBC layer is unchecked, the port declares no checked exception").isEmpty();
        }
        Method porMovimientos = datos.getMethod("porMovimientos", Long.class, List.class);
        assertThat(porMovimientos.getReturnType()).isEqualTo(List.class);
        Method porFiltro = datos.getMethod("porFiltro", Long.class, String.class, String.class, String.class, String.class, String.class, String.class);
        assertThat(porFiltro.getReturnType()).isEqualTo(List.class);
        assertThat(datos.getMethod("modalidadesPago").getReturnType()).isEqualTo(List.class);
        assertThat(datos.getMethod("configuracionRetencion").getReturnType()).isEqualTo(List.class);
        assertThat(datos.getMethod("actualizar", List.class).getReturnType()).isEqualTo(void.class);
    }

    @Test
    @DisplayName("T6-B: the facade implements the port, has ONE public constructor (LiquidacionDatos, ConfiguracionSistemaPort, DiccionarioMensajesPort, SecuenciaPort, CrearIngresoEgresoPuerto, CrearCargoAbonoPort, LogPort), "
            + "only final instance fields and no mutable static state; the adapter implements the data port over final fields")
    void laFachadaYElAdaptadorNoTienenEstado() {
        Class<?> fachada = ApiT6b.cargar(ApiT6b.FACHADA);
        Class<?> jdbc = ApiT6b.cargar(ApiT6b.DATOS_JDBC);
        assertThat(ApiT6b.cargar(ApiT6b.PUERTO).isAssignableFrom(fachada)).isTrue();
        assertThat(ApiT6b.cargar(ApiT6b.DATOS).isAssignableFrom(jdbc)).isTrue();
        assertThat(fachada.getConstructors()).hasSize(1);
        assertThat(fachada.getConstructors()[0].getParameterTypes()).containsExactly(ApiT6b.cargar(ApiT6b.DATOS), ConfiguracionSistemaPort.class, DiccionarioMensajesPort.class, SecuenciaPort.class,
                CrearIngresoEgresoPuerto.class, CrearCargoAbonoPort.class, LogPort.class);
        assertThat(jdbc.getConstructors()).hasSize(1);
        assertThat(jdbc.getConstructors()[0].getParameterTypes()).containsExactly(ConexionUnidadDeTrabajo.class);
        for (Class<?> c : List.of(fachada, jdbc, ApiT6b.cargar(ApiT6b.HANDLER), ApiT6b.cargar(ApiT6b.CONFIGURACION))) {
            for (Field f : c.getDeclaredFields()) {
                if (f.isSynthetic()) continue;
                if (Modifier.isStatic(f.getModifiers())) {
                    assertThat(Modifier.isFinal(f.getModifiers()) && inmutable(f.getType())).as(c.getSimpleName() + ": static field " + f + " must be a final String / primitive constant").isTrue();
                } else if (c != ApiT6b.cargar(ApiT6b.CONFIGURACION)) {
                    assertThat(Modifier.isFinal(f.getModifiers())).as(c.getSimpleName() + ": instance field " + f + " must be final").isTrue();
                }
            }
        }
    }

    @Test
    @DisplayName("T6-B FM-LIQ-08: the facade reaches the world only through ports: no Connection, no JDBC, no SqlNombrado, no Spring, no static state; the two sequences go through SecuenciaPort.siguientes, once each")
    void laFachadaSoloUsaPuertos() throws Exception {
        ApiT6b.cargar(ApiT6b.FACHADA);
        String fuente = Files.readString(PRODUCCION.resolve("LiquidacionFachada.java"), StandardCharsets.UTF_8);
        assertThat(fuente).doesNotContain("java.sql.Connection").doesNotContain("SqlNombrado").doesNotContain("DriverManager").doesNotContain("JdbcTemplate").doesNotContain("springframework")
                .doesNotContain("System.out").doesNotContain("printStackTrace");
        assertThat(count(fuente, ApiT6b.SECUENCIA_CARTA)).as("the sequence of the carta is named once").isEqualTo(1);
        assertThat(count(fuente, ApiT6b.SECUENCIA_COMPROBANTE)).as("the sequence of the comprobante is named once").isEqualTo(1);
        assertThat(count(fuente, ".siguientes(")).as("two requests to the sequences").isEqualTo(2);
        assertThat(Pattern.compile("(?m)^\\s*(public |private |protected )?static (?!final )[^(=;]*(=[^;]*)?;\\s*$").matcher(fuente).find()).as("no mutable static field (static methods and final constants are fine)").isFalse();
        assertThat(fuente).as("RULE-076: only logica is ported").doesNotContain("static void main");
    }

    private static int count(String s, String parte) {
        int n = 0;
        for (int i = s.indexOf(parte); i >= 0; i = s.indexOf(parte, i + 1)) n++;
        return n;
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the SQL: verbatim legacy text, the adapter writes only through the batch UPDATE
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T6-B FM-LIQ-04/05/11/14: the four SQL files of the class are the VERBATIM legacy text (SHA-256) and the APV withholding SQL is copied verbatim into src/main/resources/cl/bcs/APV/sql")
    void elSqlEsElDelLegacy() throws Exception {
        for (String archivo : List.of(ApiT6b.SQL_POR_MOVIMIENTOS, ApiT6b.SQL_POR_FILTRO, ApiT6b.SQL_ACTUALIZAR, ApiT6b.SQL_MODALIDADES)) {
            byte[] bytes = Files.readAllBytes(RECURSOS.resolve("cl/bcs/fondomutuo/sql").resolve(archivo));
            assertThat(sha256(bytes)).as(archivo).isEqualTo(ApiT6b.SQL_SHA256.get(archivo));
            assertThat(sha256(SqlRecursos.modulo(archivo).getBytes(StandardCharsets.UTF_8))).as(archivo + " through SqlRecursos").isEqualTo(ApiT6b.SQL_SHA256.get(archivo));
        }
        Path retencion = RECURSOS.resolve(ApiT6b.SQL_RETENCION_RUTA);
        assertThat(Files.exists(retencion)).as("T6-B not implemented: the withholding SQL must be a file of src/main/resources (not borrowed from another module)").isTrue();
        assertThat(sha256(Files.readAllBytes(retencion))).isEqualTo(ApiT6b.SQL_SHA256.get(ApiT6b.SQL_RETENCION_RUTA));
        assertThat(sha256(SqlRecursos.cargar(ApiT6b.SQL_RETENCION_RUTA).getBytes(StandardCharsets.UTF_8))).isEqualTo(ApiT6b.SQL_SHA256.get(ApiT6b.SQL_RETENCION_RUTA));
        String sql = Files.readString(retencion, StandardCharsets.UTF_8);
        assertThat(sql).startsWith("SELECT ConfRetencion.\"PorcentajeRetencion\"/100 AS \"PorcentajeRetencion\"").contains("\"IdEstado\" = 'VIG'").doesNotContainIgnoringCase("order by");
    }

    @Test
    @DisplayName("T6-B: the adapter holds no SQL of its own (it loads the five files), reads with SqlNombrado.consultar and writes ONLY with the batch SqlLote.ejecutar of the UPDATE; no commit, no rollback")
    void elAdaptadorSoloUsaElSqlDelLegacy() throws Exception {
        ApiT6b.cargar(ApiT6b.DATOS_JDBC);
        String fuente = Files.readString(PRODUCCION.resolve("LiquidacionDatosJdbc.java"), StandardCharsets.UTF_8);
        for (String archivo : List.of(ApiT6b.SQL_POR_MOVIMIENTOS, ApiT6b.SQL_POR_FILTRO, ApiT6b.SQL_ACTUALIZAR, ApiT6b.SQL_MODALIDADES)) assertThat(fuente).as(archivo).contains(archivo);
        assertThat(fuente).contains(ApiT6b.SQL_RETENCION_RUTA);
        assertThat(fuente).contains("SqlNombrado.consultar").contains("SqlLote.ejecutar");
        assertThat(count(fuente, "SqlLote.ejecutar")).as("the ONE write of the adapter is the batch UPDATE (the legacy SQL.batch semantics of SqlLote, which runs SqlNombrado.actualizarLote)").isEqualTo(1);
        assertThat(fuente).doesNotContain("SqlNombrado.actualizar").doesNotContain("executeUpdate").doesNotContain("commit").doesNotContain("rollback").doesNotContain("batchUpdate");
        assertThat(Pattern.compile("(?i)\"\\s*(select|insert|update|delete|merge)\\s").matcher(fuente).find()).as("no SQL literal in the adapter").isFalse();
        String modalidades = SqlRecursos.modulo(ApiT6b.SQL_MODALIDADES);
        String palabras = Pattern.compile("(?i)\\b(insert|update|delete|merge|truncate|upsert|drop|alter|create)\\b").matcher(modalidades + SqlRecursos.modulo(ApiT6b.SQL_POR_MOVIMIENTOS) + SqlRecursos.modulo(ApiT6b.SQL_POR_FILTRO)
                + SqlRecursos.cargar(ApiT6b.SQL_RETENCION_RUTA)).results().map(r -> r.group()).toList().toString();
        assertThat(palabras).as("the reads of the class have no writing keyword").isEqualTo("[]");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the handler opens with G0; the wiring
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T6-B: the handler opens call() with the G0 gate as its FIRST statement (before the input is decoded) and the gate reads Compuertas.listasServidas() for LiquidacionConfiguracion.LISTA")
    void g0EsLaPrimeraSentencia() throws Exception {
        ApiT6b.cargar(ApiT6b.HANDLER);
        String fuente = Files.readString(PRODUCCION.resolve("LiquidacionJavaCallHandler.java"), StandardCharsets.UTF_8);
        assertThat(Pattern.compile("public JsonObject call\\(Connection \\w+, JsonObject \\w+\\) throws Exception \\{\\s*if \\(!compuertas\\.listasServidas\\(\\)\\.contains\\(LiquidacionConfiguracion\\.LISTA\\)\\) \\{\\s*throw new ServicioNoServidoException\\(\"G0\"")
                .matcher(fuente).find()).as("the first statement of call() is the G0 check").isTrue();
        assertThat(fuente).as("RULE-076: only call / logica are ported").doesNotContain("static void main");
    }

    @Test
    @DisplayName("T6-B: the wiring is a @Configuration with the constant LISTA and the @Bean of the data adapter and of the port; any other @Bean is a fail-closed replacement marked @ConditionalOnMissingBean, and it never replaces CrearCargoAbono / Log / Tesoreria")
    void laConfiguracionDeclaraLosBeansDelPaso() {
        Class<?> c = ApiT6b.cargar(ApiT6b.CONFIGURACION);
        assertThat(c.isAnnotationPresent(Configuration.class)).isTrue();
        List<Method> beans = Arrays.stream(c.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Bean.class)).toList();
        assertThat(beans.stream().map(m -> m.getReturnType().getName())).contains(ApiT6b.PUERTO, ApiT6b.DATOS);
        for (Method m : beans) {
            if (m.getReturnType().getName().equals(ApiT6b.PUERTO) || m.getReturnType().getName().equals(ApiT6b.DATOS)) continue;
            assertThat(m.isAnnotationPresent(ConditionalOnMissingBean.class)).as(m.getName() + ": a replacement of a port without adapter must be @ConditionalOnMissingBean").isTrue();
            assertThat(m.getReturnType()).as(m.getName() + ": the ports with an adapter or a fail-closed replacement already (Certificar's wiring, Tesoreria in process, the Log) are REUSED")
                    .isNotIn(CrearCargoAbonoPort.class, LogPort.class, CrearIngresoEgresoPuerto.class, SecuenciaPort.class);
        }
        assertThat(ApiT6b.lista()).isEqualTo(ApiT6b.LISTA_ESPERADA);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // files
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T6-B: the files of the step use LF line endings (tests, bridge, harness, assumed API; and the new production sources once they exist)")
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
        ApiT6b.cargar(ApiT6b.PUERTO);   // red before code: the production sources must exist for this check to mean anything
        for (Path p : fuentesNuevas()) assertThat(Files.exists(p)).as("missing production source: " + p).isTrue();
    }

    @Test
    @DisplayName("T6-B RULE-076: no URL with a scheme, no credential-like literal, no connection string and no call to the legacy main() in the files of the step or in the new production sources")
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
