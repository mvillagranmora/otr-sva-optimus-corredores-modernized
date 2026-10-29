package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ConsultarValorCuotaHistoricoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarValorCuotaHistoricoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;

    public ConsultarValorCuotaHistoricoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultarValorCuotaHistorico_SelectValoresCuotaHistorico.sql").getInputStream(),
            StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ConsultarValorCuotaHistorico");

        String idSerieFondo = params.has("p_IdSerieFondo") ? params.get("p_IdSerieFondo").getAsString() : "";
        String idFamiliaFondo = params.has("p_IdFamiliaFondo") ? params.get("p_IdFamiliaFondo").getAsString() : "";
        Long idInstrumento = params.has("p_IdInstrumento") && !params.get("p_IdInstrumento").isJsonNull() ? params.get("p_IdInstrumento").getAsLong() : null;
        String idEmisor = params.has("p_IdEmisor") ? params.get("p_IdEmisor").getAsString() : "";
        String fechaDesde = params.has("p_FechaVigenciaDesde") ? params.get("p_FechaVigenciaDesde").getAsString() : "0001-01-01";
        String fechaHasta = params.has("p_FechaVigenciaHasta") ? params.get("p_FechaVigenciaHasta").getAsString() : "0001-01-01";
        String nemotecnico = params.has("p_Nemotecnico") ? params.get("p_Nemotecnico").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("Nemotecnico", nemotecnico)
            .addValue("IdSerieFondo", idSerieFondo)
            .addValue("IdFamiliaFondo", idFamiliaFondo)
            .addValue("IdInstrumento", idInstrumento)
            .addValue("IdEmisor", idEmisor)
            .addValue("FechaVigenciaDesde", fechaDesde)
            .addValue("FechaVigenciaHasta", fechaHasta)
            .addValue("FiltraPorNemotecnico", nemotecnico != null && !nemotecnico.isEmpty() ? 1L : 0L)
            .addValue("FiltraPorIdSerieFondo", idSerieFondo != null && !idSerieFondo.isEmpty() ? 1L : 0L)
            .addValue("FiltraPorIdFamiliaFondo", idFamiliaFondo != null && !idFamiliaFondo.isEmpty() ? 1L : 0L)
            .addValue("FiltraPorIdInstrumento", idInstrumento != null ? 1L : 0L)
            .addValue("FiltraPorIdEmisor", idEmisor != null && !idEmisor.isEmpty() ? 1L : 0L)
            .addValue("FiltraPorFechaVigenciaDesde", !"0001-01-01".equals(fechaDesde) ? 1L : 0L)
            .addValue("FiltraPorFechaVigenciaHasta", !"0001-01-01".equals(fechaHasta) ? 1L : 0L);

        List<Map<String, Object>> rows = jdbc.queryForList(sqlSelect, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : rows) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_VALORES_CUOTAS", arr);
        return out;
    }
}
