package cl.bcs.tesoreria.jobs.support;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * The scripted {@link Colaboradores} port: records every call and answers what the case scripts, the same answers the oracle fakes give:
 * ConsultarEstadoModulo / ObtenerDiaAnterior: "colaboradores.&lt;servicio&gt;.salida" (defaults: no modules / one date 2025-03-31);
 * AgregarLog: counter 0; ValidarMandatosPAC: nothing; ValidaEjecucionProcesoRecPAC: empty output. A scripted
 * "&lt;servicio&gt;.lanza" failure must be unchecked here (the port cannot throw checked exceptions).
 */
public final class ColaboradoresDeCaso implements Colaboradores {

    private final Traza traza;

    public ColaboradoresDeCaso(Traza traza) {
        this.traza = traza;
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        traza.registrar(servicio, entrada);
        try {
            traza.lanzaSiCorresponde(servicio + ".lanza");
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionError("a scripted failure of a Colaboradores service must be unchecked: " + e, e);
        }
        JsonObject script = traza.objeto(servicio);
        JsonElement salida = script.get("salida");
        switch (servicio) {
            case "ConsultarEstadoModulo":
                return salida != null ? salida.getAsJsonObject() : JsonParser.parseString("{\"p_out_ListaModulos\":[]}").getAsJsonObject();
            case "ObtenerDiaAnterior":
                return salida != null ? salida.getAsJsonObject()
                        : JsonParser.parseString("{\"fechas\":[{\"fechaCalculada\":\"2025-03-31\"}]}").getAsJsonObject();
            case "AgregarLog":
                return JsonParser.parseString("{\"P_OUT_ERRORESCONTADOR\":0}").getAsJsonObject();
            case "ValidaEjecucionProcesoRecPAC":
                return JsonParser.parseString("{\"P_OUT_MENSAJES\":[],\"P_OUT_ERRORESCONTADOR\":0}").getAsJsonObject();
            default:
                return new JsonObject();
        }
    }
}
