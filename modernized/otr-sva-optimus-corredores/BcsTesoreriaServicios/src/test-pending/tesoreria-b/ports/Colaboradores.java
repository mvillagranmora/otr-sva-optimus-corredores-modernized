package cl.bcs.tesoreria.port.out;

import com.google.gson.JsonObject;

/**
 * INTENDED API (Step 2; not in src/main yet). Outbound port for every service the legacy classes of family B call through their typed
 * {@code logica(...)} (RevisarRequisitosBasicos, CrearIngresoEgreso, LiquidarComprobante, CrearComprobante, CrearCargoAbono, AnularCargoAbono, CalcularParidad,
 * CalcularMontoNumeroCuota, CrearSolicitudAporteRescateNormal, ConsultarInstrumentoPrecioBolsa, CalcularMontoYCobrosOrden, CrearOrdenes, AgregarPrograma, AgregarLog,
 * CrearEventoAutorizacionMasivo, AnulacionContratoCSC, RevocacionContratoCSC, CambiarEstadoContratosSAAPV, StoredProcedure).
 * It is ALWAYS the last constructor argument of a service. {@code servicio} is the legacy class simple name; {@code entrada} is the legacy input
 * object serialised with the legacy Gson settings (the same JSON the golden "colaboradores" trace holds under "entrada").
 */
public interface Colaboradores {

    /** @return the legacy output object of that service as JSON (shape documented in the Javadoc of each oracle shim under legacy-oracle/shims). */
    JsonObject invocar(String servicio, JsonObject entrada);
}
