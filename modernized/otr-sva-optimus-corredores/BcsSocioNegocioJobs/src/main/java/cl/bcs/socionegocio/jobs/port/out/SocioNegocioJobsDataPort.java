package cl.bcs.socionegocio.jobs.port.out;

import cl.bcs.socionegocio.jobs.domain.ClienteABloquear;
import cl.bcs.socionegocio.jobs.domain.DocumentoCliente;

import java.util.List;

/**
 * Output port: data access operations for SocioNegocio jobs.
 * HANA-backed in production; H2-backed in tests.
 */
public interface SocioNegocioJobsDataPort {

    /**
     * Retrieves client documents that have an expiry date set.
     * Maps to: SELECT ... FROM DOCUMENTOS_CLIENTE_T WHERE FECHA_VIGENCIA IS NOT NULL
     */
    List<DocumentoCliente> consultarDocumentosPorVencer();

    /**
     * Marks the given annexes as expired (ID_ESTADO = 'VEN').
     * Maps to: UPDATE DOCUMENTOS_CLIENTE_T SET ID_ESTADO='VEN' WHERE ID_ANEXO IN (...)
     */
    void actualizarAnexos(List<Long> idAnexos);

    /**
     * Applies blocking data to the given clients.
     * Maps to: UPDATE CLIENTES_T SET ID_TIPO_BLOQUEO=..., COMENTARIO_BLOQUEO=... WHERE ID_CLIENTE=...
     */
    void bloquearClientes(List<ClienteABloquear> clientes);

    /**
     * Returns the system date string configured for the given company.
     * Maps to: SELECT VALOR FROM CONFIGURACION_T WHERE ID_EMPRESA=? AND CLAVE='PLA.FechaSistema'
     */
    String obtenerFechaSistema(Long idEmpresa);

    /**
     * Returns the number of days used for expiry calculation.
     * Maps to: SELECT CAST(VALOR AS INTEGER) FROM CONFIGURACION_T WHERE ... CLAVE='PLA.NumeroDiasFechaVencimiento'
     */
    int obtenerDiasVencimiento(Long idEmpresa);

    /**
     * Executes the HANA stored procedure SP_PURGA_PROSPECTO_CLIENTE.
     * Throws UnsupportedOperationException because the SP cannot run on H2.
     */
    void purgaProspectosClientes(Long idEmpresa, Long idUsuario);
}
