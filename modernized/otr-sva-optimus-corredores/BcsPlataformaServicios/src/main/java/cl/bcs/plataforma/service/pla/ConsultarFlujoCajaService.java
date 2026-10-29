package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import cl.bcs.plataforma.service.pla.SaldoFlujoCajaService.ObjFLUJOCAJA;
import cl.bcs.plataforma.service.pla.SaldoFlujoCajaService.ObjFLUJOCAJADETALLADO;
import cl.bcs.plataforma.service.pla.SaldoFlujoCajaService.ObjPortafolio;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates dates and uso code, resolves clients→portafolios, inserts into
 * ConsultarFlujoCaja process table, then delegates to SaldoFlujoCajaService.
 */
@Service
public class ConsultarFlujoCajaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarFlujoCajaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final SaldoFlujoCajaService saldoFlujoCaja;
    private final String SQL_USOS;
    private final String SQL_INSERT_PORTAFOLIOS_TEMPORAL;
    private final String SQL_DELETE_PORTAFOLIOS_TEMPORAL;
    private final String SQL_PORTAFOLIOS_POR_MONEDA;
    private final String SQL_PORTAFOLIOS_POR_CUENTA_INVERSION;
    private final String SQL_PORTAFOLIOS_POR_CLIENTE;

    public ConsultarFlujoCajaService(NamedParameterJdbcTemplate jdbc, SaldoFlujoCajaService saldoFlujoCaja) {
        this.jdbc = jdbc;
        this.saldoFlujoCaja = saldoFlujoCaja;
        SQL_USOS                        = loadSql("cl/bcs/plataforma/pla/sql/ConsultarFlujoCaja_SelectUso.sql");
        SQL_INSERT_PORTAFOLIOS_TEMPORAL  = loadSql("cl/bcs/plataforma/pla/sql/ConsultarFlujoCaja_InsertTablaTemporalPortafolios.sql");
        SQL_DELETE_PORTAFOLIOS_TEMPORAL  = "DELETE FROM \"BSCL\".\"BSCL.Plataforma.BD::PLA.ConsultarFlujoCaja_Portafolio_Proceso\" WHERE 1=1";
        SQL_PORTAFOLIOS_POR_MONEDA       = loadSql("cl/bcs/plataforma/pla/sql/ConsultarFlujoCaja_SelectPortafoliosPorMoneda.sql");
        SQL_PORTAFOLIOS_POR_CUENTA_INVERSION = loadSql("cl/bcs/plataforma/pla/sql/ConsultarFlujoCaja_SelectPortafoliosPorCuentaInversion.sql");
        SQL_PORTAFOLIOS_POR_CLIENTE      = loadSql("cl/bcs/plataforma/pla/sql/ConsultarFlujoCaja_SelectPortafoliosPorCliente.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ConsultarFlujoCaja"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ConsultarFlujoCaja");
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_FLUJOCAJA", new JsonArray());
        out.add("P_OUT_FLUJOCAJADETALLADO", new JsonArray());
        out.add("P_OUT_MENSAJES", new JsonArray());
        try {
            Long idEmpresa    = params.has("p_IdEmpresa")       ? params.get("p_IdEmpresa").getAsLong()      : 1L;
            Long idUsuario    = params.has("p_IdUsuario")       ? params.get("p_IdUsuario").getAsLong()      : 0L;
            String idModulo   = params.has("p_IdModulo")        ? params.get("p_IdModulo").getAsString()     : "PLA";
            String fechaDesde = params.has("p_FechaDesde")      ? params.get("p_FechaDesde").getAsString()  : "";
            String fechaHasta = params.has("p_FechaHasta")      ? params.get("p_FechaHasta").getAsString()  : "";
            String uso        = params.has("p_Uso") && !params.get("p_Uso").isJsonNull() ? params.get("p_Uso").getAsString() : null;
            String desplegar  = params.has("p_DesplegarEnUI")   && !params.get("p_DesplegarEnUI").isJsonNull()  ? params.get("p_DesplegarEnUI").getAsString()  : "N";
            String agrupar    = params.has("p_AgruparPorOrigen")&& !params.get("p_AgruparPorOrigen").isJsonNull()? params.get("p_AgruparPorOrigen").getAsString(): "N";

            // Validate dates
            if ("0001-01-01".equals(fechaDesde) || "0001-01-01".equals(fechaHasta)) {
                addError(out, "FECHA_NO_VALIDA");
                return out;
            }
            if (Date.valueOf(fechaDesde).compareTo(Date.valueOf(fechaHasta)) > 0) {
                addError(out, "FECHA_NO_VALIDA");
                return out;
            }

            // Validate uso code
            if (uso != null) {
                List<Map<String,Object>> usosRows = jdbc.queryForList(SQL_USOS,
                    new MapSqlParameterSource("Uso", uso));
                if (usosRows.isEmpty()) {
                    addError(out, "USO_FLUJO_CAJA_NO_EXISTE");
                    return out;
                }
            }

            // Collect portafolios and clientes from params
            JsonArray portafoliosJson = params.has("p_PortafolioAConsultar") ? params.getAsJsonArray("p_PortafolioAConsultar") : new JsonArray();
            JsonArray clientesJson    = params.has("p_ClienteAConsultar")    ? params.getAsJsonArray("p_ClienteAConsultar")    : new JsonArray();

            List<ObjPortafolio> portafolios = new ArrayList<>();
            for (JsonElement el : portafoliosJson) {
                JsonObject po = el.getAsJsonObject();
                ObjPortafolio p = new ObjPortafolio();
                p.IdPortafolio = po.has("IdPortafolio") ? po.get("IdPortafolio").getAsLong() : 0L;
                p.IdMoneda     = po.has("IdMoneda") && !po.get("IdMoneda").isJsonNull() ? po.get("IdMoneda").getAsString() : null;
                portafolios.add(p);
            }

            // Resolve clientes → portafolios
            if (clientesJson.size() > 0) {
                List<Long> idClientes = new ArrayList<>();
                for (JsonElement el : clientesJson) {
                    JsonObject cli = el.getAsJsonObject();
                    idClientes.add(cli.has("IdCliente") ? cli.get("IdCliente").getAsLong() : 0L);
                }
                List<Map<String,Object>> cliPortRows = jdbc.queryForList(SQL_PORTAFOLIOS_POR_CLIENTE,
                    new MapSqlParameterSource("IdCliente", idClientes));
                for (Map<String,Object> row : cliPortRows) {
                    ObjPortafolio p = new ObjPortafolio();
                    p.IdPortafolio = ((Number) row.get("IdPortafolio")).longValue();
                    // find moneda from clientesJson matching IdCliente
                    Long idCliRow = ((Number) row.get("IdCliente")).longValue();
                    for (JsonElement el : clientesJson) {
                        JsonObject cli = el.getAsJsonObject();
                        if (cli.has("IdCliente") && cli.get("IdCliente").getAsLong() == idCliRow) {
                            p.IdMoneda = cli.has("IdMoneda") && !cli.get("IdMoneda").isJsonNull() ? cli.get("IdMoneda").getAsString() : null;
                            break;
                        }
                    }
                    portafolios.add(p);
                }
            }

            if (portafolios.isEmpty()) {
                return out;
            }

            // Insert into ConsultarFlujoCaja process table, resolve portafolios with persona info
            List<String> idMonedas = new ArrayList<>();
            for (ObjPortafolio p : portafolios) { if (p.IdMoneda != null && !idMonedas.contains(p.IdMoneda)) idMonedas.add(p.IdMoneda); }
            if (idMonedas.isEmpty()) idMonedas.add("");

            jdbc.update(SQL_DELETE_PORTAFOLIOS_TEMPORAL, new MapSqlParameterSource());
            MapSqlParameterSource[] batchPort = portafolios.stream()
                .map(p -> new MapSqlParameterSource()
                    .addValue("IdPortafolio", p.IdPortafolio)
                    .addValue("IdMoneda", p.IdMoneda))
                .toArray(MapSqlParameterSource[]::new);
            jdbc.batchUpdate(SQL_INSERT_PORTAFOLIOS_TEMPORAL, batchPort);

            MapSqlParameterSource portParams = new MapSqlParameterSource()
                .addValue("IdMoneda", idMonedas)
                .addValue("FiltroMonedaValido", idMonedas.contains("") ? 1 : 0);

            List<Map<String,Object>> resolvedRows = jdbc.queryForList(
                "CUENTA_INVERSION".equals(uso) ? SQL_PORTAFOLIOS_POR_CUENTA_INVERSION : SQL_PORTAFOLIOS_POR_MONEDA,
                portParams);

            jdbc.update(SQL_DELETE_PORTAFOLIOS_TEMPORAL, new MapSqlParameterSource());

            // Build final portafolios list for SaldoFlujoCaja
            List<ObjPortafolio> saldoPortafolios = new ArrayList<>();
            for (ObjPortafolio req : portafolios) {
                if (req.IdMoneda == null) {
                    for (Map<String,Object> row : resolvedRows) {
                        ObjPortafolio p2 = new ObjPortafolio();
                        p2.IdPortafolio = ((Number) row.get("IdPortafolio")).longValue();
                        p2.IdMoneda = (String) row.get("IdMoneda");
                        saldoPortafolios.add(p2);
                    }
                    break;
                } else {
                    for (Map<String,Object> row : resolvedRows) {
                        Long rowPort = ((Number) row.get("IdPortafolio")).longValue();
                        String rowMon = (String) row.get("IdMoneda");
                        if (rowPort.equals(req.IdPortafolio) && req.IdMoneda.equals(rowMon)) {
                            ObjPortafolio p2 = new ObjPortafolio();
                            p2.IdPortafolio = rowPort; p2.IdMoneda = rowMon;
                            saldoPortafolios.add(p2);
                            break;
                        }
                    }
                }
            }

            // Build params for SaldoFlujoCajaService
            JsonObject saldoParams = new JsonObject();
            saldoParams.addProperty("P_IDEMPRESA", idEmpresa);
            saldoParams.addProperty("P_FECHADESDE", fechaDesde);
            saldoParams.addProperty("P_FECHAHASTA", fechaHasta);
            if (uso != null) saldoParams.addProperty("P_USO", uso);
            else saldoParams.add("P_USO", JsonNull.INSTANCE);
            saldoParams.addProperty("P_DESPLEGARENUI", desplegar);
            saldoParams.addProperty("P_AGRUPARPORORIGEN", agrupar);
            JsonArray pfArray = new JsonArray();
            for (ObjPortafolio p : saldoPortafolios) {
                JsonObject po = new JsonObject();
                po.addProperty("IdPortafolio", p.IdPortafolio);
                if (p.IdMoneda != null) po.addProperty("IdMoneda", p.IdMoneda);
                else po.add("IdMoneda", JsonNull.INSTANCE);
                pfArray.add(po);
            }
            saldoParams.add("P_PORTAFOLIOACONSULTAR", pfArray);

            JsonObject saldoOut = saldoFlujoCaja.execute(saldoParams);

            // Transfer saldo results into our output
            out.addProperty("P_OUT_ERRORESCONTADOR", saldoOut.has("P_OUT_ERRORESCONTADOR") ? saldoOut.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L);
            out.add("P_OUT_FLUJOCAJA",       saldoOut.has("P_OUT_FLUJOCAJA")       ? saldoOut.get("P_OUT_FLUJOCAJA")       : new JsonArray());
            out.add("P_OUT_FLUJOCAJADETALLADO", saldoOut.has("P_OUT_FLUJOCAJADETALLADO") ? saldoOut.get("P_OUT_FLUJOCAJADETALLADO") : new JsonArray());

        } catch (Exception e) {
            log.error("Error en ConsultarFlujoCaja: {}", e.getMessage(), e);
            out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            try { jdbc.update(SQL_DELETE_PORTAFOLIOS_TEMPORAL, new MapSqlParameterSource()); } catch (Exception ignored) {}
        }
        log.info("FIN::ConsultarFlujoCaja");
        return out;
    }

    private void addError(JsonObject out, String codigo) {
        out.addProperty("P_OUT_ERRORESCONTADOR", out.get("P_OUT_ERRORESCONTADOR").getAsLong() + 1);
        JsonObject msg = new JsonObject();
        msg.addProperty("tipo", "ERROR"); msg.addProperty("codigo", codigo);
        out.getAsJsonArray("P_OUT_MENSAJES").add(msg);
    }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }
}
