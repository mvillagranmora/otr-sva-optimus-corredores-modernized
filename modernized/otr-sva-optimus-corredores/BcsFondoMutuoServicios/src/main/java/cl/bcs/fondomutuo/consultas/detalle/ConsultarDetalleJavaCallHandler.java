package cl.bcs.fondomutuo.consultas.detalle;

import java.sql.Connection;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * Direct JavaCall entry of {@code cl.bcs.fondomutuo.ConsultarDetalleSolicitud}: legacy {@code call(Connection, JsonObject)} with a plain {@code new Gson()} in and out
 * ({@link JsonDet}). DISABLED BY DEFAULT: unless {@code fondomutuo.solicitud.listas-servidas} names {@link ConsultarDetalleConfiguracion#LISTA} the call is a 503 before
 * anything is read.
 */
@Component
public class ConsultarDetalleJavaCallHandler implements JavaCallHandler {

    private final ConsultarDetallePort servicio;
    private final Compuertas compuertas;

    public ConsultarDetalleJavaCallHandler(ConsultarDetallePort servicio, Compuertas compuertas) {
        this.servicio = servicio;
        this.compuertas = compuertas;
    }

    @Override
    public String fqn() {
        return JavaCallDispatcher.CONSULTAR_DETALLE_SOLICITUD;
    }

    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        if (!compuertas.listasServidas().contains(ConsultarDetalleConfiguracion.LISTA)) {
            throw new ServicioNoServidoException("G0", "ConsultarDetalleSolicitud is not enabled in this module");
        }
        Gson gson = JsonDet.gson();
        ConsultarDetalleContrato.Entrada jin = gson.fromJson(in.toString(), ConsultarDetalleContrato.Entrada.class);
        ConsultarDetalleContrato.Salida out = servicio.logica(jin);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }
}
