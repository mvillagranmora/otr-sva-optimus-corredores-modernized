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
public class CambiarClaveUsuarioWSService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CambiarClaveUsuarioWSService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final CambiarClaveUsuarioService cambiarClaveUsuarioService;
    private final String SQL_GET_ID_USUARIO;

    public CambiarClaveUsuarioWSService(NamedParameterJdbcTemplate jdbc,
                                        CambiarClaveUsuarioService cambiarClaveUsuarioService) {
        this.jdbc = jdbc;
        this.cambiarClaveUsuarioService = cambiarClaveUsuarioService;
        this.SQL_GET_ID_USUARIO = loadSql("cl/bcs/plataforma/acc/sql/CambiarClaveUsuarioWS_SelectIdUsuario.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.CambiarClaveUsuarioWS"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::CambiarClaveUsuarioWS");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            Long idUsuarioResuelto = in.p_IdUsuario;

            if ((idUsuarioResuelto == null || idUsuarioResuelto == 0) && in.p_CodigoUsuario != null) {
                List<DatosUsuario> usuarios = jdbc.query(SQL_GET_ID_USUARIO,
                        new MapSqlParameterSource()
                                .addValue("CodigoUsuario", in.p_CodigoUsuario)
                                .addValue("IdEmpresa", in.p_IdEmpresa),
                        BeanPropertyRowMapper.newInstance(DatosUsuario.class));
                if (!usuarios.isEmpty()) {
                    idUsuarioResuelto = usuarios.get(0).IdUsuario;
                }
            }

            if (idUsuarioResuelto == null || idUsuarioResuelto == 0) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, "E", "USUARIO_NO_ENCONTRADO",
                        "Usuario no encontrado: " + in.p_CodigoUsuario));
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            // Delegate to CambiarClaveUsuario logic
            JsonObject delegateParams = new JsonObject();
            delegateParams.addProperty("p_Clave", in.p_Clave);
            delegateParams.addProperty("p_ClaveNueva", in.p_ClaveNueva);
            delegateParams.addProperty("p_IdUsuario", idUsuarioResuelto);
            delegateParams.addProperty("p_IdEmpresa", in.p_IdEmpresa);
            delegateParams.addProperty("p_IdModulo", in.p_IdModulo);

            JsonObject delegateResult = cambiarClaveUsuarioService.execute(delegateParams);
            return delegateResult;

        } catch (Exception e) {
            log.error("Error en CambiarClaveUsuarioWS: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, "E", "ERROR_INTERNO", e.getMessage()));
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public String p_Clave;
        public String p_ClaveNueva;
        public Long p_IdUsuario;
        public String p_CodigoUsuario;
        public Long p_IdEmpresa;
        public String p_IdModulo;
    }

    public static class DatosUsuario {
        public Long IdUsuario;
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
        try (InputStream is = CambiarClaveUsuarioWSService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
