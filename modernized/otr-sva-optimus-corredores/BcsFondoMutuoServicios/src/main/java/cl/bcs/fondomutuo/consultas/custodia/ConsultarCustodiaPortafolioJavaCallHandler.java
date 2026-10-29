package cl.bcs.fondomutuo.consultas.custodia;

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
 * Direct JavaCall entry of {@code cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo}: legacy {@code call(Connection, JsonObject)} with the Gson of
 * {@code Utils.getGsonBuilder()} in and out ({@link ConsultarCustodiaPortafolioJson}, nulls written). DISABLED BY DEFAULT: unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@link ConsultarCustodiaPortafolioConfiguracion#LISTA} the call is a 503 before anything is read.
 */
@Component
public class ConsultarCustodiaPortafolioJavaCallHandler implements JavaCallHandler {

    private final ConsultarCustodiaPortafolioPort servicio;
    private final Compuertas compuertas;

    public ConsultarCustodiaPortafolioJavaCallHandler(ConsultarCustodiaPortafolioPort servicio, Compuertas compuertas) {
        this.servicio = servicio;
        this.compuertas = compuertas;
    }

    @Override
    public String fqn() {
        return JavaCallDispatcher.CONSULTAR_CUSTODIA_PORTAFOLIO_FONDO_MUTUO;
    }

    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        if (!compuertas.listasServidas().contains(ConsultarCustodiaPortafolioConfiguracion.LISTA)) {
            throw new ServicioNoServidoException("G0", "ConsultarCustodiaPortafolioFondoMutuo is not enabled in this module");
        }
        Gson gson = ConsultarCustodiaPortafolioJson.gson();
        ConsultarCustodiaPortafolioContrato.Entrada jin = gson.fromJson(in.toString(), ConsultarCustodiaPortafolioContrato.Entrada.class);
        ConsultarCustodiaPortafolioContrato.Salida out = servicio.logica(jin);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }
}
