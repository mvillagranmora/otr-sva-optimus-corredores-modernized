package cl.bcs.canalagente.port.out;

import java.sql.Connection;

import com.google.gson.JsonObject;

import cl.bcs.canalagente.kit.ServicioNoServidoException;

/** The default of the module: the cartola is not ported here, so the call is refused (the router repeats the request in TomEE). */
public class ColaboradoresNoPortados implements ConsultarCartolaDeMovimientoPort {

    @Override
    public JsonObject consultar(Connection con, JsonObject entrada) {
        throw new ServicioNoServidoException("G-CTA", "ConsultarCartolaDeMovimiento is not ported into this module");
    }
}
