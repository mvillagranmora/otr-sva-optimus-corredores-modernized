package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class ActualizarConfiguracionFlujoCajaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarConfiguracionFlujoCajaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT;
    private final String SQL_UPSERT;

    public ActualizarConfiguracionFlujoCajaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT = loadSql("cl/bcs/plataforma/pla/sql/ActualizarConfiguracionFlujoCaja_SelectConfiguracionFlujoCaja.sql");
        this.SQL_UPSERT = loadSql("cl/bcs/plataforma/pla/sql/ActualizarConfiguracionFlujoCaja_UpsertConfiguracionFlujoCaja.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ActualizarConfiguracionFlujoCaja"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ActualizarConfiguracionFlujoCaja");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            List<ConfiguracionFlujoCaja> existentes = jdbc.query(SQL_SELECT,
                    new MapSqlParameterSource("IdEmpresa", in.p_IdEmpresa),
                    BeanPropertyRowMapper.newInstance(ConfiguracionFlujoCaja.class));

            if (!in.p_ConfiguracionFlujoCaja.isEmpty()) {
                SqlParameterSource[] batch = in.p_ConfiguracionFlujoCaja.stream()
                        .map(BeanPropertySqlParameterSource::new)
                        .toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_UPSERT, batch);
            }
            out.P_OUT_ERRORESCONTADOR = 0L;
        } catch (Exception e) {
            log.error("Error en ActualizarConfiguracionFlujoCaja: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<ConfiguracionFlujoCaja> p_ConfiguracionFlujoCaja = new ArrayList<>();
    }

    public static class ConfiguracionFlujoCaja {
        public Long IdUsoFlujoCaja;
        public String Item;
        public String FuncionOperador;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Object> P_OUT_MENSAJES = new ArrayList<>();
    }

    private static String loadSql(String path) {
        try (InputStream is = ActualizarConfiguracionFlujoCajaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
