package cl.bcs.contabilidad.domain.port.out;

import java.time.LocalDate;
import java.util.List;

/**
 * Run counter of the SIGA interface (CONT-A-03).
 *
 * <p>Legacy: {@code CreaInterfazDiaria.logica()} lines 93-103 ran {@code ConsultaContadorInterfazSIGA.sql}
 * with {@code :FechaEjecucion} on the platform (HANA) database and kept the first row's {@code Cont}.
 */
public interface InterfazContadorSource {

    /** Every {@code Cont} value returned by ConsultaContadorInterfazSIGA.sql, in query order. */
    List<Long> contadoresInterfazSiga(LocalDate fechaEjecucion);
}
