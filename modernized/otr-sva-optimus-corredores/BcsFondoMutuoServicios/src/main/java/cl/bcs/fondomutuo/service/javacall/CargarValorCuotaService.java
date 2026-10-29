package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CargarValorCuotaService {

    private static final Logger log = LoggerFactory.getLogger(CargarValorCuotaService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSerieFondo;
    private final String sqlSerieFondoISIN;
    private final String sqlExistenteFecha;
    private final String sqlDiaAnterior;
    private final String sqlUpdateUltimaCarga;
    private final String sqlInsertHistorico;
    private final String sqlDelete;
    private final String sqlInsert;

    public CargarValorCuotaService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSerieFondo = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSerieFondoISIN = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CargarValorCuota_SelectSerieFondoISIN.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlExistenteFecha = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CargarValorCuota_SelectValorCuotaExistenteFecha.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlDiaAnterior = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CargarValorCuota_SelectValorCuotaDiaAnteriorFechaCarga.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateUltimaCarga = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CargarValorCuota_UpdateValorCuotaUltimaCarga.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertHistorico = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CargarValorCuota_InsertValorCuotaHistorico.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlDelete = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CargarValorCuota_DeleteValorCuota.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsert = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CargarValorCuota_InsertValorCuota.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("CargarValorCuota");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fechaCarga = params.has("p_FechaCarga") ? params.get("p_FechaCarga").getAsString() : "";
        JsonArray valoresCuota = params.has("p_ValoresCuota") ? params.getAsJsonArray("p_ValoresCuota") : new JsonArray();

        List<String> series = new ArrayList<>();
        for (JsonElement e : valoresCuota) {
            JsonObject v = e.getAsJsonObject();
            if (v.has("IdSerieFondo")) series.add(v.get("IdSerieFondo").getAsString());
        }

        List<Map<String, Object>> seriesFondo = series.isEmpty() ? List.of() :
            jdbc.queryForList(sqlSerieFondo, new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa).addValue("IdSerieFondo", series));

        List<SqlParameterSource> batchDelete = new ArrayList<>();
        List<SqlParameterSource> batchInsert = new ArrayList<>();
        List<SqlParameterSource> batchHistorico = new ArrayList<>();

        for (JsonElement e : valoresCuota) {
            JsonObject v = e.getAsJsonObject();
            MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("IdSerieFondo", v.has("IdSerieFondo") ? v.get("IdSerieFondo").getAsString() : "")
                .addValue("FechaCarga", fechaCarga)
                .addValue("ValorCuota", v.has("ValorCuota") && !v.get("ValorCuota").isJsonNull() ? v.get("ValorCuota").getAsBigDecimal() : BigDecimal.ZERO);
            batchDelete.add(ps);
            batchInsert.add(ps);
            batchHistorico.add(ps);
        }

        if (!batchDelete.isEmpty()) {
            jdbc.batchUpdate(sqlInsertHistorico, batchHistorico.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlDelete, batchDelete.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlInsert, batchInsert.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlUpdateUltimaCarga, batchInsert.toArray(new SqlParameterSource[0]));
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrSeries = new JsonArray();
        for (Map<String, Object> row : seriesFondo) arrSeries.add(GSON.toJsonTree(row));
        out.add("P_OUT_SERIES", arrSeries);
        return out;
    }
}
