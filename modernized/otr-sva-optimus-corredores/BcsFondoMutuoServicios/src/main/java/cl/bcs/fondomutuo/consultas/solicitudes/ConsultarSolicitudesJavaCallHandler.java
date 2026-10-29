package cl.bcs.fondomutuo.consultas.solicitudes;

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
 * Direct JavaCall entry of {@code cl.bcs.fondomutuo.ConsultarSolicitudesFFMM}: legacy {@code call(Connection, JsonObject)} with a plain {@code new Gson()} in and out
 * ({@link JsonSfm}). DISABLED BY DEFAULT: unless {@code fondomutuo.solicitud.listas-servidas} names {@link ConsultarSolicitudesConfiguracion#LISTA} the call is a 503 before anything is read.
 */
@Component
public class ConsultarSolicitudesJavaCallHandler implements JavaCallHandler {

    private final ConsultarSolicitudesPort servicio;
    private final Compuertas compuertas;

    public ConsultarSolicitudesJavaCallHandler(ConsultarSolicitudesPort servicio, Compuertas compuertas) {
        this.servicio = servicio;
        this.compuertas = compuertas;
    }

    @Override
    public String fqn() {
        return JavaCallDispatcher.CONSULTAR_SOLICITUDES_FFMM;
    }

    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        if (!compuertas.listasServidas().contains(ConsultarSolicitudesConfiguracion.LISTA)) {
            throw new ServicioNoServidoException("G0", "ConsultarSolicitudesFFMM is not enabled in this module");
        }
        Gson gson = JsonSfm.gson();
        ConsultarSolicitudesContrato.Entrada jin = gson.fromJson(in.toString(), ConsultarSolicitudesContrato.Entrada.class);
        ConsultarSolicitudesContrato.Salida out = servicio.logica(jin);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }
}
