package cl.bcs.apv.jobs.support;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.apv.domain.port.out.Colaboradores;

/**
 * Fake of the {@link Colaboradores} port for the collaborators OUTSIDE APV that the jobs call (ConsultarEmpresa, ConsultarFlujoCustodiaFondoMutuo,
 * CrearSolicitudAporteRescateNormal, CrearOrdenes, CalcularMontoYCobrosOrden, ConsultarCustodiaPortafolio, ConsultarFlujoCaja, AgregarLog and the
 * stored procedure {@code StoredProcedure}). It answers from the case and traces every call as {@code {servicio, entrada}}; it reproduces what the
 * legacy oracle fakes (legacy-oracle/shims, class Hooks) answered:
 * <ul>
 *   <li>{@code colaboradores.<Servicio>}: an object answers every call, an array answers the n-th call (the last element repeats); with no script the
 *       answer is an empty object, which the caller must read as the legacy empty output (absent lists are empty, an explicit null is null);</li>
 *   <li>{@code {"_lanza": {"clase", "mensaje"}}} makes the call throw (same exception factory as the oracle);</li>
 *   <li>{@code ConsultarEmpresa} with no script throws {@code Exception("Empresa no existe")} (ConsultarEmpresa.java:40-41);</li>
 *   <li>{@code StoredProcedure} ({@code {procedimiento, entrada}}) answers {@code colaboradores.SalidaProcedimiento.<procedure>}; for the price
 *       procedure the trace also holds {@code tablaTemporal}, the rows of the local temporary table {@code #p_Instrumentos} the HANA procedure reads.</li>
 * </ul>
 */
public class ColaboradoresDeCaso implements Colaboradores {

    static final String SP_PRECIO = "BSCL.Plataforma.BD.Servicios::INS.ConsultarInstrumentoPrecioBolsa";

    private final JsonObject guion;
    private final CasoJob caso;
    private final List<JsonObject> traza = new ArrayList<>();
    private final Map<String, Integer> llamadas = new HashMap<>();

    public ColaboradoresDeCaso(CasoJob caso) {
        this.caso = caso;
        this.guion = caso.colaboradores();
    }

    public List<JsonObject> traza() {
        return traza;
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        JsonObject registro = new JsonObject();
        registro.addProperty("servicio", servicio);
        JsonObject copia = entrada.deepCopy();
        registro.add("entrada", copia);
        traza.add(registro);
        if (servicio.equals("StoredProcedure")) {
            return procedimiento(copia);
        }
        JsonElement g = guionado(servicio);
        lanzarSiCorresponde(g);
        if (g == null) {
            if (servicio.equals("ConsultarEmpresa")) {
                sneaky(new Exception("Empresa no existe"));
            }
            return new JsonObject();
        }
        return g.getAsJsonObject().deepCopy();
    }

    private JsonElement guionado(String servicio) {
        int n = llamadas.merge(servicio, 1, Integer::sum) - 1;
        JsonElement g = guion.get(servicio);
        if (g != null && g.isJsonArray()) {
            JsonArray a = g.getAsJsonArray();
            g = a.size() == 0 ? null : a.get(Math.min(n, a.size() - 1));
        }
        return g == null || g.isJsonNull() ? null : g;
    }

    private JsonObject procedimiento(JsonObject entrada) {
        String nombre = entrada.get("procedimiento").getAsString();
        if (nombre.equals(SP_PRECIO)) {
            entrada.add("tablaTemporal", tablaTemporal());
        }
        JsonElement todas = guion.get("SalidaProcedimiento");
        JsonElement g = todas != null && todas.isJsonObject() ? todas.getAsJsonObject().get(nombre) : null;
        lanzarSiCorresponde(g);
        JsonObject o = new JsonObject();
        if (g != null && g.isJsonObject()) {
            g.getAsJsonObject().entrySet().forEach(e -> o.add(e.getKey(), e.getValue().deepCopy()));
        }
        return o;
    }

    /** Rows of the local temporary table #p_Instrumentos at call time (null when the job did not create it), as the oracle dumped them. */
    private JsonElement tablaTemporal() {
        try (Statement st = caso.conexion().createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM #p_Instrumentos")) {
            ResultSetMetaData md = rs.getMetaData();
            JsonArray filas = new JsonArray();
            while (rs.next()) {
                JsonObject fila = new JsonObject();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    Object v = rs.getObject(i);
                    if (v == null) {
                        fila.add(md.getColumnLabel(i), com.google.gson.JsonNull.INSTANCE);
                    } else {
                        fila.addProperty(md.getColumnLabel(i), v instanceof Number num ? new BigDecimal(num.toString()).stripTrailingZeros().toPlainString() : v.toString());
                    }
                }
                filas.add(fila);
            }
            return filas;
        } catch (Exception e) {
            return com.google.gson.JsonNull.INSTANCE;
        }
    }

    static void lanzarSiCorresponde(JsonElement g) {
        if (g != null && g.isJsonObject() && g.getAsJsonObject().has("_lanza")) {
            JsonObject l = g.getAsJsonObject().getAsJsonObject("_lanza");
            sneaky(Excepciones.crear(l.get("clase").getAsString(), l.has("mensaje") && !l.get("mensaje").isJsonNull() ? l.get("mensaje").getAsString() : null));
        }
    }

    @SuppressWarnings("unchecked")
    static <E extends Throwable> void sneaky(Throwable t) throws E {
        throw (E) t;
    }
}
