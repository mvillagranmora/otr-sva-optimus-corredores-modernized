package cl.bcs.plataforma.service.pla.utils;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ConsultarEmpresaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarEmpresaService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_CONSULTAR;

    public ConsultarEmpresaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_CONSULTAR = loadSql("cl/bcs/plataforma/pla/sql/ConsultarEmpresa_Select.sql");
    }

    @Override public String fqn() { return "cl.bcs.plataforma.pla.utils.ConsultarEmpresa"; }

    @Override
    public JsonObject execute(JsonObject params) {
        Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;

        List<Map<String, Object>> rows = jdbc.queryForList(SQL_CONSULTAR,
            new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa));

        JsonObject result = new JsonObject();
        if (!rows.isEmpty()) {
            Map<String, Object> row = rows.get(0);
            row.forEach((k, v) -> result.addProperty(k, v != null ? v.toString() : null));
        }
        result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
