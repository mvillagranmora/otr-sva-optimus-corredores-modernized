package cl.bcs.fondomutuo.certificacion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.adapter.out.jdbc.ComisionesJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.DiccionarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.SeriesFondoJdbc;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.fondomutuo.normal.SesionHana;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

/**
 * Harness of F3 W3 T4-B (CertificarSolicitudAporteRescate, fronts F-G Facturacion and F-I reinversion / switch, on top of F-C..F-H of T2 / T3): for a case under
 * {@code src/test/resources/fondomutuo-cer/cases-t4/<id>/} (case.json, seed.sql and the expected.json RECORDED from the legacy by legacy-oracle/record_cer_t4.sh, copied by sync_cer_t4.py)
 * it builds the same H2 as T3, runs the T1 facade for real and, at the seam, runs the steps of F-C..F-I ({@link ApiT4.Pasos}) over the context it receives, in the order of the legacy.
 *
 * <p>Collaborators: everything the module answers by script (cash flow, custody, parities, prices, cuenta, Tesoreria, Spot, Facturacion) comes from the case script ({@link Guion}); the two
 * that the oracle does NOT script, AR (it may run real) and the child certification (the legacy calls itself), are REPLAYED from the golden: the answer the legacy got, call by call, in
 * order. The request the new code makes is compared with the recorded one (complete arguments), so a replay cannot hide a wrong call.
 *
 * <p>FRONT J IS NOT RUN. Its only effect that F-I reads is the UPDATE of {@code MontoMonedaSistema} (FM-CER-64 sums the rows AFTER the UPDATE to CRT): the harness emulates exactly that
 * column ("J-lite", {@link #frenteJ}) between Spot and the recursion; the real UPDATE is T5. A missing golden FAILS (never skips); a missing production class FAILS with "T4 not
 * implemented". Nothing here touches HANA or the network: H2 in memory only.
 */
public final class CasoCerT4 {

    /** What one run of the new code leaves. */
    public static final class Corrida {
        /** the oracle-format dump of what the harness can see: colaboradores, tablasIniciales, tablasTrasLaLlamada, secuenciasSolicitadas, excepcion */
        public final JsonObject volcado;
        /** the context after F-A, F-B, F-C..F-I (null when the run ended before the seam) */
        public final CertificarAporteRescateContexto contexto;
        /** the exception that ended the run: {@link ParadaT4} when F-C..F-I finished, anything else is what raised inside the facade, F-C..F-I */
        public final Throwable excepcion;
        /** how many collaborator calls had been made when the seam was reached (the calls of F-A and F-B: {@code EntregarPreciosTasas}, {@code CalcularParidad}) */
        public final int llamadasAntesDelSeam;

        Corrida(JsonObject volcado, CertificarAporteRescateContexto contexto, Throwable excepcion, int llamadasAntesDelSeam) {
            this.volcado = volcado;
            this.contexto = contexto;
            this.excepcion = excepcion;
            this.llamadasAntesDelSeam = llamadasAntesDelSeam;
        }

        public boolean terminoF_C_F_I() {
            return excepcion instanceof ParadaT4;
        }
    }

    public enum Modo {
        COMPLETO, EXCEPCION, EXCEPCION_POSTERIOR
    }

