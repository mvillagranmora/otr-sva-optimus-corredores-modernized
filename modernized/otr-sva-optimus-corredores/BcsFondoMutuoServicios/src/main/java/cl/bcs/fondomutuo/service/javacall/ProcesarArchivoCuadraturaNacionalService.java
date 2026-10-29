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
public class ProcesarArchivoCuadraturaNacionalService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarArchivoCuadraturaNacionalService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectDatosCuadratura;
    private final String sqlSelectValorCuota;
    private final String sqlInsertDatosCuadratura;
    private final String sqlDeleteDatosCuadratura;
    private final String sqlSelectDatosFondoRun;

    public ProcesarArchivoCuadraturaNacionalService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectDatosCuadratura = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesCuadratura_SelectDatosCuadratura.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectValorCuota = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Comunes_SelectValorCuota.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertDatosCuadratura = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesCuadratura_InsertDatosCuadratura.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlDeleteDatosCuadratura = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesCuadratura_DeleteDatosCuadratura.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectDatosFondoRun = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/Comunes_SelectDatosFondoRun.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("ProcesarArchivoCuadraturaNacional");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fecha = params.has("p_FechaCuadratura") ? params.get("p_FechaCuadratura").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FechaCuadratura", fecha);

        List<Map<String, Object>> datosCuadratura = jdbc.queryForList(sqlSelectDatosCuadratura, ps);
        List<Map<String, Object>> valorCuota = jdbc.queryForList(sqlSelectValorCuota, ps);
        List<Map<String, Object>> datosFondoRun = jdbc.queryForList(sqlSelectDatosFondoRun, ps);

        jdbc.update(sqlDeleteDatosCuadratura, ps);
        jdbc.update(sqlInsertDatosCuadratura, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrDatos = new JsonArray();
        for (Map<String, Object> row : datosCuadratura) arrDatos.add(GSON.toJsonTree(row));
        out.add("P_OUT_DATOS_CUADRATURA", arrDatos);
        JsonArray arrVC = new JsonArray();
        for (Map<String, Object> row : valorCuota) arrVC.add(GSON.toJsonTree(row));
        out.add("P_OUT_VALOR_CUOTA", arrVC);
        JsonArray arrFondo = new JsonArray();
        for (Map<String, Object> row : datosFondoRun) arrFondo.add(GSON.toJsonTree(row));
        out.add("P_OUT_DATOS_FONDO", arrFondo);
        return out;
    }
}
