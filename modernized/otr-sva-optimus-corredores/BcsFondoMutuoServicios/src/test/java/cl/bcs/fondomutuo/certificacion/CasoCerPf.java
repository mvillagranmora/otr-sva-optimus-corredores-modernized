package cl.bcs.fondomutuo.certificacion;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ComisionesJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.DiccionarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.RevisarRequisitosBasicosJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.SeriesFondoJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.CuotasJdbc;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Salida;
import cl.bcs.fondomutuo.certificacionporfiltros.ApiT6a;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.cierre3b.SecuenciaH2;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.normal.SesionHana;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

/**
 * Harness of F3 W3 T6-A (CertificarPorFiltrosSolicitudAporteRescate): OWN COPY of {@link CasoCerPf} (that file is NOT edited). For a case under {@code src/test/resources/fondomutuo-cerpf/cases/<id>/}
 * (case.json, seed.sql and the expected.json RECORDED from the legacy by legacy-oracle/record_cerpf.sh, copied by sync_cerpf.py) it builds the same H2 as T5 (plus {@code schema-cerpf.sql}: the two
 * columns of the view that the select of the filters reads and the table of the loads) and runs the WHOLE request through the PRODUCTION in-process port of CertificarPorFiltros
 * ({@link ApiT6a#crear}) over the REAL JDBC adapter of the select ({@link ApiT6a#datosJdbc}) and the REAL production facade of Certificar (built exactly as {@link CasoCerPf} builds it: REAL JDBC adapters,
 * TYPED ports answered by the scripted collaborators of the case, {@link Guion}).
 *
 * <p>What is observed, as the oracle observes it ({@code PfSpy.certificar}): the ONE collaborator of the class, Certificar, is wrapped so that the complete entry the class built for it and its answer
 * are recorded as a collaborator call named {@link #PF} ({@code argumentos}, {@code respuesta}), and everything Certificar did to ITS collaborators (the child certification records included) goes to
 * the field {@code internas}. The recursion of Certificar is recorded as in T5 ({@link #HIJO}). AR is REPLAYED from the golden (the legacy answers, call by call).
 *
 * <p>The host decision is emulated as the servlet does (CallServlet:441-456,495): counter missing or &gt; 0 => rollback, 0 => commit, an exception => rollback. A missing golden FAILS (never skips); a
 * missing production class FAILS with "T6-A not implemented". {@code -Dcer.autocomprobacion=true} makes {@link #ejecutar(String)} return the golden itself (no production code is run): the proof that the
 * assertions of the rule tests describe what the legacy did. Nothing here touches HANA or the network: H2 in memory only. It lives in the package {@code certificacion} only because the pieces of the T5
 * harness it reuses ({@code ApiT5}, {@code CasoCer}, {@code SujetoCer}) are package-private there; its API is public.
 */
public final class CasoCerPf {

    public static final String HIJO = "CertificarSolicitudAporteRescate.hijo";
    /** the record of the ONE collaborator of the class (the same name as the oracle spy {@code PfSpy.CERTIFICAR}) */
    public static final String PF = "CertificarPorFiltros.certificar";
    static final String AR = "CrearSolicitudAporteReinversion";

    /** Which gates are open (all open by default: the equivalence runs) and which collaborators the ROUTER fails for (503 after the writes: G5). */
    public static final class Opciones {
        public boolean g1 = true;
        public boolean g2 = true;
        public boolean g3 = true;
        public boolean g4 = true;
        public boolean gCustodia = true;
        public boolean g6 = true;
        public boolean g8 = true;
        public Set<String> routerCae = Set.of();

        public static Opciones abiertas() {
            return new Opciones();
        }

        public Opciones cerrando(String compuerta) {
            switch (compuerta) {
                case "G1" -> g1 = false;
                case "G2" -> g2 = false;
                case "G3" -> g3 = false;
                case "G4" -> g4 = false;
                case "GCUS" -> gCustodia = false;
                case "G6" -> g6 = false;
                case "G8" -> g8 = false;
                default -> throw new IllegalArgumentException(compuerta);
            }
            return this;
        }

        public Opciones routerCae(String... colaboradores) {
            this.routerCae = Set.of(colaboradores);
            return this;
        }
    }

    /** What one run leaves: {@code volcado} has the SAME shape as a golden of the oracle. */
    public static final class Corrida {
        public final JsonObject volcado;
        public final JsonObject salida;
        public final String excepcionTexto;
        public final Throwable excepcion;
        public final boolean delGolden;

