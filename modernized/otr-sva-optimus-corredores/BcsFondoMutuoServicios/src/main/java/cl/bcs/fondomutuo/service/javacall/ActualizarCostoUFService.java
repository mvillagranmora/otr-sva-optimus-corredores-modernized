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
public class ActualizarCostoUFService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarCostoUFService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectRescates;
    private final String sqlSelectAportes;
    private final String sqlSelectReinversiones;
    private final String sqlSelectCarteras;
    private final String sqlUpdateSolicitudes;
    private final String sqlUpdateCarteras;
    private final String sqlUpdateRegistroMLT;

    public ActualizarCostoUFService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectRescates = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_SelectSolicitudesRescates.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectAportes = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_SelectSolicitudesAportes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectReinversiones = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_SelectSolicitudesReinversiones.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCarteras = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_SelectCarteras.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateSolicitudes = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_UpdateSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateCarteras = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_UpdateCarteras.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateRegistroMLT = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ActualizarCostoUF_UpdateRegistroMLT.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ActualizarCostoUF");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fechaValor = params.has("p_FechaValor") ? params.get("p_FechaValor").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FechaValor", fechaValor);

        List<Map<String, Object>> rescates = jdbc.queryForList(sqlSelectRescates, ps);
        List<Map<String, Object>> aportes = jdbc.queryForList(sqlSelectAportes, ps);
        List<Map<String, Object>> reinversiones = jdbc.queryForList(sqlSelectReinversiones, ps);
        List<Map<String, Object>> carteras = jdbc.queryForList(sqlSelectCarteras, ps);

        jdbc.update(sqlUpdateSolicitudes, ps);
        jdbc.update(sqlUpdateCarteras, ps);
        jdbc.update(sqlUpdateRegistroMLT, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrR = new JsonArray();
        for (Map<String, Object> row : rescates) arrR.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES_RESCATES", arrR);
        JsonArray arrA = new JsonArray();
        for (Map<String, Object> row : aportes) arrA.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES_APORTES", arrA);
        JsonArray arrReinv = new JsonArray();
        for (Map<String, Object> row : reinversiones) arrReinv.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES_REINVERSIONES", arrReinv);
        JsonArray arrCart = new JsonArray();
        for (Map<String, Object> row : carteras) arrCart.add(GSON.toJsonTree(row));
        out.add("P_OUT_CARTERAS", arrCart);
        return out;
    }
}
