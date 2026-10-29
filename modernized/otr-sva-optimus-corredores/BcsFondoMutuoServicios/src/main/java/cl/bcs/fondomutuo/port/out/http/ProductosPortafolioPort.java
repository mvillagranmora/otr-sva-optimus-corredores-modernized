package cl.bcs.fondomutuo.port.out.http;

import com.google.gson.JsonObject;

/** Port H (read): legacy {@code cl.bcs.plataforma.com.ConsultarProductosPortafolio}, a collaborator the plan missed (RULES_FONDOMUTUO_SOLICITUD section 1.4), used by the requirements closure. */
public interface ProductosPortafolioPort {

    JsonObject consultar(JsonObject entrada);
}