        Corrida(JsonObject volcado, JsonObject salida, String excepcionTexto, Throwable excepcion, boolean delGolden) {
            this.volcado = volcado;
            this.salida = salida;
            this.excepcionTexto = excepcionTexto;
            this.excepcion = excepcion;
            this.delGolden = delGolden;
        }

        public boolean termino() {
            return excepcionTexto == null;
        }
    }

    private static final String[] ESQUEMA = { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" };

    private CasoCerPf() {
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the cases
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    /** the resources of the T1..T5 harness (schemas and bases shared with Certificar) */
    public static Path recursos() {
        return CasoCer.recursos();
    }

    /** {@code src/test/resources/fondomutuo-cerpf}: the cases and the stand-in schema of the select of the filters */
    public static Path recursosPf() {
        try {
            return Path.of(CasoCerPf.class.getClassLoader().getResource("fondomutuo-cerpf/cases").toURI()).getParent();
        } catch (Exception e) {
            throw new AssertionError("fixture directory fondomutuo-cerpf missing (run legacy-oracle/sync_cerpf.py)", e);
        }
    }

    public static Path directorio() {
        return recursosPf().resolve("cases");
    }

    public static List<String> casos() {
        List<String> ids = new ArrayList<>();
        Path dir = directorio();
        if (!Files.isDirectory(dir)) throw new AssertionError("fixture directory fondomutuo-cerpf/cases missing (run legacy-oracle/sync_cerpf.py)");
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
        if (!Files.exists(p)) throw new AssertionError("golden missing (record it with legacy-oracle/record_cerpf.sh): " + p);
        return CasoCer.leer(p);
    }

    public static String slug(String id) {
        return id.substring(id.indexOf('_') + 1);
    }

    /** The id of the case with the given slug (the part after {@code FMM-CER-nnn_}). */
    public static String idPorSlug(String slug) {
        return casos().stream().filter(c -> slug(c).equals(slug)).findFirst().orElseThrow(() -> new AssertionError("no T6-A case with slug " + slug));
    }

    /** The id of the case {@code FMM-CERPF-<nnn>_...} (the numbering is deterministic: make_cases_cerpf.py). */
    public static String id(int nnn) {
        String prefijo = String.format("FMM-CERPF-%03d_", nnn);
        return casos().stream().filter(c -> c.startsWith(prefijo)).findFirst().orElseThrow(() -> new AssertionError("no case " + prefijo + "* under fondomutuo-cerpf/cases"));
    }

    /** The ids that reached Certificar, in order (the entry the class built for it, recorded as the call {@link #PF}); empty when the legacy threw before calling it ({@link #llamoACertificar}). */
    public static List<Long> idsHaciaCertificar(Corrida c) {
        List<Long> ids = new ArrayList<>();
        for (JsonElement s : entradaDeCertificar(c).getAsJsonArray("p_Solicitudes")) ids.add(s.getAsJsonObject().get("IdSolicitudAporteRescate").getAsLong());
        return ids;
    }

    public static boolean llamoACertificar(Corrida c) {
        for (JsonElement e : c.volcado.getAsJsonArray("colaboradores")) if (PF.equals(e.getAsJsonObject().get("colaborador").getAsString())) return true;
        return false;
    }

    /** The complete entry of the call to Certificar (nulls written): header, p_NotificaOWP, p_CertificacionAporteSwitchSimulado, p_Solicitudes. */
    public static JsonObject entradaDeCertificar(Corrida c) {
        for (JsonElement e : c.volcado.getAsJsonArray("colaboradores")) {
            JsonObject o = e.getAsJsonObject();
            if (PF.equals(o.get("colaborador").getAsString())) return o.getAsJsonArray("argumentos").get(0).getAsJsonObject();
        }
        throw new AssertionError("the class never called Certificar in this run");
    }

    public static List<String> refs(String id) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : caso(id).getAsJsonArray("ref")) out.add(e.getAsString());
        return out;
    }

    public static boolean conTag(String id, String tag) {
        return refs(id).contains(tag);
    }

    /** The cases that carry a tag such as {@code PF-filtro:Nacional} (in the order of the directory). */
    public static List<String> conTag(String tag) {
        return casos().stream().filter(c -> conTag(c, tag)).toList();
    }

