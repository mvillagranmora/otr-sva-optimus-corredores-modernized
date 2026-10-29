package cl.bcs.fondomutuo.rescatereinversion;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo;
import cl.bcs.custodia.flujo.h2.HanaH2;
import cl.bcs.fondomutuo.adapter.out.jdbc.CalendarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConfiguracionSistemaJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.CuotasJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.DiccionarioJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.FechasJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.PerfilFondoMutuoJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.RevisarRequisitosBasicosJdbc;
import cl.bcs.fondomutuo.adapter.out.jdbc.SeriesFondoJdbc;
import cl.bcs.fondomutuo.agf.AgfJdbc;
import cl.bcs.fondomutuo.cierre3b.Guion;
import cl.bcs.fondomutuo.cierre3b.SecuenciaH2;
import cl.bcs.fondomutuo.comisionnn.ComisionNnJdbc;
import cl.bcs.fondomutuo.custodia.EsquemaCustodiaFusionado;
import cl.bcs.fondomutuo.custodia.FlujoCustodiaEnProceso;
import cl.bcs.fondomutuo.custodia.FlujoCustodiaFmJdbc;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.patrimonio.PatrimonioEmisorJdbc;
import cl.bcs.fondomutuo.requisitos.RequisitosJdbc;

/**
 * F3 custody in process, END TO END: {@code CrearSolicitudRescateReinversion} on H2 with the REAL custody flow ({@link FlujoCustodiaEnProceso} over
 * {@code ConsultarFlujoCustodiaFlujo}) instead of the scripted double of the FMM-RRE goldens, G-CUS OPEN, in the transaction of the request. FondoMutuo and
 * Custodia share ONE database (the custody stand-in schema is merged into the FondoMutuo one, {@link EsquemaCustodiaFusionado}) and ONE connection: the custody
 * position of the case (quantity 1000 of instrument 900, client 100, portfolio 500, regime GEN: {@code custodia-rre-seed.sql}) is what the double returned, so
 * the answer, the decision, the four tables of the case and every OTHER collaborator call must equal the golden recorded from the legacy; only the call to
 * {@code ConsultarFlujoCustodia} is no longer a collaborator call (it is in process). The goldens and the existing doubles are not touched.
 *
 * <p>Harness note: the custody SQL uses a few HANA constructs that H2 needs bridged ({@link HanaH2}, the dialect bridge of the custody harness), so the
 * in-process adapter gets a thin proxy of the SAME physical connection, with the bridge on top; the sequences of Custodia are read from the H2 sequences of the
 * same name on that connection and recorded apart from those of FondoMutuo.
 */
class RreCustodiaEnProcesoTest {

    private static final String[] ESQUEMA = { "schema-types.sql", "schema-fmm.sql", "schema-keys.sql", "schema-session.sql", "seed/base-fmm.sql" };
    private static final String CUSTODIA = "ConsultarFlujoCustodia";

    /** What a run leaves: the dump of the case (as CasoRre) plus what the custody side did. */
    record Resultado(JsonObject volcado, int llamadasAlAdaptador, int matrizUsoRestante) {
    }

