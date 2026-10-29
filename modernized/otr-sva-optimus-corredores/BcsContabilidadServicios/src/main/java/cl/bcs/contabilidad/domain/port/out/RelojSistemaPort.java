package cl.bcs.contabilidad.domain.port.out;

/**
 * Process date and time stamped on generated records. Legacy {@code FechaDB.ObtenerFechaHoraTs} formatted
 * {@code yyyy-MM-dd} and {@code FechaDB.ObtenerHoraSistemaLocal} (ReversaPreComprobante.java:139-140).
 */
public interface RelojSistemaPort {

    /** {@code yyyy-MM-dd}. */
    String fechaProceso(Long idEmpresa);

    /** {@code HH:mm:ss}. */
    String horaProceso(Long idEmpresa);
}
