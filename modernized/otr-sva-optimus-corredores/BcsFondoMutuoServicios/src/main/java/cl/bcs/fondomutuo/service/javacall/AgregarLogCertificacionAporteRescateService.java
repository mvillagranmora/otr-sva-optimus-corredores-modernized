package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.AgregarLogCertificacionAporteRescate.
 */
@Service
public class AgregarLogCertificacionAporteRescateService {

    private static final Logger log = LoggerFactory.getLogger(AgregarLogCertificacionAporteRescateService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlInsertLog;

    public AgregarLogCertificacionAporteRescateService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlInsertLog = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/AgregarLogCertificacionAporteRescate_InsertLogCertificacion.sql").getInputStream(),
            StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("AgregarLogCertificacionAporteRescate");
        JsonArray logArray = params.has("p_Log") ? params.getAsJsonArray("p_Log") : new JsonArray();

        List<SqlParameterSource> batchParams = new ArrayList<>();
        for (JsonElement elem : logArray) {
            JsonObject entry = elem.getAsJsonObject();
            MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("IdSolicitudAporteRescate", getL(entry, "IdSolicitudAporteRescate"))
                .addValue("Codigo", getS(entry, "Codigo"))
                .addValue("IdEstado", getS(entry, "IdEstado"))
                .addValue("TipoSolicitud", getS(entry, "TipoSolicitud"))
                .addValue("IdTipoMovimientoFondoMutuo", getS(entry, "IdTipoMovimientoFondoMutuo"))
                .addValue("IdUsuario", getL(entry, "IdUsuario"))
                .addValue("IdEmpresa", getL(entry, "IdEmpresa"))
                .addValue("FechaSistema", getS(entry, "FechaSistema"))
                .addValue("IdSerieFondo", getS(entry, "IdSerieFondo"))
                .addValue("IdEmisor", getS(entry, "IdEmisor"))
                .addValue("Monto", getBD(entry, "Monto"))
                .addValue("IdMoneda", getS(entry, "IdMoneda"))
                .addValue("MontoOriginal", getBD(entry, "MontoOriginal"))
                .addValue("TipoCambio", getBD(entry, "TipoCambio"))
                .addValue("IdMonedaOriginal", getS(entry, "IdMonedaOriginal"))
                .addValue("ValorCuota", getBD(entry, "ValorCuota"))
                .addValue("NumeroCuotas", getBD(entry, "NumeroCuotas"))
                .addValue("IdCliente", getL(entry, "IdCliente"))
                .addValue("IdPortafolio", getL(entry, "IdPortafolio"))
                .addValue("Simulacion", getS(entry, "Simulacion"));
            batchParams.add(ps);
        }

        if (!batchParams.isEmpty()) {
            jdbc.batchUpdate(sqlInsertLog, batchParams.toArray(new SqlParameterSource[0]));
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return out;
    }

    private String getS(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : null;
    }

    private Long getL(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsLong() : null;
    }

    private BigDecimal getBD(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsBigDecimal() : null;
    }
}