    static Resultado ejecutar(String id, boolean custodiaAbierta) throws Exception {
        java.util.TimeZone zona = java.util.TimeZone.getDefault();
        java.util.Locale idioma = java.util.Locale.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            org.h2.util.DateTimeUtils.resetCalendar();
            return correr(CasoRre.recursos().resolve("cases").resolve(id), custodiaAbierta);
        } finally {
            java.util.TimeZone.setDefault(zona);
            java.util.Locale.setDefault(idioma);
            org.h2.util.DateTimeUtils.resetCalendar();
        }
    }

    private static Resultado correr(Path dir, boolean custodiaAbierta) throws Exception {
        Path base = CasoRre.recursos();
        JsonObject caso = CasoRre.leer(dir.resolve("case.json"));
        String url = "jdbc:h2:mem:rrecus" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
        try (Connection raw = DriverManager.getConnection(url, "sa", "")) {
            for (String f : ESQUEMA) CasoRre.script(raw, Files.readString(base.getParent().resolve("fondomutuo-3a").resolve(f), StandardCharsets.UTF_8));
            for (JsonElement e : caso.getAsJsonArray("esquemasExtra")) CasoRre.script(raw, Files.readString(base.resolve(e.getAsString()), StandardCharsets.UTF_8));
            EsquemaCustodiaFusionado.aplicar(raw);
            if (Files.exists(dir.resolve("seed.sql"))) CasoRre.script(raw, Files.readString(dir.resolve("seed.sql"), StandardCharsets.UTF_8));
            EsquemaCustodiaFusionado.semillaRescateReinversion(raw);
            raw.commit();
            raw.setAutoCommit(false);

            SesionRre sesion = new SesionRre(raw, url, caso.has("reloj") ? caso.get("reloj").getAsString() : CasoRre.RELOJ);
            Connection con = sesion.conexion();
            JsonObject doubles = CasoRre.leer(base.getParent().resolve("fondomutuo-3a/seed/doubles-base.json"));
            if (caso.has("colaboradores")) {
                for (String k : caso.getAsJsonObject("colaboradores").keySet()) doubles.add(k, caso.getAsJsonObject("colaboradores").get(k));
            }
            // the "ConsultarFlujoCustodia" double of the case stays in the script but nobody calls it: custody is real here (the golden comparison drops that call)
            Guion guion = new Guion(doubles);
            JsonArray secuencias = new JsonArray();
            JsonArray secuenciasCustodia = new JsonArray();
            Compuertas compuertas = new Compuertas("NORMAL,RESCATE_REINVERSION", true, false, false, false, custodiaAbierta);

            ConexionUnidadDeTrabajo conexion = new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(con, true));
            ConexionUnidadDeTrabajo conexionCustodia = new ConexionUnidadDeTrabajo(new SingleConnectionDataSource(HanaH2.wrap(con), true));
            ConsultarFlujoCustodiaFlujo biblioteca = new ConsultarFlujoCustodiaFlujo(FlujoCustodiaEnProceso.valorizadorCerrado(),
                    (nombre, cantidad) -> conexionCustodia.usar(c -> new SecuenciaH2(c, secuenciasCustodia).siguientes(nombre, cantidad)));
            AtomicInteger llamadas = new AtomicInteger();
            FlujoCustodiaEnProceso adaptador = new FlujoCustodiaEnProceso(conexionCustodia, biblioteca) {
                @Override
                public JsonObject consultarFlujo(JsonObject entrada) {
                    llamadas.incrementAndGet();
                    return super.consultarFlujo(entrada);
                }
            };

            RescateReinversionFabrica.Dependencias d = new RescateReinversionFabrica.Dependencias();
            ConfiguracionSistemaJdbc configuracion = new ConfiguracionSistemaJdbc(conexion);
            DiccionarioJdbc diccionario = new DiccionarioJdbc(conexion);
            d.configuracion = configuracion;
            d.diccionario = diccionario;
            d.basicos = new RevisarRequisitosBasicosJdbc(conexion, diccionario);
            d.calendario = new CalendarioJdbc(conexion, configuracion);
            d.empresas = idEmpresa -> new Gson().fromJson(guion.responder("ConsultarEmpresa", new JsonPrimitive(idEmpresa)), InformacionEmpresa.class);
            d.series = new SeriesFondoJdbc(conexion);
            d.requisitosDatos = new RequisitosJdbc(conexion);
            d.comisionNnDatos = new ComisionNnJdbc(conexion);
            d.datos = new RreJdbc(conexion);
            d.bloqueo = new BloqueoReinversionJdbc(conexion);
            d.perfilPort = new PerfilFondoMutuoJdbc(conexion);
            d.fechasPort = new FechasJdbc(conexion);
            d.cuotasPort = new CuotasJdbc(conexion);
            d.patrimonioPort = new PatrimonioEmisorJdbc(conexion);
            d.flujoCustodiaDatos = new FlujoCustodiaFmJdbc(conexion);
            d.agfDatos = new AgfJdbc(conexion);
            d.secuencias = new SecuenciaH2(con, secuencias);
            d.paridad = e -> guion.responder("CalcularParidad", e);
            d.precios = e -> guion.responder("EntregarPreciosTasas", e);
            d.custodia = adaptador;
            d.log = e -> guion.responder("AgregarLog", e);
            d.batch = e -> guion.responder("InscribirProcesoBatch", e);
            d.productos = e -> guion.responder("ConsultarProductosPortafolio", e);
            d.compuertas = compuertas;
            RescateReinversionPort rre = RescateReinversionFabrica.crear(d);

            JsonObject res = new JsonObject();
            String decision;
            try {
                JsonObject out = new RescateReinversionJavaCallHandler(rre, compuertas).call(con, caso.getAsJsonObject("entrada").deepCopy());
                res.addProperty("salidaCruda", out.toString());
                JsonElement cnt = out.get("P_OUT_ERRORESCONTADOR");
                boolean commit = cnt != null && !cnt.isJsonNull() && cnt.getAsLong() == 0;
                decision = commit ? "COMMIT" : "ROLLBACK";
                if (commit) raw.commit(); else raw.rollback();
            } catch (Throwable t) {
                raw.rollback();
                decision = t instanceof ServicioNoServidoException ? "ROLLBACK_503" : "ROLLBACK_HTTP500";
                res.addProperty("excepcion", t.getClass().getSimpleName() + ": " + t.getMessage());
                if (t instanceof ServicioNoServidoException) res.addProperty("compuerta", ((ServicioNoServidoException) t).compuerta());
            }
            res.addProperty("decisionServlet", decision);
            JsonObject tablas = new JsonObject();
            for (JsonElement el : caso.getAsJsonArray("tablas")) {
                JsonObject t = el.getAsJsonObject();
                tablas.add(t.get("nombre").getAsString(), volcar(raw, t.get("nombre").getAsString(), t.get("orden").getAsString()));
            }
            res.add("tablas", tablas);
            res.add("colaboradores", guion.llamadas());
            res.add("secuenciasCustodia", secuenciasCustodia);
            int restante;
            try (var st = raw.createStatement(); var rs = st.executeQuery("SELECT COUNT(*) FROM \"BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso\"")) {
                rs.next();
                restante = rs.getInt(1);
            }
            return new Resultado(res, llamadas.get(), restante);
        }
    }

    /** Same rendering as {@code CasoRre.volcar} (private there): numbers normalised, dates as text, NULL as JSON null. */
    private static JsonArray volcar(Connection conn, String table, String orderBy) throws Exception {
        JsonArray rows = new JsonArray();
        try (var st = conn.createStatement(); var rs = st.executeQuery("SELECT * FROM \"" + table + "\" ORDER BY " + orderBy)) {
            var md = rs.getMetaData();
            while (rs.next()) {
                JsonObject row = new JsonObject();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    Object v = rs.getObject(i);
                    String col = md.getColumnLabel(i);
                    if (v == null) row.add(col, com.google.gson.JsonNull.INSTANCE);
                    else if (v instanceof Number) row.add(col, new JsonPrimitive(new java.math.BigDecimal(new java.math.BigDecimal(v.toString()).stripTrailingZeros().toPlainString())));
                    else if (v instanceof java.util.Date) row.addProperty(col, rs.getString(i));
                    else row.addProperty(col, v.toString());
                }
                rows.add(row);
            }
        }
        return rows;
    }

    /** the collaborator calls of the golden WITHOUT the custody one (in process now) */
    private static String sinCustodia(JsonElement colaboradores) {
        JsonArray n = new JsonArray();
        for (JsonElement x : colaboradores.getAsJsonArray()) {
            if (CUSTODIA.equals(x.getAsJsonObject().get("colaborador").getAsString())) continue;
            n.add(x);
        }
        return RreEquivalenciaTest.texto(n);
    }

    private void igualAlGolden(String id) throws Exception {
        igualAlGolden(id, null, null);
    }

    /**
     * @param escalaReal  text of the output where the REAL custody figure appears with the scale of the library (8 decimals, as the legacy flow prints its
     *                    quantities: "Cantidad":-40.00000000); the golden holds the integer of the scripted double. Null = the output must be identical.
     * @param escalaGolden the same text as the golden spells it. The ONLY accepted difference, and only when the actual output contains {@code escalaReal}.
     */
    private void igualAlGolden(String id, String escalaReal, String escalaGolden) throws Exception {
        JsonObject golden = CasoRre.golden(id);
        Resultado r = ejecutar(id, true);
        JsonObject v = r.volcado();
        assertThat(v.has("excepcion")).as(id + " exception: " + v.get("excepcion")).isEqualTo(golden.has("excepcion"));
        String salida = RreEquivalenciaTest.texto(v.get("salidaCruda"));
        if (escalaReal != null) {
            assertThat(salida).as(id + ": the custody figure carries the scale of the real flow").contains(escalaReal);
            salida = salida.replace(escalaReal, escalaGolden);
        }
        assertThat(salida).as(id + " output text").isEqualTo(RreEquivalenciaTest.texto(golden.get("salidaCruda")));
        assertThat(RreEquivalenciaTest.texto(v.get("decisionServlet"))).as(id + " servlet decision").isEqualTo(RreEquivalenciaTest.texto(golden.get("decisionServlet")));
        assertThat(RreEquivalenciaTest.texto(v.get("tablas"))).as(id + " the four tables").isEqualTo(RreEquivalenciaTest.texto(golden.get("tablas")));
        assertThat(sinCustodia(v.get("colaboradores"))).as(id + " every other collaborator call").isEqualTo(sinCustodia(golden.get("colaboradores")));
        assertThat(r.llamadasAlAdaptador()).as(id + ": the in-process custody was called").isPositive();
        assertThat(r.matrizUsoRestante()).as(id + ": the custody work table is left empty").isZero();
        assertThat(v.getAsJsonArray("secuenciasCustodia")).as(id + ": the library asked for its key sequence").isNotEmpty();
    }

    @Test
    @DisplayName("FMM-RRE-01 (real, one pair) with the REAL custody in process and G-CUS open = the golden of the legacy")
    void parNacionalConCustodiaReal() throws Exception {
        igualAlGolden("FMM-RRE-01_real-un-par-nacional");
    }

    @Test
    @DisplayName("FMM-RRE-41 (custody quantity insufficient, request rejected) with the REAL custody = the golden: the quantity comes from the real position")
    void custodiaInsuficienteConCustodiaReal() throws Exception {
        igualAlGolden("FMM-RRE-41_custodia-insuficiente-break", "custodia 1000.00000000", "custodia 1000");
    }

    @Test
    @DisplayName("G-CUS closed with the in-process adapter present: 503 at the gate, custody never called, nothing written")
    void compuertaCerradaNoLlamaALaBiblioteca() throws Exception {
        Resultado r = ejecutar("FMM-RRE-01_real-un-par-nacional", false);
        JsonObject v = r.volcado();
        assertThat(v.get("decisionServlet").getAsString()).isEqualTo("ROLLBACK_503");
        assertThat(v.get("compuerta").getAsString()).isEqualTo("G-CUS");
        assertThat(r.llamadasAlAdaptador()).as("the gate is evaluated before the custody call").isZero();
        assertThat(v.getAsJsonArray("secuenciasCustodia")).as("no custody sequence consumed").isEmpty();
        assertThat(r.matrizUsoRestante()).isZero();
        for (var e : v.getAsJsonObject("tablas").entrySet()) {
            assertThat(e.getValue().getAsJsonArray()).as(e.getKey() + " untouched (503 rolls back)").isEmpty();
        }
    }
}
