package cl.bcs.custodia.jobs.port.out;

import cl.bcs.custodia.jobs.domain.InstrumentoPrecio;
import cl.bcs.custodia.jobs.domain.PosicionCustodia;

import java.math.BigDecimal;
import java.util.List;

/**
 * Output port: defines all data-access operations for Custodia Jobs.
 *
 * Methods backed by HANA stored procedures throw UnsupportedOperationException.
 * Methods backed by plain SQL are implemented in the H2-testable adapter.
 */
public interface CustodiaJobsDataPort {

    /**
     * Returns the system date configured for the given company.
     * SQL: SELECT VALOR FROM CONFIGURACION_T WHERE ID_EMPRESA=:idEmpresa AND CLAVE='PLA.FechaSistema'
     *
     * Legacy: ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema")
     *   used across ValidarHorarioEjercicioBloqueoOpciones, PautaTipificarInstrumentos, PautaVencimientoIF
     */
    String obtenerFechaSistema(Long idEmpresa);

    /**
     * Returns instrument prices for a given company and date.
     * SQL: SELECT ID_INSTRUMENTO, CODIGO_NEMO, PRECIO, FECHA_PRECIO
     *      FROM INSTRUMENTOS_PRECIO_T WHERE ID_EMPRESA=:idEmpresa AND FECHA_PRECIO=:fecha
     *
     * Legacy: RegistraPrecioInstrumento.java — SELECT_INSTRUMENTOS query
     */
    List<InstrumentoPrecio> consultarPreciosInstrumentos(Long idEmpresa, String fecha);

    /**
     * Updates the price of a specific instrument on a given date.
     * SQL: UPDATE INSTRUMENTOS_PRECIO_T SET PRECIO=:precio
     *      WHERE ID_INSTRUMENTO=:idInstrumento AND FECHA_PRECIO=:fecha
     *
     * Legacy: RegistraPrecioInstrumento.java — INSERT_INSTRUMENTOS / DELETE_INSTRUMENTOS pattern
     */
    void actualizarPrecioInstrumento(Long idInstrumento, BigDecimal precio, String fecha);

    /**
     * Processes product maturities via HANA stored procedure.
     * NOT testable with H2 — always throws UnsupportedOperationException.
     *
     * Legacy: VencimientoProductos.java — HANA SP_PROCESAR_VENCIMIENTO_PRODUCTOS
     */
    void procesarVencimientoProductos(Long idEmpresa, String tipoProducto, String fecha);

    /**
     * Classifies instruments via HANA stored procedure.
     * NOT testable with H2 — always throws UnsupportedOperationException.
     *
     * Legacy: PautaTipificarInstrumentos.java — TipificarInstrumentos service (HANA SP)
     */
    void tipificarInstrumentos(Long idEmpresa);

    /**
     * Executes maturity processing via HANA stored procedure.
     * NOT testable with H2 — always throws UnsupportedOperationException.
     *
     * Legacy: PautaVencimientoIF.java — EjecutarVencimiento service (HANA SP)
     */
    void ejecutarVencimiento(Long idEmpresa, String fecha);

    /**
     * Returns custody positions for a given company and date.
     * SQL: SELECT ID_CLIENTE, ID_MONEDA, CANTIDAD, VALOR_MERCADO
     *      FROM POSICION_CUSTODIA_T WHERE ID_EMPRESA=:idEmpresa AND FECHA_POSICION=:fecha
     *
     * Legacy: GenerarResumenPosiciones.java — SP_RESUMEN_TARJETA_EXISTENCIA (data portion)
     */
    List<PosicionCustodia> consultarPosicionesCliente(Long idEmpresa, String fecha);
}
