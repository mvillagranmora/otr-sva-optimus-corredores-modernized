package cl.bcs.fondomutuo.port.out.http;

import com.google.gson.JsonObject;

/** Port H (read; gate G3 while Plataforma does not expose it as JavaCall, D-5): legacy {@code cl.bcs.plataforma.pla.SaldoFlujoCaja}. */
public interface FlujoCajaPort {

    JsonObject saldo(JsonObject entrada);
}
