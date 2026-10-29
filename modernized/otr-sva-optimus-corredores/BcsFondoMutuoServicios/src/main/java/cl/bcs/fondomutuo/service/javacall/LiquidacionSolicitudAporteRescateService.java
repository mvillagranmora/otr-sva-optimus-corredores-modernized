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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class LiquidacionSolicitudAporteRescateService {

    private static final Logger log = LoggerFactory.getLogger(LiquidacionSolicitudAporteRescateService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectSolicitudes;
    private final String sqlSelectSolicitudesFiltros;
    private final String sqlUpdateSolicitudes;
    private final String sqlSelectModalidadPago;

    public LiquidacionSolicitudAporteRescateService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectSolicitudes = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/LiquidacionSolicitudAporteRescate_SelectSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectSolicitudesFiltros = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/LiquidacionSolicitudAporteRescate_SelectSolicitudesFiltros.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateSolicitudes = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/LiquidacionSolicitudAporteRescate_UpdateSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectModalidadPago = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectModalidadPago.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("LiquidacionSolicitudAporteRescate");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fechaLiquidacion = params.has("p_FechaLiquidacion") ? params.get("p_FechaLiquidacion").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FechaLiquidacion", fechaLiquidacion);

        List<Map<String, Object>> solicitudes = jdbc.queryForList(sqlSelectSolicitudes, ps);
        List<Map<String, Object>> solicitudesFiltros = jdbc.queryForList(sqlSelectSolicitudesFiltros, ps);
        List<Map<String, Object>> modalidades = jdbc.queryForList(sqlSelectModalidadPago, ps);

        jdbc.update(sqlUpdateSolicitudes, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrSol = new JsonArray();
        for (Map<String, Object> row : solicitudes) arrSol.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES", arrSol);
        JsonArray arrFil = new JsonArray();
        for (Map<String, Object> row : solicitudesFiltros) arrFil.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES_FILTROS", arrFil);
        JsonArray arrMod = new JsonArray();
        for (Map<String, Object> row : modalidades) arrMod.add(GSON.toJsonTree(row));
        out.add("P_OUT_MODALIDADES_PAGO", arrMod);
        return out;
    }
}
