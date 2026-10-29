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
public class CrearSolicitudAporteOvernightService {

    private static final Logger log = LoggerFactory.getLogger(CrearSolicitudAporteOvernightService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectSerieFondo;
    private final String sqlSelectPortafolio;
    private final String sqlSelectTipoMovimiento;
    private final String sqlSelectRegimenSerieDeFondo;
    private final String sqlSelectIndicadoresPortafolio;
    private final String sqlSelectIndicadoresPortafolioFFMM;
    private final String sqlSelectMoneda;
    private final String sqlInsertSolicitud;
    private final String sqlSelectConfiguracionProposito;

    public CrearSolicitudAporteOvernightService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectSerieFondo = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectPortafolio = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectTipoMovimiento = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Comunes_SelectTipoMovimientoFondoMutuo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectRegimenSerieDeFondo = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectIndicadoresPortafolio = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectIndicadoresPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectIndicadoresPortafolioFFMM = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectIndicadoresPortafolioFFMM.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectMoneda = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectMoneda.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertSolicitud = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_InsertSolicitud.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectConfiguracionProposito = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectConfiguracionPropositoPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("CrearSolicitudAporteOvernight");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String idSerieFondo = params.has("p_IdSerieFondo") ? params.get("p_IdSerieFondo").getAsString() : "";
        long idPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L;
        JsonArray solicitudes = params.has("p_SolicitudesAporte") ? params.getAsJsonArray("p_SolicitudesAporte") : new JsonArray();

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSerieFondo", idSerieFondo)
            .addValue("IdPortafolio", idPortafolio);

        List<Map<String, Object>> series = jdbc.queryForList(sqlSelectSerieFondo, ps);
        List<Map<String, Object>> portafolio = jdbc.queryForList(sqlSelectPortafolio, ps);
        List<Map<String, Object>> tipoMovimiento = jdbc.queryForList(sqlSelectTipoMovimiento, ps);
        List<Map<String, Object>> regimen = jdbc.queryForList(sqlSelectRegimenSerieDeFondo, ps);
        List<Map<String, Object>> indicadores = jdbc.queryForList(sqlSelectIndicadoresPortafolioFFMM, ps);
        List<Map<String, Object>> moneda = jdbc.queryForList(sqlSelectMoneda, ps);

        List<SqlParameterSource> batch = new ArrayList<>();
        for (JsonElement e : solicitudes) {
            JsonObject s = e.getAsJsonObject();
            batch.add(new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("IdSerieFondo", s.has("IdSerieFondo") ? s.get("IdSerieFondo").getAsString() : idSerieFondo));
        }
        if (!batch.isEmpty()) jdbc.batchUpdate(sqlInsertSolicitud, batch.toArray(new SqlParameterSource[0]));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        addArray(out, "P_OUT_SERIE_FONDO", series);
        addArray(out, "P_OUT_PORTAFOLIO", portafolio);
        addArray(out, "P_OUT_TIPO_MOVIMIENTO", tipoMovimiento);
        addArray(out, "P_OUT_REGIMEN", regimen);
        addArray(out, "P_OUT_INDICADORES", indicadores);
        addArray(out, "P_OUT_MONEDA", moneda);
        return out;
    }

    private void addArray(JsonObject out, String key, List<Map<String, Object>> rows) {
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : rows) arr.add(GSON.toJsonTree(row));
        out.add(key, arr);
    }
}
