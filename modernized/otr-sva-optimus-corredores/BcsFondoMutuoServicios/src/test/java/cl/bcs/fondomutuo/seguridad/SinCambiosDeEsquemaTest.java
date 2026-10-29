package cl.bcs.fondomutuo.seguridad;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Project rule: NO database change of any kind. The ONLY schema-level statements the legacy SQL contains are the session-local ones the user accepted
 * (PLAN section 12, D-2 and D-F2-1): {@code CREATE LOCAL TEMPORARY TABLE #p_Bloqueo...}, {@code DROP TABLE #p_Bloqueo...} and the {@code CALL} of the lock procedures
 * (AUTONOMOUS TRANSACTION, confirmed in the catalog). This test pins that set: any other CREATE/ALTER/DROP/TRUNCATE/GRANT/REVOKE, any permanent or GLOBAL
 * table, any other CALL, in the 397 SQL resources (module + kit) or in a string literal of the new code, fails; and a SQL file that is loaded by active code
 * (outside the stub packages that are out of the Spring context) may only hold DDL/CALL from the accepted list.
 */
class SinCambiosDeEsquemaTest {

    private static final Path JAVA = Path.of("src", "main", "java");
    private static final Path SQL_MODULO = Path.of("src", "main", "resources", "cl", "bcs", "fondomutuo", "sql");
    private static final Path SQL_KIT = Path.of("src", "main", "resources", "cl", "bcs", "fondomutuo", "kit", "sql");
    private static final String COMENTARIOS = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)|(?:--.*)";
    private static final Pattern DDL = Pattern.compile("\\b(CREATE|ALTER|DROP|TRUNCATE|GRANT|REVOKE|RENAME)\\b[^;(]*", Pattern.CASE_INSENSITIVE);
    private static final Pattern DDL_ACEPTADO = Pattern.compile(
            "(CREATE\\s+LOCAL\\s+TEMPORARY\\s+TABLE\\s+#\\w+\\s*)|(DROP\\s+TABLE\\s+#\\w+\\s*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CALL = Pattern.compile("\\bCALL\\s+\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    /** Session-local DDL accepted by the user: Normal lock (D-2) and reinversion lock (D-F2-1). */
    private static final Set<String> DDL_ACEPTADO_ARCHIVOS = Set.of(
            "CrearSolicitudAporteRescateNormal_CreateTableBloqueoSolicitudAporteRescate.sql",
            "CrearSolicitudAporteRescateNormal_DropTableBloqueoSolicitudAporteRescate.sql",
            "CrearReinversionRescate_Tabla_Bloqueo.sql", "DropReinversionRescate_Tabla_Bloqueo.sql");

    /** The two lock procedures of F1 and the two of reinversion (F2): AUTONOMOUS TRANSACTION, confirmed by the catalog. */
    private static final Map<String, String> CALL_ACEPTADO = Map.of(
            "CrearSolicitudAporteRescateNormal_CallCrearBloqueoSolicitudAporteRescate.sql", "BSCL.FondoMutuo.BD.Servicios::CrearBloqueoSolicitudAporteRescate",
            "CrearSolicitudAporteRescateNormal_CallEliminarBloqueoSolicitudAporteRescate.sql", "BSCL.FondoMutuo.BD.Servicios::EliminarBloqueoSolicitudAporteRescate",
            "ProcesarReinversionRescate_Call_CrearBloqueoReinversion.sql", "BSCL.FondoMutuo.BD.Servicios::CrearBloqueoProcesarReinversion",
            "ProcesarReinversion_Call_EliminarBloqueoReinversion.sql", "BSCL.FondoMutuo.BD.Servicios::EliminarBloqueoReinversion");

    /** DDL/CALL files of classes OUTSIDE F1/F2 (stubs out of the context): present in the tree, never loaded by active code. */
    private static final Set<String> DDL_FUERA_DE_ALCANCE = Set.of(
            "CalcularFechaSolicitudAporteRescate_CrearTablaTempSolicitudAporteNormal.sql", "CertificarSolicitudAporteRescate_CrearTablaTemporalIdSolicitudesAprteRescate.sql",
            "CrearSolicitudAporteRescateNormal_CrearTablaTemporalSolicitudRescateNormal.sql", "ValorCuotaFileManager_CrearTablaTemporalValorCuota.sql");
    private static final Set<String> CALL_FUERA_DE_ALCANCE = Set.of(
            "CalcularFechaSolicitudAporteRescate_CallCalcularFechaSolicitudAporteRescate.sql", "GenerarCertificado31Masivo_certificado31_Call.sql",
            "GenerarInformeRentabilidadFFMMExtranjeroMasivo_CallObtenerRentabilidadCustodiaCliente.sql", "ValorCuotaFileManager_CallCargarValorCuota.sql");

    private static Map<String, String> sqls() throws IOException {
        Map<String, String> m = new TreeMap<>();
        for (Path d : new Path[] {SQL_MODULO, SQL_KIT}) {
            assertThat(d).as("SQL resources (run from the module directory)").isDirectory();
            try (Stream<Path> s = Files.list(d)) {
                for (Path p : s.filter(f -> f.toString().endsWith(".sql")).toList()) {
                    m.put(p.getFileName().toString(), Files.readString(p, StandardCharsets.UTF_8).replaceAll(COMENTARIOS, ""));
                }
            }
        }
        return m;
    }

    @Test
    void elDdlDelLegacySoloEsDeSesion_CreateLocalTemporaryYDropTableDeTablasConAlmohadilla() throws IOException {
        Map<String, String> ddl = new TreeMap<>();
        for (Map.Entry<String, String> e : sqls().entrySet()) {
            Matcher m = DDL.matcher(e.getValue());
            while (m.find()) {
                ddl.put(e.getKey(), m.group());
                assertThat(DDL_ACEPTADO.matcher(m.group().trim()).matches()).as(e.getKey() + ": " + m.group()).isTrue();
            }
        }
        assertThat(ddl.keySet()).as("every SQL file with session-local DDL").containsExactlyInAnyOrderElementsOf(
                union(DDL_ACEPTADO_ARCHIVOS, DDL_FUERA_DE_ALCANCE));
    }

    @Test
    void losUnicosCallSonLosDeLosBloqueosYLosDeClasesFueraDeAlcance() throws IOException {
        Map<String, String> calls = new TreeMap<>();
        for (Map.Entry<String, String> e : sqls().entrySet()) {
            Matcher m = CALL.matcher(e.getValue());
            if (m.find()) calls.put(e.getKey(), m.group(1));
        }
        assertThat(calls.keySet()).containsExactlyInAnyOrderElementsOf(union(CALL_ACEPTADO.keySet(), CALL_FUERA_DE_ALCANCE));
        CALL_ACEPTADO.forEach((archivo, procedimiento) -> assertThat(calls.get(archivo)).isEqualTo(procedimiento));
    }

    @Test
    void elSqlQueCargaElCodigoActivo_noTieneDdlNiCallSalvoLosAceptados() throws IOException {
        Map<String, String> sqls = sqls();
        Set<String> referenciados = new TreeSet<>();
        Pattern nombre = Pattern.compile("\"([A-Za-z0-9_]+\\.sql)\"");
        try (Stream<Path> s = Files.walk(JAVA)) {
            for (Path p : s.filter(f -> f.toString().endsWith(".java")).toList()) {
                String ruta = p.toString().replace('\\', '/');
                if (ruta.contains("/fondomutuo/service/") || ruta.contains("/adapter/in/web/") || ruta.contains("/adapter/out/hana/")) continue;   // out of the context
                Matcher m = nombre.matcher(Files.readString(p, StandardCharsets.UTF_8));
                while (m.find()) referenciados.add(m.group(1));
            }
        }
        assertThat(referenciados).as("SQL files named by active code").isNotEmpty();
        for (String archivo : referenciados) {
            assertThat(sqls).as("referenced SQL exists: " + archivo).containsKey(archivo);
            String texto = sqls.get(archivo);
            boolean tieneDdl = DDL.matcher(texto).find();
            boolean tieneCall = CALL.matcher(texto).find();
            if (tieneDdl) assertThat(DDL_ACEPTADO_ARCHIVOS).as("active code loads DDL file " + archivo).contains(archivo);
            if (tieneCall) assertThat(CALL_ACEPTADO.keySet()).as("active code loads CALL file " + archivo).contains(archivo);
        }
    }

    @Test
    void elCodigoNuevoNoTieneDdlNiEscrituraEnLiteralesDeSql() throws IOException {
        Pattern prohibidas = Pattern.compile("\\b(CREATE|ALTER|DROP|TRUNCATE|GRANT|REVOKE|MERGE|UPSERT)\\b|INSERT\\s+INTO|UPDATE\\s+\\S+\\s+SET|DELETE\\s+FROM|\\bCALL\\s+\"",
                Pattern.CASE_INSENSITIVE);
        List<Path> fuentes = new ArrayList<>();
        try (Stream<Path> s = Files.walk(JAVA)) {
            s.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                String ruta = p.toString().replace('\\', '/');
                boolean heredado = ruta.contains("/fondomutuo/service/") || ruta.contains("/adapter/in/web/") || ruta.contains("/adapter/out/hana/")
                        || ruta.endsWith("config/GlobalExceptionHandler.java") || ruta.contains("/domain/") || ruta.contains("/port/in/")
                        || ruta.endsWith("port/out/HanaFondoMutuoPort.java") || ruta.endsWith("port/out/ParidadPort.java") || ruta.endsWith("port/out/ValorCuotaPort.java");
                if (!heredado) fuentes.add(p);
            });
        }
        assertThat(fuentes).hasSizeGreaterThanOrEqualTo(25);
        for (Path p : fuentes) {
            String t = Files.readString(p, StandardCharsets.UTF_8).replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\n]*", "");
            Matcher lit = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(t);
            while (lit.find()) {
                assertThat(prohibidas.matcher(lit.group(1)).find()).as(p + " literal: " + lit.group(1)).isFalse();
            }
        }
    }

    /**
     * F2 W1 (plan 7, "SinCambiosDeEsquemaTest con la lista blanca ampliada"): the writes (INSERT / UPDATE / DELETE) of the SQL files loaded by active code
     * are EXACTLY the accepted ones, each on its table: F1 (requests, AGF, Normal lock rows, the issuer-equity step table) plus F2 W1 RescateReinversion
     * (INSERT ReinversionSerieFondoDestino, INSERT of the reinversion lock rows into its session-local table). The UPDATE of FechaFinalReinversion of
     * AporteReinversion (W2) is accepted by the plan but NOT loaded by any active class yet (RescateReinversion does not run it: C-W0A-1).
     */
    private static final Map<String, String> ESCRITURA_ACEPTADA = Map.ofEntries(
            Map.entry("ComunesSolicitudes_InsertSolicitud.sql", "INSERT \"BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate\""),
            Map.entry("ComunesSolicitudes_InsertReinversionSolicitud.sql", "INSERT \"BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino\""),
            Map.entry("CrearSolicitudAGF_InsertBeneficiario.sql", "INSERT \"BSCL.FondoMutuo.BD::FMM.InterfazAgf\""),
            Map.entry("CrearSolicitudAGF_InsertSolicitud.sql", "INSERT \"BSCL.FondoMutuo.BD::FMM.InterfazAgf\""),
            Map.entry("CrearSolicitudAporteRescateNormal_InsertTableBloqueoSolicitudAporteRescate.sql", "INSERT #p_BloqueoSolicitudAporteRescate"),
            Map.entry("InsertReinversionRescate_Tabla_Bloqueo.sql", "INSERT #p_BloqueoProcesarReinversion"),
            Map.entry("ValidaPatrimonioEmisor_DeleteTablaPaso.sql", "DELETE \"BSCL.Orden.BD::GBL.ValidaPatrimonioEmisor_Proceso_Paso\""),
            Map.entry("ValidaPatrimonioEmisor_InsertTablaPaso.sql", "INSERT \"BSCL.Orden.BD::GBL.ValidaPatrimonioEmisor_Proceso_Paso\""),
            Map.entry("CrearSolicitudAporteReinversion_UpdateFechaFinalReinversion.sql", "UPDATE \"BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino\""),
            // F3 W2 CVC (CargarValorCuota, the only UI class that writes; list CARGAR_VALOR_CUOTA closed by default): the four writes of the class
            Map.entry("CargarValorCuota_UpdateValorCuotaUltimaCarga.sql", "UPDATE \"BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico\""),
            Map.entry("CargarValorCuota_InsertValorCuotaHistorico.sql", "INSERT \"BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico\""),
            Map.entry("CargarValorCuota_DeleteValorCuota.sql", "DELETE \"BSCL.FondoMutuo.BD::FMM.ValorCuota\""),
            Map.entry("CargarValorCuota_InsertValorCuota.sql", "INSERT \"BSCL.FondoMutuo.BD::FMM.ValorCuota\""),
            // F3 W3 T1 CER (CertificarAporteRescate): the log INSERT of the early exit (FM-CER-14), a legitimate documented write
            Map.entry("AgregarLogCertificacionAporteRescate_InsertLogCertificacion.sql", "INSERT \"BSCL.FondoMutuo.BD::FMM.LogCertificacionAporteRescate\""));

    @Test
    void lasEscriturasDelCodigoActivoSonExactamenteLasAceptadas_F2W1() throws IOException {
        Map<String, String> sqls = sqls();
        Pattern dml = Pattern.compile("\\b(INSERT\\s+INTO|UPDATE|DELETE\\s+FROM)\\s+(\"[^\"]+\"|#\\w+)", Pattern.CASE_INSENSITIVE);
        Pattern nombre = Pattern.compile("\"([A-Za-z0-9_]+\\.sql)\"");
        Set<String> referenciados = new TreeSet<>();
        try (Stream<Path> s = Files.walk(JAVA)) {
            for (Path p : s.filter(f -> f.toString().endsWith(".java")).toList()) {
                String ruta = p.toString().replace('\\', '/');
                if (ruta.contains("/fondomutuo/service/") || ruta.contains("/adapter/in/web/") || ruta.contains("/adapter/out/hana/")) continue;
                Matcher m = nombre.matcher(Files.readString(p, StandardCharsets.UTF_8));
                while (m.find()) referenciados.add(m.group(1));
            }
        }
        Map<String, String> escrituras = new TreeMap<>();
        for (String archivo : referenciados) {
            Matcher m = dml.matcher(sqls.getOrDefault(archivo, ""));
            while (m.find()) escrituras.put(archivo, m.group(1).split("\\s+")[0].toUpperCase() + " " + m.group(2));
        }
        Map<String, String> esperado = new TreeMap<>(ESCRITURA_ACEPTADA);
        esperado.remove("CrearSolicitudAporteReinversion_UpdateFechaFinalReinversion.sql");   // accepted, not loaded yet (W2)
        assertThat(escrituras).isEqualTo(esperado);
        assertThat(sqls.get("CrearSolicitudAporteReinversion_UpdateFechaFinalReinversion.sql")).as("the accepted UPDATE exists verbatim").contains("FechaFinalReinversion");
        // the reinversion lock DDL / CALL are now loaded by active code (BloqueoReinversionJdbc), and only from the accepted files
        assertThat(referenciados).contains("CrearReinversionRescate_Tabla_Bloqueo.sql", "DropReinversionRescate_Tabla_Bloqueo.sql",
                "ProcesarReinversionRescate_Call_CrearBloqueoReinversion.sql", "ProcesarReinversion_Call_EliminarBloqueoReinversion.sql");
    }

    private static Set<String> union(Set<String> a, Set<String> b) {
        Set<String> r = new TreeSet<>(a);
        r.addAll(b);
        return r;
    }
}
