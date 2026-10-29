package cl.bcs.facturacion.jobs.port.out;

import cl.bcs.facturacion.jobs.domain.ResumenFacturacionCliente;

import java.util.List;

/**
 * Output port for all database operations required by the facturacion jobs module.
 */
public interface FacturacionJobsDataPort {

    /**
     * Returns billing document summaries for a given company within a date range.
     *
     * @param idEmpresa  company identifier
     * @param fechaDesde start date (inclusive), format "yyyy-MM-dd"
     * @param fechaHasta end date (inclusive), format "yyyy-MM-dd"
     * @return list of matching billing document summaries
     */
    List<ResumenFacturacionCliente> consultarResumenFacturas(Long idEmpresa,
                                                             String fechaDesde,
                                                             String fechaHasta);

    /**
     * Returns the current system date for a company from the configuration table.
     *
     * @param idEmpresa company identifier
     * @return value of "PLA.FechaSistema" for the given company
     */
    String obtenerFechaSistema(Long idEmpresa);

    /**
     * Triggers the HANA stored procedure for mass voucher issuance.
     *
     * @throws UnsupportedOperationException always — requires live HANA environment
     */
    void emitirComprobantesMasivos(Long idEmpresa, Long idUsuario);

    /**
     * Triggers the HANA stored procedure for mass pre-invoice generation.
     *
     * @throws UnsupportedOperationException always — requires live HANA environment
     */
    void generarPrefacturasMasivas(Long idEmpresa, Long idUsuario);
}
