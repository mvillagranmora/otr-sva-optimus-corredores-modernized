package cl.bcs.tesoreria.javacall.bcp;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import cl.bcs.tesoreria.javacall.kit.SqlNombrado;

/**
 * Next business day of a country: the part of the legacy platform service {@code ObtenerDiaSiguiente} that RecaudacionBCP uses (business-day
 * counting, {@code p_diaHabil = 'S'}, one date, TES-B-65). A day is not a business day when it is a FERIADO date of the country or its weekday
 * (Monday = 0 .. Sunday = 6, the Optimus numbering) is an INHABIL weekday of the country (REG.DiaFeriado, active rows).
 */
@Component
public class DiaHabilSiguiente {

    /** Row of REG.DiaFeriado as the legacy SQL selects it. */
    public static class DiaFeriado {
        public Long IdFeriado;
        public String Tipo;
        public java.sql.Date Fecha;
        public Integer Dia;
        public String DescripcionDia;
        public Integer AgnoCarga;
        public Long IdPais;
    }

    private static final String SQL_FERIADOS = SqlNombrado.recurso("RecaudacionBCP_ConsultarFeriados");

    private final SqlNombrado sql;

    public DiaHabilSiguiente(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    /** The date ({@code yyyy-MM-dd}) that is {@code dias} business days after {@code fechaInicial}; {@code fechaInicial} itself when {@code dias < 1}. */
    public String sumarDiasHabiles(Long idPais, String fechaInicial, long dias) throws SQLException {
        List<DiaFeriado> filas = sql.consultar(SQL_FERIADOS, Map.of("IdPais", List.of(idPais)), DiaFeriado.class);
        Set<LocalDate> feriados = new HashSet<>();
        Set<Integer> semanaInhabil = new HashSet<>();
        for (DiaFeriado f : filas) {
            if ("FERIADO".equals(f.Tipo)) {
                feriados.add(f.Fecha.toLocalDate());
            }
            if ("INHABIL".equals(f.Tipo)) {
                semanaInhabil.add(f.Dia);
            }
        }
        LocalDate pivote = LocalDate.parse(fechaInicial);
        for (long sumados = 0; sumados < dias; sumados++) {
            pivote = pivote.plusDays(1);
            while (feriados.contains(pivote) || semanaInhabil.contains(pivote.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue())) {
                pivote = pivote.plusDays(1);
            }
        }
        return pivote.toString();
    }
}
