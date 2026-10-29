package cl.bcs.fondomutuo.consultas.dolar;

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
 * Direct JavaCall entry of {@code cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo}: legacy {@code call(Connection, JsonObject)} with a plain {@code new Gson()}
 * in and out ({@link JsonVdo}). DISABLED BY DEFAULT: unless {@code fondomutuo.solicitud.listas-servidas} names {@link ConsultarDolarConfiguracion#LISTA} the
 * call is a 503 before anything is read.
 */
@Component
public class ConsultarDolarJavaCallHandler implements JavaCallHandler {

    private final ConsultarDolarPort servicio;
    private final Compuertas compuertas;

    public ConsultarDolarJavaCallHandler(ConsultarDolarPort servicio, Compuertas compuertas) {
        this.servicio = servicio;
        this.compuertas = compuertas;
    }

    @Override
    public String fqn() {
        return JavaCallDispatcher.CONSULTAR_VALOR_DOLAR_FONDO_MUTUO;
    }

    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        if (!compuertas.listasServidas().contains(ConsultarDolarConfiguracion.LISTA)) {
            throw new ServicioNoServidoException("G0", "ConsultarValorDolarFondoMutuo is not enabled in this module");
        }
        Gson gson = JsonVdo.gson();
        ConsultarDolarContrato.Entrada jin = gson.fromJson(in.toString(), ConsultarDolarContrato.Entrada.class);
        ConsultarDolarContrato.Salida out = servicio.logica(jin);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }
}
