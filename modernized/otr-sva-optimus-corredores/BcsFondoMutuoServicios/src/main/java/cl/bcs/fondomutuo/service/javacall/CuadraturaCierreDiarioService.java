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
public class CuadraturaCierreDiarioService {

    private static final Logger log = LoggerFactory.getLogger(CuadraturaCierreDiarioService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectCuadratura;
    private final String sqlSelectSolicitudes;
    private final String sqlDelete;
    private final String sqlInsert;
    private final String sqlSelectDetalleCuadratura;

    public CuadraturaCierreDiarioService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectCuadratura = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CuadraturaCierreDiario_SelectCuadraturaDiariaOperaciones.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectSolicitudes = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CuadraturaCierreDiario_SelectSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlDelete = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsert = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectDetalleCuadratura = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CuadraturaCierreDiario_SelectJSONCuadraturaDiariaOperaciones.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("CuadraturaCierreDiario");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fecha = params.has("p_Fecha") ? params.get("p_Fecha").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("Fecha", fecha);

        List<Map<String, Object>> solicitudes = jdbc.queryForList(sqlSelectSolicitudes, ps);
        List<Map<String, Object>> cuadratura = jdbc.queryForList(sqlSelectCuadratura, ps);

        jdbc.update(sqlDelete, ps);
        jdbc.update(sqlInsert, ps);

        List<Map<String, Object>> detalleCuadratura = jdbc.queryForList(sqlSelectDetalleCuadratura, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrSol = new JsonArray();
        for (Map<String, Object> row : solicitudes) arrSol.add(GSON.toJsonTree(row));
        out.add("P_OUT_SOLICITUDES", arrSol);
        JsonArray arrCuad = new JsonArray();
        for (Map<String, Object> row : cuadratura) arrCuad.add(GSON.toJsonTree(row));
        out.add("P_OUT_CUADRATURA", arrCuad);
        JsonArray arrDet = new JsonArray();
        for (Map<String, Object> row : detalleCuadratura) arrDet.add(GSON.toJsonTree(row));
        out.add("P_OUT_DETALLE_CUADRATURA", arrDet);
        return out;
    }
}
