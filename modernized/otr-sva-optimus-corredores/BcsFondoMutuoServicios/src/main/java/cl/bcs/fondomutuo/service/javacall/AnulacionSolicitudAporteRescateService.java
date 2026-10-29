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
public class AnulacionSolicitudAporteRescateService {

    private static final Logger log = LoggerFactory.getLogger(AnulacionSolicitudAporteRescateService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;
    private final String sqlSelectCarta;
    private final String sqlSelectIngreso;
    private final String sqlSelectCarteraAportes;
    private final String sqlSelectCarteraRescates;
    private final String sqlUpdate;
    private final String sqlSelectCertificar;
    private final String sqlSelectSpot;

    public AnulacionSolicitudAporteRescateService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_SelectSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCarta = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_SelectCartaLiquidacion.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectIngreso = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_SelectIngresoEgreso.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCarteraAportes = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_SelectCarteraAportes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCarteraRescates = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_SelectCarteraRescates.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdate = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_UpdateSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCertificar = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_SelectSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectSpot = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AnulacionSolicitudAporteRescate_SelectOperacionSpot.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("AnulacionSolicitudAporteRescate");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonArray solicitudesInput = params.has("p_Solicitudes") ? params.getAsJsonArray("p_Solicitudes") : new JsonArray();

        List<Long> ids = new ArrayList<>();
        for (JsonElement e : solicitudesInput) {
            JsonObject s = e.getAsJsonObject();
            if (s.has("IdSolicitudAporteRescate")) ids.add(s.get("IdSolicitudAporteRescate").getAsLong());
        }

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSolicitudAporteRescate", ids.isEmpty() ? List.of(0L) : ids);

        List<Map<String, Object>> solicitudes = jdbc.queryForList(sqlSelect, ps);
        List<Map<String, Object>> carteraAportes = jdbc.queryForList(sqlSelectCarteraAportes, ps);
        List<Map<String, Object>> carteraRescates = jdbc.queryForList(sqlSelectCarteraRescates, ps);
        List<Map<String, Object>> ingresosEgreso = jdbc.queryForList(sqlSelectIngreso, ps);

        List<SqlParameterSource> batch = new ArrayList<>();
        for (JsonElement e : solicitudesInput) {
            JsonObject s = e.getAsJsonObject();
            batch.add(new MapSqlParameterSource()
                .addValue("IdSolicitudAporteRescate", s.has("IdSolicitudAporteRescate") ? s.get("IdSolicitudAporteRescate").getAsLong() : null)
                .addValue("IdEmpresa", idEmpresa));
        }
        if (!batch.isEmpty()) jdbc.batchUpdate(sqlUpdate, batch.toArray(new SqlParameterSource[0]));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : solicitudes) arr.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES", arr);
        JsonArray arrCartA = new JsonArray();
        for (Map<String, Object> row : carteraAportes) arrCartA.add(GSON.toJsonTree(row));
        out.add("P_OUT_CARTERA_APORTES", arrCartA);
        JsonArray arrCartR = new JsonArray();
        for (Map<String, Object> row : carteraRescates) arrCartR.add(GSON.toJsonTree(row));
        out.add("P_OUT_CARTERA_RESCATES", arrCartR);
        return out;
    }
}
