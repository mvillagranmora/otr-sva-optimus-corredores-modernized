package cl.bcs.plataforma.service.pla.utils;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsultarValoresPorDefectoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarValoresPorDefectoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_CONSULTAR;

    public ConsultarValoresPorDefectoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_CONSULTAR = loadSql("cl/bcs/plataforma/pla/sql/ConsultarValoresDefecto_Select.sql");
    }

    @Override public String fqn() { return "cl.bcs.plataforma.pla.utils.ConsultarValoresPorDefecto"; }

    @Override
    public JsonObject execute(JsonObject params) {
        Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 1L;
        String tipoEntidad = params.has("p_TipoEntidad") ? params.get("p_TipoEntidad").getAsString() : "";
        String canalIngreso = params.has("p_IdTipoCanalIngreso") ? params.get("p_IdTipoCanalIngreso").getAsString() : "";

        List<Map<String, Object>> rows = jdbc.queryForList(SQL_CONSULTAR,
            new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("TipoEntidad", tipoEntidad)
                .addValue("IdTipoCanalIngreso", canalIngreso));

        JsonObject result = new JsonObject();
        JsonObject valores = new JsonObject();
        rows.forEach(r -> {
            String id = r.get("IdValorDefectoSistema") != null ? r.get("IdValorDefectoSistema").toString() : "";
            String val = r.get("Valor") != null ? r.get("Valor").toString() : "";
            valores.addProperty(id, val);
        });
        result.add("valores", valores);
        result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
