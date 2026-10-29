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
public class CrearUsuarioService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearUsuarioService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_SOCIONEGOCIO;
    private final String SQL_SELECT_USUARIO;
    private final String SQL_CONSULTA_EXISTE_USUARIO;
    private final String SQL_INSERT_USUARIO;
    private final String SQL_SELECT_ROL_USUARIO;
    private final String SQL_SELECT_HEREDAR_ROL_USUARIO;
    private final String SQL_INSERT_ROLES_HEREDADOS;
    private final String SQL_UPDATE_MENU_HEREDADOS;

    public CrearUsuarioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_SOCIONEGOCIO = loadSql("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_SocioNegocio.sql");
        this.SQL_SELECT_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_Usuario.sql");
        this.SQL_CONSULTA_EXISTE_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/Consulta_Existe_Usuario.sql");
        this.SQL_INSERT_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/CrearUsuario_Insert_Usuario.sql");
        this.SQL_SELECT_ROL_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_RolUsuario.sql");
        this.SQL_SELECT_HEREDAR_ROL_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/CrearUsuario_Select_HeredarRolUsuario.sql");
        this.SQL_INSERT_ROLES_HEREDADOS = loadSql("cl/bcs/plataforma/acc/sql/CrearUsuario_Insert_RolesHeredados.sql");
        this.SQL_UPDATE_MENU_HEREDADOS = loadSql("cl/bcs/plataforma/acc/sql/CrearUsuario_Update_MenuUsuario.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.CrearUsuario"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::CrearUsuario");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            // Validate socionegocio/entidad
            List<Map<String, Object>> socioRows = jdbc.queryForList(SQL_SELECT_SOCIONEGOCIO,
                    new MapSqlParameterSource()
                            .addValue("TipoEntidad", in.p_TipoEntidad)
                            .addValue("IdEntidad", in.p_IdEntidad)
                            .addValue("IdEmpresa", in.p_IdEmpresa));

            if (socioRows.isEmpty()) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "E", "ENTIDAD_NO_ENCONTRADA",
                        "No se encontró la entidad: " + in.p_TipoEntidad + "/" + in.p_IdEntidad));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            // Check if CodigoUsuario already exists
            Long countExiste = jdbc.queryForObject(SQL_CONSULTA_EXISTE_USUARIO,
                    new MapSqlParameterSource()
                            .addValue("CodigoUsuario", in.p_CodigoUsuario)
                            .addValue("IdEmpresa", in.p_IdEmpresa),
                    Long.class);
            if (countExiste != null && countExiste > 0) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "E", "USUARIO_DUPLICADO",
                        "Ya existe un usuario con código: " + in.p_CodigoUsuario));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            // Get next IdUsuario
            Long nextId = jdbc.queryForObject(
                    "SELECT \"BSCL.Plataforma.BD.Secuencias\".\"Usuario\".NEXTVAL FROM DUMMY",
                    new MapSqlParameterSource(), Long.class);

            // Hash password
            String hashedClave = in.p_Clave != null && !in.p_Clave.isEmpty()
                    ? CambiarClaveUsuarioService.sha256Double(in.p_Clave)
                    : CambiarClaveUsuarioService.sha256Double("password");

            // Insert user
            MapSqlParameterSource insertParams = new MapSqlParameterSource()
                    .addValue("IdUsuario", nextId)
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("CodigoUsuario", in.p_CodigoUsuario)
                    .addValue("Clave", hashedClave)
                    .addValue("FechaIngreso", in.p_FechaIngreso)
                    .addValue("FechaVigencia", in.p_FechaVigencia)
                    .addValue("Conectado", in.p_Conectado != null ? in.p_Conectado : "N")
                    .addValue("Terminal", in.p_Terminal)
                    .addValue("IdEstado", in.p_IdEstado != null ? in.p_IdEstado : "ACT")
                    .addValue("IntentosConexion", in.p_IntentosConexion != null ? in.p_IntentosConexion : 0)
                    .addValue("TipoEntidad", in.p_TipoEntidad)
                    .addValue("IdEntidad", in.p_IdEntidad)
                    .addValue("Canal", in.p_Canal != null ? in.p_Canal : "CORREDORES")
                    .addValue("UsuarioCorredores", in.p_UsuarioCorredores != null ? in.p_UsuarioCorredores : "N")
                    .addValue("UsuarioNegocios", in.p_UsuarioNegocios != null ? in.p_UsuarioNegocios : "N")
                    .addValue("UsuarioClientes", in.p_UsuarioClientes != null ? in.p_UsuarioClientes : "N")
                    .addValue("UsuarioServicios", in.p_UsuarioServicios != null ? in.p_UsuarioServicios : "N")
                    .addValue("EsUsuarioBolsa", in.p_EsUsuarioBolsa != null ? in.p_EsUsuarioBolsa : "N");
            jdbc.update(SQL_INSERT_USUARIO, insertParams);

            // Inherit roles if requested
            if (in.p_HeredarRolesUsuario != null && in.p_HeredarRolesUsuario > 0) {
                List<Map<String, Object>> rolesHeredar = jdbc.queryForList(SQL_SELECT_HEREDAR_ROL_USUARIO,
                        new MapSqlParameterSource("IdUsuario", in.p_HeredarRolesUsuario));
                if (!rolesHeredar.isEmpty()) {
                    for (Map<String, Object> rol : rolesHeredar) {
                        Long nextRolId = jdbc.queryForObject(
                                "SELECT \"BSCL.Plataforma.BD.Secuencias\".\"RolUsuario\".NEXTVAL FROM DUMMY",
                                new MapSqlParameterSource(), Long.class);
                        jdbc.update(SQL_INSERT_ROLES_HEREDADOS, new MapSqlParameterSource()
                                .addValue("IdRolUsuario", nextRolId)
                                .addValue("IdUsuarioNuevo", nextId)
                                .addValue("IdRol", rol.get("IdRol"))
                                .addValue("IdEmpresa", in.p_IdEmpresa));
                    }
                    jdbc.update(SQL_UPDATE_MENU_HEREDADOS, new MapSqlParameterSource()
                            .addValue("IdUsuarioNuevo", nextId)
                            .addValue("IdUsuarioOrigen", in.p_HeredarRolesUsuario));
                }
            }

            out.P_OUT_NEW_USER_ID = nextId;
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "I", "USUARIO_CREADO", "Usuario creado exitosamente"));

        } catch (Exception e) {
            log.error("Error en CrearUsuario: {}", e.getMessage(), e);
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
        public String p_FechaIngreso;
        public String p_FechaVigencia;
        public String p_FechaUltimaConexion;
        public String p_Conectado;
        public String p_Hora;
        public String p_Terminal;
        public String p_IdEstado;
        public Integer p_IntentosConexion;
        public String p_TipoEntidad;
        public Long p_IdEntidad;
        public Long p_HeredarRolesUsuario;
        public String p_Canal = "CORREDORES";
        public String p_UsuarioCorredores = "N";
        public String p_UsuarioNegocios = "N";
        public String p_UsuarioClientes = "N";
        public String p_UsuarioServicios = "N";
        public String p_EsUsuarioBolsa = "N";
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
        try (InputStream is = CrearUsuarioService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