    public static boolean esExcepcion(String id) {
        return conTag(id, "PF-modo:excepcion");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // running
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    /** The run the rule tests use: the production facade, or the golden itself with {@code -Dcer.autocomprobacion=true}. */
    public static Corrida ejecutar(String id) throws Exception {
        if (Boolean.getBoolean("cer.autocomprobacion")) return desdeGolden(id);
        return ejecutarReal(id, Opciones.abiertas());
    }

    /** ALWAYS the production facade (the equivalence, determinism and gate tests use this one, never the self-check of {@link #ejecutar(String)}). */
    public static Corrida ejecutarReal(String id, Opciones opciones) throws Exception {
        java.util.TimeZone zona = java.util.TimeZone.getDefault();
        java.util.Locale idioma = java.util.Locale.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            org.h2.util.DateTimeUtils.resetCalendar();
            return correr(directorio().resolve(id), opciones);
        } finally {
            java.util.TimeZone.setDefault(zona);
            java.util.Locale.setDefault(idioma);
            org.h2.util.DateTimeUtils.resetCalendar();
        }
    }

    static Corrida desdeGolden(String id) {
        JsonObject g = golden(id).deepCopy();
        JsonObject salida = g.has("salidaCruda") ? com.google.gson.JsonParser.parseString(g.get("salidaCruda").getAsString()).getAsJsonObject() : null;
        return new Corrida(g, salida, g.has("excepcion") ? g.get("excepcion").getAsString() : null, null, true);
    }

