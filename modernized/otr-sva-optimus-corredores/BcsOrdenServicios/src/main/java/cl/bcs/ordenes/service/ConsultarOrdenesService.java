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
import java.util.List;
import java.util.Map;

/**
 * Migrates cl.bcs.ordenes.ConsultarOrdenes.
 * Cross-domain: calls BcsSocioNegocioServicios for ObtenerPortafoliosEjecutivo.
 */
@Service
public class ConsultarOrdenesService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarOrdenesService.class);
    private static final String FQN_PORTAFOLIOS = "cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo";

    private final NamedParameterJdbcTemplate jdbc;
    private final RestClient socionegocioClient;
    private final String sqlSelect;
    private final String sqlInsertId;
    private final String sqlInsertFull;
    private final String sqlDelete;

    public ConsultarOrdenesService(
            NamedParameterJdbcTemplate jdbc,
            @Value("${bcs.socionegocio.url:http://bcs-socio-negocio-servicios:8098}") String socionegocioUrl) {
        this.jdbc = jdbc;
        this.socionegocioClient = RestClient.builder().baseUrl(socionegocioUrl).build();
        this.sqlSelect    = loadSql("ConsultarOrdenes_Select.sql");
        this.sqlInsertId  = loadSql("ConsultarOrdenes_InsertIdPortafolioTablaPaso.sql");
        this.sqlInsertFull = loadSql("ConsultarOrdenes_InsertFullPortafolioTablaPaso.sql");
        this.sqlDelete    = loadSql("ConsultarOrdenes_DeleteIdPortafolioTablaPaso.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ConsultarOrdenes");
        JsonObject result = new JsonObject();
        result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        result.add("P_OUT_MENSAJES", new JsonArray());
        result.add("P_OUT_ORDENES", new JsonArray());

        try {
            long idEmpresa  = longParam(params, "p_IdEmpresa", 1L);
            long idUsuario  = longParam(params, "p_IdUsuario", 0L);
            String idModulo = strParam(params, "p_IdModulo", "");
            long idEmpleado = longParam(params, "p_IdEmpleadoConsulta", 0L);
            long idPortafolio = longParam(params, "p_IdPortafolio", 0L);

            try {
                if (idPortafolio != 0) {
                    jdbc.update(sqlInsertId, new MapSqlParameterSource("IdPortafolio", idPortafolio));
                } else {
                    JsonObject snParams = new JsonObject();
                    snParams.addProperty("p_IdEmpresa", idEmpresa);
                    snParams.addProperty("p_IdUsuario", idUsuario);
                    snParams.addProperty("p_IdModulo", idModulo);
                    snParams.addProperty("p_IdEmpleadoConsulta", idEmpleado);
                    JsonObject snResponse = callSocioNegocio(FQN_PORTAFOLIOS, snParams);

                    boolean restringe = snResponse.has("restringe") && snResponse.get("restringe").getAsBoolean();
                    if (restringe && snResponse.has("portafolios")) {
                        for (JsonElement el : snResponse.getAsJsonArray("portafolios")) {
                            jdbc.update(sqlInsertId, new MapSqlParameterSource("IdPortafolio", el.getAsLong()));
                        }
                    } else {
                        jdbc.update(sqlInsertFull, new MapSqlParameterSource());
                    }
                }

                MapSqlParameterSource qp = buildQueryParams(params, idEmpresa);
                List<Map<String, Object>> rows = jdbc.queryForList(sqlSelect, qp);

                JsonArray ordenes = new JsonArray();
                for (Map<String, Object> row : rows) {
                    JsonObject o = new JsonObject();
                    row.forEach((k, v) -> {
                        if (v == null) o.add(k, com.google.gson.JsonNull.INSTANCE);
                        else o.addProperty(k, v.toString());
                    });
                    ordenes.add(o);
                }
                result.add("P_OUT_ORDENES", ordenes);

            } finally {
                jdbc.update(sqlDelete, new MapSqlParameterSource());
            }

        } catch (Exception e) {
            log.error("ConsultarOrdenes failed", e);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
        }

        log.info("FIN::ConsultarOrdenes");
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
        long instr = longParam(p, "p_Instrumento", 0L);
        ps.addValue("p_IdInstrumentoUI", instr);
        ps.addValue("var_IdInstrumentoUI", instr == 0 ? 1 : 0);
        ps.addValue("p_FechaOperacionOrdenI", strParam(p, "p_FechaOperacionOrdenI", ""));
        ps.addValue("p_FechaOperacionOrdenF", strParam(p, "p_FechaOperacionOrdenF", ""));
        String condLiq = strParam(p, "p_CondicionLiquidacion", "0");
        ps.addValue("p_IdCondicionDeLiquidacion", "0".equals(condLiq) ? "" : condLiq);
        ps.addValue("var_IdCondicionDeLiquidacion", "0".equals(condLiq) ? 1 : 0);
        long libro = longParam(p, "p_LibroOrdenes", 0L);
        ps.addValue("p_IdLibroOrdenes", libro);
        ps.addValue("var_IdLibroOrdenes", libro == 0 ? 1 : 0);
        String canal = strParam(p, "p_IdTipoCanalIngreso", "0");
        ps.addValue("p_IdTipoCanalIngreso", "0".equals(canal) ? "" : canal);
        ps.addValue("var_IdTipoCanalIngreso", "0".equals(canal) ? 1 : 0);
        long producto = longParam(p, "p_IdProducto", 0L);
        ps.addValue("p_IdProducto", producto);
        ps.addValue("var_IdProducto", producto == 0 ? 1 : 0);
        ps.addValue("p_EsVentaCorta", strParam(p, "p_EsVentaCorta", ""));
        // IncluyeOrdenesVigentes: "S" → null (include vigentes), else → "S" (exclude)
        String incluyeVigentes = strParam(p, "p_IncluyeOrdenesVigentes", "N");
        boolean incluyeVigentesFlag = "S".equals(incluyeVigentes);
        ps.addValue("p_IncluyeOrdenesVigentes", incluyeVigentesFlag ? null : "S");
        ps.addValue("var_IncluyeOrdenesNoVigentes", incluyeVigentesFlag ? 0 : 1);
        boolean ordenesPorAsignar = p.has("p_OrdenesPorAsignar") && p.get("p_OrdenesPorAsignar").getAsBoolean();
        long cantDisp = ordenesPorAsignar ? 0L : longParam(p, "p_CantidadDisponibleOrden", 0L);
        String estadoOrden = ordenesPorAsignar ? "OFE" : strParam(p, "p_IdEstadoOrden", "");
        ps.addValue("p_CantidadDisponibleOrden", cantDisp);
        ps.addValue("p_IdEstadoOrden", estadoOrden);
        ps.addValue("var_OrdenesPorAsignar", ordenesPorAsignar ? 1 : 0);
        long cliente = longParam(p, "p_IdCliente", 0L);
        ps.addValue("p_IdCliente", cliente);
        ps.addValue("var_IdCliente", cliente == 0 ? 1 : 0);
        String moneda = strParam(p, "p_Moneda", "0");
        ps.addValue("p_Moneda", "0".equals(moneda) ? "" : moneda);
        ps.addValue("varMoneda", "0".equals(moneda) ? 1 : 0);
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

    private long longParam(JsonObject p, String key, long def) {
        try { return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsLong() : def; }
        catch (Exception e) { return def; }
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
