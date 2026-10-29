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
public class ProcesarComisionesNNService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarComisionesNNService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectComisionesNNProcesadas;
    private final String sqlSelectConsultarCartera;
    private final String sqlSelectConsultarCarteraSelectivo;
    private final String sqlInsertComisionesNNProcesadas;

    public ProcesarComisionesNNService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectComisionesNNProcesadas = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarComisionesNN_SelectComisionesNNProcesadas.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectConsultarCartera = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarComisionesNN_SelectConsultarCartera.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectConsultarCarteraSelectivo = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarComisionesNN_SelectConsultarCarteraSelectivo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertComisionesNNProcesadas = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarComisionesNN_InsertComisionesNNProcesadas.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ProcesarComisionesNN");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fecha = params.has("p_Fecha") ? params.get("p_Fecha").getAsString() : "";
        boolean selectivo = params.has("p_Selectivo") && params.get("p_Selectivo").getAsBoolean();

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("Fecha", fecha);

        List<Map<String, Object>> comisionesNNProcesadas = jdbc.queryForList(sqlSelectComisionesNNProcesadas, ps);
        List<Map<String, Object>> cartera = selectivo
            ? jdbc.queryForList(sqlSelectConsultarCarteraSelectivo, ps)
            : jdbc.queryForList(sqlSelectConsultarCartera, ps);

        jdbc.update(sqlInsertComisionesNNProcesadas, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrComisiones = new JsonArray();
        for (Map<String, Object> row : comisionesNNProcesadas) arrComisiones.add(GSON.toJsonTree(row));
        out.add("P_OUT_COMISIONES_NN_PROCESADAS", arrComisiones);
        JsonArray arrCartera = new JsonArray();
        for (Map<String, Object> row : cartera) arrCartera.add(GSON.toJsonTree(row));
        out.add("P_OUT_CARTERA", arrCartera);
        return out;
    }
}