    private static void esquema(Connection raw, JsonObject caso) throws Exception {
        Path base = recursos();
        for (String f : ESQUEMA) CasoCer.script(raw, Files.readString(base.getParent().resolve("fondomutuo-3a").resolve(f), StandardCharsets.UTF_8));
        for (JsonElement e : caso.getAsJsonArray("esquemasExtra")) {
            Path f = e.getAsString().equals("schema-cerpf.sql") ? recursosPf().resolve("schema-cerpf.sql") : base.resolve(e.getAsString());   // the only schema of T6-A; the rest is shared with Certificar
            CasoCer.script(raw, Files.readString(f, StandardCharsets.UTF_8));
        }
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

    /** The collaborators of F-D that the REAL {@code CalcularComisiones} reaches: the company, the custody flow and the quota value (the oracle's registered doubles). */
    private static final class DoblesComision implements EmpresaPort, CustodiaPort, PreciosTasasPort {
        private final Guion guion;

        DoblesComision(Guion guion) {
            this.guion = guion;
        }

        @Override
        public InformacionEmpresa informacion(Long idEmpresa) {
            return JsonLegacy.plano().fromJson(guion.responder("ConsultarEmpresa", new JsonPrimitive(idEmpresa)), InformacionEmpresa.class);
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

    /** The calls of the golden in CHRONOLOGICAL order with the records of the child expanded in place (its inner calls come before the record, as the oracle left them). */
    static void aplanar(JsonArray calls, List<JsonObject> salida) {
        for (JsonElement e : calls) {
            JsonObject c = e.getAsJsonObject();
            if (HIJO.equals(c.get("colaborador").getAsString()) || PF.equals(c.get("colaborador").getAsString())) {
                if (c.has("internas")) aplanar(c.getAsJsonArray("internas"), salida);
            } else {
                salida.add(c);
            }
        }
    }

    /** The answers AR gave in the legacy run, in chronological order (parent and child), as one-shot scripted answers. */
    private static JsonObject repeticionDeAr(JsonObject golden) {
        JsonArray respuestas = new JsonArray();
        if (golden.has("colaboradores")) {
            List<JsonObject> todas = new ArrayList<>();
            aplanar(golden.getAsJsonArray("colaboradores"), todas);
            for (JsonObject c : todas) {
                if (!AR.equals(c.get("colaborador").getAsString())) continue;
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

    private static JsonArray moverInternas(JsonArray calls, int desde) {
        JsonArray out = new JsonArray();
        while (calls.size() > desde) out.add(calls.remove(desde));
        return out;
    }

    private static String nombreDeClase(Throwable t) {
        return t.getClass() == RuntimeException.class ? "Exception" : t.getClass().getSimpleName();
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneaky(Throwable t) throws E {
        throw (E) t;
    }

    private static Corrida correr(Path dir, Opciones opciones) throws Exception {
        Path base = recursos();
        JsonObject caso = CasoCer.leer(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:cerpf" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
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
            doubles.add(AR, repeticionDeAr(golden));
            Guion guion = new Guion(doubles);
            JsonArray secuencias = new JsonArray();
            ConexionUnidadDeTrabajo uow = new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(con, true));

            // ---- the facade over the REAL adapters and the scripted ports
            ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(uow);
            DiccionarioJdbc diccionario = new DiccionarioJdbc(uow);
            DoblesComision dobles = new DoblesComision(guion);
            ParidadPort paridad = e -> guion.responder("CalcularParidad", e);
            CertificarAporteRescateFabrica.Dependencias d = new CertificarAporteRescateFabrica.Dependencias();
            d.configuracion = configuracion;
            d.diccionario = diccionario;
            d.basicos = new RevisarRequisitosBasicosJdbc(uow, diccionario);
            d.calendario = new CalendarioJdbc(uow, configuracion);
            d.series = new SeriesFondoJdbc(uow);
            d.cuotas = new CuotasJdbc(uow);
            d.precios = e -> guion.responder("EntregarPreciosTasas", e);
            d.paridad = paridad;
            d.datos = new CertificarAporteRescateDatosJdbc(uow);
            d.escritura = new CertificarAporteRescateEscrituraJdbc(uow);
            d.secuencias = new SecuenciaH2(con, secuencias);
            d.posteriores = null;   // NO seam: the facade runs every front

            boolean asyncParametros = caso.has("asyncParametros") && caso.get("asyncParametros").getAsBoolean();
            ApiT5.Responder responder = (colaborador, argumento, retorno) -> {
                JsonElement a = argumento == null ? JsonNull.INSTANCE : argumento instanceof JsonElement je ? je : JsonLegacy.conNulos().toJsonTree(argumento);
                if (asyncParametros && "NotificarEstadoOrdenFondoMutuo".equals(colaborador)) {
                    // the ADAPTER of the asynchronous notification (not the facade) reads the two parameters of the batch server BEFORE it sends anything (legacy logicaAsync): the case says that
                    // this run reads them; a missing one raises the legacy Exception("Parametro ... en modulo PLATAFORMA no encontrado") and the call is not recorded
                    Long empresa = a.getAsJsonObject().get("p_IdEmpresa").getAsLong();
                    configuracion.texto(empresa, "PLATAFORMA", "PLA.CallBatchURL");
                    configuracion.texto(empresa, "PLATAFORMA", "PLA.CallBatchToken");
                }
                if (opciones.routerCae.contains(colaborador)) {
                    JsonObject rec = new JsonObject();
                    rec.addProperty("colaborador", colaborador);
                    JsonArray args = new JsonArray();
                    args.add(a);
                    rec.add("argumentos", args);
                    rec.addProperty("respuesta", "error: ServicioNoServidoException");
                    guion.llamadas().add(rec);
                    throw new ServicioNoServidoException("ROUTER", "the router answers 503 for " + colaborador);
                }
                JsonObject resp = guion.responder(colaborador, a);
                if (retorno == void.class || retorno == Void.class) return null;
                if (resp == null) return null;
                if (retorno == JsonObject.class) return resp;
                return JsonLegacy.conNulos().fromJson(resp, retorno);
            };
            Map<String, Object> nuevos = new LinkedHashMap<>();
            nuevos.put("flujoCaja", new ApiT5.Puerto("SaldoFlujoCaja"));
            nuevos.put("procesarCustodia", new ApiT5.Puerto("ProcesarCustodiaFondoMutuo"));
            nuevos.put("calculaCustodia", new ApiT5.Puerto("CalculaCustodiaRescate"));
            nuevos.put("comisiones", new CalcularComisiones(configuracion, dobles, new SeriesFondoJdbc(uow), new ComisionesJdbc(uow), dobles, dobles, paridad));
            nuevos.put("formato", SujetoCer.formato(uow));
            nuevos.put("crearCargoAbono", new ApiT5.Puerto("CrearCargoAbono"));
            nuevos.put("tesoreria", new ApiT5.Puerto("ProcesarIngresoEgresoTesoreria"));
            nuevos.put("spot", new ApiT5.Puerto("ProcesarIngresoOperacionSpot"));
            nuevos.put("facturacion", new ApiT5.Puerto("crearMovimientoComprobante", "CrearMovimientoComprobante", "crearComprobante", "CrearComprobante"));
            nuevos.put("crearAporteReinversion", new ApiT5.Puerto(AR));
            nuevos.put("escrituraMonto", new CertificarAporteRescateEscrituraMontoJdbc(uow));
            nuevos.put("escrituraCierre", ApiT5.escrituraCierre(uow));
            nuevos.put("notificacionCertificacion", new ApiT5.Puerto("NotificarEstadoOrdenFondoMutuo"));
            nuevos.put("notificacionOwp", new ApiT5.Puerto("NotificarEstadoOrdenesOWP"));
            nuevos.put("agregarLog", new ApiT5.Puerto("AgregarLog"));
            nuevos.put("compuertas", ApiT5.compuertas(opciones.g1, opciones.g2, opciones.g3, opciones.g4, opciones.gCustodia, opciones.g6, opciones.g8));
            UnaryOperator<CertificacionHijaPort> envoltura = interna -> entrada -> {
                JsonArray calls = guion.llamadas();
                int antes = calls.size();
                JsonObject rec = new JsonObject();
                rec.addProperty("colaborador", HIJO);
                rec.addProperty("metodo", "logica");
                JsonArray args = new JsonArray();
                args.add(JsonLegacy.conNulos().toJsonTree(entrada));
                rec.add("argumentos", args);
                try {
                    Salida s = interna.certificar(entrada);
                    rec.add("respuesta", JsonLegacy.conNulos().toJsonTree(s));
                    rec.add("internas", moverInternas(calls, antes));
                    calls.add(rec);
                    return s;
                } catch (Throwable t) {
                    rec.addProperty("respuesta", "error: " + nombreDeClase(t));
                    rec.add("internas", moverInternas(calls, antes));
                    calls.add(rec);
                    CasoCerPf.<RuntimeException>sneaky(t);
                    return null;
                }
            };
            nuevos.put("envolverHija", envoltura);
            ApiT5.asignar(d, nuevos, responder);
            CertificarAporteRescatePort certificar = CertificarAporteRescateFabrica.crear(d);
            // the record of the ONE collaborator of the class (what PfSpy.certificar records in the oracle): the complete entry, the answer, and what Certificar did inside it
            CertificarAporteRescatePort registrado = entrada -> {
                JsonArray calls = guion.llamadas();
                int antes = calls.size();
                JsonObject rec = new JsonObject();
                rec.addProperty("colaborador", PF);
                rec.addProperty("metodo", "logica");
                JsonArray args = new JsonArray();
                args.add(JsonLegacy.conNulos().toJsonTree(entrada));
                rec.add("argumentos", args);
                try {
                    Salida s = certificar.logica(entrada);
                    rec.add("respuesta", JsonLegacy.conNulos().toJsonTree(s));
                    rec.add("internas", moverInternas(calls, antes));
                    calls.add(rec);
                    return s;
                } catch (Throwable t) {
                    rec.addProperty("respuesta", "error: " + CasoCer.clase(t));
                    rec.add("internas", moverInternas(calls, antes));
                    calls.add(rec);
                    CasoCerPf.<RuntimeException>sneaky(t);
                    return null;
                }
            };
            ApiT6a.PorFiltros servicio = ApiT6a.crear(registrado, ApiT6a.datosJdbc(uow));

            JsonObject res = new JsonObject();
            JsonObject inicial = tablas(raw, caso);
            Throwable fallo = null;
            JsonObject salida = null;
            try {
                salida = servicio.llamar(caso.getAsJsonObject("entrada").deepCopy());
            } catch (Throwable t) {
                if (t instanceof AssertionError && t.getMessage() != null && t.getMessage().startsWith("T6-A")) throw t;   // "not implemented" is never an observation of the legacy
                fallo = t;
            }
            JsonObject trasLlamada = tablas(raw, caso);
            String decision;
            if (fallo != null) {
                raw.rollback();
                decision = fallo instanceof ServicioNoServidoException ? "ROLLBACK_HTTP503" : "ROLLBACK_HTTP500";
                res.addProperty("excepcion", CasoCer.texto(fallo));
            } else {
                JsonElement cnt = salida.get("P_OUT_ERRORESCONTADOR");
                boolean confirma = cnt != null && !cnt.isJsonNull() && cnt.getAsLong() == 0;
                decision = confirma ? "COMMIT" : "ROLLBACK";
                if (confirma) raw.commit();
                else raw.rollback();
                res.add("salida", salida);
                res.addProperty("salidaCruda", salida.toString());
            }
            res.addProperty("decisionServlet", decision);
            res.add("tablasIniciales", inicial);
            res.add("tablasTrasLaLlamada", trasLlamada);
            res.add("tablas", tablas(raw, caso));
            res.add("colaboradores", guion.llamadas());
            res.add("secuenciasSolicitadas", secuencias);
            res.add("previas", new JsonArray());
            JsonObject ses = new JsonObject();
            ses.add("tablasLocalesSinDrop", new JsonArray());
            ses.add("commitsAutonomos", new JsonArray());
            res.add("sesion", ses);
            return new Corrida(res, salida, fallo == null ? null : CasoCer.texto(fallo), fallo, false);
        }
    }
}
