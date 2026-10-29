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
 * Local equivalent of cl.bcs.plataforma.pla.ObtenerPlazo.
 * Calculates number of calendar or business days between two dates.
 */
@Component
public class ObtenerPlazoUtil {

    private static final Logger log = LoggerFactory.getLogger(ObtenerPlazoUtil.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_FERIADOS;

    public ObtenerPlazoUtil(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_FERIADOS = loadSql("cl/bcs/ordenes/sql/Feriados_ConsultarFeriados.sql");
    }

    public static class FechasIn {
        public Long IdInterno;
        public String FechaDesde;
        public String FechaHasta;
        public String SoloDiasHabiles;
    }

    public static class FechasOut {
        public Long IdInterno;
        public String FechaDesde;
        public String FechaHasta;
        public String SoloDiasHabiles;
        public Long Plazo;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<FechasOut> p_out_Fechas = new ArrayList<>();
    }

    public Salida calcular(Long idPais, List<FechasIn> fechas) {
        Salida salida = new Salida();
        if (fechas == null || fechas.isEmpty()) return salida;

        List<Map<String, Object>> feriadoRows = Collections.emptyList();
        if (idPais != null) {
            try {
                feriadoRows = jdbc.queryForList(SQL_FERIADOS,
                    new MapSqlParameterSource("IdPais", List.of(idPais)));
            } catch (Exception e) {
                log.warn("Could not load feriados for pais={}: {}", idPais, e.getMessage());
            }
        }

        Map<String, Boolean> feriadoMap = new HashMap<>();
        for (Map<String, Object> row : feriadoRows) {
            String tipo = String.valueOf(row.get("Tipo"));
            if ("FERIADO".equals(tipo) && row.get("Fecha") != null)
                feriadoMap.put("FERIADO_" + row.get("Fecha") + "_" + idPais, Boolean.TRUE);
            if ("INHABIL".equals(tipo) && row.get("Dia") != null)
                feriadoMap.put("INHABIL_" + row.get("Dia") + "_" + idPais, Boolean.TRUE);
        }

        for (FechasIn item : fechas) {
            FechasOut out = new FechasOut();
            out.IdInterno = item.IdInterno;
            out.FechaDesde = item.FechaDesde;
            out.FechaHasta = item.FechaHasta;
            out.SoloDiasHabiles = item.SoloDiasHabiles;
            out.Plazo = 0L;

            if (item.FechaDesde == null || item.FechaHasta == null) {
                salida.p_out_Fechas.add(out);
                continue;
            }

            Date desde = toDate(item.FechaDesde);
            Date hasta = toDate(item.FechaHasta);

            boolean soloDiasHabiles = "S".equalsIgnoreCase(item.SoloDiasHabiles);
            long plazo = 0L;
            Date pivot = hasta;

            while (pivot.after(desde)) {
                pivot = subtractDays(pivot, 1);
                if (soloDiasHabiles) {
                    while (esInhabil(feriadoMap, pivot, idPais)) {
                        pivot = subtractDays(pivot, 1);
                    }
                }
                if (!pivot.before(desde)) plazo++;
                if (pivot.equals(desde)) break;
            }

            out.Plazo = plazo;
            salida.p_out_Fechas.add(out);
        }

        return salida;
    }

    private static boolean esInhabil(Map<String, Boolean> map, Date fecha, Long pais) {
        String fechaStr = fecha.toLocalDate().toString();
        Calendar c = Calendar.getInstance();
        c.setTime(fecha);
        int day = c.get(Calendar.DAY_OF_WEEK) == 1 ? 6 : c.get(Calendar.DAY_OF_WEEK) - 2;
        return map.containsKey("FERIADO_" + fechaStr + "_" + pais)
            || map.containsKey("INHABIL_" + day + "_" + pais);
    }

    private static Date subtractDays(Date d, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DATE, -days);
        return new Date(c.getTimeInMillis());
    }

    private static Date toDate(String s) {
        if (s == null || s.isBlank()) return new Date(System.currentTimeMillis());
        return Date.valueOf(s.substring(0, Math.min(10, s.length())));
    }

    private static String loadSql(String path) {
        try (var is = ObtenerPlazoUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
