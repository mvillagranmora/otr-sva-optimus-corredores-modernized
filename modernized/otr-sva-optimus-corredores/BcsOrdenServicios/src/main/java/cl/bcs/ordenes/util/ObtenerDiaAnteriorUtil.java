package cl.bcs.ordenes.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.*;

/**
 * Local equivalent of cl.bcs.plataforma.pla.ObtenerDiaAnterior.
 * Calculates N previous business days from a starting date.
 */
@Component
public class ObtenerDiaAnteriorUtil {

    private static final Logger log = LoggerFactory.getLogger(ObtenerDiaAnteriorUtil.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_FERIADOS;

    public ObtenerDiaAnteriorUtil(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_FERIADOS = loadSql("cl/bcs/ordenes/sql/Feriados_ConsultarFeriados.sql");
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<FechaOut> p_out_fechas = new ArrayList<>();
    }

    public static class FechaOut {
        public String fechaInicial;
        public Long numeroDias;
        public String fechaCalculada;
        public Long IdInterno;
        public Long idPais;
    }

    public static class FechaIn {
        public String fechaInicial;
        public Long numeroDias;
        public Long pais;
        public Long IdInterno = 0L;

        public FechaIn() {}
        public FechaIn(String fechaInicial, Long numeroDias, Long pais) {
            this.fechaInicial = fechaInicial;
            this.numeroDias   = numeroDias;
            this.pais         = pais;
        }
    }

    public Salida calcular(Long idPais, String diaHabil, List<FechaIn> fechas) {
        Salida salida = new Salida();
        if (fechas == null || fechas.isEmpty()) return salida;

        List<Long> paises = new ArrayList<>();
        for (FechaIn f : fechas) {
            if (f.pais == null) f.pais = idPais;
            if (f.pais != null && !paises.contains(f.pais)) paises.add(f.pais);
        }

        Map<String, Boolean> feriadoMap = new HashMap<>();
        if (!paises.isEmpty()) {
            try {
                List<Map<String, Object>> rows = jdbc.queryForList(SQL_FERIADOS,
                    new MapSqlParameterSource("IdPais", paises));
                for (Map<String, Object> row : rows) {
                    String tipo = String.valueOf(row.get("Tipo"));
                    Long pais = toLong(row.get("IdPais"));
                    if ("FERIADO".equals(tipo) && row.get("Fecha") != null)
                        feriadoMap.put("FERIADO_" + row.get("Fecha") + "_" + pais, Boolean.TRUE);
                    if ("INHABIL".equals(tipo) && row.get("Dia") != null)
                        feriadoMap.put("INHABIL_" + row.get("Dia") + "_" + pais, Boolean.TRUE);
                }
            } catch (Exception e) {
                log.warn("Could not load feriados: {}", e.getMessage());
            }
        }

        boolean usarHabiles = "S".equalsIgnoreCase(diaHabil);

        for (FechaIn item : fechas) {
            FechaOut out = new FechaOut();
            out.fechaInicial  = item.fechaInicial;
            out.numeroDias    = item.numeroDias;
            out.fechaCalculada = item.fechaInicial;
            out.IdInterno      = item.IdInterno;
            out.idPais         = item.pais;

            if (item.numeroDias == null || item.numeroDias < 1) {
                salida.p_out_fechas.add(out);
                continue;
            }

            Date pivot = toDate(item.fechaInicial);
            int diasContados = 0;

            while (diasContados < item.numeroDias) {
                pivot = subtractDays(pivot, 1);
                if (usarHabiles && esInhabil(feriadoMap, pivot, item.pais)) continue;
                diasContados++;
            }

            out.fechaCalculada = pivot.toLocalDate().toString();
            salida.p_out_fechas.add(out);
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

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static String loadSql(String path) {
        try (var is = ObtenerDiaAnteriorUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
