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
public class EliminarPerfilRolService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(EliminarPerfilRolService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_COUNT;
    private final String SQL_DELETE_ROLJPERFIL;

    public EliminarPerfilRolService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_COUNT = loadSql("cl/bcs/plataforma/acc/sql/EliminarPerfilRol_Select_Count.sql");
        this.SQL_DELETE_ROLJPERFIL = loadSql("cl/bcs/plataforma/acc/sql/EliminarPerfilRol_Delete_RolPerfil.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.EliminarPerfilRol"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::EliminarPerfilRol");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            MapSqlParameterSource checkParams = new MapSqlParameterSource("IdRolPerfil", in.p_IdRolPerfil);
            Long count = jdbc.queryForObject(SQL_SELECT_COUNT, checkParams, Long.class);

            if (count == null || count == 0) {
                out.P_OUT_ERRORESCONTADOR = 1;
                out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ROL_PERFIL_NO_ENCONTRADO",
                        "No se encontró la asignación rol-perfil: " + in.p_IdRolPerfil));
                out.P_FLAGROLPERFIL = 0L;
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            jdbc.update(SQL_DELETE_ROLJPERFIL, new MapSqlParameterSource("IdRolPerfil", in.p_IdRolPerfil));
            out.P_FLAGROLPERFIL = 1L;

        } catch (Exception e) {
            log.error("Error en EliminarPerfilRol: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1;
            out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_INTERNO", e.getMessage()));
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdRolPerfil;
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
        public Long P_FLAGROLPERFIL = 0L;
        public int P_OUT_ADVERTENCIASCONTADOR = 0;
        public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
        public int P_OUT_ERRORESCONTADOR = 0;
        public List<ErrorDetalle> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    private static String loadSql(String path) {
        try (InputStream is = EliminarPerfilRolService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
