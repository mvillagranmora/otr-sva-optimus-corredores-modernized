package cl.bcs.fondomutuo.fechas;

import java.time.LocalDate;
import java.util.List;

import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ConfiguracionFechaFFMM;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.DiaFeriado;

/**
 * Read port of {@link CalcularFechaSolicitudAporteRescate} (the two SELECT of the legacy class; verbatim SQL in the adapter): the active date
 * configuration of the company ({@code FMM.ConfiguracionFechasFondoMutuo}) and the active holidays of the given dates and countries
 * ({@code REG.DiaFeriado}, {@code "Fecha" IN (...) AND "IdPais" IN (...)}). Reads only.
 */
public interface FechasPort {

    List<ConfiguracionFechaFFMM> configuracionFechas(Long idEmpresa);

    List<DiaFeriado> feriados(List<LocalDate> fechas, List<Long> idPaises);
}
