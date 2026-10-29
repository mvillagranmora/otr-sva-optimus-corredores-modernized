package cl.bcs.plataforma.service.acc;

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
import java.util.Map;

@Service
public class AgregarRolUsuarioService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AgregarRolUsuarioService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_COUNT_ROL;
    private final String SQL_COUNT_ROL_USUARIO;
    private final String SQL_SELECT_NOMBRE_ROL;
    private final String SQL_INSERT_ROL_USUARIO;

    public AgregarRolUsuarioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_COUNT_ROL = loadSql("cl/bcs/plataforma/acc/sql/AgregarRolUsuario_Count_Rol.sql");
        this.SQL_COUNT_ROL_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/AgregarRolUsuario_Count_RolUsuario.sql");
        this.SQL_SELECT_NOMBRE_ROL = loadSql("cl/bcs/plataforma/acc/sql/AgregarRolUsuario_Select_NombreRol.sql");
        this.SQL_INSERT_ROL_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/AgregarRolUsuario_Insert.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.AgregarRolUsuario"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::AgregarRolUsuario");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            // Validate rol exists
            Long countRol = jdbc.queryForObject(SQL_COUNT_ROL,
                    new MapSqlParameterSource()
                            .addValue("IdRol", in.p_IdRol)
                            .addValue("IdEmpresa", in.p_IdEmpresaRol != null ? in.p_IdEmpresaRol : in.p_IdEmpresa),
                    Long.class);
            if (countRol == null || countRol == 0) {
                out.P_OUT_ERRORESCONTADOR = 1;
                out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ROL_NO_ENCONTRADO",
                        "Rol no encontrado: " + in.p_IdRol));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            // Validate not already assigned
            Long countExiste = jdbc.queryForObject(SQL_COUNT_ROL_USUARIO,
                    new MapSqlParameterSource()
                            .addValue("IdUsuarioRol", in.p_IdUsuarioRol)
                            .addValue("IdRol", in.p_IdRol),
                    Long.class);
            if (countExiste != null && countExiste > 0) {
                out.P_OUT_ERRORESCONTADOR = 1;
                out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ROL_USUARIO_DUPLICADO",
                        "El usuario ya tiene asignado este rol"));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            // Get next sequence
            Long nextId = jdbc.queryForObject(
                    "SELECT \"BSCL.Plataforma.BD.Secuencias\".\"RolUsuario\".NEXTVAL FROM DUMMY",
                    new MapSqlParameterSource(), Long.class);

            jdbc.update(SQL_INSERT_ROL_USUARIO, new MapSqlParameterSource()
                    .addValue("IdRolUsuario", nextId)
                    .addValue("IdUsuario", in.p_IdUsuarioRol)
                    .addValue("IdRol", in.p_IdRol)
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("FechaSistema", in.p_FechaSistema));

            out.P_OUT_IDROLUSUARIO = nextId;

        } catch (Exception e) {
            log.error("Error en AgregarRolUsuario: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1;
            out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_INTERNO", e.getMessage()));
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdUsuarioRol;
        public Long p_IdRol;
        public Long p_IdEmpresaRol;
        public String p_CodigoUsuario;
        public String p_FechaSistema;
    }

    public static class ErrorDetalle {
        public String Codigo;
        public String Descripcion;
        public ErrorDetalle(String codigo, String descripcion) { this.Codigo = codigo; this.Descripcion = descripcion; }
    }

    public static class AdvertenciaDetalle {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;
    }

    public static class Salida {
        public Long P_OUT_IDROLUSUARIO = 0L;
        public int P_OUT_ADVERTENCIASCONTADOR = 0;
        public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
        public int P_OUT_ERRORESCONTADOR = 0;
        public List<ErrorDetalle> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    private static String loadSql(String path) {
        try (InputStream is = AgregarRolUsuarioService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
