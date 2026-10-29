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

@Service
public class ActualizarRolService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarRolService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_ROL;
    private final String SQL_UPDATE_ROL;

    public ActualizarRolService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_ROL = loadSql("cl/bcs/plataforma/acc/sql/ActualizarRol_Select_Rol.sql");
        this.SQL_UPDATE_ROL = loadSql("cl/bcs/plataforma/acc/sql/ActualizarRol_Update_Rol.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.ActualizarRol"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ActualizarRol");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            List<RolRow> roles = jdbc.query(SQL_SELECT_ROL,
                    new MapSqlParameterSource("IdRol", in.p_IdRol),
                    BeanPropertyRowMapper.newInstance(RolRow.class));

            if (roles.isEmpty()) {
                out.P_OUT_ERRORESCONTADOR = 1;
                out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ROL_NO_ENCONTRADO", "Rol no encontrado: " + in.p_IdRol));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            MapSqlParameterSource updateParams = new MapSqlParameterSource()
                    .addValue("IdRol", in.p_IdRol)
                    .addValue("Nombre", in.p_Nombre)
                    .addValue("IdEstado", in.p_IdEstado)
                    .addValue("IdEmpresa", in.p_IdEmpresa);
            jdbc.update(SQL_UPDATE_ROL, updateParams);
            out.P_OUT_UPDATE = 1;

        } catch (Exception e) {
            log.error("Error en ActualizarRol: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1;
            out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_INTERNO", e.getMessage()));
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdRol;
        public String p_Nombre;
        public String p_IdEstado;
    }

    public static class RolRow {
        public Long IdRol;
        public String Nombre;
        public String IdEstado;
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
        public int P_OUT_UPDATE = 0;
        public int P_OUT_ADVERTENCIASCONTADOR = 0;
        public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
        public int P_OUT_ERRORESCONTADOR = 0;
        public List<ErrorDetalle> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    private static String loadSql(String path) {
        try (InputStream is = ActualizarRolService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
