package cl.bcs.apv.domain.port.out;

import com.google.gson.JsonObject;

/**
 * Outbound port (same contract as BcsTesoreriaServicios) for every service the legacy APV classes call through their typed {@code logica(...)}
 * that is NOT an APV use case of this module: CrearIngresoEgreso, LiquidarComprobante, CrearComprobante, CrearCargoAbono, CrearPersona, CrearCliente,
 * CrearTelefono, CrearDireccionElectronica, AgregarLog, CrearEventoAutorizacionMasivo, RevisarRequisitosBasicos, CalcularParidad,
 * ConsultarEmpresa, EntregarPreciosTasas, CalcularMontoNumeroCuota, CalcularMontoYCobrosOrden, CrearOrdenes, ConsultarInstrumentoPrecioBolsa,
 * CrearSolicitudAporteRescateNormal, ConsultarCustodiaPortafolio, ConsultarSaldoCuentaInversion, AnulacionMandatoPAC, RevocacionMandatoPAC,
 * IngresarMandatoPAC, CrearPersonaCuentaBancaria, ModificarPortafolio, StoredProcedure.
 * It is ALWAYS the last constructor argument of a service. {@code servicio} is the legacy class simple name; {@code entrada} is the legacy input
 * object serialised with the legacy Gson settings (the same JSON the golden "colaboradores" trace holds under "entrada").
 * The other APV classes of this module are NOT called through this port: they are injected as typed use cases.
 */
public interface Colaboradores {

    /** @return the legacy output object of that service as JSON. */
    JsonObject invocar(String servicio, JsonObject entrada);
}
