package cl.bcs.apv.familia_b.support;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.domain.port.out.Colaboradores;

/**
 * Test double of the {@link Colaboradores} port for the collaborators OUTSIDE the APV module (Socio de Negocio, Plataforma, Tesoreria, Cuenta Inversion
 * and the HANA stored procedures). It answers exactly what the oracle fakes answered: the script {@code colaboradores.<servicio>} of case.json
 * ({@code salidas}: k-th call answers k-th element, the last repeats; {@code salida}: every call; {@code lanza}: every call throws), so the modern
 * use case receives what the legacy code received. Stored procedures arrive as {@code servicio = "StoredProcedure"} with
 * {@code entrada = {procedimiento, entrada}} and are scripted per procedure name ({@code colaboradores.StoredProcedure.<name>}); a scripted failure is a
 * {@link SQLException}, as the oracle SQL.call shim throws.
 *
 * <p>It also records {@code {servicio, entrada}} in call order; that list is compared with the golden "colaboradores" trace.
 * The oracle {@code Hooks.POR_DEFECTO} table (AgregarLog, CrearEventoAutorizacionMasivo) is mirrored here.
 * A checked exception is rethrown as is (sneaky) because the port declares none: the goldens pin its simple class name.
 */
public class ColaboradoresDeCaso implements Colaboradores {

    private static final Map<String, String> POR_DEFECTO = Map.of(
            "AgregarLog", "{\"P_OUT_ERRORESCONTADOR\":0}",
            "CrearEventoAutorizacionMasivo", "{\"P_OUT_IDEVENTO\":[],\"P_OUT_ADVERTENCIASCONTADOR\":0,\"P_OUT_ADVERTENCIASDETALLE\":[],\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_ERRORESDETALLE\":[]}");

    private final JsonObject guiones;
    private final List<JsonObject> traza = new ArrayList<>();
    private final Map<String, Integer> contadores = new HashMap<>();

    public ColaboradoresDeCaso(Caso caso) {
        this.guiones = caso.colaboradores();
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        JsonObject r = new JsonObject();
        r.addProperty("servicio", servicio);
        r.add("entrada", entrada == null ? com.google.gson.JsonNull.INSTANCE : entrada.deepCopy());
        traza.add(r);
        if ("StoredProcedure".equals(servicio)) {
            String nombre = entrada.get("procedimiento").getAsString();
            JsonObject porProc = guiones.has(servicio) ? guiones.getAsJsonObject(servicio) : new JsonObject();
            try {
                return respuesta(servicio + ":" + nombre, porProc.has(nombre) ? porProc.getAsJsonObject(nombre) : null, null);
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw ColaboradoresDeCaso.<RuntimeException>sneaky(new SQLException(e.getMessage()));
            }
        }
        try {
            return respuesta(servicio, guiones.has(servicio) ? guiones.getAsJsonObject(servicio) : null, POR_DEFECTO.get(servicio));
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                throw (IllegalStateException) e;
            }
            throw ColaboradoresDeCaso.<RuntimeException>sneaky(e);
        }
    }

    private JsonObject respuesta(String clave, JsonObject guion, String porDefecto) throws Exception {
        int k = contadores.merge(clave, 1, Integer::sum) - 1;
        if ((guion == null || guion.size() == 0) && porDefecto != null) {
            return JsonParser.parseString(porDefecto).getAsJsonObject();
        }
        if (guion == null || guion.size() == 0) {
            throw new IllegalStateException("case.json has no script colaboradores." + clave + " (a collaborator outside APV answers only what the case scripts)");
        }
        if (guion.has("lanza")) {
            throw new Exception(guion.get("lanza").getAsString());
        }
        if (guion.has("salidas")) {
            JsonArray a = guion.getAsJsonArray("salidas");
            return a.get(Math.min(k, a.size() - 1)).getAsJsonObject().deepCopy();
        }
        if (guion.has("salida")) {
            return guion.getAsJsonObject("salida").deepCopy();
        }
        throw new IllegalStateException("script colaboradores." + clave + " needs salida, salidas or lanza");
    }

    public List<JsonObject> traza() {
        return traza;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T sneaky(Throwable t) throws T {
        throw (T) t;
    }
}
