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
public class ModificarMontoNumeroCuotasSolicitudService {

    private static final Logger log = LoggerFactory.getLogger(ModificarMontoNumeroCuotasSolicitudService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectSolicitud;
    private final String sqlSelectSolicitudReinversion;
    private final String sqlUpdateAporte;
    private final String sqlUpdateRescate;
    private final String sqlUpdateReinversion;

    public ModificarMontoNumeroCuotasSolicitudService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectSolicitud = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ModificarMontoNumeroCuotasSolicitud_SelectSolicitud.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectSolicitudReinversion = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ModificarMontoNumeroCuotasSolicitud_SelectSolicitudReinversion.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateAporte = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ModificarMontoNumeroCuotasSolicitud_UpdateSolicitudAporte.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateRescate = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ModificarMontoNumeroCuotasSolicitud_UpdateSolicitudRescate.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateReinversion = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ModificarMontoNumeroCuotasSolicitud_UpdateSolicitudReinversion.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ModificarMontoNumeroCuotasSolicitud");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idSolicitud = params.has("p_IdSolicitudAporteRescate") ? params.get("p_IdSolicitudAporteRescate").getAsLong() : 0L;

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSolicitudAporteRescate", idSolicitud);

        List<Map<String, Object>> solicitud = jdbc.queryForList(sqlSelectSolicitud, ps);
        List<Map<String, Object>> reinversion = jdbc.queryForList(sqlSelectSolicitudReinversion, ps);

        jdbc.update(sqlUpdateAporte, ps);
        jdbc.update(sqlUpdateRescate, ps);
        jdbc.update(sqlUpdateReinversion, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrSol = new JsonArray();
        for (Map<String, Object> row : solicitud) arrSol.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUD", arrSol);
        JsonArray arrReinv = new JsonArray();
        for (Map<String, Object> row : reinversion) arrReinv.add(GSON.toJsonTree(row));
        out.add("P_OUT_REINVERSION", arrReinv);
        return out;
    }
}
