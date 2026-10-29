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
import java.util.stream.Collectors;

@Service
public class ConsultarRegistroSolicitudCambioMasivoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarRegistroSolicitudCambioMasivoService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_INSERT_TEMP;
    private final String SQL_SELECT;
    private final String SQL_DELETE_TEMP;

    public ConsultarRegistroSolicitudCambioMasivoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_INSERT_TEMP = loadSql("cl/bcs/plataforma/pla/sql/ConsultarRegistroSolicitudCambioMasivo_InsertConsultaSolicitudCambio.sql");
        this.SQL_SELECT = loadSql("cl/bcs/plataforma/pla/sql/ConsultarRegistroSolicitudCambioMasivo_SelectSolicitudCambio.sql");
        this.SQL_DELETE_TEMP = loadSql("cl/bcs/plataforma/pla/sql/ConsultarRegistroSolicitudCambioMasivo_DeleteConsultaSolicitudCambio.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambioMasivo"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ConsultarRegistroSolicitudCambioMasivo");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        long idTrabajo = (long)(Math.random() * 10_000_000_000L);

        try {
            // INSERT temp rows
            List<SolicitudCambioConsulta> consultas = in.p_SolicitudesCambio.stream().map(s -> {
                SolicitudCambioConsulta c = new SolicitudCambioConsulta();
                c.IdTrabajo = idTrabajo;
                c.IdSolicitudCambio = s.IdSolicitudCambio;
                c.IdEntidadOrigen = s.IdEntidadOrigen;
                c.IdTipoEntidad = s.IdTipoEntidad;
                return c;
            }).collect(Collectors.toList());

            SqlParameterSource[] batch = consultas.stream()
                    .map(BeanPropertySqlParameterSource::new)
                    .toArray(SqlParameterSource[]::new);
            jdbc.batchUpdate(SQL_INSERT_TEMP, batch);

            // SELECT
            List<SolicitudCambio> resultados = jdbc.query(SQL_SELECT,
                    new MapSqlParameterSource("IdTrabajo", idTrabajo),
                    BeanPropertyRowMapper.newInstance(SolicitudCambio.class));
            out.P_OUT_SOLICITUDES_CAMBIO = resultados;

            // Validate — add error messages for entries not found
            for (EntradaSolicitudCambio esc : in.p_SolicitudesCambio) {
                Long idEnt = Long.valueOf(esc.IdEntidadOrigen);
                boolean found = resultados.stream()
                        .anyMatch(r -> idEnt.equals(r.IdEntidadOrigenLong));
                if (!found) {
                    Msg err = new Msg();
                    err.id = idEnt;
                    err.tipo = "E";
                    err.codigo = "UI_ERROR_CONSULTA_SOLICITUD_CAMBIO";
                    out.P_OUT_MENSAJES.add(err);
                    out.P_OUT_ERRORESCONTADOR++;
                }
            }

        } catch (Exception e) {
            log.error("Error en ConsultarRegistroSolicitudCambioMasivo: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR++;
        } finally {
            try {
                SolicitudCambioConsulta del = new SolicitudCambioConsulta();
                del.IdTrabajo = idTrabajo;
                jdbc.batchUpdate(SQL_DELETE_TEMP, new SqlParameterSource[]{
                        new BeanPropertySqlParameterSource(del)});
            } catch (Exception ex) {
                log.warn("Cleanup temp failed: {}", ex.getMessage());
            }
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public List<EntradaSolicitudCambio> p_SolicitudesCambio = new ArrayList<>();
    }

    public static class EntradaSolicitudCambio {
        public Long IdSolicitudCambio;
        public String IdEntidadOrigen;
        public String IdTipoEntidad;
    }

    public static class SolicitudCambioConsulta {
        public Long IdTrabajo;
        public Long IdSolicitudCambio;
        public String IdEntidadOrigen;
        public String IdTipoEntidad;
    }

    public static class SolicitudCambio {
        public Long IdSolicitudCambio;
        public String CampoModificado;
        public String TipoDato;
        public String ValorAntiguo;
        public String ValorNuevo;
        public Long IdEntidadOrigenLong;
        public String IdTipoEntidad;
        public String IdEstadoAutorizacion;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Msg> P_OUT_MENSAJES = new ArrayList<>();
        public List<SolicitudCambio> P_OUT_SOLICITUDES_CAMBIO = new ArrayList<>();
    }

    public static class Msg {
        public Long id;
        public String tipo;
        public String codigo;
    }

    private static String loadSql(String path) {
        try (InputStream is = ConsultarRegistroSolicitudCambioMasivoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
