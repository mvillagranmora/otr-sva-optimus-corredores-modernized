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
 * Direct JavaCall entry of {@code cl.bcs.fondomutuo.ConsultarSolicitudesFFMMWS}: legacy {@code call(Connection, JsonObject)} with a plain {@code new Gson()} in and out
 * ({@link JsonSfmWs}). DISABLED BY DEFAULT: unless {@code fondomutuo.solicitud.listas-servidas} names {@link ConsultarSolicitudesWsConfiguracion#LISTA} the call is a 503 before anything is read.
 */
@Component
public class ConsultarSolicitudesWsJavaCallHandler implements JavaCallHandler {

    private final ConsultarSolicitudesWsPort servicio;
    private final Compuertas compuertas;

    public ConsultarSolicitudesWsJavaCallHandler(ConsultarSolicitudesWsPort servicio, Compuertas compuertas) {
        this.servicio = servicio;
        this.compuertas = compuertas;
    }

    @Override
    public String fqn() {
        return JavaCallDispatcher.CONSULTAR_SOLICITUDES_FFMM_WS;
    }

    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        if (!compuertas.listasServidas().contains(ConsultarSolicitudesWsConfiguracion.LISTA)) {
            throw new ServicioNoServidoException("G0", "ConsultarSolicitudesFFMMWS is not enabled in this module");
        }
        Gson gson = JsonSfmWs.gson();
        ConsultarSolicitudesWsContrato.Entrada jin = gson.fromJson(in.toString(), ConsultarSolicitudesWsContrato.Entrada.class);
        ConsultarSolicitudesWsContrato.Salida out = servicio.logica(jin);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }
}
