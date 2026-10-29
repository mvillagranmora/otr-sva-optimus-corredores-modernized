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
public class CargarCustodiaFondoMutuoService {

    private static final Logger log = LoggerFactory.getLogger(CargarCustodiaFondoMutuoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectCuadratura;
    private final String sqlSelectCarteras;
    private final String sqlSelectValorCuota;
    private final String sqlInsertCuadratura;
    private final String sqlDeleteCuadratura;

    public CargarCustodiaFondoMutuoService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectCuadratura = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesCuadratura_SelectDatosCuadratura.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCarteras = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CargarCustodiaFondoMutuo_SelectDatosCarteras.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectValorCuota = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/Comunes_SelectValorCuota.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlInsertCuadratura = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesCuadratura_InsertDatosCuadratura.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlDeleteCuadratura = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesCuadratura_DeleteDatosCuadratura.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("CargarCustodiaFondoMutuo");

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fecha = params.has("p_FechaCuadratura") ? params.get("p_FechaCuadratura").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FechaCuadratura", fecha);

        List<Map<String, Object>> carteras = jdbc.queryForList(sqlSelectCarteras, ps);
        List<Map<String, Object>> valorCuota = jdbc.queryForList(sqlSelectValorCuota, ps);
        List<Map<String, Object>> cuadratura = jdbc.queryForList(sqlSelectCuadratura, ps);

        jdbc.update(sqlDeleteCuadratura, ps);
        jdbc.update(sqlInsertCuadratura, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrCart = new JsonArray();
        for (Map<String, Object> row : carteras) arrCart.add(GSON.toJsonTree(row));
        out.add("P_OUT_CARTERAS", arrCart);
        JsonArray arrVC = new JsonArray();
        for (Map<String, Object> row : valorCuota) arrVC.add(GSON.toJsonTree(row));
        out.add("P_OUT_VALOR_CUOTA", arrVC);
        return out;
    }
}
