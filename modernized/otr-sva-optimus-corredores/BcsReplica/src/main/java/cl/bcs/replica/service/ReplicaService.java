package cl.bcs.replica.service;

import org.springframework.stereotype.Service;

/**
 * Pure-logic service for replica operations.
 *
 * All methods are public static — no I/O, no Spring injection required.
 *
 * Business rules:
 *   B-REP-01  buildParametersString formats "BCS_query:<queryName>;Fecha:<fecha>"
 *   B-REP-02  Full uses "QueryReplica"; Delta uses "QueryReplicaDelta"
 *   B-REP-03  esRespaldoActivo gates backup via case-insensitive "S" check
 *   B-REP-04  extractCodPer returns substring(0,7) — year-month from fechaSistema
 */
@Service
public class ReplicaService {

    /**
     * B-REP-01: Builds the PARAMETERS string inserted into WORKSTACK_T.
     *
     * @param queryName e.g. "QueryReplica" or "QueryReplicaDelta"
     * @param fecha     e.g. "2024-01-15"
     * @return "BCS_query:QueryReplica;Fecha:2024-01-15"
     */
    public static String buildParametersString(String queryName, String fecha) {
        return "BCS_query:" + queryName + ";Fecha:" + fecha;
    }

    /**
     * B-REP-04: Extracts COD_PER (year-month) from fechaSistema.
     *
     * @param fechaSistema e.g. "2024-01-15"
     * @return "2024-01"
     */
    public static String extractCodPer(String fechaSistema) {
        return fechaSistema.substring(0, 7);
    }

    /**
     * B-REP-03: Returns true when the RespaldoReplicaTablasSebra config param is "S" (case-insensitive).
     *
     * @param configValue value from CONFIGURACION_T
     * @return true if "S" or "s"
     */
    public static boolean esRespaldoActivo(String configValue) {
        return "S".equalsIgnoreCase(configValue);
    }
}
