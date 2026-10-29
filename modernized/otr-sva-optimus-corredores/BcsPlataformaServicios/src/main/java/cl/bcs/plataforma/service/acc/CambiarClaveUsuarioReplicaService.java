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
public class CambiarClaveUsuarioReplicaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CambiarClaveUsuarioReplicaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_UPD_CLAVE_USUARIO_REPLICA;

    public CambiarClaveUsuarioReplicaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_UPD_CLAVE_USUARIO_REPLICA = loadSql("cl/bcs/plataforma/acc/sql/ActualizarUsuario_ClaveUsuarioReplica.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.acc.CambiarClaveUsuarioReplica"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::CambiarClaveUsuarioReplica");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            String newHash = CambiarClaveUsuarioService.sha256Double(in.p_ClaveNueva != null ? in.p_ClaveNueva : "");

            MapSqlParameterSource updParams = new MapSqlParameterSource()
                    .addValue("CodigoUsuario", in.p_CodigoUsuario)
                    .addValue("Clave", newHash)
                    .addValue("IdEmpresa", in.p_IdEmpresa);
            int rows = jdbc.update(SQL_UPD_CLAVE_USUARIO_REPLICA, updParams);

            out.P_OUT_UPDATE = rows > 0 ? "OK" : "NO_ROWS";

        } catch (Exception e) {
            log.error("Error en CambiarClaveUsuarioReplica: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, "E", "ERROR_INTERNO", e.getMessage()));
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public String p_ClaveNueva;
        public String p_CodigoUsuario;
        public Long p_IdEmpresa;
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
        try (InputStream is = CambiarClaveUsuarioReplicaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
