package cl.bcs.plataforma.service.acc;

import cl.bcs.plataforma.service.JavaCallService;
import cl.bcs.plataforma.service.utils.ConfiguracionSistemaUtil;
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
import java.util.Map;

@Service
public class BloqueoUsuariosPorVigenciaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(BloqueoUsuariosPorVigenciaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil configUtil;
    private final String SQL_SELECT_USUARIO_A_BLOQUEAR;
    private final String SQL_UPDATE_BLOQUEO;
    private final String SQL_SELECT_PARAM_TIPO_BLOQUEO;

    public BloqueoUsuariosPorVigenciaService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistemaUtil configUtil) {
        this.jdbc = jdbc;
        this.configUtil = configUtil;
        this.SQL_SELECT_USUARIO_A_BLOQUEAR = loadSql("cl/bcs/plataforma/acc/sql/BloqueoUsuarioPorVigencia_Select_Usuario.sql");
        this.SQL_UPDATE_BLOQUEO = loadSql("cl/bcs/plataforma/acc/sql/BloqueoUsuariosPorVigencia_Update_Usuarios.sql");
        this.SQL_SELECT_PARAM_TIPO_BLOQUEO = loadSql("cl/bcs/plataforma/acc/sql/BloqueoUsuarioPorVigencia_Select_Param_Tipo_Bloqueo.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.BloqueoUsuariosPorVigencia"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::BloqueoUsuariosPorVigencia");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            String fechaSistema = configUtil.getParametroString(in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

            List<Map<String, Object>> usuarios = jdbc.queryForList(SQL_SELECT_USUARIO_A_BLOQUEAR,
                    new MapSqlParameterSource("FechaSistema", fechaSistema));

            if (usuarios.isEmpty()) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "I", "NO_HAY_USUARIO_POR_ACTUALIZAR",
                        "No hay usuarios a bloquear por vigencia"));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            List<Map<String, Object>> tipoBloqueo = jdbc.queryForList(SQL_SELECT_PARAM_TIPO_BLOQUEO,
                    new MapSqlParameterSource("IdTipoBloqueo", "BVIG"));

            String idTipoBloqueo = "BVIG";
            String nombreTipoBloqueo = "";
            String comentarioBloqueo = "";
            if (!tipoBloqueo.isEmpty()) {
                Map<String, Object> tb = tipoBloqueo.get(0);
                idTipoBloqueo = tb.get("IdTipoBloqueo") != null ? tb.get("IdTipoBloqueo").toString() : "BVIG";
                nombreTipoBloqueo = tb.get("NombreTipoBloqueo") != null ? tb.get("NombreTipoBloqueo").toString() : "";
                comentarioBloqueo = tb.get("ComentarioPorDefecto") != null ? tb.get("ComentarioPorDefecto").toString() : "";
            }

            final String ftIdTipoBloqueo = idTipoBloqueo;
            final String ftNombre = nombreTipoBloqueo;
            final String ftComentario = comentarioBloqueo;

            SqlParameterSource[] batchParams = usuarios.stream().map(u -> {
                Long idUsuario = u.get("IdUsuario") != null ? ((Number) u.get("IdUsuario")).longValue() : null;
                return new MapSqlParameterSource()
                        .addValue("IdUsuario", idUsuario)
                        .addValue("IdEmpresa", in.p_IdEmpresa)
                        .addValue("IdEstado", "BLO")
                        .addValue("IdTipoBloqueo", ftIdTipoBloqueo)
                        .addValue("NombreTipoBloqueo", ftNombre)
                        .addValue("ComentarioBloqueo", ftComentario)
                        .addValue("FechaSistema", fechaSistema);
            }).toArray(SqlParameterSource[]::new);

            jdbc.batchUpdate(SQL_UPDATE_BLOQUEO, batchParams);
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "I", "USUARIOS_BLOQUEADOS",
                    "Se bloquearon " + usuarios.size() + " usuarios por vigencia"));

        } catch (Exception e) {
            log.error("Error en BloqueoUsuariosPorVigencia: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
    }

    public static class MensajeSalida {
        public Long idMensaje;
        public String tipoMensaje;
        public String codigoMensaje;
        public String descripcionMensaje;
        public MensajeSalida(Long id, String tipo, String codigo, String desc) {
            this.idMensaje = id; this.tipoMensaje = tipo;
            this.codigoMensaje = codigo; this.descripcionMensaje = desc;
        }
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    private static String loadSql(String path) {
        try (InputStream is = BloqueoUsuariosPorVigenciaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
