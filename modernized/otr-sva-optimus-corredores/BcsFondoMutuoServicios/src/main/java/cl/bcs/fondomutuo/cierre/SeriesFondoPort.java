package cl.bcs.fondomutuo.cierre;

import java.util.List;

/**
 * Read port of the closure of step 3A: {@code ComunesSolicitudes_SelectSerieFondo.sql} (verbatim) - the series of a company by id, with the fund and
 * fund-type data and the four national/foreign-dollar flags, mapped by column label onto {@link SerieFondoSolicitudes}. Reads only.
 */
public interface SeriesFondoPort {

    List<SerieFondoSolicitudes> series(Long idEmpresa, List<String> idSerieFondo);
}
