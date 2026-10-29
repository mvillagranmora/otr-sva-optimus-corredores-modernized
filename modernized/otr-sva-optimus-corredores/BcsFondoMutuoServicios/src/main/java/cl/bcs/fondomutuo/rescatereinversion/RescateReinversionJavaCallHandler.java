package cl.bcs.fondomutuo.rescatereinversion;

import java.sql.Connection;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * Direct JavaCall entry of {@code cl.bcs.fondomutuo.CrearSolicitudRescateReinversion} (registry id 1436, the only F2 class with direct use: plan 6.3):
 * legacy {@code call(Connection, JsonObject)} with {@code Utils.getGsonBuilder()} in and out ({@link JsonRre}). DISABLED BY DEFAULT: unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@code RESCATE_REINVERSION} the call is a 503 before anything is read or written (the router repeats it
 * in TomEE). The same switch opens the Manejador list, so both entries of the class are opened (and reverted) together.
 */
@Component
public class RescateReinversionJavaCallHandler implements JavaCallHandler {

    private final RescateReinversionPort servicio;
    private final Compuertas compuertas;

    public RescateReinversionJavaCallHandler(RescateReinversionPort servicio, Compuertas compuertas) {
        this.servicio = servicio;
        this.compuertas = compuertas;
    }

    @Override
    public String fqn() {
        return JavaCallDispatcher.CREAR_SOLICITUD_RESCATE_REINVERSION;
    }

    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        if (!compuertas.listasServidas().contains(RescateReinversionConfiguracion.LISTA)) {
            throw new ServicioNoServidoException("G0", "CrearSolicitudRescateReinversion is not enabled in this module");
        }
        RescateReinversionContrato.Entrada jin = JsonRre.gson().fromJson(in.toString(), RescateReinversionContrato.Entrada.class);
        RescateReinversionContrato.Salida out = servicio.logica(jin);
        return JsonParser.parseString(JsonRre.gson().toJson(out)).getAsJsonObject();
    }
}
