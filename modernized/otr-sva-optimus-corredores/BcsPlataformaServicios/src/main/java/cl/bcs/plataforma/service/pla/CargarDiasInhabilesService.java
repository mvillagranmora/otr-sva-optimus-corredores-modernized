package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import cl.bcs.plataforma.service.utils.ConfiguracionSistemaUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Service
public class CargarDiasInhabilesService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CargarDiasInhabilesService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final Map<Integer, String> DIAS_INHABILES = Map.of(5, "Sábado", 6, "Domingo");

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil config;
    private final String SQL_SELECT;
    private final String SQL_UPDATE;
    private final String SQL_INSERT;

    public CargarDiasInhabilesService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistemaUtil config) {
        this.jdbc = jdbc;
        this.config = config;
        this.SQL_SELECT = loadSql("cl/bcs/plataforma/pla/sql/CargarDiasInhabiles_SelectDiasInhabiles.sql");
        this.SQL_UPDATE = loadSql("cl/bcs/plataforma/pla/sql/CargarDiasInhabiles_UpdateDiasInhabiles.sql");
        this.SQL_INSERT = loadSql("cl/bcs/plataforma/pla/sql/CargarDiasInhabiles_InsertDiasInhabiles.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.CargarDiasInhabiles"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::CargarDiasInhabiles");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            String fechaSistema = config.getParametroString(in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
            Long idPais = config.getParametroLong(in.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
            int agnoSiguiente = LocalDate.parse(fechaSistema).getYear() + 1;

            List<DiaFeriado> existentes = jdbc.query(SQL_SELECT,
                new MapSqlParameterSource().addValue("agnoCarga", agnoSiguiente).addValue("idPais", idPais),
                BeanPropertyRowMapper.newInstance(DiaFeriado.class));

            List<DiaFeriado> toInsert = new ArrayList<>();
            List<DiaFeriado> toUpdate = new ArrayList<>();

            if (existentes.isEmpty()) {
                toInsert.add(crearDia(5, agnoSiguiente, idPais, 1L));
                toInsert.add(crearDia(6, agnoSiguiente, idPais, 2L));
            } else {
                procesarDia(existentes, toInsert, toUpdate, 5, agnoSiguiente, idPais, 1L);
                procesarDia(existentes, toInsert, toUpdate, 6, agnoSiguiente, idPais, 2L);
            }

            if (!toInsert.isEmpty()) {
                SqlParameterSource[] srcs = toInsert.stream()
                    .map(BeanPropertySqlParameterSource::new).toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_INSERT, srcs);
            }
            if (!toUpdate.isEmpty()) {
                SqlParameterSource[] srcs = toUpdate.stream()
                    .map(BeanPropertySqlParameterSource::new).toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_UPDATE, srcs);
            }
        } catch (Exception e) {
            log.error("Error en CargarDiasInhabiles: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    private DiaFeriado crearDia(int dia, int agno, Long idPais, Long numero) {
        DiaFeriado d = new DiaFeriado();
        d.Dia = dia;
        d.Tipo = "INHABIL";
        d.DescripcionDia = DIAS_INHABILES.getOrDefault(dia, "");
        d.Fecha = Date.valueOf("0001-01-01");
        d.IdEstado = "ACT";
        d.IdPais = idPais;
        d.AgnoCarga = agno;
        d.Numero = numero;
        return d;
    }

    private void procesarDia(List<DiaFeriado> existentes, List<DiaFeriado> toInsert, List<DiaFeriado> toUpdate,
                              int dia, int agno, Long idPais, Long numero) {
        existentes.stream().filter(d -> d.Dia != null && d.Dia == dia).findFirst().ifPresentOrElse(
            found -> { if ("INA".equalsIgnoreCase(found.IdEstado)) toUpdate.add(found); },
            () -> toInsert.add(crearDia(dia, agno, idPais, numero))
        );
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
    }

    public static class Salida {
        public List<Object> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    public static class DiaFeriado {
        public Long IdFeriado;
        public String Tipo;
        public Integer Dia;
        public Date Fecha;
        public Long IdPais;
        public String DescripcionDia;
        public String IdEstado;
        public Integer AgnoCarga;
        public Long Numero = 0L;
    }

    private static String loadSql(String path) {
        try (InputStream is = CargarDiasInhabilesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
