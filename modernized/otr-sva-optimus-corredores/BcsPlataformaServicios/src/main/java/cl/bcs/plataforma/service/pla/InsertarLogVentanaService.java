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

@Service
public class InsertarLogVentanaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(InsertarLogVentanaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_INSERT;

    public InsertarLogVentanaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_INSERT = loadSql("cl/bcs/plataforma/pla/sql/InsertarLogVentana_InsertRegistroLogVentana.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.InsertarLogVentana"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::InsertarLogVentana");
        EntradaLogServicio in = GSON.fromJson(params, EntradaLogServicio.class);
        SalidaLogServicio out = new SalidaLogServicio();

        try {
            String titulo = in.p_TituloVentana;
            if (titulo != null && titulo.length() > 100) titulo = titulo.substring(0, 100);

            SqlParameterSource src = new MapSqlParameterSource()
                .addValue("p_IdVentana", in.p_IdVentana)
                .addValue("p_TituloVentana", titulo)
                .addValue("p_IdServicio", in.p_IdServicio)
                .addValue("p_IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_INSERT, src);
            out.P_OUT_ERRORESCONTADOR = 0L;
        } catch (Exception e) {
            log.error("Error en InsertarLogVentana: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class EntradaLogServicio {
        public String p_IdVentana;
        public String p_TituloVentana;
        public Long p_IdServicio;
        public Long p_IdUsuario;
    }

    public static class SalidaLogServicio {
        public Long P_OUT_ERRORESCONTADOR;
    }

    private static String loadSql(String path) {
        try (InputStream is = InsertarLogVentanaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