    private static final String[] ESQUEMA = { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" };

    private CasoCerT4() {
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the cases of T3
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static Path recursos() {
        return CasoCer.recursos();
    }

    public static Path directorio() {
        return recursos().resolve("cases-t4");
    }

    public static List<String> casos() {
        List<String> ids = new ArrayList<>();
        Path dir = directorio();
        if (!Files.isDirectory(dir)) throw new AssertionError("fixture directory fondomutuo-cer/cases-t4 missing (run legacy-oracle/sync_cer_t2.py)");
        try (var s = Files.list(dir)) {
            s.sorted().forEach(d -> {
                if (Files.exists(d.resolve("case.json"))) ids.add(d.getFileName().toString());
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return ids;
    }

    public static JsonObject caso(String id) {
        return CasoCer.leer(directorio().resolve(id).resolve("case.json"));
    }

    /** The recorded golden of the case; a case without golden FAILS the test (an unrecorded or unstable case must never pass by omission). */
    public static JsonObject golden(String id) {
        Path p = directorio().resolve(id).resolve("expected.json");
        if (!Files.exists(p)) throw new AssertionError("golden missing (record it with legacy-oracle/record_cer_t2.sh): " + p);
        return CasoCer.leer(p);
    }

    public static String slug(String id) {
        return id.substring(id.indexOf('_') + 1);
    }

    /** The id of the case with the given slug (the part after {@code FMM-CER-nnn_}). */
    public static String idPorSlug(String slug) {
        return casos().stream().filter(c -> slug(c).equals(slug)).findFirst().orElseThrow(() -> new AssertionError("no T4 case with slug " + slug));
    }

    public static List<String> refs(String id) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : caso(id).getAsJsonArray("ref")) out.add(e.getAsString());
        return out;
    }

    public static Modo modo(String id) {
        List<String> refs = refs(id);
        if (refs.contains("T4-costura:excepcion")) return Modo.EXCEPCION;
        if (refs.contains("T4-costura:excepcion-posterior")) return Modo.EXCEPCION_POSTERIOR;
        if (refs.contains("T4-costura:completo")) return Modo.COMPLETO;
        throw new AssertionError(id + " :: the case carries no T4-costura tag");
    }

    public static Corrida ejecutar(String id) throws Exception {
        java.util.TimeZone zona = java.util.TimeZone.getDefault();
        java.util.Locale idioma = java.util.Locale.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            org.h2.util.DateTimeUtils.resetCalendar();
            return correr(directorio().resolve(id));
        } finally {
            java.util.TimeZone.setDefault(zona);
            java.util.Locale.setDefault(idioma);
            org.h2.util.DateTimeUtils.resetCalendar();
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // one run
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    private static void esquema(Connection raw, JsonObject caso) throws Exception {
        Path base = recursos();
        for (String f : ESQUEMA) CasoCer.script(raw, Files.readString(base.getParent().resolve("fondomutuo-3a").resolve(f), StandardCharsets.UTF_8));
        for (JsonElement e : caso.getAsJsonArray("esquemasExtra")) CasoCer.script(raw, Files.readString(base.resolve(e.getAsString()), StandardCharsets.UTF_8));
    }

    private static JsonObject tablas(Connection raw, JsonObject caso) throws Exception {
        JsonObject tablas = new JsonObject();
        for (JsonElement el : caso.getAsJsonArray("tablas")) {
            JsonObject t = el.getAsJsonObject();
            tablas.add(t.get("nombre").getAsString(), volcar(raw, t.get("nombre").getAsString(), t.get("orden").getAsString()));
        }
        return tablas;
    }

    /** Same row rendering as the oracle and as {@link CasoCer}: numbers normalised, dates as ISO text, NULL as JSON null. */
    private static JsonArray volcar(Connection conn, String table, String orderBy) throws Exception {
        JsonArray rows = new JsonArray();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM \"" + table + "\" ORDER BY " + orderBy)) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                JsonObject row = new JsonObject();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    Object v = rs.getObject(i);
                    String col = md.getColumnLabel(i);
                    if (v == null) row.add(col, JsonNull.INSTANCE);
                    else if (v instanceof Number) row.add(col, new JsonPrimitive(new BigDecimal(new BigDecimal(v.toString()).stripTrailingZeros().toPlainString())));
                    else if (v instanceof java.util.Date) row.addProperty(col, rs.getString(i));
                    else row.addProperty(col, v.toString());
                }
                rows.add(row);
            }
        }
        return rows;
    }

    /** The collaborators of F-C..F-I that are answered by the script: EmpresaPort (ConsultarEmpresa), CustodiaPort (ConsultarFlujoCustodia) and PreciosTasasPort (EntregarPreciosTasas) of CalcularComisiones. */
    private static final class DoblesComision implements EmpresaPort, CustodiaPort, PreciosTasasPort {
        private final Guion guion;

        DoblesComision(Guion guion) {
            this.guion = guion;
        }

        @Override
        public InformacionEmpresa informacion(Long idEmpresa) {
            return new Gson().fromJson(guion.responder("ConsultarEmpresa", new JsonPrimitive(idEmpresa)), InformacionEmpresa.class);
        }

        @Override
        public JsonObject consultarFlujo(JsonObject entrada) {
            return guion.responder("ConsultarFlujoCustodia", entrada);
        }

        @Override
        public JsonObject entregar(JsonObject entrada) {
            return guion.responder("EntregarPreciosTasas", entrada);
        }
    }

    /** The collaborators replayed from the golden (the legacy answer, call by call, in order). */
    static final String HIJO = "CertificarSolicitudAporteRescate.hijo";
    static final String AR = "CrearSolicitudAporteReinversion";
    private static final String[] REPETIDOS = { AR, HIJO };

    private static JsonObject repeticion(JsonObject golden, String nombre) {
        JsonArray respuestas = new JsonArray();
        if (golden.has("colaboradores")) {
            for (JsonElement e : golden.getAsJsonArray("colaboradores")) {
                JsonObject c = e.getAsJsonObject();
                if (!nombre.equals(c.get("colaborador").getAsString())) continue;
                JsonObject r = new JsonObject();
                r.addProperty("una_vez", true);
                JsonElement resp = c.get("respuesta");
                if (resp != null && resp.isJsonPrimitive() && resp.getAsString().startsWith("error: ")) r.addProperty("error", resp.getAsString().substring(7));
                else r.add("respuesta", resp);
                respuestas.add(r);
            }
        }
        JsonObject out = new JsonObject();
        out.add("respuestas", respuestas);
        return out;
    }

    /** J-lite: what front J (the UPDATE to CRT) leaves that FM-CER-64 reads, the {@code MontoMonedaSistema} of the pending solicitudes. */
    private static void frenteJ(Connection raw, CertificarAporteRescateContexto ctx) {
        try (java.sql.PreparedStatement st = raw.prepareStatement("UPDATE \"" + T_SOLICITUD + "\" SET \"MontoMonedaSistema\" = ? WHERE \"IdSolicitudAporteRescate\" = ?")) {
            for (CertificarAporteRescateContrato.SolicitudProceso s : ctx.solicitudes) {
                if (!"PEN".equals(s.EstadoProceso) && !"ADV".equals(s.EstadoProceso)) continue;
                st.setBigDecimal(1, s.MontoMonedaSistema);
                st.setLong(2, s.IdSolicitudAporteRescate);
                st.executeUpdate();
            }
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    static final String T_SOLICITUD = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";

    private static Corrida correr(Path dir) throws Exception {
        Path base = recursos();
        JsonObject caso = CasoCer.leer(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:cert2" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        try (Connection raw = cl.bcs.fondomutuo.BdEfimeraH2.abrir(url)) {
            esquema(raw, caso);
            if (Files.exists(dir.resolve("seed.sql"))) CasoCer.script(raw, Files.readString(dir.resolve("seed.sql"), StandardCharsets.UTF_8));
            raw.commit();
            raw.setAutoCommit(false);
            SesionHana sesion = new SesionHana(raw, url, caso.has("reloj") ? caso.get("reloj").getAsString() : CasoCer.RELOJ);
            Connection con = sesion.conexion();
            JsonObject doubles = CasoCer.leer(base.getParent().resolve("fondomutuo-3a/seed/doubles-base.json"));
            JsonObject benignos = CasoCer.leer(base.resolve("seed/doubles-cer.json"));
            for (String k : benignos.keySet()) doubles.add(k, benignos.get(k));
            if (caso.has("colaboradores")) {
                for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
            }
            JsonObject golden = Files.exists(dir.resolve("expected.json")) ? CasoCer.leer(dir.resolve("expected.json")) : new JsonObject();
            for (String nombre : REPETIDOS) doubles.add(nombre, repeticion(golden, nombre));
            Guion guion = new Guion(doubles);
            JsonArray secuencias = new JsonArray();
            ConexionUnidadDeTrabajo uow = new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(con, true));

            // the units of F-C..F-I over the same session, the same adapters and the same scripted collaborators
            ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(uow);
            DoblesComision dobles = new DoblesComision(guion);
            ApiT3.Dependencias d3 = new ApiT3.Dependencias();
            d3.t2 = new ApiT2.Dependencias();
            d3.t2.flujoCaja = e -> guion.responder("SaldoFlujoCaja", e);
            d3.t2.paridad = e -> guion.responder("CalcularParidad", e);
            d3.t2.procesarCustodia = e -> guion.responder("ProcesarCustodiaFondoMutuo", e);
            d3.t2.calculaCustodia = e -> guion.responder("CalculaCustodiaRescate", e);
            d3.t2.datos = new CertificarAporteRescateDatosJdbc(uow);
            d3.t2.formato = SujetoCer.formato(uow);
            d3.t2.diccionario = new DiccionarioJdbc(uow);
            d3.t2.comisiones = new CalcularComisiones(configuracion, dobles, new SeriesFondoJdbc(uow), new ComisionesJdbc(uow), dobles, dobles, d3.t2.paridad);
            d3.crearCargoAbono = e -> guion.responder("CrearCargoAbono", e);
            d3.tesoreria = e -> JsonLegacy.conNulos().fromJson(guion.responder("ProcesarIngresoEgresoTesoreria", JsonLegacy.conNulos().toJsonTree(e)), ProcesosExternos.SalidaTesoreria.class);
            d3.spot = e -> JsonLegacy.conNulos().fromJson(guion.responder("ProcesarIngresoOperacionSpot", JsonLegacy.conNulos().toJsonTree(e)), ProcesosExternos.SalidaSpot.class);
            d3.datos = d3.t2.datos;
            d3.configuracion = configuracion;
            ApiT4.Dependencias d = new ApiT4.Dependencias();
            d.t3 = d3;
            d.crearMovimientoComprobante = e -> guion.responder("CrearMovimientoComprobante", e);
            d.crearComprobante = e -> guion.responder("CrearComprobante", e);
            d.crearAporteReinversion = e -> guion.responder(AR, e);
            d.hijo = entrada -> JsonLegacy.conNulos().fromJson(guion.responder(HIJO, JsonLegacy.conNulos().toJsonTree(entrada)), CertificarAporteRescateContrato.Salida.class);
            d.datos = d3.t2.datos;
            d.conexion = uow;
            d.formato = d3.t2.formato;
            ApiT4.Pasos pasos = ApiT4.crear(d);

            CertificarAporteRescateContexto[] visto = new CertificarAporteRescateContexto[1];
            int[] antes = { -1 };
            CertificarAporteRescateFasesPosteriores posteriores = contexto -> {
                visto[0] = contexto;
                antes[0] = guion.llamadas().size();
                pasos.todos(contexto, () -> frenteJ(raw, contexto));
                throw new ParadaT4();
            };
            CertificarAporteRescatePort servicio = SujetoCer.crear(uow, con, guion, secuencias, posteriores);

            JsonObject res = new JsonObject();
            JsonObject inicial = tablas(raw, caso);
            Throwable fallo = null;
            JsonObject trasLlamada;
            try {
                CasoCer.llamar(servicio, caso.getAsJsonObject("entrada").deepCopy());
                throw new AssertionError("the facade returned without reaching the seam: " + dir.getFileName());
            } catch (Throwable t) {
                fallo = t;
            }
            trasLlamada = tablas(raw, caso);
            raw.rollback();
            if (!(fallo instanceof ParadaT4)) res.addProperty("excepcion", CasoCer.texto(fallo));
            res.add("tablasIniciales", inicial);
            res.add("tablasTrasLaLlamada", trasLlamada);
            res.add("colaboradores", guion.llamadas());
            res.add("secuenciasSolicitadas", secuencias);
            return new Corrida(res, visto[0], fallo, antes[0]);
        }
    }
}
