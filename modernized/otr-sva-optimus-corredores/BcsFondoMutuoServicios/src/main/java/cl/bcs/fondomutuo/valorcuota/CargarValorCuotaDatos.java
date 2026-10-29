package cl.bcs.fondomutuo.valorcuota;

import java.util.List;

/** Read port of the three SELECTs of the class, verbatim SQL. */
public interface CargarValorCuotaDatos {

    List<CargarValorCuotaContrato.SerieFondoIsin> seriesPorIsin(List<String> isin);

    List<CargarValorCuotaContrato.ValorCuotaVigente> valoresVigentesEnFechas(List<String> idSerieFondo, List<String> fechasVigencia);

    List<CargarValorCuotaContrato.ValorCuotaVigente> historicoDiaAnterior(List<String> idSerieFondo, List<String> fechasCierre);
}
