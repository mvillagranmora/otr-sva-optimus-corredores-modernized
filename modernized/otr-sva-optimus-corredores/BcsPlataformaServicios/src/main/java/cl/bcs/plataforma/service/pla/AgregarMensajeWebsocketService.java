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
public class AgregarMensajeWebsocketService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AgregarMensajeWebsocketService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_INSERT;

    public AgregarMensajeWebsocketService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_INSERT = loadSql("cl/bcs/plataforma/pla/sql/AgregarMensajeWebSocket_Insert.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.AgregarMensajeWebsocket"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::AgregarMensajeWebsocket");
        EntradaServicio in = GSON.fromJson(params, EntradaServicio.class);
        SalidaServicio out = new SalidaServicio();
        out.P_OUT_ERRORESCONTADOR = 0L;

        try {
            if (in.p_Mensajes != null && !in.p_Mensajes.isEmpty()) {
                SqlParameterSource[] sources = in.p_Mensajes.stream()
                    .map(m -> new MapSqlParameterSource()
                        .addValue("IdEmpresa", m.IdEmpresa)
                        .addValue("Evento", m.Evento)
                        .addValue("DATA", m.DATA))
                    .toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_INSERT, sources);
            }
        } catch (Exception e) {
            log.error("Error en AgregarMensajeWebsocket: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class EntradaServicio {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<MensajeWebSocket> p_Mensajes = new ArrayList<>();
    }

    public static class MensajeWebSocket {
        public Long IdEmpresa;
        public String Evento;
        public String DATA;
    }

    public static class SalidaServicio {
        public Long P_OUT_ERRORESCONTADOR;
    }

    private static String loadSql(String path) {
        try (InputStream is = AgregarMensajeWebsocketService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
