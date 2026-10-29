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
public class CrearSolicitudRescateReinversionService {

    private static final Logger log = LoggerFactory.getLogger(CrearSolicitudRescateReinversionService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectSerieFondo;
    private final String sqlSelectPortafolio;
    private final String sqlSelectTipoMovimiento;
    private final String sqlSelectRegimenSerieDeFondo;
    private final String sqlSelectRegimenSerieDeFondoMandato;
    private final String sqlSelectMoneda;
    private final String sqlInsertSolicitud;
    private final String sqlInsertReinversionSolicitud;
    private final String sqlSelectConfiguracionRegimen;
    private final String sqlSelectConfiguracionProposito;
    private final String sqlSelectPortafolioAPV;

    public CrearSolicitudRescateReinversionService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectSerieFondo = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectPortafolio = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectTipoMovimiento = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Comunes_SelectTipoMovimientoFondoMutuo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectRegimenSerieDeFondo = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectRegimenSerieDeFondoMandato = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondoMandato.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectMoneda = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectMoneda.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertSolicitud = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_InsertSolicitud.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertReinversionSolicitud = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_InsertReinversionSolicitud.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectConfiguracionRegimen = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectConfiguracionRegimenFondoMutuo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectConfiguracionProposito = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectConfiguracionPropositoPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectPortafolioAPV = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolioAPV.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("CrearSolicitudRescateReinversion");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String idSerieFondo = params.has("p_IdSerieFondo") ? params.get("p_IdSerieFondo").getAsString() : "";
        long idPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L;
        JsonArray solicitudesRescate = params.has("p_SolicitudesRescate") ? params.getAsJsonArray("p_SolicitudesRescate") : new JsonArray();
        JsonArray solicitudesAporte = params.has("p_SolicitudesAporte") ? params.getAsJsonArray("p_SolicitudesAporte") : new JsonArray();

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSerieFondo", idSerieFondo)
            .addValue("IdPortafolio", idPortafolio);

        List<Map<String, Object>> series = jdbc.queryForList(sqlSelectSerieFondo, ps);
        List<Map<String, Object>> portafolio = jdbc.queryForList(sqlSelectPortafolio, ps);
        List<Map<String, Object>> tipoMovimiento = jdbc.queryForList(sqlSelectTipoMovimiento, ps);
        List<Map<String, Object>> regimen = jdbc.queryForList(sqlSelectRegimenSerieDeFondo, ps);
        List<Map<String, Object>> moneda = jdbc.queryForList(sqlSelectMoneda, ps);
        List<Map<String, Object>> configuracionRegimen = jdbc.queryForList(sqlSelectConfiguracionRegimen, ps);

        List<SqlParameterSource> batchRescate = new ArrayList<>();
        for (JsonElement e : solicitudesRescate) {
            JsonObject s = e.getAsJsonObject();
            batchRescate.add(new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("IdSerieFondo", s.has("IdSerieFondo") ? s.get("IdSerieFondo").getAsString() : idSerieFondo));
        }
        List<SqlParameterSource> batchAporte = new ArrayList<>();
        for (JsonElement e : solicitudesAporte) {
            JsonObject s = e.getAsJsonObject();
            batchAporte.add(new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("IdSerieFondo", s.has("IdSerieFondo") ? s.get("IdSerieFondo").getAsString() : idSerieFondo));
        }
        if (!batchRescate.isEmpty()) {
            jdbc.batchUpdate(sqlInsertSolicitud, batchRescate.toArray(new SqlParameterSource[0]));
            jdbc.batchUpdate(sqlInsertReinversionSolicitud, batchRescate.toArray(new SqlParameterSource[0]));
        }
        if (!batchAporte.isEmpty()) {
            jdbc.batchUpdate(sqlInsertSolicitud, batchAporte.toArray(new SqlParameterSource[0]));
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        addArray(out, "P_OUT_SERIE_FONDO", series);
        addArray(out, "P_OUT_PORTAFOLIO", portafolio);
        addArray(out, "P_OUT_TIPO_MOVIMIENTO", tipoMovimiento);
        addArray(out, "P_OUT_REGIMEN", regimen);
        addArray(out, "P_OUT_MONEDA", moneda);
        addArray(out, "P_OUT_CONFIGURACION_REGIMEN", configuracionRegimen);
        return out;
    }

    private void addArray(JsonObject out, String key, List<Map<String, Object>> rows) {
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : rows) arr.add(GSON.toJsonTree(row));
        out.add(key, arr);
    }
}
