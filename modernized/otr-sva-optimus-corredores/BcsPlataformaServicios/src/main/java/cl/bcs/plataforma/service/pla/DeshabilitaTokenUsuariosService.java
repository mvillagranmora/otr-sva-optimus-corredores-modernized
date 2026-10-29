package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeshabilitaTokenUsuariosService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(DeshabilitaTokenUsuariosService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_MODIFICAR_TOKEN;

    public DeshabilitaTokenUsuariosService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_MODIFICAR_TOKEN = loadSql("cl/bcs/plataforma/pla/sql/ModificarTokenSesion_Update.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.DeshabilitaTokenUsuarios"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::DeshabilitaTokenUsuarios");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            jdbc.update(SQL_MODIFICAR_TOKEN, new MapSqlParameterSource("IdEmpresa", in.IdEmpresa));
            out.P_OUT_ERRORESCONTADOR = 0L;
        } catch (Exception e) {
            log.error("Error en DeshabilitaTokenUsuarios: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada { public Long IdEmpresa; }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 1L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
        public List<Object> P_OUT_ADVERTENCIADETALLE = new ArrayList<>();
        public List<Object> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    private static String loadSql(String path) {
        try (InputStream is = DeshabilitaTokenUsuariosService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
