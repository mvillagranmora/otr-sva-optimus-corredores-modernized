package cl.bcs.replica.port.out;

import cl.bcs.replica.domain.Workstack;

/**
 * Output port — data persistence and configuration access for replica operations.
 */
public interface ReplicaDataPort {

    /**
     * Inserts a WORKSTACK_T row.
     */
    void insertarWorkstack(Workstack workstack);

    /**
     * Queries PLA.FechaSistema from CONFIGURACION_T for the given empresa.
     */
    String obtenerFechaSistema(Long idEmpresa);

    /**
     * Queries PLA.RespaldoReplicaTablasSebra from CONFIGURACION_T for the given empresa.
     */
    String obtenerParametroRespaldoSebra(Long idEmpresa);

    /**
     * Executes DELETE+INSERT for BSCL Sebra tables — HANA-specific.
     *
     * @throws UnsupportedOperationException always — this path requires a live HANA connection
     */
    void ejecutarRespaldoSebra(String codPer);
}
