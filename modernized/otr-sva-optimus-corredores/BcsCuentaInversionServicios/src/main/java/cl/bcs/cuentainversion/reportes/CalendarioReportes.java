package cl.bcs.cuentainversion.reportes;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Business-day calendar of a country ({@code REG.DiaFeriado}, active rows) for the reporting family: the in-process replacement of the
 * platform services {@code ObtenerDiaSiguiente} / {@code ObtenerDiaAnterior}. A day is non business when it is a FERIADO on that exact
 * date or an INHABIL week day (Optimus numbers Monday = 0 .. Sunday = 6).
 *
 * <p>Create one instance per request: the calendar of a country is read once.
 */
public final class CalendarioReportes {

    private static final String SQL_FERIADOS = SqlReportes.recurso("plataforma/ConsultarFeriados");

    /** Calculated date and the number of calendar days walked to reach it (non business days included). */
    public record Calculo(LocalDate fecha, long numeroDias) {
    }

    public static final class Feriado {
        public String Tipo;
        public java.sql.Date Fecha;
        public Integer Dia;
    }

    private final SqlReportes sql;
    private Long paisCargado;
    private final Set<LocalDate> feriados = new HashSet<>();
    private final Set<Integer> diasInhabiles = new HashSet<>();

    public CalendarioReportes(SqlReportes sql) {
        this.sql = sql;
    }

    /**
     * Legacy {@code ObtenerDiaSiguiente} for one date: moves forward {@code numeroDias}; with {@code soloDiasHabiles} only business days
     * are counted; otherwise, with {@code soloCaeDiaHabil}, the result is moved to a business day (backwards when
     * {@code consideraHabilAnterior}).
     */
    public Calculo siguiente(LocalDate inicial, Long numeroDias, Long idPais, boolean soloDiasHabiles, boolean soloCaeDiaHabil,
                             boolean consideraHabilAnterior) {
        cargar(idPais);
        LocalDate pivote = inicial;
        long contados = 0;
        long caminados = 0;
        boolean buscar = numeroDias != null && numeroDias >= 1;
        while (buscar) {
            pivote = pivote.plusDays(1);
            contados++;
            caminados++;
            if (soloDiasHabiles) {
                while (esInhabil(pivote)) {
                    pivote = pivote.plusDays(1);
                    caminados++;
                }
            }
            if (contados == numeroDias) {
                break;
            }
        }
        if (!soloDiasHabiles && soloCaeDiaHabil) {
            while (esInhabil(pivote)) {
                if (consideraHabilAnterior) {
                    pivote = pivote.minusDays(1);
                    caminados--;
                } else {
                    pivote = pivote.plusDays(1);
                    caminados++;
                }
            }
        }
        return new Calculo(pivote, caminados);
    }

    /** Legacy {@code ObtenerDiaAnterior} for one date (same rules, backwards; the business-day adjustment moves forward). */
    public Calculo anterior(LocalDate inicial, Long numeroDias, Long idPais, boolean soloDiasHabiles, boolean soloCaeDiaHabil) {
        cargar(idPais);
        LocalDate pivote = inicial;
        long contados = 0;
        long caminados = 0;
        boolean buscar = numeroDias != null && numeroDias >= 1;
        while (buscar) {
            pivote = pivote.minusDays(1);
            contados++;
            caminados++;
            if (soloDiasHabiles) {
                while (esInhabil(pivote)) {
                    pivote = pivote.minusDays(1);
                    caminados++;
                }
            }
            if (contados == numeroDias) {
                break;
            }
        }
        if (!soloDiasHabiles && soloCaeDiaHabil) {
            while (esInhabil(pivote)) {
                pivote = pivote.plusDays(1);
                caminados++;
            }
        }
        return new Calculo(pivote, caminados);
    }

    private boolean esInhabil(LocalDate f) {
        return feriados.contains(f) || diasInhabiles.contains(f.getDayOfWeek().getValue() - 1);
    }

    private void cargar(Long idPais) {
        if (idPais != null && idPais.equals(paisCargado)) {
            return;
        }
        feriados.clear();
        diasInhabiles.clear();
        for (Feriado f : sql.consultar(SQL_FERIADOS, Map.of("IdPais", idPais), Feriado.class)) {
            if ("FERIADO".equalsIgnoreCase(f.Tipo) && f.Fecha != null) {
                feriados.add(f.Fecha.toLocalDate());
            } else if ("INHABIL".equalsIgnoreCase(f.Tipo) && f.Dia != null) {
                diasInhabiles.add(f.Dia);
            }
        }
        paisCargado = idPais;
    }
}
