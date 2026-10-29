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
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultarRegistroSolicitudCambioService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarRegistroSolicitudCambioService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT;

    public ConsultarRegistroSolicitudCambioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT = loadSql("cl/bcs/plataforma/pla/sql/ConsultarRegistroSolicitudCambio_Select.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambio"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ConsultarRegistroSolicitudCambio");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            out.P_OUT_SOLICITUDES_CAMBIO = jdbc.query(SQL_SELECT,
                new MapSqlParameterSource()
                    .addValue("IdEntidadOrigen", in.p_IdEntidadOrigen)
                    .addValue("IdTipoEntidad", in.p_IdTipoEntidad),
                BeanPropertyRowMapper.newInstance(SolicitudCambio.class));

            if (out.P_OUT_SOLICITUDES_CAMBIO.isEmpty()) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_MENSAJES.add(new Msg(1L, "E", "UI_ERROR_CONSULTA_SOLICITUD_CAMBIO", "No se encontraron solicitudes de cambio"));
            }
        } catch (Exception e) {
            log.error("Error en ConsultarRegistroSolicitudCambio: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdSolicitudCambio = 0L;
        public String p_IdEntidadOrigen = "";
        public String p_IdTipoEntidad = "";
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Msg> P_OUT_MENSAJES = new ArrayList<>();
        public List<SolicitudCambio> P_OUT_SOLICITUDES_CAMBIO = new ArrayList<>();
    }

    public static class SolicitudCambio {
        public Long IdSolicitudCambio;
        public String IdTipoEntidad;
        public String IdEntidadOrigen;
        public String CampoModificado;
        public String TipoDato;
        public String ValorAntiguo;
        public String ValorNuevo;
        public String IdEstadoAutorizacion;
    }

    public static class Msg {
        public Long idMensaje;
        public String tipoMensaje;
        public String codigo;
        public String descripcion;
        public Msg(Long id, String tipo, String cod, String desc) {
            this.idMensaje = id; this.tipoMensaje = tipo; this.codigo = cod; this.descripcion = desc;
        }
    }

    private static String loadSql(String path) {
        try (InputStream is = ConsultarRegistroSolicitudCambioService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
