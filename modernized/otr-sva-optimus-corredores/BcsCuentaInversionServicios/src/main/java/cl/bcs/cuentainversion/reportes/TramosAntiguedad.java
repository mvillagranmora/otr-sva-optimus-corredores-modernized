package cl.bcs.cuentainversion.reportes;

import cl.bcs.utils.Fecha;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Age tranches of the aging reports, counted in calendar days back from the query date (the legacy asked the platform
 * {@code ObtenerDiaAnterior} for each of the 32 previous days with {@code diaHabil = N}, i.e. plain calendar days, no holidays):
 * A = 0..2 days, B = 3..10, C = 11..30, D = 31 (and, by {@link #de}, anything older than the start of D).
 *
 * <p>Used by CTA-B-69..108 (Bolsa, Acreedores, Antiguedad). A date after the query date belongs to no tranche ({@link Tramo#NINGUNO}).
 */
public final class TramosAntiguedad {

    public enum Tramo { A, B, C, D, NINGUNO }

    public record Rango(LocalDate inicio, LocalDate fin) {
        boolean contiene(LocalDate f) {
            return !f.isBefore(inicio) && !f.isAfter(fin);
        }
    }

    private static final int DIAS_RETROCESO = 31;

    private final Map<Tramo, Rango> rangos = new EnumMap<>(Tramo.class);
    private final List<LocalDate> fechas = new ArrayList<>();

    private TramosAntiguedad() {
    }

    /** @throws java.time.format.DateTimeParseException when {@code fechaConsulta} is not a yyyy-MM-dd date (as the legacy). */
    public static TramosAntiguedad desde(String fechaConsulta) {
        LocalDate consulta = Fecha.toDate(fechaConsulta).toLocalDate();
        TramosAntiguedad t = new TramosAntiguedad();
        for (int dias = DIAS_RETROCESO; dias >= 0; dias--) {
            t.fechas.add(consulta.minusDays(dias));
        }
        t.rangos.put(Tramo.A, new Rango(consulta.minusDays(2), consulta));
        t.rangos.put(Tramo.B, new Rango(consulta.minusDays(10), consulta.minusDays(3)));
        t.rangos.put(Tramo.C, new Rango(consulta.minusDays(30), consulta.minusDays(11)));
        t.rangos.put(Tramo.D, new Rango(consulta.minusDays(31), consulta.minusDays(31)));
        return t;
    }

    public Rango rango(Tramo t) {
        return rangos.get(t);
    }

    /** The 32 dates, oldest first. */
    public List<LocalDate> fechas() {
        return fechas;
    }

    /** Tranche of a date: inside A, B or C; otherwise D when it is not after the (single) day D covers, else none. */
    public Tramo de(LocalDate f) {
        for (Tramo t : new Tramo[]{Tramo.A, Tramo.B, Tramo.C}) {
            if (rangos.get(t).contiene(f)) {
                return t;
            }
        }
        return !f.isAfter(rangos.get(Tramo.D).fin()) ? Tramo.D : Tramo.NINGUNO;
    }
}
