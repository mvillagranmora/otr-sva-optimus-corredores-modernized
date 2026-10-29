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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ObtenerDiaHabilMultiplesPaisesService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ObtenerDiaHabilMultiplesPaisesService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_FERIADOS;

    public ObtenerDiaHabilMultiplesPaisesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_FERIADOS = loadSql("cl/bcs/plataforma/pla/sql/ConsultarFeriados.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ObtenerDiaHabilMultiplesPaises"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ObtenerDiaHabilMultiplesPaises");
        Entrada in = GSON.fromJson(params, Entrada.class);
        if (in.p_diaHabil == null) in.p_diaHabil = "N";
        in.p_soloCaeDiaHabil = "S".equalsIgnoreCase(in.p_diaHabil) ? "S"
            : (in.p_soloCaeDiaHabil == null ? "N" : in.p_soloCaeDiaHabil);

        // Collect all paises
        List<Long> paises = in.p_fechas.stream()
            .flatMap(f -> f.paises.stream()).distinct().collect(Collectors.toList());

        List<DiaFeriado> listaDiaFeriado = paises.isEmpty() ? List.of()
            : jdbc.query(SQL_FERIADOS, new MapSqlParameterSource("IdPais", paises),
                BeanPropertyRowMapper.newInstance(DiaFeriado.class));

        Salida salida = new Salida();
        salida.P_OUT_ERRORESCONTADOR = 0L;

        for (FechasIn item : in.p_fechas) {
            FechasOut temp = new FechasOut();
            temp.idInterno = item.idInterno;
            temp.fechaInicial = item.fechaInicial;
            temp.numeroDiasInicial = item.numeroDias;
            temp.numeroDias = item.numeroDias;
            temp.fechaCalculada = item.fechaInicial;
            temp.idPaises = item.paises;

            boolean buscar = item.numeroDias != null && item.numeroDias >= 1;
            int diasSumados = 0;
            int diasSumadosReal = 0;
            LocalDate fPivot = LocalDate.parse(item.fechaInicial);

            while (buscar) {
                fPivot = fPivot.plusDays(1);
                diasSumados++;
                diasSumadosReal++;

                if ("S".equals(in.p_diaHabil)) {
                    boolean esinhabil = true;
                    while (esinhabil) {
                        esinhabil = false;
                        int day = javaDayToOptimus(fPivot.getDayOfWeek().getValue());
                        final LocalDate fp = fPivot;
                        for (Long pais : item.paises) {
                            for (DiaFeriado fer : listaDiaFeriado) {
                                if (!Objects.equals(fer.IdPais, pais)) continue;
                                if (("FERIADO".equalsIgnoreCase(fer.Tipo) && fp.equals(fer.Fecha.toLocalDate()))
                                    || ("INHABIL".equalsIgnoreCase(fer.Tipo) && day == fer.Dia)) {
                                    esinhabil = true;
                                    break;
                                }
                            }
                            if (esinhabil) break;
                        }
                        if (esinhabil) { fPivot = fPivot.plusDays(1); diasSumadosReal++; }
                    }
                }
                if (diasSumados == item.numeroDias) break;
            }

            temp.fechaCalculada = fPivot.toString();
            temp.numeroDias = (long) diasSumadosReal;
            salida.p_out_fechas.add(temp);
        }

        return JsonParser.parseString(GSON.toJson(salida)).getAsJsonObject();
    }

    private int javaDayToOptimus(int javaDow) { return javaDow == 7 ? 6 : javaDow - 1; }

    public static class Entrada {
        public String p_diaHabil;
        public String p_soloCaeDiaHabil;
        public List<FechasIn> p_fechas = new ArrayList<>();
    }

    public static class FechasIn {
        public Long idInterno;
        public String fechaInicial;
        public Long numeroDias;
        public List<Long> paises = new ArrayList<>();
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<FechasOut> p_out_fechas = new ArrayList<>();
    }

    public static class FechasOut {
        public Long idInterno;
        public String fechaInicial;
        public Long numeroDiasInicial;
        public Long numeroDias;
        public String fechaCalculada;
        public List<Long> idPaises;
    }

    public static class DiaFeriado {
        public String Tipo;
        public Date Fecha;
        public Integer Dia;
        public Long IdPais;
    }

    private static String loadSql(String path) {
        try (InputStream is = ObtenerDiaHabilMultiplesPaisesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
