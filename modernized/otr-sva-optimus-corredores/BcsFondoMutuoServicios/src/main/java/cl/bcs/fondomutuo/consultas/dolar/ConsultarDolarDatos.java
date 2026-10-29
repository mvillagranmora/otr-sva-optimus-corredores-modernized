package cl.bcs.fondomutuo.consultas.dolar;

import java.util.List;

/** Read port: all rows of {@code ConsultarValorDolarFondoMutuo_SelectValorCuotaTipoCambio.sql}, verbatim; {@code IN (:IdSerieFondo)} receives the list as given. */
public interface ConsultarDolarDatos {

    List<ConsultarDolarContrato.ValorCuotaTipoParidad> valoresTipoCambio(List<String> idsSerieFondo, Long idEmpresa);
}
