package cl.bcs.fondomutuo.consultas.reinversion;

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
 * Direct JavaCall entry of {@code cl.bcs.fondomutuo.ConsultarFondoMutuoReinversion}: legacy {@code call(Connection, JsonObject)} with a plain {@code new Gson()}
 * in and out ({@link JsonFmr}). DISABLED BY DEFAULT: unless {@code fondomutuo.solicitud.listas-servidas} names {@link ConsultarReinversionConfiguracion#LISTA}
 * the call is a 503 before anything is read.
 */
@Component
public class ConsultarReinversionJavaCallHandler implements JavaCallHandler {

    private final ConsultarReinversionPort servicio;
    private final Compuertas compuertas;

    public ConsultarReinversionJavaCallHandler(ConsultarReinversionPort servicio, Compuertas compuertas) {
        this.servicio = servicio;
        this.compuertas = compuertas;
    }

    @Override
    public String fqn() {
        return JavaCallDispatcher.CONSULTAR_FONDO_MUTUO_REINVERSION;
    }

    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        if (!compuertas.listasServidas().contains(ConsultarReinversionConfiguracion.LISTA)) {
            throw new ServicioNoServidoException("G0", "ConsultarFondoMutuoReinversion is not enabled in this module");
        }
        Gson gson = JsonFmr.gson();
        ConsultarReinversionContrato.Entrada jin = gson.fromJson(in.toString(), ConsultarReinversionContrato.Entrada.class);
        ConsultarReinversionContrato.Salida out = servicio.logica(jin);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }
}
