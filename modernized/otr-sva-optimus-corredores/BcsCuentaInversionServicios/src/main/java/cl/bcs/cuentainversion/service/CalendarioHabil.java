package cl.bcs.cuentainversion.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Business-day calendar of a country ({@code REG.DiaFeriado}): adds or subtracts days, optionally counting only business
 * days. Replaces the legacy platform services ObtenerDiaSiguiente / ObtenerDiaAnterior for the Cuenta de Inversion module.
 *
 * <p>A day is non business when it is a FERIADO on that exact date or an INHABIL week day (Optimus numbers Monday = 0 ..
 * Sunday = 6).
 */
@Component
public class CalendarioHabil {

    private static final String SQL_FERIADOS = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/Calendario_ConsultarFeriados.sql");

    /** Resulting date and the number of calendar days that were actually walked (non business days included). */
    public record Resultado(LocalDate fecha, long numeroDias) {
    }

    private record Inhabiles(Set<LocalDate> feriados, Set<Integer> diasSemana) {
        boolean esInhabil(LocalDate f) {
            return feriados.contains(f) || diasSemana.contains(diaOptimus(f));
        }
    }

    private final NamedParameterJdbcTemplate jdbc;

    public CalendarioHabil(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Next date after {@code numeroDias} days (CTA-A-12..31, CTA-A-48..76 and the WS cases that depend on the settlement date).
     *
     * @param soloDiasHabiles       days are counted over business days only
     * @param soloCaeDiaHabil       when days are counted over calendar days, move the result forward to a business day
     * @param consideraHabilAnterior with {@code soloCaeDiaHabil}, move backwards instead of forwards
     */
    public Resultado siguiente(LocalDate inicial, Long numeroDias, Long idPais, boolean soloDiasHabiles,
                               boolean soloCaeDiaHabil, boolean consideraHabilAnterior) {
        Inhabiles inhabiles = cargar(idPais);
        boolean ajustar = !soloDiasHabiles && soloCaeDiaHabil;
        LocalDate pivote = inicial;
        long dias = 0;
        long diasReales = 0;
        boolean buscar = numeroDias != null && numeroDias >= 1;
        while (buscar) {
            pivote = pivote.plusDays(1);
            dias++;
            diasReales++;
            if (soloDiasHabiles) {
                while (inhabiles.esInhabil(pivote)) {
                    pivote = pivote.plusDays(1);
                    diasReales++;
                }
            }
            if (dias == numeroDias) {
                break;
            }
        }
        long caminados = diasReales;
        if (ajustar) {
            while (inhabiles.esInhabil(pivote)) {
                if (consideraHabilAnterior) {
                    pivote = pivote.minusDays(1);
                    caminados--;
                } else {
                    pivote = pivote.plusDays(1);
                    caminados++;
                }
            }
        }
        return new Resultado(pivote, caminados);
    }

    /**
     * Previous date after {@code numeroDias} days back (consulta de movimientos: day before the go-live cut-off).
     *
     * @param excluirFeriados only INHABIL week days (not FERIADO dates) make a day non business
     */
    public Resultado anterior(LocalDate inicial, Long numeroDias, Long idPais, boolean soloDiasHabiles,
                              boolean soloCaeDiaHabil, boolean excluirFeriados) {
        Inhabiles inhabiles = cargar(idPais);
        Inhabiles aplicables = excluirFeriados ? new Inhabiles(Set.of(), inhabiles.diasSemana()) : inhabiles;
        LocalDate pivote = inicial;
        long dias = 0;
        long diasReales = 0;
        boolean buscar = numeroDias != null && numeroDias >= 1;
        while (buscar) {
            pivote = pivote.minusDays(1);
            dias++;
            diasReales++;
            if (soloDiasHabiles) {
                while (aplicables.esInhabil(pivote)) {
                    pivote = pivote.minusDays(1);
                    diasReales++;
                }
            }
            if (dias == numeroDias) {
                break;
            }
        }
        long caminados = diasReales;
        if (!soloDiasHabiles && soloCaeDiaHabil) {
            while (aplicables.esInhabil(pivote)) {
                pivote = pivote.plusDays(1);
                caminados++;
            }
        }
        return new Resultado(pivote, caminados);
    }

    /** Optimus week day number: Monday = 0 .. Sunday = 6. */
    static int diaOptimus(LocalDate f) {
        DayOfWeek d = f.getDayOfWeek();
        return d.getValue() - 1;
    }

    private static LocalDate aFecha(Object valor) {
        if (valor instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        if (valor instanceof java.sql.Timestamp t) {
            return t.toLocalDateTime().toLocalDate();
        }
        return LocalDate.parse(valor.toString().substring(0, 10));
    }

    private Inhabiles cargar(Long idPais) {
        List<Map<String, Object>> filas = jdbc.queryForList(SQL_FERIADOS, new MapSqlParameterSource("IdPais", idPais));
        Set<LocalDate> feriados = new HashSet<>();
        Set<Integer> semana = new HashSet<>();
        for (Map<String, Object> f : filas) {
            String tipo = String.valueOf(f.get("Tipo"));
            if ("FERIADO".equals(tipo) && f.get("Fecha") != null) {
                feriados.add(aFecha(f.get("Fecha")));
            } else if ("INHABIL".equals(tipo) && f.get("Dia") != null) {
                semana.add(((Number) f.get("Dia")).intValue());
            }
        }
        return new Inhabiles(feriados, semana);
    }
}
