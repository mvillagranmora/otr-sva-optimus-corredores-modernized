package cl.bcs.fondomutuo.manejador;

import java.sql.Connection;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;

/**
 * JavaCall entry of {@code cl.bcs.fondomutuo.ManejadorSolicitudAporteRescate} (registry id 601190): legacy {@code call(Connection, JsonObject)}.
 * The JSON is read with the plain {@code new Gson()} of the legacy (so a missing {@code p_FormaPagoSpot} is {@code null}: D-MAN-2) and the answer is
 * written with the same plain Gson (null keys omitted, the counter always present). The connection is the one of the unit of work of the call, already
 * ambient for every adapter, so it is not used here. A new service instance per call is not needed: the service keeps no state.
 */
@Component
public class ManejadorJavaCallHandler implements JavaCallHandler {

    private final ManejadorSolicitudAporteRescateService servicio;

    public ManejadorJavaCallHandler(ManejadorSolicitudAporteRescateService servicio) {
        this.servicio = servicio;
    }

    @Override
    public String fqn() {
        return JavaCallDispatcher.MANEJADOR;
    }

    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        Gson gson = new Gson();
        ManejadorContrato.Entrada jin = gson.fromJson(in.toString(), ManejadorContrato.Entrada.class);
        ManejadorContrato.Salida out = servicio.logica(jin);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }
}
