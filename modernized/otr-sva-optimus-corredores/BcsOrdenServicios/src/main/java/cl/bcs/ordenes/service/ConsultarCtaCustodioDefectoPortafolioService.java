package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.RequisitosBasicosUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.ConsultarCtaCustodioDefectoPortafolio (JavaCall).
 */
@Service
public class ConsultarCtaCustodioDefectoPortafolioService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCtaCustodioDefectoPortafolioService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final ConfiguracionSistemaUtil configUtil;
    private final String SQL_SELECT_CUENTASCUSTODIO;

    public ConsultarCtaCustodioDefectoPortafolioService(
            NamedParameterJdbcTemplate jdbc,
            RequisitosBasicosUtil requisitosUtil,
            ConfiguracionSistemaUtil configUtil) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.configUtil = configUtil;
        this.SQL_SELECT_CUENTASCUSTODIO = loadSql("cl/bcs/ordenes/sql/ConsultarCtaCustodioDefectoPortafolio_SelectCtasCustodio.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.debug("ConsultarCtaCustodioDefectoPortafolioService.execute()");
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : null;

            RequisitosBasicosUtil.Salida rb = requisitosUtil.validar(idEmpresa, idUsuario, idModulo);
            if (rb.contarErrores() > 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORES_CONTADOR", rb.contarErrores());
                out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(rb.p_out_Mensajes)));
                return out;
            }

            String localizacion = configUtil.getParametroString(idEmpresa, "PLA.LocalizacionSistema");
            String idModeloCustodia = params.has("p_IdModeloCustodia") ? params.get("p_IdModeloCustodia").getAsString() : "";
            String idClasificacion = params.has("p_IdClasificacionTipoInstrumento") ? params.get("p_IdClasificacionTipoInstrumento").getAsString() : "";

            String codigoParametro;
            if ("CL".equalsIgnoreCase(localizacion)) {
                codigoParametro = "IdBovedaDefecto";
            } else {
                if ("LOCAL".equalsIgnoreCase(idModeloCustodia))
                    codigoParametro = "IdBovedaDefectoLOCAL";
                else if ("EXTERIOR".equalsIgnoreCase(idModeloCustodia) && "RV".equalsIgnoreCase(idClasificacion))
                    codigoParametro = "IdBovedaDefectoEXTRV";
                else if ("EXTERIOR".equalsIgnoreCase(idModeloCustodia) && "RF".equalsIgnoreCase(idClasificacion))
                    codigoParametro = "IdBovedaDefectoEXTRF";
                else if ("EXTERIOR".equalsIgnoreCase(idModeloCustodia) && "DV".equalsIgnoreCase(idClasificacion))
                    codigoParametro = "IdBovedaDefectoEXTOPC";
                else
                    codigoParametro = "IdBovedaDefectoLOCAL";
            }

            String idBovedaDefecto = configUtil.getParametroString(idEmpresa, codigoParametro);

            List<Long> portafolios = new ArrayList<>();
            if (params.has("p_Portafolios") && params.get("p_Portafolios").isJsonArray()) {
                params.get("p_Portafolios").getAsJsonArray().forEach(e -> portafolios.add(e.getAsLong()));
            }

            MapSqlParameterSource sqlParams = new MapSqlParameterSource()
                .addValue("idsPortafolios", portafolios)
                .addValue("modeloCustodia", idModeloCustodia)
                .addValue("bovedaSugerida", idBovedaDefecto);

            List<Map<String, Object>> cuentas = jdbc.queryForList(SQL_SELECT_CUENTASCUSTODIO, sqlParams);

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORES_CONTADOR", 0);
            out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
            out.add("P_OUT_CUENTAS_CUSTODIO", JsonParser.parseString(GSON.toJson(cuentas)));
            return out;
        } catch (Exception e) {
            log.error("ConsultarCtaCustodioDefectoPortafolioService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORES_CONTADOR", 1);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String loadSql(String path) {
        try (var is = ConsultarCtaCustodioDefectoPortafolioService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
