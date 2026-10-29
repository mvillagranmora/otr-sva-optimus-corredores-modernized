package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Migrated from legacy cl.bcs.plataforma.pla.ObtenerPlazo.
 * Calculates the number of calendar or business days between two dates.
 */
@Service
public class ObtenerPlazoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ObtenerPlazoService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_CONSULTAR_FERIADOS;

    public ObtenerPlazoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_CONSULTAR_FERIADOS = loadSql("cl/bcs/plataforma/pla/sql/ObtenerPlazo_SelectFeriados.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ObtenerPlazo"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ObtenerPlazo");
        InObtenerPlazo in = GSON.fromJson(params, InObtenerPlazo.class);
        OutObtenerPlazo out = logica(in);
        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public OutObtenerPlazo logica(InObtenerPlazo entrada) {
        OutObtenerPlazo salida = new OutObtenerPlazo();

        // Load holidays for the given country
        List<DiaFeriado> listaDiaFeriado = jdbc.query(SQL_CONSULTAR_FERIADOS,
            new MapSqlParameterSource("IdPais", entrada.p_IdPais),
            BeanPropertyRowMapper.newInstance(DiaFeriado.class));

        for (FechasIn item : entrada.p_Fechas) {
            salida.p_out_Fechas.add(calcular(item, listaDiaFeriado));
        }

        return salida;
    }

    private FechasOut calcular(FechasIn item, List<DiaFeriado> listaDiaFeriado) {
        FechasOut temp = new FechasOut();
        temp.FechaDesde = item.FechaDesde;
        temp.FechaHasta = item.FechaHasta;
        temp.IdInterno = item.IdInterno;
        temp.SoloDiasHabiles = item.SoloDiasHabiles;
        temp.Plazo = 0L;

        if (item.FechaDesde == null || item.FechaHasta == null) return temp;

        LocalDate desde = LocalDate.parse(item.FechaDesde);
        LocalDate hasta = LocalDate.parse(item.FechaHasta);

        if (!hasta.isAfter(desde)) return temp;

        int diasSumados = 0;
        LocalDate fPivot = hasta;

        while (fPivot.isAfter(desde)) {
            fPivot = fPivot.minusDays(1);
            diasSumados++;

            if ("S".equals(item.SoloDiasHabiles)) {
                boolean esinhabil = true;
                while (esinhabil) {
                    esinhabil = false;
                    int day = javaDayToOptimus(fPivot.getDayOfWeek().getValue());
                    for (DiaFeriado fer : listaDiaFeriado) {
                        if (("FERIADO".equalsIgnoreCase(fer.Tipo) && fPivot.equals(fer.Fecha.toLocalDate()))
                            || ("INHABIL".equalsIgnoreCase(fer.Tipo) && day == fer.Dia)) {
                            esinhabil = true;
                            break;
                        }
                    }
                    if (esinhabil) {
                        fPivot = fPivot.minusDays(1);
                        diasSumados++;
                        if (!fPivot.isAfter(desde)) break;
                    }
                }
            }

            if (fPivot.isEqual(desde) || !fPivot.isAfter(desde)) break;
        }

        temp.Plazo = (long) diasSumados;
        return temp;
    }

    private int javaDayToOptimus(int javaDow) {
        return javaDow == 7 ? 6 : javaDow - 1;
    }

    // ---- DTOs ----
    public static class InObtenerPlazo {
        public List<FechasIn> p_Fechas = new ArrayList<>();
        public Long p_IdPais;
        public String p_EnTablaTemporal = "N";
    }

    public static class FechasIn {
        public Long IdInterno;
        public String FechaDesde;
        public String FechaHasta;
        public String SoloDiasHabiles;
    }

    public static class OutObtenerPlazo {
        public List<FechasOut> p_out_Fechas = new ArrayList<>();
    }

    public static class FechasOut {
        public Long IdInterno;
        public String FechaDesde;
        public String FechaHasta;
        public String SoloDiasHabiles;
        public Long Plazo;
    }

    public static class DiaFeriado {
        public Long IdFeriado;
        public String Tipo;
        public Date Fecha;
        public Integer Dia;
        public String DescripcionDia;
        public Integer AgnoCarga;
        public Long IdPais;
    }

    private static String loadSql(String path) {
        try (InputStream is = ObtenerPlazoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
