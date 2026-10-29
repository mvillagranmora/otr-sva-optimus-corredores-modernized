package cl.bcs.fondomutuo.port.out.http;

import com.google.gson.JsonObject;

/** Port H (read): legacy {@code cl.bcs.custodia.ConsultarFlujoCustodia} (id 14000), used by the custody-flow and commission closure classes. */
public interface CustodiaPort {

    JsonObject consultarFlujo(JsonObject entrada);
}
