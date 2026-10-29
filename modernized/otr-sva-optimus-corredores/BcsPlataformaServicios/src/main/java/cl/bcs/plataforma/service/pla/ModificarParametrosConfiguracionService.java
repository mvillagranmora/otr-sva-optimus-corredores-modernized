package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ModificarParametrosConfiguracionService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ModificarParametrosConfiguracionService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private static final String BASE = "cl/bcs/plataforma/pla/sql/parametrosconfiguracion/";

    private final NamedParameterJdbcTemplate jdbc;
    private final Map<String, String[]> SQL_BY_MODULE;

    public ModificarParametrosConfiguracionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        Map<String, String[]> map = new LinkedHashMap<>();
        for (String mod : new String[]{
                "CuentaInversion","Custodia","Facturacion","FondosMutuos","Mesa",
                "Normativa","NormativaParametro","Ordenes","Plataforma","SocioNegocio",
                "Tesoreria","Producto","Contabilidad","Limite"}) {
            map.put(toKey(mod), new String[]{
                loadSql(BASE + mod + "_Update.sql"),
                loadSql(BASE + mod + "_Select.sql")
            });
        }
        this.SQL_BY_MODULE = Collections.unmodifiableMap(map);
    }

    private static String toKey(String mod) {
        return switch (mod) {
            case "FondosMutuos" -> "FFMM";
            case "CuentaInversion" -> "CTAINV";
            case "SocioNegocio" -> "SOCIONEGOCIO";
            case "NormativaParametro" -> "NORMATIVAPARAMETRO";
            case "Limite" -> "LIMITES";
            default -> mod.toUpperCase();
        };
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ModificarParametrosConfiguracion"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ModificarParametrosConfiguracion");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        String[] sqls = in.p_Modulo != null ? SQL_BY_MODULE.get(in.p_Modulo.toUpperCase()) : null;
        if (sqls == null) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_ERRORESDETALLE.add(new Error("UI_ERROR", "UI_MODULO_NO_EXISTE"));
            return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
        }

        String updateSql = sqls[0];
        String selectSql = sqls[1];

        try {
            UpdateParam data = new UpdateParam();
            data.p_IdEmpresa = in.p_IdEmpresa;
            data.p_Codigo = in.p_Codigo;
            data.p_Valor = in.p_Valor;
            data.p_Usuario = "SYSTEM";

            MapSqlParameterSource selectParams = new MapSqlParameterSource()
                    .addValue("p_IdEmpresa", in.p_IdEmpresa)
                    .addValue("p_Codigo", in.p_Codigo);

            List<Map<String, Object>> rows = jdbc.queryForList(selectSql, selectParams);
            if (!rows.isEmpty()) {
                jdbc.update(updateSql, new BeanPropertySqlParameterSource(data));
                out.p_Respuesta.add("UI_ACTUALIZADO_OK");
                log.info("ModificarParametrosConfiguracion: {} = {}", in.p_Codigo, in.p_Valor);
            }

        } catch (Exception e) {
            log.error("Error en ModificarParametrosConfiguracion: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_ERRORESDETALLE.add(new Error("UI_ERROR", e.getMessage()));
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_Modulo;
        public String p_Codigo;
        public String p_Valor;
    }

    public static class UpdateParam {
        public Long p_IdEmpresa;
        public String p_Codigo;
        public String p_Valor;
        public String p_Usuario;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
        public List<Object> P_OUT_ADVERTENCIADETALLE = new ArrayList<>();
        public List<Error> P_OUT_ERRORESDETALLE = new ArrayList<>();
        public List<String> p_Respuesta = new ArrayList<>();
    }

    public static class Error {
        public String Codigo;
        public String RequiereAutorizacion = "N";
        public String Descripcion;

        public Error(String codigo, String descripcion) {
            this.Codigo = codigo;
            this.Descripcion = descripcion;
        }
    }

    private static String loadSql(String path) {
        try (InputStream is = ModificarParametrosConfiguracionService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
