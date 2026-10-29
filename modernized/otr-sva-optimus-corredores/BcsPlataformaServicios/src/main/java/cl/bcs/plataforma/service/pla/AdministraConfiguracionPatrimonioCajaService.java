package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdministraConfiguracionPatrimonioCajaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AdministraConfiguracionPatrimonioCajaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final Type ITEM_LIST = new TypeToken<List<Item>>(){}.getType();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_UPDATE;

    public AdministraConfiguracionPatrimonioCajaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_UPDATE = loadSql("cl/bcs/plataforma/pla/sql/AdministraConfiguracionPatrimonioCaja_UpdateConfiguracionPatrimonioCaja.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.AdministraConfiguracionPatrimonioCaja"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::AdministraConfiguracionPatrimonioCaja");
        Salida out = new Salida();
        try {
            List<Item> items = params.has("p_Items")
                ? GSON.fromJson(params.get("p_Items"), ITEM_LIST)
                : new ArrayList<>();

            if (items.isEmpty()) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                addMsg(out, "ERROR", "NO_SE_INGRESARON_ITEMS", "No se ingresaron items");
                return toJson(out);
            }

            boolean hayInvalidos = items.stream().anyMatch(i ->
                !"0".equals(i.FuncionOperador) && !"+".equals(i.FuncionOperador) && !"-".equals(i.FuncionOperador));
            if (hayInvalidos) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                addMsg(out, "ERROR", "FUNCION_OPERADOR_INVALIDO", "FuncionOperador debe ser 0, + o -");
                return toJson(out);
            }

            SqlParameterSource[] batch = items.stream()
                .map(i -> (SqlParameterSource) new MapSqlParameterSource()
                    .addValue("CodigoItem", i.CodigoItem)
                    .addValue("DescripcionItem", i.DescripcionItem)
                    .addValue("FuncionOperador", i.FuncionOperador)
                    .addValue("IdEstado", i.IdEstado))
                .toArray(SqlParameterSource[]::new);

            jdbc.batchUpdate(SQL_UPDATE, batch);
        } catch (Exception e) {
            log.error("Error en AdministraConfiguracionPatrimonioCaja: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            addMsg(out, "ERROR", "ERROR_INTERNO", e.getMessage());
        }
        return toJson(out);
    }

    private void addMsg(Salida out, String tipo, String codigo, String desc) {
        Mensaje m = new Mensaje(); m.tipo = tipo; m.codigo = codigo; m.descripcion = desc;
        out.P_OUT_MENSAJES.add(m);
    }

    private JsonObject toJson(Object o) {
        return JsonParser.parseString(GSON.toJson(o)).getAsJsonObject();
    }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    public static class Item {
        public String CodigoItem; public String DescripcionItem;
        public String FuncionOperador; public String IdEstado;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Mensaje> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class Mensaje {
        public String tipo; public String codigo; public String descripcion;
    }
}
