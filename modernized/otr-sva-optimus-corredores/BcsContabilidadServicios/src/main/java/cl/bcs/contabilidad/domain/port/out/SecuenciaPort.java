package cl.bcs.contabilidad.domain.port.out;

import java.util.List;

/** Database sequences, legacy {@code Utils.getSecuencia(con, nombre, cantidad)}. */
public interface SecuenciaPort {

    /** {@code cantidad} next values of the sequence, in order; empty when {@code cantidad <= 0}. */
    List<Long> siguientes(String nombreSecuencia, long cantidad);
}
