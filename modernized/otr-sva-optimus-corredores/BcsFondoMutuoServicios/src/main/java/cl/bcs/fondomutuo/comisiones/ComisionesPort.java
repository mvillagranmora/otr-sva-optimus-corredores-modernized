package cl.bcs.fondomutuo.comisiones;

import java.util.List;

/**
 * Read port of {@link CalcularComisiones}: {@code Comunes_SelectTramoComision.sql} (verbatim) - the active commission brackets (entry / exit / quarterly) of
 * the latest active commission of each series. The series data come from {@code SeriesFondoPort}. Reads only.
 */
public interface ComisionesPort {

    List<TramoComision> tramos(Long idEmpresa, List<String> idSerieFondo);
}
