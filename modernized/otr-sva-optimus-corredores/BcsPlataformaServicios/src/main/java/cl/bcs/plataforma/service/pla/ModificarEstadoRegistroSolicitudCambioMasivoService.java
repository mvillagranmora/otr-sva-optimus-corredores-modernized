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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ModificarEstadoRegistroSolicitudCambioMasivoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ModificarEstadoRegistroSolicitudCambioMasivoService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_UPDATE_POR_SOLICITUD;
    private final String SQL_UPDATE_POR_ENTIDAD;

    public ModificarEstadoRegistroSolicitudCambioMasivoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_UPDATE_POR_SOLICITUD = loadSql("cl/bcs/plataforma/pla/sql/ModificarEstadoRegistroSolicitudCambioMasivo_Update_PorSolicitud.sql");
        this.SQL_UPDATE_POR_ENTIDAD = loadSql("cl/bcs/plataforma/pla/sql/ModificarEstadoRegistroSolicitudCambioMasivo_Update_PorEntidad.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ModificarEstadoRegistroSolicitudCambioMasivo");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            if (in.p_SolicitudCambio != null && !in.p_SolicitudCambio.isEmpty()) {
                List<UpdatePorSolicitud> items = new ArrayList<>();
                for (SolicitudCambio sc : in.p_SolicitudCambio) {
                    UpdatePorSolicitud upd = new UpdatePorSolicitud();
                    upd.IdSolicitudCambio = sc.IdSolicitudCambio;
                    upd.IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
                    items.add(upd);
                }
                SqlParameterSource[] batch = items.stream()
                        .map(BeanPropertySqlParameterSource::new)
                        .toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_UPDATE_POR_SOLICITUD, batch);

            } else if (in.p_EntidadOrigen != null && !in.p_EntidadOrigen.isEmpty()) {
                List<UpdatePorEntidad> items = new ArrayList<>();
                for (EntidadOrigen eo : in.p_EntidadOrigen) {
                    UpdatePorEntidad upd = new UpdatePorEntidad();
                    upd.IdEntidadOrigen = eo.IdEntidadOrigen;
                    upd.IdTipoEntidad = eo.IdTipoEntidad;
                    upd.IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
                    items.add(upd);
                }
                SqlParameterSource[] batch = items.stream()
                        .map(BeanPropertySqlParameterSource::new)
                        .toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_UPDATE_POR_ENTIDAD, batch);
            }

        } catch (Exception e) {
            log.error("Error en ModificarEstadoRegistroSolicitudCambioMasivo: {}", e.getMessage(), e);
            out.p_out_ErroresContador = 1L;
            out.p_out_ErroresDetalle.add(new ErrorDetalle("", e.getMessage()));
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<SolicitudCambio> p_SolicitudCambio = new ArrayList<>();
        public List<EntidadOrigen> p_EntidadOrigen = new ArrayList<>();
        public String p_IdEstadoAutorizacion;
    }

    public static class SolicitudCambio {
        public Long IdSolicitudCambio;
    }

    public static class EntidadOrigen {
        public String IdEntidadOrigen;
        public String IdTipoEntidad;
    }

    public static class UpdatePorSolicitud {
        public Long IdSolicitudCambio;
        public String IdEstadoAutorizacion;
    }

    public static class UpdatePorEntidad {
        public String IdEntidadOrigen;
        public String IdTipoEntidad;
        public String IdEstadoAutorizacion;
    }

    public static class Salida {
        public Long p_out_AdvertenciasContador = 0L;
        public List<Object> p_out_AdvertenciasDetalle = new ArrayList<>();
        public Long p_out_ErroresContador = 0L;
        public List<ErrorDetalle> p_out_ErroresDetalle = new ArrayList<>();
    }

    public static class ErrorDetalle {
        public String Codigo;
        public String Descripcion;
        public ErrorDetalle(String codigo, String descripcion) {
            this.Codigo = codigo;
            this.Descripcion = descripcion;
        }
    }

    private static String loadSql(String path) {
        try (InputStream is = ModificarEstadoRegistroSolicitudCambioMasivoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
