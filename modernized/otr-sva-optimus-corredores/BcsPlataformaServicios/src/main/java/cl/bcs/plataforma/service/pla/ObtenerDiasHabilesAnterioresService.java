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

@Service
public class ObtenerDiasHabilesAnterioresService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ObtenerDiasHabilesAnterioresService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_FERIADOS;

    public ObtenerDiasHabilesAnterioresService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_FERIADOS = loadSql("cl/bcs/plataforma/pla/sql/ConsultarFeriados.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ObtenerDiasHabilesAnteriores");
        InObtenerDiasHabilesAnteriores in = GSON.fromJson(params, InObtenerDiasHabilesAnteriores.class);

        List<DiaFeriado> listaDiaFeriado = jdbc.query(SQL_FERIADOS,
            new MapSqlParameterSource("IdPais", in.idPais),
            BeanPropertyRowMapper.newInstance(DiaFeriado.class));

        OutObtenerDiasHabilesAnteriores salida = new OutObtenerDiasHabilesAnteriores();
        salida.erroresContador = 0L;

        for (FechasIn item : in.fechas) {
            FechasOut temp = new FechasOut();
            temp.fechaInicial = item.fechaInicial;
            temp.numeroDiasInicial = item.numeroDias;
            temp.numeroDias = item.numeroDias;
            temp.fechaCalculada = "0001-01-01";

            int diasSumadosReal = 0;
            LocalDate fPivot = LocalDate.parse(item.fechaInicial);

            for (int i = item.numeroDias != null ? item.numeroDias.intValue() : 0; i > 0; i--) {
                fPivot = fPivot.minusDays(1);
                diasSumadosReal++;

                if ("S".equals(in.diaHabil)) {
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
                            diasSumadosReal++;
                        }
                    }
                }
            }

            temp.fechaCalculada = fPivot.toString();
            temp.numeroDias = (long) diasSumadosReal;
            salida.fechas.add(temp);
        }

        return JsonParser.parseString(GSON.toJson(salida)).getAsJsonObject();
    }

    private int javaDayToOptimus(int javaDow) { return javaDow == 7 ? 6 : javaDow - 1; }

    public static class InObtenerDiasHabilesAnteriores {
        public Long idPais;
        public String diaHabil;
        public String soloCaeDiaHabil = "N";
        public List<FechasIn> fechas = new ArrayList<>();
    }

    public static class FechasIn {
        public String fechaInicial;
        public Long numeroDias;
    }

    public static class OutObtenerDiasHabilesAnteriores {
        public Long erroresContador = 0L;
        public List<FechasOut> fechas = new ArrayList<>();
    }

    public static class FechasOut {
        public String fechaInicial;
        public Long numeroDiasInicial;
        public Long numeroDias;
        public String fechaCalculada;
    }

    public static class DiaFeriado {
        public String Tipo;
        public Date Fecha;
        public Integer Dia;
        public Long IdPais;
    }

    private static String loadSql(String path) {
        try (InputStream is = ObtenerDiasHabilesAnterioresService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
