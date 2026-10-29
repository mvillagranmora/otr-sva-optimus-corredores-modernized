package cl.bcs.canalagente.port.out;

import cl.bcs.canalagente.domain.HistorialMovimientosRequest;
import cl.bcs.canalagente.domain.HistorialMovimientosResponse;

import java.util.Optional;

/**
 * Outbound data port for Canal Agente Servicios.
 *
 * findIdClienteByPortafolio is H2-testable.
 * consultarHistorialCompleto requires full SAP HANA connectivity
 * (calls ConsultarCartolaDeMovimiento + HANA temp table pattern) and
 * throws UnsupportedOperationException until a HANA adapter is wired.
 */
public interface CanalAgenteDataPort {

    /**
     * Finds the IdCliente associated with a given IdPortafolio.
     *
     * Legacy SQL: SELECT TOP 1 "IdCliente" FROM "BSCL.Portafolio.BD::POR.Portafolio"
     * WHERE "IdPortafolio" = :IdPortafolio
     */
    Optional<Long> findIdClienteByPortafolio(Long idPortafolio);

    /**
     * Executes the full HANA-driven historial movimientos workflow:
     * 1. Calls ConsultarCartolaDeMovimiento stored-procedure chain.
     * 2. Inserts results into temp table ConsultarHistorialMovimientosCuentaInversion_Proceso.
     * 3. Selects enriched data (FolioComprobante, CodigoPortafolio, IdRespuestaDTE,
     *    RespuestaPdfDTE, DocumentoIdentidad) via HANA JOINs.
     * 4. Deletes temp table rows.
     * 5. Returns the combined result.
     *
     * Requires HANA — throws UnsupportedOperationException otherwise.
     */
    HistorialMovimientosResponse consultarHistorialCompleto(
            HistorialMovimientosRequest request, Long idCliente);
}
