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
public class ActualizarPerfilAccesoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarPerfilAccesoService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_ESTADO_ANTIGUO;
    private final String SQL_UPDATE_PERFIL;
    private final String SQL_SELECT_COUNT_ACCESO_PERFIL;
    private final String SQL_SELECT_ID_ACCESO_PERFIL;
    private final String SQL_DELETE_PERFIL_ACCESO;
    private final String SQL_UPDATE_PERFIL_ACCESO;
    private final String SQL_SELECT_COUNT_ACCESO_REPETIDO;
    private final String SQL_INSERT_PERFIL_ACCESO;

    public ActualizarPerfilAccesoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_ESTADO_ANTIGUO = loadSql("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Select_EstadoAntiguo.sql");
        this.SQL_UPDATE_PERFIL = loadSql("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Update_Perfil.sql");
        this.SQL_SELECT_COUNT_ACCESO_PERFIL = loadSql("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Select_Count_AccesoPerfil.sql");
        this.SQL_SELECT_ID_ACCESO_PERFIL = loadSql("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Select_IdAccesoPerfil.sql");
        this.SQL_DELETE_PERFIL_ACCESO = loadSql("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Delete_PerfilAcceso.sql");
        this.SQL_UPDATE_PERFIL_ACCESO = loadSql("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Update_PerfilAcceso.sql");
        this.SQL_SELECT_COUNT_ACCESO_REPETIDO = loadSql("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Select_Count_AccesoRepetido.sql");
        this.SQL_INSERT_PERFIL_ACCESO = loadSql("cl/bcs/plataforma/acc/sql/ActualizarPerfilAcceso_Insert_PerfilAcceso.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.ActualizarPerfilAcceso"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ActualizarPerfilAcceso");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            // Get current perfil state
            List<Map<String, Object>> estadoAntiguo = jdbc.queryForList(SQL_SELECT_ESTADO_ANTIGUO,
                    new MapSqlParameterSource("IdPerfil", in.p_IdPerfil));

            // Update perfil name/state
            jdbc.update(SQL_UPDATE_PERFIL, new MapSqlParameterSource()
                    .addValue("IdPerfil", in.p_IdPerfil)
                    .addValue("Nombre", in.p_Nombre)
                    .addValue("IdEstado", in.p_IdEstado)
                    .addValue("IdEmpresa", in.p_IdEmpresa));

            if (in.p_Accesos != null) {
                for (AccesoItem acceso : in.p_Accesos) {
                    Long idAcceso = acceso.p_IdAcceso;
                    String existe = acceso.p_Existe;

                    Long countExiste = jdbc.queryForObject(SQL_SELECT_COUNT_ACCESO_PERFIL,
                            new MapSqlParameterSource()
                                    .addValue("IdPerfil", in.p_IdPerfil)
                                    .addValue("IdAcceso", idAcceso),
                            Long.class);

                    if ("N".equalsIgnoreCase(existe)) {
                        // Should not exist: delete if found
                        if (countExiste != null && countExiste > 0) {
                            jdbc.update(SQL_DELETE_PERFIL_ACCESO, new MapSqlParameterSource()
                                    .addValue("IdPerfil", in.p_IdPerfil)
                                    .addValue("IdAcceso", idAcceso));
                        }
                    } else {
                        // Should exist: insert or update
                        if (countExiste != null && countExiste > 0) {
                            jdbc.update(SQL_UPDATE_PERFIL_ACCESO, new MapSqlParameterSource()
                                    .addValue("IdPerfil", in.p_IdPerfil)
                                    .addValue("IdAcceso", idAcceso)
                                    .addValue("IdEstado", in.p_IdEstado != null ? in.p_IdEstado : "ACT"));
                        } else {
                            // Check for duplicates across other perfiles
                            Long countRepetido = jdbc.queryForObject(SQL_SELECT_COUNT_ACCESO_REPETIDO,
                                    new MapSqlParameterSource()
                                            .addValue("IdPerfil", in.p_IdPerfil)
                                            .addValue("IdAcceso", idAcceso),
                                    Long.class);

                            Long nextId = jdbc.queryForObject(
                                    "SELECT \"BSCL.Plataforma.BD.Secuencias\".\"AccesoPerfil\".NEXTVAL FROM DUMMY",
                                    new MapSqlParameterSource(), Long.class);

                            jdbc.update(SQL_INSERT_PERFIL_ACCESO, new MapSqlParameterSource()
                                    .addValue("IdAccesoPerfil", nextId)
                                    .addValue("IdPerfil", in.p_IdPerfil)
                                    .addValue("IdAcceso", idAcceso)
                                    .addValue("IdEstado", in.p_IdEstado != null ? in.p_IdEstado : "ACT")
                                    .addValue("IdEmpresa", in.p_IdEmpresa));
                        }
                    }
                }
            }

            out.P_OUT_UPDATE = "OK";

        } catch (Exception e) {
            log.error("Error en ActualizarPerfilAcceso: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_INTERNO", e.getMessage()));
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class AccesoItem {
        public Long p_IdAcceso;
        public String p_Existe;
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_Nombre;
        public String p_IdEstado;
        public Long p_IdPerfil;
        public List<AccesoItem> p_Accesos;
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
        public String P_OUT_UPDATE = "";
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
        public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<ErrorDetalle> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    private static String loadSql(String path) {
        try (InputStream is = ActualizarPerfilAccesoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
