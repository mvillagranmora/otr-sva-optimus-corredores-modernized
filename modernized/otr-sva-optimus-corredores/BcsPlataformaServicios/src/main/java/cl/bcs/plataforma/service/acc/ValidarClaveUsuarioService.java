package cl.bcs.plataforma.service.acc;

import cl.bcs.plataforma.service.JavaCallService;
import cl.bcs.plataforma.service.utils.ConfiguracionSistemaUtil;
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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidarClaveUsuarioService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ValidarClaveUsuarioService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil configUtil;
    private final String SQL_CLAVE_USUARIO;

    public ValidarClaveUsuarioService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistemaUtil configUtil) {
        this.jdbc = jdbc;
        this.configUtil = configUtil;
        this.SQL_CLAVE_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/ConsultaClaveUsuario.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.ValidarClaveUsuario"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ValidarClaveUsuario");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            List<ClaveUsuario> result = jdbc.query(SQL_CLAVE_USUARIO,
                    new MapSqlParameterSource("p_IdUsuario", in.p_IdUsuario),
                    BeanPropertyRowMapper.newInstance(ClaveUsuario.class));

            String hashActual = sha256Double(in.p_Clave != null ? in.p_Clave : "");

            if (!result.isEmpty()) {
                boolean coincide = result.get(0).Clave != null &&
                        result.get(0).Clave.equalsIgnoreCase(hashActual);

                if (!coincide) {
                    out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, "E", "CLAVE_NO_COINCIDE",
                            "La clave actual no coincide"));
                    out.P_OUT_ERRORESCONTADOR = 1L;
                } else {
                    out.P_OUT_CLAVEACTUAL_OK = true;
                }
            }

            // Validate new password criteria if provided
            if (in.p_ClaveNueva != null && !in.p_ClaveNueva.isEmpty()) {
                out.P_OUT_ESTADOCRITERIOSNUEVACLAVE = validarCriterioClave(in.p_ClaveNueva, in.p_IdEmpresa, in.p_IdModulo);
                boolean allOk = out.P_OUT_ESTADOCRITERIOSNUEVACLAVE.stream().allMatch(c -> c.cumple);
                out.P_OUT_VALIDACION_OK = allOk && out.P_OUT_CLAVEACTUAL_OK;
            }

        } catch (Exception e) {
            log.error("Error en ValidarClaveUsuario: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    private List<CriterioClave> validarCriterioClave(String clave, Long idEmpresa, String idModulo) {
        List<CriterioClave> criterios = new ArrayList<>();
        // Minimum length 8
        criterios.add(new CriterioClave("LONGITUD_MINIMA", "Mínimo 8 caracteres", clave.length() >= 8));
        // At least one uppercase
        criterios.add(new CriterioClave("MAYUSCULA", "Al menos una mayúscula", clave.chars().anyMatch(Character::isUpperCase)));
        // At least one lowercase
        criterios.add(new CriterioClave("MINUSCULA", "Al menos una minúscula", clave.chars().anyMatch(Character::isLowerCase)));
        // At least one digit
        criterios.add(new CriterioClave("NUMERO", "Al menos un número", clave.chars().anyMatch(Character::isDigit)));
        return criterios;
    }

    private static String sha256Double(String input) {
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
        public Long p_IdEmpresa;
        public String p_IdModulo;
    }

    public static class ClaveUsuario {
        public String CodigoUsuario;
        public String Clave;
        public Long IdUsuario;
    }

    public static class CriterioClave {
        public String idCriterio;
        public String descripcion;
        public boolean cumple;
        public CriterioClave(String id, String desc, boolean cumple) {
            this.idCriterio = id; this.descripcion = desc; this.cumple = cumple;
        }
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
        public List<CriterioClave> P_OUT_ESTADOCRITERIOSNUEVACLAVE = new ArrayList<>();
        public boolean P_OUT_VALIDACION_OK = false;
        public boolean P_OUT_CLAVEACTUAL_OK = false;
    }

    private static String loadSql(String path) {
        try (InputStream is = ValidarClaveUsuarioService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
