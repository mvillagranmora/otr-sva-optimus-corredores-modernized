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
public class EliminarRolUsuarioService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(EliminarRolUsuarioService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_COUNT_ROL;
    private final String SQL_COUNT_ROL_USUARIO;
    private final String SQL_SELECT_ID_ROL_USUARIO;
    private final String SQL_DELETE_ROL_USUARIO;

    public EliminarRolUsuarioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_COUNT_ROL = loadSql("cl/bcs/plataforma/acc/sql/EliminarRolUsuario_Count_Rol.sql");
        this.SQL_COUNT_ROL_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/EliminarRolUsuario_Count_RolUsuario.sql");
        this.SQL_SELECT_ID_ROL_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/EliminarRolUsuario_Select_IdRolUsuario.sql");
        this.SQL_DELETE_ROL_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/EliminarRolUsuario_Delete.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.EliminarRolUsuario"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::EliminarRolUsuario");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            Long empresaRol = in.p_IdEmpresaRol != null ? in.p_IdEmpresaRol : in.p_IdEmpresa;

            // Validate rol exists
            Long countRol = jdbc.queryForObject(SQL_COUNT_ROL,
                    new MapSqlParameterSource()
                            .addValue("IdRol", in.p_IdRol)
                            .addValue("IdEmpresa", empresaRol),
                    Long.class);
            if (countRol == null || countRol == 0) {
                out.P_OUT_ERRORESCONTADOR = 1;
                out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ROL_NO_ENCONTRADO",
                        "Rol no encontrado: " + in.p_IdRol));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            // Validate assignment exists
            Long countExiste = jdbc.queryForObject(SQL_COUNT_ROL_USUARIO,
                    new MapSqlParameterSource()
                            .addValue("IdUsuario", in.p_IdUsuarioRol)
                            .addValue("IdRol", in.p_IdRol),
                    Long.class);
            if (countExiste == null || countExiste == 0) {
                out.P_OUT_ERRORESCONTADOR = 1;
                out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ROL_USUARIO_NO_ENCONTRADO",
                        "El usuario no tiene asignado este rol"));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            // Get IdRolUsuario to return
            List<IdRolUsuario> idRows = jdbc.query(SQL_SELECT_ID_ROL_USUARIO,
                    new MapSqlParameterSource()
                            .addValue("IdUsuario", in.p_IdUsuarioRol)
                            .addValue("IdRol", in.p_IdRol),
                    (rs, n) -> { IdRolUsuario r = new IdRolUsuario(); r.IdRolUsuario = rs.getLong(1); return r; });

            Long idRolUsuario = idRows.isEmpty() ? 0L : idRows.get(0).IdRolUsuario;

            jdbc.update(SQL_DELETE_ROL_USUARIO, new MapSqlParameterSource()
                    .addValue("IdUsuario", in.p_IdUsuarioRol)
                    .addValue("IdRol", in.p_IdRol));

            out.P_OUT_IDROLUSUARIO = idRolUsuario;

        } catch (Exception e) {
            log.error("Error en EliminarRolUsuario: {}", e.getMessage(), e);
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

    public static class IdRolUsuario {
        public Long IdRolUsuario;
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
        try (InputStream is = EliminarRolUsuarioService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
