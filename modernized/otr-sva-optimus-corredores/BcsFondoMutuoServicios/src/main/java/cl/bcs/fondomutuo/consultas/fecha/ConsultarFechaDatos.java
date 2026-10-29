package cl.bcs.fondomutuo.consultas.fecha;

import java.util.List;

/** Read port: all rows of {@code ConsultarFechaSolicitudAporteRescate_SelectSerieFondo.sql}, verbatim, with the list as given. */
public interface ConsultarFechaDatos {

    List<ConsultarFechaContrato.ConfiguracionSerie> configuracionSeries(List<String> idsSerieFondo, Long idEmpresa);
}
