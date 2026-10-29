package cl.bcs.cuentainversion.port.out;

import com.google.gson.JsonObject;

/**
 * Collaborators of the reporting services (family B).
 *
 * The legacy reporting classes call four services that live outside the reporting suite:
 * <ul>
 *   <li>{@code cl.bcs.plataforma.pla.utils.ConsultarEmpresa.logica}  -> {@link #consultarEmpresa(long)}</li>
 *   <li>{@code cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.logica} -> {@link #consultarSaldo(JsonObject)}</li>
 *   <li>{@code cl.bcs.plataforma.mer.CalcularParidad.logica} -> {@link #calcularParidad(JsonObject)}</li>
 *   <li>{@code cl.bcs.facturacion.CrearMovimientoComprobante.logica} -> {@link #crearMovimientoComprobante(JsonObject)}</li>
 * </ul>
 * The modern services must receive them as constructor collaborators (so they can be faked) with the same JSON contract the
 * JavaCall services expose: the input and output are the JSON form of the legacy Entrada_/Salida_ POJOs (field names and
 * semantics unchanged). {@code llamadasColaboradores} in every expected.json is the legacy call log the modern services must reproduce.
 */
public interface ColaboradoresReportes {

    /** Company data (at least IdPais); throws an unchecked exception with message "Empresa no existe" when it does not exist. */
    JsonObject consultarEmpresa(long idEmpresa);

    /** Input/output: JSON of EntradaConsultarSaldoCuentaInversion / SalidaConsultarSaldoCuentaInversion. */
    JsonObject consultarSaldo(JsonObject entrada);

    /** Input/output: JSON of CalcularParidadEntrada / CalcularParidadSalida. */
    JsonObject calcularParidad(JsonObject entrada);

    /** Input/output: JSON of CrearMovimientoComprobante_Entrada / CrearMovimientoComprobante_Salida. */
    JsonObject crearMovimientoComprobante(JsonObject entrada);

    /**
     * InformeAttributionAnalysis only. Input: JSON of BcsCartola.EntradaCartola. Output: the rows (flat objects) of
     * SalidaCartola.P_OUT_SALIDA["movimientosTitulos"] (MontoCompraMonedaInstrumento, Accion, ... as JSON numbers / texts).
     */
    java.util.List<JsonObject> movimientosTitulos(JsonObject entradaCartola);

    /** Same contract for SalidaCartola.P_OUT_SALIDA["movimientosCaja"] (Moneda, Nemotecnico, AfectaRentabilidad, Abono, Cargo, ...). */
    java.util.List<JsonObject> movimientosCaja(JsonObject entradaCartola);

    /** Same contract for SalidaCartola.P_OUT_SALIDA["datosSocioNegocio"]. */
    java.util.List<JsonObject> datosSocioNegocio(JsonObject entradaCartola);

    /** Input/output: JSON of EntregarPreciosTasas_Entrada / EntregarPreciosTasas_Salida (P_OUT_PRECIOSTASAS with FechaFuente "yyyy-MM-dd HH:mm:ss"). */
    JsonObject entregarPreciosTasas(JsonObject entrada);
}
