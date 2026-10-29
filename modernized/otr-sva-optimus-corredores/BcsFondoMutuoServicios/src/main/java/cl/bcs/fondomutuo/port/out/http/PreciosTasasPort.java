package cl.bcs.fondomutuo.port.out.http;

import com.google.gson.JsonObject;

/** Port H (read): legacy {@code cl.bcs.plataforma.tas.EntregarPreciosTasas} (not in the router registry: no id, so no TomEE fallback by id, Q-07). */
public interface PreciosTasasPort {

    JsonObject entregar(JsonObject entrada);
}
