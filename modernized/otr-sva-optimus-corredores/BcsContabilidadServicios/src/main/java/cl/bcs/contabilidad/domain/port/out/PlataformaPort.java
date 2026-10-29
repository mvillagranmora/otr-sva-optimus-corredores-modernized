package cl.bcs.contabilidad.domain.port.out;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;

/**
 * Port for cross-cutting platform services previously accessed through TomEE
 * utilities (ConsultarEmpresa, ObtenerDiaAnterior, ConfiguracionSistema).
 *
 * Derived from calls made in:
 *  - CentralizaAnticipoPactos.centralizar()
 *  - CentralizaMultiSeleccion.logica()
 *
 * Credential-shaped values (host, passwords) are never included in this interface;
 * concrete implementations read them from environment variables.
 */
public interface PlataformaPort {

    /**
     * Returns company information for the given company identifier.
     * Legacy: ConsultarEmpresa.logica(con, idEmpresa) -> InformacionEmpresa
     */
    EmpresaInfo consultarEmpresa(Long idEmpresa);

    /**
     * Returns the previous business day for the given country and reference date.
     *
     * Legacy: ObtenerDiaAnterior.logica() with
     *   diaHabil      = "S"
     *   soloCaeDiaHabil = "S"
     *   numeroDias    = 1
     *   fechaInicial  = fechaContable
     *
     * @param idPais       country code obtained from EmpresaInfo.idPais()
     * @param fechaContable accounting date in ISO format (yyyy-MM-dd)
     * @return previous business day in ISO format (yyyy-MM-dd)
     */
    String obtenerDiaAnterior(Long idPais, String fechaContable);

    /**
     * Returns the system date configured for the company.
     * Legacy: ConfiguracionSistema.getParametroConfiguracionString(..., "PLA.FechaSistema")
     */
    String obtenerFechaSistema(Long idEmpresa);

    /**
     * Returns the localisation code for the company (e.g. "CL").
     * Legacy: ConfiguracionSistema.getParametroConfiguracionString(..., "PLA.LocalizacionSistema")
     */
    String obtenerLocalizacionSistema(Long idEmpresa);

    /**
     * Returns the system currency code for the company.
     * Legacy: ConfiguracionSistema.getParametroConfiguracionString(..., "PLA.MonedaSistema")
     */
    String obtenerMonedaSistema(Long idEmpresa);

    /**
     * Returns an arbitrary configuration parameter.
     * Legacy: ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, modulo, parametro)
     */
    String obtenerParametroConfiguracion(Long idEmpresa, String modulo, String parametro, String valorDefault);

    /**
     * Retrieves clearing financiero detail from BcsTesoreriaServicios (cross-module).
     * Legacy: ConsultarClearingFinanciero.logica(con, idEmpresa, idUsuario, idModulo, fechaLiquidacion)
     */
    java.util.List<ClearingFinancieroDetalle> consultarClearingFinanciero(
            Long idEmpresa, Long idUsuario, String idModulo, String fechaLiquidacion);

    /** Row returned by ConsultarClearingFinanciero. */
    record ClearingFinancieroDetalle(
            Long       idTransaccion,
            Long       idComprobanteTesoreria,
            String     folioTransaccion,
            java.math.BigDecimal totalVenta,
            java.math.BigDecimal totalCompra,
            String     idMonedaTransaccion,
            String     rutContraparte,
            String     razonSocialContraparte,
            String     compraVenta,
            String     idClasificacionTipoInstrumento,
            String     nombreGrupoCamara,
            String     idCondicionDeLiquidacion,
            String     idBolsa,
            String     fechaLiquidacion
    ) {}
}
