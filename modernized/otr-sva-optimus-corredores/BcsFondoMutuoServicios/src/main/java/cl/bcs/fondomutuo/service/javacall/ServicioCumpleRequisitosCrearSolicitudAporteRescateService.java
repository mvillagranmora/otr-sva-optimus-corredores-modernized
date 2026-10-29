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
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ServicioCumpleRequisitosCrearSolicitudAporteRescateService {

    private static final Logger log = LoggerFactory.getLogger(ServicioCumpleRequisitosCrearSolicitudAporteRescateService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectEmpleadoUsuario;
    private final String sqlSelectPortafolio;

    public ServicioCumpleRequisitosCrearSolicitudAporteRescateService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectEmpleadoUsuario = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ServicioCumpleRequisitosCrearSolicitudAporteRescate_SelectEmpleadoUsuario.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectPortafolio = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ServicioCumpleRequisitosCrearSolicitudAporteRescate");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : 0L;
        long idPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L;

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdUsuario", idUsuario)
            .addValue("IdPortafolio", idPortafolio);

        List<Map<String, Object>> empleado = jdbc.queryForList(sqlSelectEmpleadoUsuario, ps);
        List<Map<String, Object>> portafolio = jdbc.queryForList(sqlSelectPortafolio, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrEmp = new JsonArray();
        for (Map<String, Object> row : empleado) arrEmp.add(GSON.toJsonTree(row));
        out.add("P_OUT_EMPLEADO", arrEmp);
        JsonArray arrPort = new JsonArray();
        for (Map<String, Object> row : portafolio) arrPort.add(GSON.toJsonTree(row));
        out.add("P_OUT_PORTAFOLIO", arrPort);
        return out;
    }
}
