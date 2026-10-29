package cl.bcs.fondomutuo.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.adapter.out.http.RouterClient;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * Fixtures of the F2 W1 tests with the REAL Tesoreria library: one H2 database ({@code MODE=LEGACY}) with Tesoreria's family-A schema and base seed (copied
 * from BcsTesoreriaServicios, synthetic data) plus {@code seed-fondomutuo.sql}, reached through ONE connection wrapped by {@link H2Hana} (copy of Tesoreria's
 * dialect bridge: its production SQL runs unmodified). The connection starts in auto-commit, as a pooled connection: only a Spring transaction makes work
 * roll back. Plus the test doubles of Tesoreria's three configuration ports and of the router. No HANA, no credentials.
 */
final class TesoreriaH2 {

    private TesoreriaH2() {
    }

    static SingleConnectionDataSource nueva() throws Exception {
        Connection raw = DriverManager.getConnection("jdbc:h2:mem:fmtes" + UUID.randomUUID().toString().replace("-", "") + ";MODE=LEGACY;DB_CLOSE_DELAY=-1", "sa", "");
        for (String f : new String[] { "schema-tesoreria-a.sql", "base-tesoreria-a.sql", "seed-fondomutuo.sql" }) {
            ScriptUtils.executeSqlScript(raw, new ClassPathResource("fondomutuo-tes/" + f));
        }
        raw.setAutoCommit(true);
        return new SingleConnectionDataSource(H2Hana.wrap(raw), true);
    }

    static long contar(javax.sql.DataSource ds, String tabla) {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM \"" + tabla + "\"")) {
            rs.next();
            return rs.getLong(1);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Tesoreria's configuration ports as in its family-A harness (seed/config.json): no table needed. */
    static final Map<String, String> CONFIG = Map.of("PLATAFORMA/PLA.FechaSistema", "2025-04-01", "PLATAFORMA/PLA.SegundosZonaHoraria", "-10800",
            "TESORERIA/SEPARACOMPROBANTECOMSPT", "N", "TESORERIA/LIQUIDACIONPARCIALENVIAACTA", "N", "TESORERIA/PERMITECUENTABANCARSOCIONEGOCIO", "N");

    static cl.bcs.tesoreria.port.out.ConfiguracionSistema configuracion() {
        return (empresa, modulo, nombre) -> CONFIG.get(modulo + "/" + nombre);
    }

    static cl.bcs.tesoreria.port.out.DiccionarioMensajes diccionario() {
        return (empresa, codigo, valores) -> "[" + codigo + "]" + (valores == null ? "" : valores.toString());
    }

    static cl.bcs.tesoreria.port.out.Secuencias secuencias() {
        Map<String, Long> s = new ConcurrentHashMap<>(Map.of("BSCL.Tesoreria.BD.Secuencias::IngresoEgreso", 5000L,
                "BSCL.Tesoreria.BD.Secuencias::IngresoEgresoFormaDePago", 6000L));
        return nombre -> s.merge(nombre, 1L, Long::sum) - 1;
    }

    /** The router seen by Tesoreria's Plataforma collaborators: RevisarRequisitosBasicos without messages, AgregarLog counter 0; or a 503. */
    static final class Router extends RouterClient {
        final List<String> llamadas = new ArrayList<>();
        volatile String noServido;   // legacy class that answers 503

        Router() {
            super("http://localhost:1", java.time.Duration.ofMillis(100), java.time.Duration.ofMillis(100), "");
        }

        @Override
        public JsonObject invocar(String clase, JsonObject params) {
            llamadas.add(clase);
            if (clase.equals(noServido)) throw new ServicioNoServidoException("ROUTER", clase + " not served (HTTP 503)");
            JsonObject o = new JsonObject();
            if (clase.endsWith("RevisarRequisitosBasicos")) o.add("p_out_Mensajes", new com.google.gson.JsonArray());
            else o.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            return o;
        }
    }

    /** A non-CIN contribution of person 101 / portfolio 8001 of the Tesoreria seed, as Normal hands it to the helper. */
    static SolicitudAporteRescate aporteTra() {
        SolicitudAporteRescate s = new SolicitudAporteRescate();
        s.IdInterno = 1L;
        s.IdSolicitudAporteRescate = 11413788L;
        s.IdPortafolio = 8001L;
        s.IdPersona = 101L;
        s.TipoSolicitud = "APO";
        s.IdTipoMovimientoFondoMutuo = "APONORM";
        s.IdMoneda = "CLP";
        s.MontoPagoLocal = new BigDecimal("10000");
        s.IdMedioDePago = "TRA";
        s.IdPreferenciaPagoPortafolio = 9001L;
        s.FechaEmision = java.sql.Date.valueOf("2025-04-01");
        s.FechaAceptacion = java.sql.Date.valueOf("2025-04-01");
        s.ParidadFechaInversion = BigDecimal.ONE;
        s.GeneraSpot = "N";
        return s;
    }
}
