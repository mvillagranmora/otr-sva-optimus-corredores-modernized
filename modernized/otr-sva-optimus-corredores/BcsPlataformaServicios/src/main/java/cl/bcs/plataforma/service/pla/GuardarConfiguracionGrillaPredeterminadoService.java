package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class GuardarConfiguracionGrillaPredeterminadoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(GuardarConfiguracionGrillaPredeterminadoService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT;
    private final String SQL_UPDATE;
    private final String SQL_INSERT;

    public GuardarConfiguracionGrillaPredeterminadoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT = loadSql("cl/bcs/plataforma/pla/sql/GuardarConfiguracionGrillaPredeterminado_Select.sql");
        this.SQL_UPDATE = loadSql("cl/bcs/plataforma/pla/sql/GuardarConfiguracionGrillaPredeterminado_Update.sql");
        this.SQL_INSERT = loadSql("cl/bcs/plataforma/pla/sql/GuardarConfiguracionGrillaPredeterminado_Insert.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.GuardarConfiguracionGrillaPredeterminado"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::GuardarConfiguracionGrillaPredeterminado");
        Parametros_Entrada in = GSON.fromJson(params, Parametros_Entrada.class);
        Parametros_Salida out = new Parametros_Salida();

        try {
            if (in.p_Configuracion == null || in.p_Configuracion.isEmpty()) {
                out.P_OUT_ERRORESCONTADOR = 0L;
                return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
            }

            MapSqlParameterSource searchParams = new MapSqlParameterSource()
                .addValue("p_IdEmpresa", in.p_IdEmpresa)
                .addValue("p_Llave", in.p_Configuracion.get(0).Llave);

            List<EntidadConfiguracion> existe = jdbc.query(SQL_SELECT, searchParams,
                BeanPropertyRowMapper.newInstance(EntidadConfiguracion.class));

            EntidadConfiguracion dato = new EntidadConfiguracion();
            dato.p_IdEmpresa = in.p_IdEmpresa;
            dato.p_Llave = in.p_Configuracion.get(0).Llave;
            dato.p_Valor = in.p_Configuracion.get(0).Valor;

            if (!existe.isEmpty()) {
                jdbc.update(SQL_UPDATE, new BeanPropertySqlParameterSource(dato));
            } else {
                jdbc.update(SQL_INSERT, new BeanPropertySqlParameterSource(dato));
            }

            out.P_OUT_ERRORESCONTADOR = 0L;
            out.P_OUT_RESULTADO = "OK";
        } catch (Exception e) {
            log.error("Error en GuardarConfiguracionGrillaPredeterminado: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Parametros_Entrada {
        public Long p_IdEmpresa;
        public String p_IdModulo;
        public List<ObjConfiguracion> p_Configuracion;
    }

    public static class Parametros_Salida {
        public String P_OUT_RESULTADO;
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Object> P_OUT_ERRORESDETALLE = new ArrayList<>();
    }

    public static class ObjConfiguracion {
        public String Llave;
        public String Valor;
    }

    public static class EntidadConfiguracion {
        public Long IdConfiguracionGrilla;
        public Long p_IdEmpresa;
        public String p_Llave;
        public String p_Valor;
    }

    private static String loadSql(String path) {
        try (InputStream is = GuardarConfiguracionGrillaPredeterminadoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
