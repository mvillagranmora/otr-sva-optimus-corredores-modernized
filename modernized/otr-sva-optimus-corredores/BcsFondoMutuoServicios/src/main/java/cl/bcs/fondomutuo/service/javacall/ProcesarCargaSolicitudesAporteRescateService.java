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
public class ProcesarCargaSolicitudesAporteRescateService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCargaSolicitudesAporteRescateService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectDetalleCarga;
    private final String sqlSelectMediosDePago;
    private final String sqlActualizarProcesoDetalleCarga;
    private final String sqlActualizarDetalle;

    public ProcesarCargaSolicitudesAporteRescateService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectDetalleCarga = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarCargaSolicitudesAporteRescate_SelectDetalleCarga.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectMediosDePago = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarCargaSolicitudesAporteRescate_SelectMediosDePago.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlActualizarProcesoDetalleCarga = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarCargaSolicitudesAporteRescate_ActualizarProcesoDetalleCarga.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlActualizarDetalle = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarCargaSolicitudesAporteRescate_ActualizarDetalle.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ProcesarCargaSolicitudesAporteRescate");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idProceso = params.has("p_IdProceso") ? params.get("p_IdProceso").getAsLong() : 0L;

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdProceso", idProceso);

        List<Map<String, Object>> detalleCarga = jdbc.queryForList(sqlSelectDetalleCarga, ps);
        List<Map<String, Object>> mediosPago = jdbc.queryForList(sqlSelectMediosDePago, ps);

        jdbc.update(sqlActualizarProcesoDetalleCarga, ps);
        jdbc.update(sqlActualizarDetalle, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrDet = new JsonArray();
        for (Map<String, Object> row : detalleCarga) arrDet.add(GSON.toJsonTree(row));
        out.add("P_OUT_DETALLE_CARGA", arrDet);
        JsonArray arrMed = new JsonArray();
        for (Map<String, Object> row : mediosPago) arrMed.add(GSON.toJsonTree(row));
        out.add("P_OUT_MEDIOS_PAGO", arrMed);
        return out;
    }
}
