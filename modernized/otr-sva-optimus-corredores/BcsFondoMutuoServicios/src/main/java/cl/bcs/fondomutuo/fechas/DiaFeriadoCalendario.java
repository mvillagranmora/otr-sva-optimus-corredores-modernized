package cl.bcs.fondomutuo.fechas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Row of {@code ConsultarFeriados.sql} as the three {@code ObtenerDia*} helpers read it (legacy {@code DiaFeriado}: Tipo FERIADO/INHABIL, Fecha for
 * a FERIADO, Dia 0..6 for an INHABIL, IdPais). The kit port returns rows by column label; the date is a {@link LocalDate}.
 */
final class DiaFeriadoCalendario {

    String tipo;
    LocalDate fecha;
    Integer dia;
    Long idPais;

    static List<DiaFeriadoCalendario> desde(List<Map<String, Object>> filas) {
        List<DiaFeriadoCalendario> salida = new ArrayList<>();
        for (Map<String, Object> fila : filas) {
            DiaFeriadoCalendario d = new DiaFeriadoCalendario();
            d.tipo = (String) fila.get("Tipo");
            Object f = fila.get("Fecha");
            if (f instanceof java.sql.Date sql) {
                d.fecha = sql.toLocalDate();
            } else if (f instanceof java.util.Date util) {
                d.fecha = new java.sql.Date(util.getTime()).toLocalDate();
            }
            Object dia = fila.get("Dia");
            d.dia = dia == null ? null : Integer.valueOf(((Number) dia).intValue());
            Object pais = fila.get("IdPais");
            d.idPais = pais == null ? null : (pais instanceof Long l ? l : Long.valueOf(((Number) pais).longValue()));
            salida.add(d);
        }
        return salida;
    }
}
