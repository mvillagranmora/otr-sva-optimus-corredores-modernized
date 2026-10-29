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
public class ConsultarDetalleAnularMovimientosService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarDetalleAnularMovimientosService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectMovimientosAnular;

    public ConsultarDetalleAnularMovimientosService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectMovimientosAnular = loadSql(
            "cl/bcs/facturacion/sql/ConsultarDetalleAnularMovimientos_SelectMovimientosAnular.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ConsultarDetalleAnularMovimientos");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",       getL(params, "p_IdEmpresa"))
            .addValue("IdPortafolio",    getL(params, "p_IdPortafolio"))
            .addValue("IdInstrumento",   getL(params, "p_IdInstrumento"))
            .addValue("IdLibroOrdenes",  getL(params, "p_IdLibroOrdenes"))
            .addValue("SecuenciaOrden",  getL(params, "p_SecuenciaOrden"))
            .addValue("FolioTransaccion",getS(params, "p_FolioTransaccion"))
            .addValue("Fecha",           getS(params, "p_Fecha"))
            .addValue("Operacion",       getS(params, "p_Operacion"))
            .addValue("MarcaAsignacion", getS(params, "p_MarcaAsignacion"))
            .addValue("FiltraPorAsignacionMasiva", getS(params, "p_FiltraPorAsignacionMasiva", "N"));

        List<Map<String, Object>> rows = jdbc.queryForList(sqlSelectMovimientosAnular, p);

        JsonArray movimientos = new JsonArray();
        for (Map<String, Object> row : rows) {
            movimientos.add(GSON.toJsonTree(row));
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());
        out.add("P_OUT_MOVIMIENTOS_ANULAR", movimientos);
        return out;
    }

    private static Long getL(JsonObject p, String key) {
        return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsLong() : null;
    }

    private static String getS(JsonObject p, String key) {
        return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsString() : null;
    }

    private static String getS(JsonObject p, String key, String def) {
        return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsString() : def;
    }

    private static String loadSql(String path) {
        try (InputStream is = ConsultarDetalleAnularMovimientosService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
