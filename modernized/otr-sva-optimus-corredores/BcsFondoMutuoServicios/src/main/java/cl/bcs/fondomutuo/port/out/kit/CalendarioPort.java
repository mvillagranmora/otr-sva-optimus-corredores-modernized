package cl.bcs.fondomutuo.port.out.kit;

import java.util.List;
import java.util.Map;

/**
 * Kit port K: the calendar and clock data that the date helpers of the closure ({@code ObtenerDiaAnterior}, {@code ObtenerDiaSiguiente},
 * {@code ObtenerDiaHabilMultiplesPaises}, {@code FechaDB}, {@code MonedaFormato}) read. Only the READS live here (verbatim SQL); the date arithmetic
 * of those helpers is ported with {@code CalcularFechaSolicitudAporteRescate} in step 3A, on top of this port.
 */
public interface CalendarioPort {

    /** {@code ConsultarFeriados.sql}: active holidays of the given countries (rows: IdFeriado, Tipo, Fecha, Dia, DescripcionDia, AgnoCarga, IdPais). */
    List<Map<String, Object>> feriados(List<Long> idPaises);

    /** {@code Formato_ConsultarMonedaFormato.sql}: the whole {@code PLA.MonedaFormato} table (decimals and rounding per currency/use). */
    List<Map<String, Object>> monedaFormato();

    /** Legacy {@code FechaDB.ObtenerFechaHoraSistemaLocal}-style clock: {@code PLA.FechaSistema} date + database UTC time shifted by {@code PLA.SegundosZonaHoraria}. */
    java.sql.Timestamp fechaHoraSistemaLocal(Long idEmpresa);
}
