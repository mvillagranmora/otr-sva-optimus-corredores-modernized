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
public class TrxCFileManagerService {

    private static final Logger log = LoggerFactory.getLogger(TrxCFileManagerService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectEmisor;
    private final String sqlSelectCabeceras;
    private final String sqlUpdateSolicitudAporteRescate;
    private final String sqlUpdateEstadoCabecera;
    private final String sqlUpdateOmnibusDetalle;

    public TrxCFileManagerService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectEmisor = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Comunes_SelectEmisor.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCabeceras = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Comunes_SelectCabeceras.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateSolicitudAporteRescate = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/TrxFileManager_UpdateSolicitudAporteRescate.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateEstadoCabecera = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/TrxFileManager_UpdateEstadoCabecera.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateOmnibusDetalle = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/TrxFileManager_UpdateOmnibusDetalle.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("TrxCFileManager");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String idEmisor = params.has("p_IdEmisor") ? params.get("p_IdEmisor").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdEmisor", idEmisor);

        List<Map<String, Object>> emisor = jdbc.queryForList(sqlSelectEmisor, ps);
        List<Map<String, Object>> cabeceras = jdbc.queryForList(sqlSelectCabeceras, ps);

        jdbc.update(sqlUpdateSolicitudAporteRescate, ps);
        jdbc.update(sqlUpdateEstadoCabecera, ps);
        jdbc.update(sqlUpdateOmnibusDetalle, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrEm = new JsonArray();
        for (Map<String, Object> row : emisor) arrEm.add(GSON.toJsonTree(row));
        out.add("P_OUT_EMISOR", arrEm);
        JsonArray arrCab = new JsonArray();
        for (Map<String, Object> row : cabeceras) arrCab.add(GSON.toJsonTree(row));
        out.add("P_OUT_CABECERAS", arrCab);
        return out;
    }
}
