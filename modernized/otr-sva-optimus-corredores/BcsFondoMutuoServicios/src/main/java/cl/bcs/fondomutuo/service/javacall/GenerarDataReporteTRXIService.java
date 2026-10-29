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
public class GenerarDataReporteTRXIService {

    private static final Logger log = LoggerFactory.getLogger(GenerarDataReporteTRXIService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectSolicitudes;
    private final String sqlSelectAporteRescateCabeceraNacional;
    private final String sqlSelectAporteRescateCabeceraInternacional;
    private final String sqlSelectAporteRescateCabeceraInternacionalSeparado;
    private final String sqlSelectAporteRescateCabecera;
    private final String sqlSelectAporteRescate;
    private final String sqlSelectConfiguracionEmisorOmnibus;
    private final String sqlSelectConfiguracionOmnibus;
    private final String sqlInsertOmnibus;
    private final String sqlInsertOmnibusDetalle;
    private final String sqlUpdateSolicitudAporteRescate;
    private final String sqlUpdateFolioOperacionEmisor;
    private final String sqlSelectBloqueoSolicitudes;

    public GenerarDataReporteTRXIService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectSolicitudes = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectAporteRescateCabeceraNacional = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectAporteRescateCabeceraNacional.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectAporteRescateCabeceraInternacional = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectAporteRescateCabeceraInternacional.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectAporteRescateCabeceraInternacionalSeparado = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectAporteRescateCabeceraInternacionalSeparado.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectAporteRescateCabecera = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectAporteRescateCabecera.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectAporteRescate = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectAporteRescate.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectConfiguracionEmisorOmnibus = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectConfiguracionEmisorOmnibus.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectConfiguracionOmnibus = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectConfiguracionOmnibus.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertOmnibus = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_InsertOmnibus.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertOmnibusDetalle = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_InsertOmnibusDetalle.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateSolicitudAporteRescate = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_UpdateSolicitudAporteRescate.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateFolioOperacionEmisor = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_UpdateFolioOperacionEmisor.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectBloqueoSolicitudes = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectBloqueoSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("GenerarDataReporteTRXI");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fechaProceso = params.has("p_FechaProceso") ? params.get("p_FechaProceso").getAsString() : "";
        JsonArray solicitudesInput = params.has("p_Solicitudes") ? params.getAsJsonArray("p_Solicitudes") : new JsonArray();

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FechaProceso", fechaProceso);

        List<Map<String, Object>> solicitudes = jdbc.queryForList(sqlSelectSolicitudes, ps);
        List<Map<String, Object>> cabeceraNacional = jdbc.queryForList(sqlSelectAporteRescateCabeceraNacional, ps);
        List<Map<String, Object>> cabeceraInternacional = jdbc.queryForList(sqlSelectAporteRescateCabeceraInternacional, ps);
        List<Map<String, Object>> cabeceraInternacionalSeparado = jdbc.queryForList(sqlSelectAporteRescateCabeceraInternacionalSeparado, ps);
        List<Map<String, Object>> cabecera = jdbc.queryForList(sqlSelectAporteRescateCabecera, ps);
        List<Map<String, Object>> aporteRescate = jdbc.queryForList(sqlSelectAporteRescate, ps);
        List<Map<String, Object>> configuracionEmisorOmnibus = jdbc.queryForList(sqlSelectConfiguracionEmisorOmnibus, ps);
        List<Map<String, Object>> configuracionOmnibus = jdbc.queryForList(sqlSelectConfiguracionOmnibus, ps);
        List<Map<String, Object>> bloqueoSolicitudes = jdbc.queryForList(sqlSelectBloqueoSolicitudes, ps);

        List<SqlParameterSource> batchOmnibus = new ArrayList<>();
        List<SqlParameterSource> batchSolicitudes = new ArrayList<>();
        for (JsonElement e : solicitudesInput) {
            JsonObject s = e.getAsJsonObject();
            MapSqlParameterSource row = new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("FechaProceso", fechaProceso)
                .addValue("IdSolicitud", s.has("IdSolicitud") ? s.get("IdSolicitud").getAsLong() : 0L);
            batchOmnibus.add(row);
            batchSolicitudes.add(row);
        }
        if (!batchOmnibus.isEmpty()) {
            jdbc.batchUpdate(sqlInsertOmnibus, batchOmnibus.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlInsertOmnibusDetalle, batchOmnibus.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlUpdateSolicitudAporteRescate, batchSolicitudes.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlUpdateFolioOperacionEmisor, batchSolicitudes.toArray(new SqlParameterSource[0]));
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        addArray(out, "P_OUT_SOLICITUDES", solicitudes);
        addArray(out, "P_OUT_CABECERA_NACIONAL", cabeceraNacional);
        addArray(out, "P_OUT_CABECERA_INTERNACIONAL", cabeceraInternacional);
        addArray(out, "P_OUT_CABECERA_INTERNACIONAL_SEPARADO", cabeceraInternacionalSeparado);
        addArray(out, "P_OUT_CABECERA", cabecera);
        addArray(out, "P_OUT_APORTE_RESCATE", aporteRescate);
        addArray(out, "P_OUT_CONFIGURACION_EMISOR_OMNIBUS", configuracionEmisorOmnibus);
        addArray(out, "P_OUT_CONFIGURACION_OMNIBUS", configuracionOmnibus);
        addArray(out, "P_OUT_BLOQUEO_SOLICITUDES", bloqueoSolicitudes);
        return out;
    }

    private void addArray(JsonObject out, String key, List<Map<String, Object>> rows) {
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : rows) arr.add(GSON.toJsonTree(row));
        out.add(key, arr);
    }
}
