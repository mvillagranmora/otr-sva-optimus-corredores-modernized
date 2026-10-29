package cl.bcs.tesoreria.familia_a.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * Fake of the {@link Colaboradores} port: answers the collaborator services (CrearCargoAbono, AnularCargoAbono, ActualizarSaldosCuentaBanco,
 * ConsultarFlujoCaja, CrearEventoAutorizacionMasivo, ...) from the case, and traces every call as {@code {servicio, entrada}} for
 * {@link Verifica#comparar(Caso, Verifica.Ejecucion, List, String...)}.
 *
 * <p>It reproduces the answers of the legacy oracle fakes (legacy-oracle/shims, class Hooks):
 * <ul>
 *   <li>generic services: case.json {@code colaboradores.<Servicio>}: an object answers every call, an array answers the n-th call (the last element
 *       repeats), nothing scripted gives an empty object (the caller decodes it into the legacy empty Salida, which carries the legacy defaults);</li>
 *   <li>helpers that are not plain JSON services in the legacy are mapped from the oracle keys: {@code RevisarRequisitosBasicos} (key
 *       {@code requisitosBasicos}: {@code {p_out_Mensajes}}), {@code SaldoFlujoCaja} ({@code saldoFlujoCaja}, {@code saldoFlujoCajaErrores}),
 *       {@code AgregarLog} (counter 0), {@code CrearEventoAutorizacionMasivo} (unscripted: one IdEvento 7000+n per event row),
 *       {@code ObtenerPortafoliosEjecutivo(.restringeCartera)} ({@code {"valor": ...}}), {@code Cloper} ({@code {"valor": [rows]}}, absent
 *       {@code valor} = the legacy answer null) and {@code StoredProcedure} ({@code colaboradores.SalidaProcedimiento.<procedure>}).</li>
 * </ul>
 */
public class ColaboradoresDeCaso implements Colaboradores {

    private final JsonObject guion;
    private final java.util.Map<String, String> configuracion;
    private final List<JsonObject> traza = new ArrayList<>();
    private final Map<String, Integer> llamadas = new HashMap<>();

    public ColaboradoresDeCaso(Caso caso) {
        this.guion = caso.colaboradores();
        this.configuracion = caso.config();
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
            case "SaldoFlujoCaja" -> {
                JsonObject o = new JsonObject();
                o.add("P_OUT_FLUJOCAJA", lista("saldoFlujoCaja"));
                o.addProperty("P_OUT_ERRORESCONTADOR", guion.has("saldoFlujoCajaErrores") ? guion.get("saldoFlujoCajaErrores").getAsLong() : 0L);
                yield o;
            }
            case "AgregarLog" -> {
                JsonObject o = new JsonObject();
                o.addProperty("P_OUT_ERRORESCONTADOR", 0L);
                yield o;
            }
            case "CrearEventoAutorizacionMasivo" -> guion.has(servicio) ? guionado(servicio, n) : eventosCreados(entrada);
            case "ObtenerPortafoliosEjecutivo.restringeCartera" -> {
                JsonObject o = new JsonObject();
                o.addProperty("valor", guion.has("restringeCartera") && guion.get("restringeCartera").getAsBoolean());
                yield o;
            }
            case "ObtenerPortafoliosEjecutivo" -> portafolios(entrada);
            case "Cloper" -> cloper(entrada);
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
                fila.add("NumeroOperacionConcepto", numero == null ? com.google.gson.JsonNull.INSTANCE : numero);
                ids.add(fila);
            }
        }
        salida.add("P_OUT_IDEVENTO", ids);
        return salida;
    }

    private JsonObject portafolios(JsonObject entrada) {
        JsonArray valor = new JsonArray();
        JsonElement filtrar = entrada.get("p_filtrarListaPortafolios");
        if (filtrar != null && !filtrar.isJsonNull() && "S".equals(filtrar.getAsString())) {
            boolean hayFiltro = guion.has("portafoliosPermitidos");
            JsonElement pedidos = entrada.get("p_ListaIdPortafolios");
            if (pedidos != null && pedidos.isJsonArray()) {
                for (JsonElement p : pedidos.getAsJsonArray()) {
                    boolean ok = !hayFiltro;
                    for (JsonElement permitido : lista("portafoliosPermitidos")) {
                        if (permitido.getAsLong() == p.getAsLong()) {
                            ok = true;
                        }
                    }
                    if (ok) {
                        valor.add(p);
                    }
                }
            }
        } else {
            valor = lista("portafoliosEjecutivo");
        }
        JsonObject o = new JsonObject();
        o.add("valor", valor);
        return o;
    }

    private JsonObject cloper(JsonObject entrada) {
        JsonObject o = new JsonObject();
        JsonObject conexion = entrada.getAsJsonObject("conexion");
        for (String k : new String[] {"wsdl", "profile", "password"}) {
            String v = conexion.get(k).getAsString();
            if ("null".equals(v) || v.isBlank()) {
                return o;
            }
        }
        // the password travels as the marker "<set>" only: a blank configured connection parameter is what makes the legacy answer null
        for (String k : new String[] {"Wsdl", "Profile", "Password"}) {
            String configurado = configuracion.get("PLATAFORMA/PLA.CloperWsBSC." + k);
            if (configurado == null || configurado.isBlank()) {
                return o;
            }
        }
        JsonArray respuesta = lista("cloper");
        if (respuesta.size() == 0) {
            return o;
        }
        JsonArray filas = new JsonArray();
        for (JsonElement e : respuesta) {
            JsonElement fecha = e.getAsJsonObject().get("fecha");
            if (fecha == null || fecha.isJsonNull() || !"NO EXISTE".equalsIgnoreCase(fecha.getAsString())) {
                filas.add(e);
            }
        }
        o.add("valor", filas);
        return o;
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
