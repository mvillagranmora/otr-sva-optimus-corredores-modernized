package cl.bcs.cuentainversion.reportes.support;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic fake of the collaborators of the reporting services; it mirrors, one to one, the legacy-oracle shims
 * (legacy-oracle/shims/cl/bcs/oracle/Colaboradores.java and the shim classes ConsultarEmpresa, ConsultarSaldoCuentaInversion,
 * CalcularParidad, CrearMovimientoComprobante) so the modern services see exactly what the legacy classes saw.
 *
 * <ul>
 *   <li>empresa: case.json colaboradores.empresa, or "Empresa no existe";</li>
 *   <li>consultarSaldo: colaboradores.consultarSaldo (object, or array answered in call order, the last one repeating); absent = an empty
 *       error free answer;</li>
 *   <li>calcularParidad: colaboradores.paridad table "ORIGEN>DESTINO" -> rate; same currency = rate 1; missing = MontoDestino null;</li>
 *   <li>crearMovimientoComprobante: colaboradores.crearMovimientoComprobante or one MovimientoComprobante_Salida per movement
 *       (IdMovimientoComprobante 900000+i, NumeroMvtoCmbte echoed).</li>
 * </ul>
 * Every call is logged in {@link #llamadas()} as {servicio, entrada}.
 */
public final class FakeColaboradores implements ColaboradoresReportes {

    private final JsonObject config;
    private final List<JsonObject> llamadas = new ArrayList<>();
    private final Map<String, Integer> indice = new HashMap<>();

    private FakeColaboradores(JsonObject config) {
        this.config = config;
    }

    public static FakeColaboradores de(JsonObject caso) {
        return new FakeColaboradores(caso.has("colaboradores") ? caso.getAsJsonObject("colaboradores") : new JsonObject());
    }

    public List<JsonObject> llamadas() {
        return List.copyOf(llamadas);
    }

    public boolean canonicalizarParidad() {
        return config.has("canonicalizarParidad");
    }

    private void registrar(String servicio, JsonObject entrada) {
        JsonObject o = new JsonObject();
        o.addProperty("servicio", servicio);
        o.add("entrada", Mask.llamada(entrada.deepCopy()));
        llamadas.add(o);
    }

    private JsonElement siguiente(String servicio) {
        if (!config.has(servicio)) {
            return null;
        }
        JsonElement e = config.get(servicio);
        if (!e.isJsonArray()) {
            return e;
        }
        JsonArray a = e.getAsJsonArray();
        if (a.size() == 0) {
            return null;
        }
        int i = indice.getOrDefault(servicio, 0);
        indice.put(servicio, i + 1);
        return a.get(Math.min(i, a.size() - 1));
    }

    @Override
    public JsonObject consultarEmpresa(long idEmpresa) {
        JsonObject in = new JsonObject();
        in.addProperty("IdEmpresa", String.valueOf(idEmpresa));
        registrar("ConsultarEmpresa", in);
        if (!config.has("empresa")) {
            throw new IllegalStateException("Empresa no existe");
        }
        return config.getAsJsonObject("empresa").deepCopy();
    }

    @Override
    public JsonObject consultarSaldo(JsonObject entrada) {
        registrar("ConsultarSaldoCuentaInversion", entrada);
        JsonElement canned = siguiente("consultarSaldo");
        JsonObject out = canned == null ? new JsonObject() : canned.getAsJsonObject().deepCopy();
        for (String k : new String[]{"P_OUT_ERRORESCONTADOR", "P_OUT_ADVERTENCIASCONTADOR", "P_OUT_INFORMACIONCONTADOR"}) {
            if (!out.has(k) || out.get(k).isJsonNull()) {
                out.addProperty(k, 0);
            }
        }
        for (String k : new String[]{"P_OUT_SALDOPORFECHAEFECTIVA", "P_OUT_SALDOPORFECHAMOVIMIENTO", "P_OUT_SALDOINICIALFINAL"}) {
            if (!out.has(k)) {
                out.add(k, new JsonArray());
            }
        }
        return out;
    }

    @Override
    public JsonObject calcularParidad(JsonObject entrada) {
        registrar("CalcularParidad", canonicalizarParidad() ? canonica(entrada) : entrada);
        JsonObject tabla = config.has("paridad") ? config.getAsJsonObject("paridad") : new JsonObject();
        JsonArray salida = new JsonArray();
        for (JsonElement e : entrada.getAsJsonArray("p_CalculosEntrada")) {
            JsonObject c = e.getAsJsonObject();
            JsonObject s = new JsonObject();
            for (String k : new String[]{"IdTrabajo", "IdInterno", "IdMonedaOrigen", "IdMonedaDestino", "MontoOrigen", "IdUso"}) {
                if (c.has(k)) {
                    s.add(k, c.get(k));
                }
            }
            String origen = c.get("IdMonedaOrigen").getAsString();
            String destino = c.get("IdMonedaDestino").getAsString();
            String clave = origen + ">" + destino;
            BigDecimal monto = c.has("MontoOrigen") && !c.get("MontoOrigen").isJsonNull() ? c.get("MontoOrigen").getAsBigDecimal() : null;
            if (origen.equals(destino)) {
                s.add("MontoDestino", monto == null ? null : new com.google.gson.JsonPrimitive(monto));
                s.add("MontoLocal", monto == null ? null : new com.google.gson.JsonPrimitive(monto));
                s.addProperty("Resultado", "OK");
            } else if (tabla.has(clave)) {
                BigDecimal rate = new BigDecimal(tabla.get(clave).getAsString());
                s.add("MontoDestino", monto == null ? null : new com.google.gson.JsonPrimitive(monto.multiply(rate)));
                s.add("MontoLocal", monto == null ? null : new com.google.gson.JsonPrimitive(monto.multiply(rate)));
                s.addProperty("Resultado", "OK");
            } else {
                s.add("MontoDestino", com.google.gson.JsonNull.INSTANCE);
                s.add("MontoLocal", com.google.gson.JsonNull.INSTANCE);
                s.addProperty("Resultado", "ERROR");
            }
            salida.add(s);
        }
        JsonObject out = new JsonObject();
        out.add("p_out_CalculosSalida", salida);
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }

    /** Same canonical form as the oracle (sorted by currency, amount, date; IdInterno = position). */
    private static JsonObject canonica(JsonObject entrada) {
        List<JsonObject> l = new ArrayList<>();
        entrada.getAsJsonArray("p_CalculosEntrada").forEach(e -> l.add(e.getAsJsonObject().deepCopy()));
        l.sort((a, b) -> {
            int c = a.get("IdMonedaOrigen").getAsString().compareTo(b.get("IdMonedaOrigen").getAsString());
            if (c == 0) {
                c = a.get("MontoOrigen").getAsBigDecimal().compareTo(b.get("MontoOrigen").getAsBigDecimal());
            }
            return c != 0 ? c : a.get("Fecha").getAsString().compareTo(b.get("Fecha").getAsString());
        });
        long pos = 1;
        JsonArray arr = new JsonArray();
        for (JsonObject o : l) {
            o.addProperty("IdInterno", pos++);
            arr.add(o);
        }
        JsonObject out = entrada.deepCopy();
        out.add("p_CalculosEntrada", arr);
        return out;
    }

    private List<JsonObject> filas(String servicio, String clave, JsonObject entrada) {
        registrar(servicio, entrada);
        List<JsonObject> out = new ArrayList<>();
        if (config.has(clave) && config.get(clave).isJsonArray()) {
            config.getAsJsonArray(clave).forEach(e -> out.add(e.getAsJsonObject().deepCopy()));
        }
        return out;
    }

    @Override
    public List<JsonObject> movimientosTitulos(JsonObject entradaCartola) {
        return filas("MovimientosTitulos", "movimientosTitulos", entradaCartola);
    }

    @Override
    public List<JsonObject> movimientosCaja(JsonObject entradaCartola) {
        return filas("MovimientosCaja", "movimientosCaja", entradaCartola);
    }

    @Override
    public List<JsonObject> datosSocioNegocio(JsonObject entradaCartola) {
        return filas("DatosCartolaSocioNegocio", "datosSocioNegocio", entradaCartola);
    }

    @Override
    public JsonObject entregarPreciosTasas(JsonObject entrada) {
        registrar("EntregarPreciosTasas", entrada);
        JsonObject out = new JsonObject();
        out.add("P_OUT_PRECIOSTASAS", config.has("preciosTasas") ? config.getAsJsonArray("preciosTasas").deepCopy() : new JsonArray());
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }

    @Override
    public JsonObject crearMovimientoComprobante(JsonObject entrada) {
        registrar("CrearMovimientoComprobante", entrada);
        JsonElement canned = siguiente("crearMovimientoComprobante");
        if (canned != null) {
            JsonObject o = canned.getAsJsonObject().deepCopy();
            if (!o.has("P_OUT_ERRORESCONTADOR")) {
                o.addProperty("P_OUT_ERRORESCONTADOR", 0);
            }
            return o;
        }
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        out.add("P_OUT_MENSAJES", new JsonArray());
        JsonArray movs = new JsonArray();
        long i = 0;
        for (JsonElement e : entrada.getAsJsonArray("p_Movimientos")) {
            JsonObject m = new JsonObject();
            m.addProperty("IdMovimientoComprobante", 900000L + i++);
            m.add("NumeroMvtoCmbte", e.getAsJsonObject().get("NumeroMvtoCmbte"));
            movs.add(m);
        }
        out.add("p_out_Movimientos", movs);
        return out;
    }
}
