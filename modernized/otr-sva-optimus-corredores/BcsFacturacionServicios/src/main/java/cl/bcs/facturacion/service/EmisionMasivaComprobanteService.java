package cl.bcs.facturacion.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class EmisionMasivaComprobanteService {

    private static final Logger log = LoggerFactory.getLogger(EmisionMasivaComprobanteService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectMovimientosI;
    private final String sqlSelectMovimientosO;
    private final String sqlSelectMovimientos;

    public EmisionMasivaComprobanteService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectMovimientosI = loadSql(
            "cl/bcs/facturacion/sql/EmisionMasivaComprobantea_SelectMovimientosI.sql");
        this.sqlSelectMovimientosO = loadSql(
            "cl/bcs/facturacion/sql/EmisionMasivaComprobantea_SelectMovimientosO.sql");
        this.sqlSelectMovimientos  = loadSql(
            "cl/bcs/facturacion/sql/EmisionMasivaComprobantea_SelectMovimientos.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::EmisionMasivaComprobante");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",                      getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",                      getL(params, "p_IdUsuario"))
            .addValue("IdModulo",                       getS(params, "p_IdModulo"))
            .addValue("IdClasificacionTipoInstrumento", getS(params, "p_IdClasificacionTipoInstrumento"))
            .addValue("IdTipoInstrumento",              getS(params, "p_IdTipoInstrumento"))
            .addValue("IdTipoComprobante",              getS(params, "p_IdTipoComprobante"))
            .addValue("IdCondicionLiquidacion",         getS(params, "p_IdCondicionLiquidacion"))
            .addValue("FechaMovimientoComprobante",     getS(params, "p_FechaMovimientoComprobante"))
            .addValue("Glosa",                          getS(params, "p_Glosa"))
            .addValue("SelCliente",                     getS(params, "p_SelCliente"));

        List<Map<String, Object>> movimientosI = jdbc.queryForList(sqlSelectMovimientosI, p);
        List<Map<String, Object>> movimientosO = jdbc.queryForList(sqlSelectMovimientosO, p);
        List<Map<String, Object>> movimientos  = jdbc.queryForList(sqlSelectMovimientos, p);

        // TODO: iterate movimientos and call CrearComprobante for each via internal service call

        JsonArray arrI = new JsonArray();
        movimientosI.forEach(r -> arrI.add(GSON.toJsonTree(r)));
        JsonArray arrO = new JsonArray();
        movimientosO.forEach(r -> arrO.add(GSON.toJsonTree(r)));
        JsonArray arrM = new JsonArray();
        movimientos.forEach(r -> arrM.add(GSON.toJsonTree(r)));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());
        out.add("P_OUT_MOVIMIENTOS_I", arrI);
        out.add("P_OUT_MOVIMIENTOS_O", arrO);
        out.add("P_OUT_MOVIMIENTOS", arrM);
        return out;
    }

    private static Long getL(JsonObject p, String key) {
        return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsLong() : null;
    }

    private static String getS(JsonObject p, String key) {
        return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsString() : null;
    }

    private static String loadSql(String path) {
        try (InputStream is = EmisionMasivaComprobanteService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
