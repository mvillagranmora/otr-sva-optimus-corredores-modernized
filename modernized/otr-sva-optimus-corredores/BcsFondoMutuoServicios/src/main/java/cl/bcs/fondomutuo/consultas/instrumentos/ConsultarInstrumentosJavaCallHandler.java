package cl.bcs.fondomutuo.consultas.instrumentos;

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
 * Direct JavaCall entry of {@code cl.bcs.fondomutuo.ConsultarInstrumentosFondosMutuos}: legacy {@code call(Connection, JsonObject)} with a plain
 * {@code new Gson()} in and out ({@link JsonIfm}). DISABLED BY DEFAULT: unless {@code fondomutuo.solicitud.listas-servidas} names
 * {@link ConsultarInstrumentosConfiguracion#LISTA} the call is a 503 before anything is read.
 */
@Component
public class ConsultarInstrumentosJavaCallHandler implements JavaCallHandler {

    private final ConsultarInstrumentosPort servicio;
    private final Compuertas compuertas;

    public ConsultarInstrumentosJavaCallHandler(ConsultarInstrumentosPort servicio, Compuertas compuertas) {
        this.servicio = servicio;
        this.compuertas = compuertas;
    }

    @Override
    public String fqn() {
        return JavaCallDispatcher.CONSULTAR_INSTRUMENTOS_FONDOS_MUTUOS;
    }

    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        if (!compuertas.listasServidas().contains(ConsultarInstrumentosConfiguracion.LISTA)) {
            throw new ServicioNoServidoException("G0", "ConsultarInstrumentosFondosMutuos is not enabled in this module");
        }
        Gson gson = JsonIfm.gson();
        ConsultarInstrumentosContrato.Entrada jin = gson.fromJson(in.toString(), ConsultarInstrumentosContrato.Entrada.class);
        ConsultarInstrumentosContrato.Salida out = servicio.logica(jin);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }
}
