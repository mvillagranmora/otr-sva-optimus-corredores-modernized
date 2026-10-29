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
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class AjustarFechaSistemaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AjustarFechaSistemaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_AJUSTAR;
    private final String SQL_MODIFICAR_TOKEN;

    public AjustarFechaSistemaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_AJUSTAR        = loadSql("cl/bcs/plataforma/pla/sql/AjustarFechaSistema_Update.sql");
        this.SQL_MODIFICAR_TOKEN = loadSql("cl/bcs/plataforma/pla/sql/ModificarTokenSesion_Update.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.AjustarFechaSistema"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::AjustarFechaSistema");
        EntradaServicio in = GSON.fromJson(params, EntradaServicio.class);
        ServicioSalida out = new ServicioSalida();

        try {
            if (in.p_FechaSistema != null && !in.p_FechaSistema.isEmpty()) {
                SqlParameterSource[] srcs = in.p_FechaSistema.stream()
                    .map(f -> new MapSqlParameterSource()
                        .addValue("FechaSistema", f.FechaSistema)
                        .addValue("IdEmpresa", f.IdEmpresa))
                    .toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_AJUSTAR, srcs);
                jdbc.batchUpdate(SQL_MODIFICAR_TOKEN, srcs);
            }
            out.P_OUT_ERRORESCONTADOR = 0L;
        } catch (Exception e) {
            log.error("Error en AjustarFechaSistema: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class EntradaServicio {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public List<EntradaFechaSistema> p_FechaSistema = new ArrayList<>();
    }

    public static class EntradaFechaSistema {
        public String FechaSistema;
        public Long IdEmpresa;
    }

    public static class ServicioSalida {
        public Long P_OUT_ERRORESCONTADOR = 1L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
        public List<Object> P_OUT_ADVERTENCIADETALLE = new ArrayList<>();
        public List<Object> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    private static String loadSql(String path) {
        try (InputStream is = AjustarFechaSistemaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
