package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ActualizarConfiguracionFlujoCustodiaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarConfiguracionFlujoCustodiaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_UPSERT;

    public ActualizarConfiguracionFlujoCustodiaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_UPSERT = loadSql("cl/bcs/plataforma/pla/sql/ActualizarConfiguracionFlujoCustodia_UpsertConfiguracionFlujoCustodia.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ActualizarConfiguracionFlujoCustodia"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ActualizarConfiguracionFlujoCustodia");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            if (!in.p_ConfiguracionFlujoCustodia.isEmpty()) {
                SqlParameterSource[] batch = in.p_ConfiguracionFlujoCustodia.stream()
                        .map(BeanPropertySqlParameterSource::new)
                        .toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_UPSERT, batch);
            }
        } catch (Exception e) {
            log.error("Error en ActualizarConfiguracionFlujoCustodia: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<ConfiguracionFlujoCustodia> p_ConfiguracionFlujoCustodia = new ArrayList<>();
    }

    public static class ConfiguracionFlujoCustodia {
        public Long IdUsoCustodia;
        public String Item;
        public String FuncionOperador;
        public BigDecimal Porcentaje;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Object> P_OUT_MENSAJES = new ArrayList<>();
    }

    private static String loadSql(String path) {
        try (InputStream is = ActualizarConfiguracionFlujoCustodiaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
