package cl.bcs.facturacion.port.out;

import cl.bcs.facturacion.domain.Comprobante;

import java.util.List;

/**
 * Output port for BcsFacturacionServicios.
 *
 * Methods that require SAP HANA (procesarFacturacionCustodia, crearComprobante,
 * procesarGananciaCapital) throw UnsupportedOperationException in all adapters
 * until a HANA connection is available.
 */
public interface FacturacionServiciosDataPort {

    String obtenerFechaSistema(Long idEmpresa);

    List<Comprobante> consultarComprobantes(Long idEmpresa, String estado);

    /** Requires HANA — throws UnsupportedOperationException in all current adapters. */
    void procesarFacturacionCustodia(Long idEmpresa, Long idPortafolio);

    /** Requires HANA — throws UnsupportedOperationException in all current adapters. */
    void crearComprobante(Long idEmpresa, String tipoComprobante);

    /** Requires HANA — throws UnsupportedOperationException in all current adapters. */
    void procesarGananciaCapital(Long idEmpresa, Long idComprobante);
}
