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
import java.util.UUID;

@Service
public class ObtenerTokenOptimusClientesService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ObtenerTokenOptimusClientesService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_USUARIO_COMPLETO;
    private final String SQL_UPDATE_DISABLE_TOKENS;
    private final String SQL_INSERT_SESION;

    public ObtenerTokenOptimusClientesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_USUARIO_COMPLETO = loadSql("cl/bcs/plataforma/acc/sql/ObtenerTokenOptimusClientes_Select_UsuarioCompleto.sql");
        this.SQL_UPDATE_DISABLE_TOKENS = loadSql("cl/bcs/plataforma/acc/sql/ObtenerTokenOptimusClientes_Update_DisableTokens.sql");
        this.SQL_INSERT_SESION = loadSql("cl/bcs/plataforma/acc/sql/ObtenerTokenOptimusClientes_Insert_Sesion.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.ObtenerTokenOptimusClientes"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ObtenerTokenOptimusClientes");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        if (in == null || in.p_IdEmpresa == null || in.p_CodigoUsuario == null) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(1L, "E", "USUARIO_CLAVE_INCORRECTA", "USUARIO_CLAVE_INCORRECTA"));
            return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
        }

        try {
            MapSqlParameterSource queryParams = new MapSqlParameterSource()
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("CodigoUsuario", in.p_CodigoUsuario);

            List<UsuarioCompleto> usuarios = jdbc.query(SQL_SELECT_USUARIO_COMPLETO, queryParams,
                    BeanPropertyRowMapper.newInstance(UsuarioCompleto.class));

            if (!usuarioValido(usuarios)) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(1L, "E", "USUARIO_CLAVE_INCORRECTA",
                        "Usuario inválido o bloqueado: " + in.p_CodigoUsuario));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            UsuarioCompleto usuario = usuarios.get(0);
            // Disable previous tokens
            jdbc.update(SQL_UPDATE_DISABLE_TOKENS, new MapSqlParameterSource()
                    .addValue("IdUsuario", usuario.IdUsuario)
                    .addValue("IdEmpresa", in.p_IdEmpresa));

            // Generate new token
            String newToken = UUID.randomUUID().toString().replace("-", "").toUpperCase();

            // Insert new session
            jdbc.update(SQL_INSERT_SESION, new MapSqlParameterSource()
                    .addValue("Token", newToken)
                    .addValue("IdUsuario", usuario.IdUsuario)
                    .addValue("IdEmpresa", in.p_IdEmpresa)
                    .addValue("CodigoUsuario", usuario.CodigoUsuario));

            out.P_OUT_TOKEN = newToken;

        } catch (Exception e) {
            log.error("Error en ObtenerTokenOptimusClientes: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(1L, "E", "ERROR_INTERNO", e.getMessage()));
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    private boolean usuarioValido(List<UsuarioCompleto> usuarios) {
        if (usuarios.isEmpty()) return false;
        UsuarioCompleto u = usuarios.get(0);
        if (!"S".equalsIgnoreCase(u.UsuarioClientes)) return false;
        if ("S".equalsIgnoreCase(u.TieneBloqueoTemporal)) return false;
        if (!"ACTIVO".equalsIgnoreCase(u.DescripcionEstado)) return false;
        return true;
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public String p_CodigoUsuario;
    }

    public static class UsuarioCompleto {
        public String Token;
        public Long IdUsuario;
        public Long IdEmpresa;
        public String CodigoUsuario;
        public String EsAutorizado;
        public String TieneBloqueoTemporal;
        public String TipoEntidad;
        public String DebeCambiarClave;
        public String DescripcionEstado;
        public String UsuarioClientes;
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
        public String P_OUT_TOKEN;
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    private static String loadSql(String path) {
        try (InputStream is = ObtenerTokenOptimusClientesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
