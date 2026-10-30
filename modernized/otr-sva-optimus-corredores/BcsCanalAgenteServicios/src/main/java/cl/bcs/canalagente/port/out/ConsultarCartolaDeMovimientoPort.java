package cl.bcs.canalagente.port.out;

import java.sql.Connection;

import com.google.gson.JsonObject;

/**
 * Collaborator outside the module: legacy {@code cl.bcs.cuentaInversion.ConsultarCartolaDeMovimiento.logica}, in the JSON form of its
 * {@code EntradaConsultarCartolaDeMovimiento} / {@code SalidaConsultarCartolaDeMovimiento} (same field names). The connection is the one the caller owns. A null answer is
 * {@code null}.
 */
public interface ConsultarCartolaDeMovimientoPort {

    JsonObject consultar(Connection con, JsonObject entrada) throws Exception;
}
