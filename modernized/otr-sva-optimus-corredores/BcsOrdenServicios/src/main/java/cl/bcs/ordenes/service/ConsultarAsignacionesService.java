package cl.bcs.ordenes.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrates cl.bcs.ordenes.ConsultarAsignaciones.
 * Cross-domain: calls BcsSocioNegocioServicios for ObtenerPortafoliosEjecutivo.
 */
@Service
public class ConsultarAsignacionesService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarAsignacionesService.class);
    private static final String FQN_PORTAFOLIOS = "cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo";

    private final NamedParameterJdbcTemplate jdbc;
    private final RestClient socionegocioClient;
    private final String sqlSelect;
    private final String sqlInsertId;
    private final String sqlInsertFull;
    private final String sqlDelete;

    public ConsultarAsignacionesService(
            NamedParameterJdbcTemplate jdbc,
            @Value("${bcs.socionegocio.url:http://bcs-socio-negocio-servicios:8098}") String socionegocioUrl) {
        this.jdbc = jdbc;
        this.socionegocioClient = RestClient.builder().baseUrl(socionegocioUrl).build();
        this.sqlSelect    = loadSql("ConsultarAsignaciones_Select.sql");
        this.sqlInsertId  = loadSql("ConsultarAsignaciones_InsertIdPortafolioTablaPaso.sql");
        this.sqlInsertFull = loadSql("ConsultarAsignaciones_InsertFullPortafolioTablaPaso.sql");
        this.sqlDelete    = loadSql("ConsultarAsignaciones_DeleteIdPortafolioTablaPaso.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ConsultarAsignaciones");
        JsonObject result = new JsonObject();
        result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        result.add("P_OUT_MENSAJES", new JsonArray());
        result.add("P_OUT_ASIGNACIONES", new JsonArray());

        try {
            long idEmpresa  = params.has("p_IdEmpresa")  ? params.get("p_IdEmpresa").getAsLong()  : 1L;
            long idUsuario  = params.has("p_IdUsuario")  ? params.get("p_IdUsuario").getAsLong()  : 0L;
            String idModulo = params.has("p_IdModulo")   ? params.get("p_IdModulo").getAsString() : "";
            long idEmpleado = params.has("p_IdEmpleadoConsulta") ? params.get("p_IdEmpleadoConsulta").getAsLong() : 0L;
            long idPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : 0L;

            // Populate step table with portfolio IDs
            try {
                if (idPortafolio != 0) {
                    MapSqlParameterSource ps = new MapSqlParameterSource("IdPortafolio", idPortafolio);
                    jdbc.update(sqlInsertId, ps);
                } else {
                    JsonObject snParams = new JsonObject();
                    snParams.addProperty("p_IdEmpresa", idEmpresa);
                    snParams.addProperty("p_IdUsuario", idUsuario);
                    snParams.addProperty("p_IdModulo", idModulo);
                    snParams.addProperty("p_IdEmpleadoConsulta", idEmpleado);
                    JsonObject snResponse = callSocioNegocio(FQN_PORTAFOLIOS, snParams);

                    boolean restringe = snResponse.has("restringe") && snResponse.get("restringe").getAsBoolean();
                    if (restringe && snResponse.has("portafolios")) {
                        JsonArray portafolios = snResponse.getAsJsonArray("portafolios");
                        for (JsonElement el : portafolios) {
                            long pid = el.getAsLong();
                            jdbc.update(sqlInsertId, new MapSqlParameterSource("IdPortafolio", pid));
                        }
                    } else {
                        jdbc.update(sqlInsertFull, new MapSqlParameterSource());
                    }
                }

                // Build query params
                MapSqlParameterSource qp = buildQueryParams(params, idEmpresa);
                List<Map<String, Object>> rows = jdbc.queryForList(sqlSelect, qp);

                JsonArray asignaciones = new JsonArray();
                for (Map<String, Object> row : rows) {
                    JsonObject a = new JsonObject();
                    row.forEach((k, v) -> {
                        if (v == null) a.add(k, com.google.gson.JsonNull.INSTANCE);
                        else a.addProperty(k, v.toString());
                    });
                    asignaciones.add(a);
                }
                result.add("P_OUT_ASIGNACIONES", asignaciones);

            } finally {
                jdbc.update(sqlDelete, new MapSqlParameterSource());
            }

        } catch (Exception e) {
            log.error("ConsultarAsignaciones failed", e);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
        }

        log.info("FIN::ConsultarAsignaciones");
        return result;
    }

    private MapSqlParameterSource buildQueryParams(JsonObject p, long idEmpresa) {
        MapSqlParameterSource ps = new MapSqlParameterSource("p_IdEmpresa", idEmpresa);
        String mercado = strParam(p, "p_Mercado", "0");
        ps.addValue("p_IdClasificacionTipoInstrumento", "0".equals(mercado) ? "" : mercado);
        ps.addValue("var_IdClasificacionTipoInstrumento", "0".equals(mercado) ? 1 : 0);
        String tipoInstr = strParam(p, "p_TipoInstrumento", "0");
        ps.addValue("p_IdTipoInstrumento", "0".equals(tipoInstr) ? "" : tipoInstr);
        ps.addValue("var_IdTipoInstrumento", "0".equals(tipoInstr) ? 1 : 0);
        String operacion = strParam(p, "p_Operacion", "0");
        ps.addValue("p_AccionOrden", "0".equals(operacion) ? "" : operacion);
        ps.addValue("var_AccionOrden", "0".equals(operacion) ? 1 : 0);
        long instr = longParam(p, "p_Instrumento");
        ps.addValue("p_IdInstrumentoUI", instr);
        ps.addValue("var_IdInstrumentoUI", instr == 0 ? 1 : 0);
        ps.addValue("p_FechaAsignacionI", strParam(p, "p_FechaAsignacionI", ""));
        ps.addValue("p_FechaAsignacionF", strParam(p, "p_FechaAsignacionF", ""));
        String condLiq = strParam(p, "p_CondicionLiquidacion", "0");
        ps.addValue("p_IdCondicionDeLiquidacion", "0".equals(condLiq) ? "" : condLiq);
        ps.addValue("var_IdCondicionDeLiquidacion", "0".equals(condLiq) ? 1 : 0);
        long libro = longParam(p, "p_LibroOrdenes");
        ps.addValue("p_IdLibroOrdenes", libro);
        ps.addValue("var_IdLibroOrdenes", libro == 0 ? 1 : 0);
        String bolsa = strParam(p, "p_Bolsa", "0");
        ps.addValue("p_IdBolsa", "0".equals(bolsa) ? "" : bolsa);
        ps.addValue("var_IdBolsa", "0".equals(bolsa) ? 1 : 0);
        long producto = longParam(p, "p_Producto");
        ps.addValue("p_IdProductoAgrupador", producto);
        ps.addValue("var_IdProductoAgrupador", producto == 0 ? 1 : 0);
        String estado = strParam(p, "p_Estado", "0");
        ps.addValue("p_IdEstadoAsignacion", "0".equals(estado) ? "" : estado);
        ps.addValue("var_IdEstadoAsignacion", "0".equals(estado) ? 1 : 0);
        long cliente = longParam(p, "p_IdCliente");
        ps.addValue("p_IdCliente", cliente);
        ps.addValue("var_IdCliente", cliente == 0 ? 1 : 0);
        String moneda = strParam(p, "p_IdMonedaOperacion", "0");
        ps.addValue("p_IdMonedaOperacion", "0".equals(moneda) ? "" : moneda);
        ps.addValue("varIdMonedaOperacion", "0".equals(moneda) ? 1 : 0);
        String marca = strParam(p, "p_MarcaAsignacion", "");
        ps.addValue("p_MarcaAsignacion", marca);
        ps.addValue("var_MarcaAsignacion", marca.isEmpty() ? 1 : 0);
        String operador = strParam(p, "p_CodigoOperador", "0");
        ps.addValue("p_CodigoOperador", "0".equals(operador) ? "" : operador);
        ps.addValue("var_CodigoOperador", "0".equals(operador) ? 1 : 0);
        return ps;
    }

    private JsonObject callSocioNegocio(String fqn, JsonObject snParams) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("class", fqn);
            body.add("params", snParams);
            String resp = socionegocioClient.post()
                .uri("/internal/java-call")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toString())
                .retrieve()
                .body(String.class);
            if (resp == null || resp.isBlank()) return new JsonObject();
            return JsonParser.parseString(resp).getAsJsonObject();
        } catch (Exception e) {
            log.warn("SocioNegocio call {} failed (graceful): {}", fqn, e.getMessage());
            return new JsonObject();
        }
    }

    private String strParam(JsonObject p, String key, String def) {
        return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsString() : def;
    }

    private long longParam(JsonObject p, String key) {
        try { return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsLong() : 0L; }
        catch (Exception e) { return 0L; }
    }

    private String loadSql(String fileName) {
        String path = "cl/bcs/ordenes/sql/" + fileName;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) return "-- SQL NOT FOUND: " + path;
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "-- SQL NOT FOUND: " + path;
        }
    }
}
