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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ActualizarCostoUF_MLTService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarCostoUF_MLTService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectSolicitudesRescates;
    private final String sqlSelectSolicitudesAportes;
    private final String sqlSelectSolicitudesReinversiones;
    private final String sqlSelectCarteras;
    private final String sqlUpdateSolicitudes;
    private final String sqlUpdateSolicitudesAportes;
    private final String sqlUpdateCarteras;
    private final String sqlInsertProcesoMandatos;
    private final String sqlUpdateProcesoMandatosPRO;
    private final String sqlUpdateRegistroMLT;
    private final String sqlSelectRescateReinversion;
    private final String sqlSelectAporteReinversion;
    private final String sqlSelectFechaRescateReinversion;
    private final String sqlUpdateProcesoReinversion;

    public ActualizarCostoUF_MLTService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectSolicitudesRescates = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectSolicitudesRescates.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectSolicitudesAportes = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectSolicitudesAportes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectSolicitudesReinversiones = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectSolicitudesReinversiones.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCarteras = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectCarteras.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateSolicitudes = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_UpdateSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateSolicitudesAportes = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_UpdateSolicitudesAportes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateCarteras = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_UpdateCarteras.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertProcesoMandatos = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_InsertProcesoMandatos.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateProcesoMandatosPRO = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_UpdateProcesoMandatosPRO.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateRegistroMLT = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_UpdateRegistroMLT.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectRescateReinversion = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectRescateReinversion.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectAporteReinversion = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectAporteReinversion.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectFechaRescateReinversion = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_SelectFechaRescateReinversion.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateProcesoReinversion = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_MLT_UpdateProcesoReinversion.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ActualizarCostoUF_MLT");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fechaProceso = params.has("p_FechaProceso") ? params.get("p_FechaProceso").getAsString() : "";
        JsonArray procesosMandatos = params.has("p_ProcesosMandatos") ? params.getAsJsonArray("p_ProcesosMandatos") : new JsonArray();

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FechaProceso", fechaProceso);

        List<Map<String, Object>> solicitudesRescates = jdbc.queryForList(sqlSelectSolicitudesRescates, ps);
        List<Map<String, Object>> solicitudesAportes = jdbc.queryForList(sqlSelectSolicitudesAportes, ps);
        List<Map<String, Object>> solicitudesReinversiones = jdbc.queryForList(sqlSelectSolicitudesReinversiones, ps);
        List<Map<String, Object>> carteras = jdbc.queryForList(sqlSelectCarteras, ps);
        List<Map<String, Object>> rescateReinversion = jdbc.queryForList(sqlSelectRescateReinversion, ps);
        List<Map<String, Object>> aporteReinversion = jdbc.queryForList(sqlSelectAporteReinversion, ps);
        List<Map<String, Object>> fechaRescateReinversion = jdbc.queryForList(sqlSelectFechaRescateReinversion, ps);

        jdbc.update(sqlUpdateSolicitudes, ps);
        jdbc.update(sqlUpdateSolicitudesAportes, ps);
        jdbc.update(sqlUpdateCarteras, ps);
        jdbc.update(sqlUpdateProcesoReinversion, ps);

        List<SqlParameterSource> batchMandatos = new ArrayList<>();
        for (JsonElement e : procesosMandatos) {
            JsonObject m = e.getAsJsonObject();
            batchMandatos.add(new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("FechaProceso", fechaProceso)
                .addValue("IdMandato", m.has("IdMandato") ? m.get("IdMandato").getAsLong() : 0L));
        }
        if (!batchMandatos.isEmpty()) {
            jdbc.batchUpdate(sqlInsertProcesoMandatos, batchMandatos.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlUpdateProcesoMandatosPRO, batchMandatos.toArray(new SqlParameterSource[0]));
        }

        jdbc.update(sqlUpdateRegistroMLT, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        addArray(out, "P_OUT_SOLICITUDES_RESCATES", solicitudesRescates);
        addArray(out, "P_OUT_SOLICITUDES_APORTES", solicitudesAportes);
        addArray(out, "P_OUT_SOLICITUDES_REINVERSIONES", solicitudesReinversiones);
        addArray(out, "P_OUT_CARTERAS", carteras);
        addArray(out, "P_OUT_RESCATE_REINVERSION", rescateReinversion);
        addArray(out, "P_OUT_APORTE_REINVERSION", aporteReinversion);
        addArray(out, "P_OUT_FECHA_RESCATE_REINVERSION", fechaRescateReinversion);
        return out;
    }

    private void addArray(JsonObject out, String key, List<Map<String, Object>> rows) {
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : rows) arr.add(GSON.toJsonTree(row));
        out.add(key, arr);
    }
}
