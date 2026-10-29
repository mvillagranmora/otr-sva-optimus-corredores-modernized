package cl.bcs.plataforma.port.out;

import java.sql.Date;
import java.util.List;

/**
 * Output port: retrieve business holidays (feriados) from PLA.Feriado.
 */
public interface FeriadoRepository {

    record Feriado(Long idFeriado, String tipo, Date fecha, Integer dia, String descripcionDia,
                   Long idPais) {}

    /**
     * Retrieve all holidays for a given country.
     *
     * @param idPais country ID
     * @return list of holidays and non-business days
     */
    List<Feriado> findByPais(Long idPais);
}
