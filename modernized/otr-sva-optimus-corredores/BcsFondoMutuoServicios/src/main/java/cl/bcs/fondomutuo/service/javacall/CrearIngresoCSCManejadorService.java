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
public class CrearIngresoCSCManejadorService {

    private static final Logger log = LoggerFactory.getLogger(CrearIngresoCSCManejadorService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlConsultaRequisitos;
    private final String sqlSelectBanco;
    private final String sqlCuentaBancariaPredeterminada;

    public CrearIngresoCSCManejadorService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlConsultaRequisitos = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/IngresarMandatoPAC_ConsultaRequisitosCrearMandato.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectBanco = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoCSCManejador_SelectBanco.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlCuentaBancariaPredeterminada = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/CrearIngresoCSCManejador_CuentaBancariaPredeterminada.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("CrearIngresoCSCManejador");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L;
        long idCliente = params.has("p_IdCliente") ? params.get("p_IdCliente").getAsLong() : 0L;

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdPortafolio", idPortafolio)
            .addValue("IdCliente", idCliente);

        List<Map<String, Object>> requisitos = jdbc.queryForList(sqlConsultaRequisitos, ps);
        List<Map<String, Object>> banco = jdbc.queryForList(sqlSelectBanco, ps);
        List<Map<String, Object>> cuentaBancaria = jdbc.queryForList(sqlCuentaBancariaPredeterminada, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrRequisitos = new JsonArray();
        for (Map<String, Object> row : requisitos) arrRequisitos.add(GSON.toJsonTree(row));
        out.add("P_OUT_REQUISITOS", arrRequisitos);
        JsonArray arrBanco = new JsonArray();
        for (Map<String, Object> row : banco) arrBanco.add(GSON.toJsonTree(row));
        out.add("P_OUT_BANCO", arrBanco);
        JsonArray arrCuenta = new JsonArray();
        for (Map<String, Object> row : cuentaBancaria) arrCuenta.add(GSON.toJsonTree(row));
        out.add("P_OUT_CUENTA_BANCARIA", arrCuenta);
        return out;
    }
}
