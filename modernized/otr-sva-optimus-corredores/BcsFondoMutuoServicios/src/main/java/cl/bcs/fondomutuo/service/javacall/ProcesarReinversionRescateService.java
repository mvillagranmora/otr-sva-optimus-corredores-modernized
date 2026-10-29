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
public class ProcesarReinversionRescateService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarReinversionRescateService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectPreferenciaPagoPortafolio;
    private final String sqlConsultaPreferenciaPagoPortafolio;
    private final String sqlConsultaPreferenciaPago;
    private final String sqlSelectPortafolio;
    private final String sqlSelectSerieFondo;
    private final String sqlSelectGrupoRegimen;
    private final String sqlSelectRegimen;
    private final String sqlConsultarCuentaBancaria;

    public ProcesarReinversionRescateService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectPreferenciaPagoPortafolio = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarReinversionRescate_SelectPreferenciaPagoPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlConsultaPreferenciaPagoPortafolio = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarReinversionRescate_ConsultaPreferenciaPagoPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlConsultaPreferenciaPago = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarReinversionRescate_ConsultaPreferenciaPago.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectPortafolio = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarReinversionRescate_SelectPortafolio.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectSerieFondo = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectGrupoRegimen = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectGrupoRegimenSerieDeFondo.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectRegimen = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarReinversionRescate_SelectRegimen.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlConsultarCuentaBancaria = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/ProcesarReinversionRescate_ConsultarCuentaBancaria.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("ProcesarReinversionRescate");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String idSerieFondo = params.has("p_IdSerieFondo") ? params.get("p_IdSerieFondo").getAsString() : "";
        long idPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L;

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("IdSerieFondo", idSerieFondo)
            .addValue("IdPortafolio", idPortafolio);

        List<Map<String, Object>> preferenciaPagoPortafolio = jdbc.queryForList(sqlSelectPreferenciaPagoPortafolio, ps);
        List<Map<String, Object>> portafolio = jdbc.queryForList(sqlSelectPortafolio, ps);
        List<Map<String, Object>> serieFondo = jdbc.queryForList(sqlSelectSerieFondo, ps);
        List<Map<String, Object>> grupoRegimen = jdbc.queryForList(sqlSelectGrupoRegimen, ps);
        List<Map<String, Object>> regimen = jdbc.queryForList(sqlSelectRegimen, ps);
        List<Map<String, Object>> cuentaBancaria = jdbc.queryForList(sqlConsultarCuentaBancaria, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrPP = new JsonArray();
        for (Map<String, Object> row : preferenciaPagoPortafolio) arrPP.add(GSON.toJsonTree(row));
        out.add("P_OUT_PREFERENCIA_PAGO_PORTAFOLIO", arrPP);
        JsonArray arrPort = new JsonArray();
        for (Map<String, Object> row : portafolio) arrPort.add(GSON.toJsonTree(row));
        out.add("P_OUT_PORTAFOLIO", arrPort);
        JsonArray arrSerie = new JsonArray();
        for (Map<String, Object> row : serieFondo) arrSerie.add(GSON.toJsonTree(row));
        out.add("P_OUT_SERIE_FONDO", arrSerie);
        JsonArray arrReg = new JsonArray();
        for (Map<String, Object> row : regimen) arrReg.add(GSON.toJsonTree(row));
        out.add("P_OUT_REGIMEN", arrReg);
        JsonArray arrCuenta = new JsonArray();
        for (Map<String, Object> row : cuentaBancaria) arrCuenta.add(GSON.toJsonTree(row));
        out.add("P_OUT_CUENTA_BANCARIA", arrCuenta);
        return out;
    }
}
