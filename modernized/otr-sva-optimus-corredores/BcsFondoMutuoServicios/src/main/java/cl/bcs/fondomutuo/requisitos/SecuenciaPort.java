package cl.bcs.fondomutuo.requisitos;

import java.util.List;

/**
 * Legacy {@code Utils.getSecuencia(con, nombreSecuencia, cantidad)}: {@code cantidad} consecutive values of a HANA sequence on the connection of the
 * call. {@code cantidad <= 0} (or null) gives an empty list without touching the database. A sequence is consumed even if the request is later
 * rolled back (HANA sequences are not transactional): ids are not gap free.
 */
public interface SecuenciaPort {

    List<Long> siguientes(String nombreSecuencia, Long cantidad);
}
