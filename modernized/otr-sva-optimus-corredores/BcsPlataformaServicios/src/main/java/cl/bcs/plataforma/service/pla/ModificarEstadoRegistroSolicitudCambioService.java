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
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ModificarEstadoRegistroSolicitudCambioService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ModificarEstadoRegistroSolicitudCambioService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_UPDATE_POR_ID;
    private final String SQL_UPDATE_POR_ENTIDAD;
    private final String SQL_SELECT_POR_ID;
    private final String SQL_SELECT_POR_ENTIDAD;

    public ModificarEstadoRegistroSolicitudCambioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_UPDATE_POR_ID      = loadSql("cl/bcs/plataforma/pla/sql/ModificarEstadoRegistroSolicitudCambio_UpdateSolicitudCambioPorId.sql");
        this.SQL_UPDATE_POR_ENTIDAD = loadSql("cl/bcs/plataforma/pla/sql/ModificarEstadoRegistroSolicitudCambio_UpdateSolicitudCambioPorEntidad.sql");
        this.SQL_SELECT_POR_ID      = loadSql("cl/bcs/plataforma/pla/sql/ModificarEstadoRegistroSolicitudCambio_SelectSolicitudCambioPorIdSolicitudCambio.sql");
        this.SQL_SELECT_POR_ENTIDAD = loadSql("cl/bcs/plataforma/pla/sql/ModificarEstadoRegistroSolicitudCambio_SelectSolicitudCambioPorIdEntidadOrigen.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ModificarEstadoRegistroSolicitudCambio");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            if (in.p_IdSolicitudCambio != null && in.p_IdSolicitudCambio > 0) {
                jdbc.update(SQL_UPDATE_POR_ID, new MapSqlParameterSource()
                    .addValue("IdSolicitudCambio", in.p_IdSolicitudCambio)
                    .addValue("IdEstadoAutorizacion", in.p_IdEstadoAutorizacion));

                List<Object> result = jdbc.query(SQL_SELECT_POR_ID, new MapSqlParameterSource()
                    .addValue("IdSolicitudCambio", in.p_IdSolicitudCambio)
                    .addValue("IdEstadoAutorizacion", in.p_IdEstadoAutorizacion),
                    BeanPropertyRowMapper.newInstance(Object.class));

                if (result.isEmpty()) {
                    out.P_OUT_ERRORESCONTADOR = 1L;
                    out.P_OUT_MENSAJES.add(new Msg(0L, "E", "ERROR_SOLICITUD_CAMBIO", "Error actualizando solicitud de cambio por Id"));
                }
            } else {
                jdbc.update(SQL_UPDATE_POR_ENTIDAD, new MapSqlParameterSource()
                    .addValue("IdEntidadOrigen", in.p_IdEntidadOrigen)
                    .addValue("IdTipoEntidad", in.p_IdTipoEntidad)
                    .addValue("IdEstadoAutorizacion", in.p_IdEstadoAutorizacion));

                List<Object> result = jdbc.query(SQL_SELECT_POR_ENTIDAD, new MapSqlParameterSource()
                    .addValue("IdEntidadOrigen", in.p_IdEntidadOrigen)
                    .addValue("IdEstadoAutorizacion", in.p_IdEstadoAutorizacion)
                    .addValue("IdTipoEntidad", in.p_IdTipoEntidad),
                    BeanPropertyRowMapper.newInstance(Object.class));

                if (result.isEmpty()) {
                    out.P_OUT_ERRORESCONTADOR = 1L;
                    out.P_OUT_MENSAJES.add(new Msg(0L, "E", "ERROR_SOLICITUD_CAMBIO", "Error actualizando solicitud de cambio por entidad"));
                }
            }
        } catch (Exception e) {
            log.error("Error en ModificarEstadoRegistroSolicitudCambio: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdSolicitudCambio = 0L;
        public String p_IdEntidadOrigen = "";
        public String p_IdTipoEntidad = "";
        public String p_IdEstadoAutorizacion = "";
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Msg> P_OUT_MENSAJES = new ArrayList<>();
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
        try (InputStream is = ModificarEstadoRegistroSolicitudCambioService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
