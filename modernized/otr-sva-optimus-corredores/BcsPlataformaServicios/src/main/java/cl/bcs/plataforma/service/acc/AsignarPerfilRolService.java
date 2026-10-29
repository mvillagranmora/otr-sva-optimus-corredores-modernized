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
public class AsignarPerfilRolService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AsignarPerfilRolService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_COUNT;
    private final String SQL_INSERT_ROLJPERFIL;

    public AsignarPerfilRolService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_COUNT = loadSql("cl/bcs/plataforma/acc/sql/AsignarPerfilRol_Select_Count.sql");
        this.SQL_INSERT_ROLJPERFIL = loadSql("cl/bcs/plataforma/acc/sql/AsignarPerfilRol_Insert_RolPerfil.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.AsignarPerfilRol"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::AsignarPerfilRol");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            MapSqlParameterSource checkParams = new MapSqlParameterSource()
                    .addValue("IdPerfil", in.p_IdPerfil)
                    .addValue("IdRol", in.p_IdRol);
            Long count = jdbc.queryForObject(SQL_SELECT_COUNT, checkParams, Long.class);

            if (count != null && count > 0) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ROL_PERFIL_DUPLICADO",
                        "El rol ya está asignado a este perfil"));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            // Get next IdRolPerfil from sequence
            Long nextId = jdbc.queryForObject(
                    "SELECT \"BSCL.Plataforma.BD.Secuencias\".\"RolPerfil\".NEXTVAL FROM DUMMY",
                    new MapSqlParameterSource(), Long.class);

            MapSqlParameterSource insertParams = new MapSqlParameterSource()
                    .addValue("IdRolPerfil", nextId)
                    .addValue("IdRol", in.p_IdRol)
                    .addValue("IdPerfil", in.p_IdPerfil)
                    .addValue("IdEstado", in.p_IdEstado != null ? in.p_IdEstado : "ACT");
            jdbc.update(SQL_INSERT_ROLJPERFIL, insertParams);
            out.P_IDROLFPERFIL = nextId;

        } catch (Exception e) {
            log.error("Error en AsignarPerfilRol: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_INTERNO", e.getMessage()));
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdPerfil;
        public Long p_IdRol;
        public String p_IdEstado;
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
        public Long P_IDROLFPERFIL = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
        public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<ErrorDetalle> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    private static String loadSql(String path) {
        try (InputStream is = AsignarPerfilRolService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
