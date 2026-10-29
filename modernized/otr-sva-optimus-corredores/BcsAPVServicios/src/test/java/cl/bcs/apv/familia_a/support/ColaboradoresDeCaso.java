package cl.bcs.apv.familia_a.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import cl.bcs.apv.domain.port.out.Colaboradores;

/**
 * Fake of the {@link Colaboradores} port for the collaborators OUTSIDE APV (CrearPersona, CrearCliente, CrearTelefono, CrearDireccionElectronica,
 * AgregarLog, CrearEventoAutorizacionMasivo, AnulacionMandatoPAC, RevocacionMandatoPAC, RevisarRequisitosBasicos, ModificarPortafolio,
 * ConsultarCustodiaPortafolio, ConsultarCustodiaPortafolioFondoMutuo, ConsultarSaldoCuentaInversion, and the stored procedures as "StoredProcedure"):
 * it answers from the case and traces every call as {@code {servicio, entrada}} for {@link Verifica#comparar(Caso, Verifica.Ejecucion, List, String...)}.
 *
 * <p>It reproduces the answers of the legacy oracle fakes (legacy-oracle/shims, class Hooks):
 * <ul>
 *   <li>generic services: case.json {@code colaboradores.<Servicio>}: an object answers every call, an array answers the n-th call (the last element
 *       repeats), nothing scripted gives an empty object (the caller decodes it into the legacy empty Salida, which carries the legacy defaults);</li>
 *   <li>{@code RevisarRequisitosBasicos} (key {@code requisitosBasicos}: {@code {p_out_Mensajes}}), {@code AgregarLog} (counter 0),
 *       {@code CrearEventoAutorizacionMasivo} (unscripted: one IdEvento 7000+n per event row) and {@code StoredProcedure}
 *       ({@code colaboradores.SalidaProcedimiento.<procedure>}: the scripted output parameters).</li>
 * </ul>
 */
public class ColaboradoresDeCaso implements Colaboradores {

    private final JsonObject guion;
    private final List<JsonObject> traza = new ArrayList<>();
    private final Map<String, Integer> llamadas = new HashMap<>();

    public ColaboradoresDeCaso(Caso caso) {
        this.guion = caso.colaboradores();
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        JsonObject registro = new JsonObject();
        registro.addProperty("servicio", servicio);
        registro.add("entrada", entrada.deepCopy());
        traza.add(registro);
        int n = llamadas.merge(servicio, 1, Integer::sum) - 1;
        return switch (servicio) {
            case "RevisarRequisitosBasicos" -> {
                JsonObject o = new JsonObject();
                o.add("p_out_Mensajes", lista("requisitosBasicos"));
                yield o;
            }
            case "AgregarLog" -> {
                JsonObject o = new JsonObject();
                o.addProperty("P_OUT_ERRORESCONTADOR", 0L);
                yield o;
            }
            case "CrearEventoAutorizacionMasivo" -> guion.has(servicio) ? guionado(servicio, n) : eventosCreados(entrada);
            case "StoredProcedure" -> procedimiento(entrada);
            default -> guionado(servicio, n);
        };
    }

    public List<JsonObject> traza() {
        return traza;
    }

    private JsonObject guionado(String servicio, int n) {
        JsonElement g = guion.get(servicio);
        if (g == null || g.isJsonNull()) {
            return new JsonObject();
        }
        if (g.isJsonArray()) {
            JsonArray a = g.getAsJsonArray();
            return a.size() == 0 ? new JsonObject() : a.get(Math.min(n, a.size() - 1)).getAsJsonObject().deepCopy();
        }
        return g.getAsJsonObject().deepCopy();
    }

    private JsonArray lista(String clave) {
        JsonElement e = guion.get(clave);
        return e == null || e.isJsonNull() ? new JsonArray() : e.getAsJsonArray().deepCopy();
    }

    private static JsonObject eventosCreados(JsonObject entrada) {
        JsonObject salida = new JsonObject();
        JsonArray ids = new JsonArray();
        long id = 7000;
        JsonElement eventos = entrada.get("P_TABLAEVENTOS");
        if (eventos != null && eventos.isJsonArray()) {
            for (JsonElement e : eventos.getAsJsonArray()) {
                JsonObject fila = new JsonObject();
                fila.addProperty("IdEvento", id++);
                JsonElement numero = e.getAsJsonObject().get("NumeroOperacionConcepto");
                fila.add("NumeroOperacionConcepto", numero == null ? JsonNull.INSTANCE : numero);
                ids.add(fila);
            }
        }
        salida.add("P_OUT_IDEVENTO", ids);
        return salida;
    }

    private JsonObject procedimiento(JsonObject entrada) {
        JsonObject o = new JsonObject();
        JsonElement todas = guion.get("SalidaProcedimiento");
        JsonElement nombre = entrada.get("procedimiento");
        if (todas != null && todas.isJsonObject() && nombre != null && todas.getAsJsonObject().has(nombre.getAsString())) {
            todas.getAsJsonObject().getAsJsonObject(nombre.getAsString()).entrySet().forEach(e -> o.add(e.getKey(), e.getValue().deepCopy()));
        }
        return o;
    }
}
