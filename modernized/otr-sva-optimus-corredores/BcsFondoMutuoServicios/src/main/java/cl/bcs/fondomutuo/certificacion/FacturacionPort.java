package cl.bcs.fondomutuo.certificacion;

import com.google.gson.JsonObject;

/**
 * Port of the legacy Facturacion services {@code CrearMovimientoComprobante.logica} and {@code CrearComprobante.logica} as the legacy JSON of their {@code _Entrada} / {@code _Salida}
 * (F-G, FM-CER-48..51). Gate G6 ({@code Compuertas.g6Facturacion}) guards it. No production implementation exists yet (T5): a bean of it must not be wired by accident.
 */
public interface FacturacionPort {

    JsonObject crearMovimientoComprobante(JsonObject entrada);

    JsonObject crearComprobante(JsonObject entrada);
}
