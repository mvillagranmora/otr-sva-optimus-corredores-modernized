package cl.bcs.fondomutuo.consultas.fecha;

import java.sql.Connection;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * Direct JavaCall entry of {@code cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate} (registry id 3022): legacy {@code call(Connection, JsonObject)} with
 * {@code Utils.getGsonBuilder()} in and out ({@link JsonFsa}). DISABLED BY DEFAULT: unless {@code fondomutuo.solicitud.listas-servidas} names
 * {@link ConsultarFechaConfiguracion#LISTA} the call is a 503 before anything is read (the router repeats it in TomEE).
 */
@Component
public class ConsultarFechaJavaCallHandler implements JavaCallHandler {

    private final ConsultarFechaPort servicio;
    private final Compuertas compuertas;

    public ConsultarFechaJavaCallHandler(ConsultarFechaPort servicio, Compuertas compuertas) {
        this.servicio = servicio;
        this.compuertas = compuertas;
    }

    @Override
    public String fqn() {
        return JavaCallDispatcher.CONSULTAR_FECHA_SOLICITUD_APORTE_RESCATE;
    }

    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        if (!compuertas.listasServidas().contains(ConsultarFechaConfiguracion.LISTA)) {
            throw new ServicioNoServidoException("G0", "ConsultarFechaSolicitudAporteRescate is not enabled in this module");
        }
        ConsultarFechaContrato.Entrada jin = JsonFsa.gson().fromJson(in.toString(), ConsultarFechaContrato.Entrada.class);
        ConsultarFechaContrato.Salida out = servicio.logica(jin);
        return JsonParser.parseString(JsonFsa.gson().toJson(out)).getAsJsonObject();
    }
}
