package cl.bcs.ordenes.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

/**
 * Local equivalent of cl.bcs.plataforma.pla.ObtenerDiaSiguiente.
 * Calculates next business day(s) for a given country, using HANA holiday calendar.
 */
@Component
public class ObtenerDiaSiguienteUtil {

    private static final Logger log = LoggerFactory.getLogger(ObtenerDiaSiguienteUtil.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_FERIADOS;

    public ObtenerDiaSiguienteUtil(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_FERIADOS = loadSql("cl/bcs/ordenes/sql/Feriados_ConsultarFeriados.sql");
    }

    public static class FechaIn {
        public String fechaInicial;
        public Long   numeroDias;
        public Long   pais;
        public Long   IdInterno = 0L;

        public FechaIn() {}
        public FechaIn(String fechaInicial, Long numeroDias, Long idPais) {
            this.fechaInicial = fechaInicial;
            this.numeroDias   = numeroDias;
            this.pais         = idPais;
        }
    }

    public static class FechaOut {
        public String fechaInicial;
        public Long   numeroDiasInicial;
        public Long   numeroDias;
        public String fechaCalculada;
        public Long   idPais;
        public Long   IdInterno;
    }

    public static class Salida {
        public Long            P_OUT_ERRORESCONTADOR = 0L;
        public List<FechaOut>  p_out_fechas          = new ArrayList<>();
    }

    /**
     * @param idPais        fallback country if individual FechaIn.pais is null
     * @param diaHabil      "S" = count only business days, "N" = count calendar days
     * @param soloCaeDiaHabil "S" = result must land on a business day
     * @param fechas        list of date+days to calculate
     */
    public Salida calcular(Long idPais, String diaHabil, String soloCaeDiaHabil, List<FechaIn> fechas) {
        Salida salida = new Salida();
        if (fechas == null || fechas.isEmpty()) return salida;

        if (diaHabil == null) diaHabil = "N";
        if (soloCaeDiaHabil == null) soloCaeDiaHabil = "N";

        // Resolve per-entry pais and collect distinct pais values
        List<Long> paises = new ArrayList<>();
        for (FechaIn f : fechas) {
            f.pais = (f.pais == null && idPais != null) ? idPais : f.pais;
            if (f.pais != null && !paises.contains(f.pais)) paises.add(f.pais);
        }

        // Load holiday calendar
        Map<String, Object> feriadoRows = new HashMap<>();
        if (!paises.isEmpty()) {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_FERIADOS,
                    new MapSqlParameterSource("IdPais", paises));
            for (Map<String, Object> row : rows) {
                String tipo   = String.valueOf(row.get("Tipo"));
                Object fecha  = row.get("Fecha");
                Object dia    = row.get("Dia");
                Long   pais   = toLong(row.get("IdPais"));
                if ("FERIADO".equals(tipo) && fecha != null)
                    feriadoRows.put("FERIADO_" + fecha + "_" + pais, Boolean.TRUE);
                if ("INHABIL".equals(tipo) && dia != null)
                    feriadoRows.put("INHABIL_" + dia + "_" + pais, Boolean.TRUE);
            }
        }

        final String finalDiaHabil      = diaHabil;
        final String finalSoloCaeDia    = soloCaeDiaHabil;

        for (FechaIn item : fechas) {
            FechaOut out = new FechaOut();
            out.fechaInicial      = item.fechaInicial;
            out.numeroDiasInicial = item.numeroDias;
            out.numeroDias        = item.numeroDias;
            out.fechaCalculada    = item.fechaInicial;
            out.IdInterno         = item.IdInterno;
            out.idPais            = item.pais;

            boolean buscar = item.numeroDias != null && item.numeroDias >= 1;
            int diasSumados     = 0;
            int diasSumadosReal = 0;
            Date fPivot = toDate(item.fechaInicial);

            while (buscar) {
                fPivot = addDays(fPivot, 1);
                diasSumados++;
                diasSumadosReal++;

                if ("S".equals(finalDiaHabil)) {
                    while (esInhabil(feriadoRows, fPivot, item.pais)) {
                        fPivot = addDays(fPivot, 1);
                        diasSumadosReal++;
                    }
                }

                if (diasSumados == item.numeroDias) break;
            }

            out.fechaCalculada = toStringDate(fPivot);
            out.numeroDias     = (long) diasSumadosReal;
            salida.p_out_fechas.add(out);
        }

        // Adjust to next business day if result must land on a business day
        if ("N".equals(finalDiaHabil) && "S".equals(finalSoloCaeDia)) {
            for (FechaOut o : salida.p_out_fechas) {
                Date fPivot = toDate(o.fechaCalculada);
                while (esInhabil(feriadoRows, fPivot, o.idPais)) {
                    fPivot = addDays(fPivot, 1);
                    o.numeroDias++;
                }
                o.fechaCalculada = toStringDate(fPivot);
            }
        }

        return salida;
    }

    // ── Date helpers ─────────────────────────────────────────────────────────────

    private static boolean esInhabil(Map<String, Object> map, Date fecha, Long pais) {
        String fechaStr = toStringDate(fecha);
        Calendar c = Calendar.getInstance();
        c.setTime(fecha);
        // Java: Sunday=1 → optimus: Monday=0
        int dia = c.get(Calendar.DAY_OF_WEEK) == 1 ? 6 : c.get(Calendar.DAY_OF_WEEK) - 2;
        return map.containsKey("FERIADO_" + fechaStr + "_" + pais)
            || map.containsKey("INHABIL_" + dia + "_" + pais);
    }

    private static Date addDays(Date d, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DATE, days);
        return new Date(c.getTimeInMillis());
    }

    private static Date toDate(String s) {
        return (s == null || s.isBlank()) ? new Date(System.currentTimeMillis()) : Date.valueOf(s.substring(0, 10));
    }

    private static String toStringDate(Date d) {
        return d.toLocalDate().toString();
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static String loadSql(String path) {
        try (var is = ObtenerDiaSiguienteUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
