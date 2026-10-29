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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CambiarClaveUsuarioService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CambiarClaveUsuarioService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_CLAVE_USUARIO;
    private final String SQL_LOG_CLAVE_USUARIO;
    private final String SQL_UPD_CLAVE_USUARIO;
    private final String SQL_INS_CLAVE_USUARIO;

    public CambiarClaveUsuarioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_CLAVE_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/ConsultaClaveUsuario.sql");
        this.SQL_LOG_CLAVE_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/ConsultaLogClaveUsuario.sql");
        this.SQL_UPD_CLAVE_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/UpdateLogClaveUsuario.sql");
        this.SQL_INS_CLAVE_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/AgregarClaveUsuarioLog.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.CambiarClaveUsuario"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::CambiarClaveUsuario");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            Long idUsuarioTarget = in.p_IdUsuarioModificar != null ? in.p_IdUsuarioModificar : in.p_IdUsuario;

            List<ClaveUsuario> resultClave = jdbc.query(SQL_CLAVE_USUARIO,
                    new MapSqlParameterSource("p_IdUsuario", idUsuarioTarget),
                    BeanPropertyRowMapper.newInstance(ClaveUsuario.class));

            if (resultClave.isEmpty()) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, "E", "USUARIO_NO_ENCONTRADO",
                        "Usuario no encontrado: " + idUsuarioTarget));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            // Only validate current password when user is changing their own password
            boolean esSuPropiaClave = in.p_IdUsuarioModificar == null ||
                    in.p_IdUsuarioModificar.equals(in.p_IdUsuario);

            if (esSuPropiaClave && in.p_Clave != null && !in.p_Clave.isEmpty()) {
                String hashActual = sha256Double(in.p_Clave);
                boolean coincide = resultClave.get(0).Clave != null &&
                        resultClave.get(0).Clave.equalsIgnoreCase(hashActual);
                if (!coincide) {
                    out.P_OUT_ERRORESCONTADOR = 1L;
                    out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, "E", "CLAVE_NO_COINCIDE",
                            "La clave actual no coincide"));
                    return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
                }
            }

            // Check password history
            List<LogClaveUsuario> logClave = jdbc.query(SQL_LOG_CLAVE_USUARIO,
                    new MapSqlParameterSource("p_IdUsuario", idUsuarioTarget),
                    BeanPropertyRowMapper.newInstance(LogClaveUsuario.class));

            String newHashClave = sha256Double(in.p_ClaveNueva);

            // Update password
            MapSqlParameterSource updParams = new MapSqlParameterSource()
                    .addValue("CodigoUsuario", resultClave.get(0).CodigoUsuario)
                    .addValue("IdUsuario", idUsuarioTarget)
                    .addValue("Clave", newHashClave)
                    .addValue("IdEmpresa", in.p_IdEmpresa);
            jdbc.update(SQL_UPD_CLAVE_USUARIO, updParams);

            // Insert password history
            MapSqlParameterSource insParams = new MapSqlParameterSource()
                    .addValue("CodigoUsuario", resultClave.get(0).CodigoUsuario)
                    .addValue("IdUsuario", idUsuarioTarget)
                    .addValue("Clave", newHashClave)
                    .addValue("IdEmpresa", in.p_IdEmpresa);
            jdbc.update(SQL_INS_CLAVE_USUARIO, insParams);

            out.P_OUT_UPDATE = "OK";

        } catch (Exception e) {
            log.error("Error en CambiarClaveUsuario: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, "E", "ERROR_INTERNO", e.getMessage()));
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    static String sha256Double(String input) {
        return sha256(sha256(input)).toUpperCase();
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Entrada {
        public String p_Clave;
        public String p_ClaveNueva;
        public Long p_IdUsuario;
        public Long p_IdUsuarioModificar;
        public Long p_IdEmpresa;
        public String p_IdModulo;
    }

    public static class ClaveUsuario {
        public String CodigoUsuario;
        public String Clave;
        public Long IdUsuario;
    }

    public static class LogClaveUsuario {
        public Long NumClaveCoincide;
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
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_ERRORESDETALLE = new ArrayList<>();
        public String P_OUT_UPDATE;
    }

    private static String loadSql(String path) {
        try (InputStream is = CambiarClaveUsuarioService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
