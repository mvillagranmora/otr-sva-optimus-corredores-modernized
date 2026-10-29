package cl.bcs.plataforma.service.acc;

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
import java.util.Map;

@Service
public class ActualizarUsuarioService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarUsuarioService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_USUARIO;
    private final String SQL_UPDATE_USUARIO;
    private final String SQL_UPDATE_INDICADOR;
    private final String SQL_INSERT_HISTORIAL_CLAVE;
    private final String SQL_SELECT_COUNT_HEREDAR_ROLES;
    private final String SQL_DELETE_ROLES_USUARIO;
    private final String SQL_INSERT_ROLES_USUARIO;
    private final String SQL_UPDATE_MENU_USUARIO;

    public ActualizarUsuarioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Select_Usuario.sql");
        this.SQL_UPDATE_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Update_Usuario.sql");
        this.SQL_UPDATE_INDICADOR = loadSql("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Update_Indicador.sql");
        this.SQL_INSERT_HISTORIAL_CLAVE = loadSql("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Insert_Historial_Clave.sql");
        this.SQL_SELECT_COUNT_HEREDAR_ROLES = loadSql("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Select_Heredar_Roles.sql");
        this.SQL_DELETE_ROLES_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Delete_Roles_Usuario.sql");
        this.SQL_INSERT_ROLES_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Insert_Roles_Usuario.sql");
        this.SQL_UPDATE_MENU_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/ActualizarUsuario_Update_Menu_Usuario.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.ActualizarUsuario"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ActualizarUsuario");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            List<Map<String, Object>> usuarioRows = jdbc.queryForList(SQL_SELECT_USUARIO,
                    new MapSqlParameterSource()
                            .addValue("CodigoUsuario", in.p_CodigoUsuario)
                            .addValue("IdEmpresa", in.p_IdEmpresa));

            if (usuarioRows.isEmpty()) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "E", "USUARIO_NO_ENCONTRADO",
                        "Usuario no encontrado: " + in.p_CodigoUsuario));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            Map<String, Object> usuarioActual = usuarioRows.get(0);
            Long idUsuarioTarget = ((Number) usuarioActual.get("IdUsuario")).longValue();

            // Update user data
            MapSqlParameterSource updateParams = new MapSqlParameterSource()
                    .addValue("IdUsuario", idUsuarioTarget)
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("FechaVigencia", in.p_FechaVigencia)
                    .addValue("IdEstado", in.p_IdEstado)
                    .addValue("IdTipoBloqueo", in.p_IdTipoBloqueo)
                    .addValue("ComentarioBloqueo", in.p_ComentarioBloqueo)
                    .addValue("NombreTipoBloqueo", in.p_NombreTipoBloqueo)
                    .addValue("UsuarioCorredores", in.p_UsuarioCorredores)
                    .addValue("UsuarioNegocios", in.p_UsuarioNegocios)
                    .addValue("UsuarioClientes", in.p_UsuarioClientes)
                    .addValue("UsuarioServicios", in.p_UsuarioServicios);
            jdbc.update(SQL_UPDATE_USUARIO, updateParams);

            // Update password if provided
            if (in.p_Clave != null && !in.p_Clave.isEmpty()) {
                String newHash = CambiarClaveUsuarioService.sha256Double(in.p_Clave);
                jdbc.update(SQL_UPDATE_INDICADOR, new MapSqlParameterSource()
                        .addValue("IdUsuario", idUsuarioTarget)
                        .addValue("Clave", newHash)
                        .addValue("IdEmpresa", in.p_IdEmpresa));
                jdbc.update(SQL_INSERT_HISTORIAL_CLAVE, new MapSqlParameterSource()
                        .addValue("IdUsuario", idUsuarioTarget)
                        .addValue("Clave", newHash)
                        .addValue("IdEmpresa", in.p_IdEmpresa));
            }

            // Inherit roles if requested
            if (in.p_HeredarRolesUsuario != null && in.p_HeredarRolesUsuario > 0) {
                Long countHeredar = jdbc.queryForObject(SQL_SELECT_COUNT_HEREDAR_ROLES,
                        new MapSqlParameterSource("IdUsuario", in.p_HeredarRolesUsuario), Long.class);

                if (countHeredar != null && countHeredar > 0) {
                    jdbc.update(SQL_DELETE_ROLES_USUARIO, new MapSqlParameterSource("IdUsuario", idUsuarioTarget));
                    jdbc.update(SQL_INSERT_ROLES_USUARIO, new MapSqlParameterSource()
                            .addValue("IdUsuarioNuevo", idUsuarioTarget)
                            .addValue("IdUsuarioOrigen", in.p_HeredarRolesUsuario));
                    jdbc.update(SQL_UPDATE_MENU_USUARIO, new MapSqlParameterSource()
                            .addValue("IdUsuarioNuevo", idUsuarioTarget)
                            .addValue("IdUsuarioOrigen", in.p_HeredarRolesUsuario));
                }
            }

            out.P_OUT_NEW_USER_ID = idUsuarioTarget;
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "I", "USUARIO_ACTUALIZADO", "Usuario actualizado exitosamente"));

        } catch (Exception e) {
            log.error("Error en ActualizarUsuario: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "E", "ERROR_INTERNO", e.getMessage()));
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_CodigoUsuario;
        public String p_Clave;
        public String p_FechaVigencia;
        public String p_IdEstado;
        public Long p_HeredarRolesUsuario;
        public String p_IdTipoBloqueo;
        public String p_ComentarioBloqueo;
        public String p_NombreTipoBloqueo;
        public String p_UsuarioCorredores = "N";
        public String p_UsuarioNegocios = "N";
        public String p_UsuarioClientes = "N";
        public String p_UsuarioServicios = "N";
    }

    public static class MensajeSalida {
        public Long id;
        public String tipo;
        public String codigo;
        public String descripcion;
        public MensajeSalida(Long id, String tipo, String codigo, String desc) {
            this.id = id; this.tipo = tipo; this.codigo = codigo; this.descripcion = desc;
        }
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_NEW_USER_ID = 0L;
    }

    private static String loadSql(String path) {
        try (InputStream is = ActualizarUsuarioService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
