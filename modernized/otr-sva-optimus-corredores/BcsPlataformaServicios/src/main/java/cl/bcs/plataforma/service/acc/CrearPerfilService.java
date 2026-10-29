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

@Service
public class CrearPerfilService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearPerfilService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_COUNT_PERFILES;
    private final String SQL_INSERT_PERFIL;

    public CrearPerfilService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_COUNT_PERFILES = loadSql("cl/bcs/plataforma/acc/sql/CrearPerfil_Select_Count_Perfiles.sql");
        this.SQL_INSERT_PERFIL = loadSql("cl/bcs/plataforma/acc/sql/CrearPerfil_Insert_Perfil.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.CrearPerfil"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::CrearPerfil");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            MapSqlParameterSource checkParams = new MapSqlParameterSource()
                    .addValue("Nombre", in.p_Nombre)
                    .addValue("IdEmpresa", in.p_IdEmpresa);
            Long count = jdbc.queryForObject(SQL_SELECT_COUNT_PERFILES, checkParams, Long.class);

            if (count != null && count > 0) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("PERFIL_DUPLICADO",
                        "Ya existe un perfil con el nombre: " + in.p_Nombre));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            Long nextId = jdbc.queryForObject(
                    "SELECT \"BSCL.Plataforma.BD.Secuencias\".\"Perfil\".NEXTVAL FROM DUMMY",
                    new MapSqlParameterSource(), Long.class);

            MapSqlParameterSource insertParams = new MapSqlParameterSource()
                    .addValue("IdPerfil", nextId)
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("Nombre", in.p_Nombre)
                    .addValue("IdEstado", in.p_IdEstado != null ? in.p_IdEstado : "ACT");
            jdbc.update(SQL_INSERT_PERFIL, insertParams);
            out.P_OUT_NEW_PERFIL_ID = nextId;

        } catch (Exception e) {
            log.error("Error en CrearPerfil: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_INTERNO", e.getMessage()));
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_Nombre;
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
        public Long P_OUT_NEW_PERFIL_ID = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
        public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<ErrorDetalle> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    private static String loadSql(String path) {
        try (InputStream is = CrearPerfilService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
