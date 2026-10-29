package cl.bcs.canalagente.adapter.out;

import cl.bcs.canalagente.domain.HistorialMovimientosRequest;
import cl.bcs.canalagente.domain.HistorialMovimientosResponse;
import cl.bcs.canalagente.domain.MovimientoCuentaInversion;
import cl.bcs.canalagente.port.out.CanalAgenteDataPort;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC adapter for CanalAgenteDataPort.
 *
 * findIdClienteByPortafolio: queries the HANA Portafolio table (test schema uses PORTAFOLIO_T).
 *
 * consultarHistorialCompleto:
 *  1. Calls BcsCuentaInversionServicios /internal/java-call → ConsultarCartolaDeMovimiento
 *  2. Batch-inserts cartola rows into the shared HANA process table
 *  3. Runs a multi-table JOIN to enrich with comprobante/voucher data
 *  4. Deletes the process-table rows (always, even on error)
 *
 * The process table is not session-scoped in HANA, so steps 2–4 are wrapped in a
 * static lock to prevent interleaving across concurrent requests.
 */
@Repository
public class CanalAgenteRepositoryAdapter implements CanalAgenteDataPort {

    private static final Logger log = LoggerFactory.getLogger(CanalAgenteRepositoryAdapter.class);

    private static final Object PROCESS_TABLE_LOCK = new Object();

    private static final String SQL_FIND_CLIENTE = """
            SELECT ID_CLIENTE
            FROM PORTAFOLIO_T
            WHERE ID_PORTAFOLIO = :idPortafolio
            LIMIT 1
            """;

    private static final String SQL_INSERT_PROCESO = """
            INSERT INTO "BSCL.CanalAgente.BD::ConsultarHistorialMovimientosCuentaInversion_Proceso"
                ("IdCargoAbonoMovimiento", "IdPortafolio", "IdComprobante")
            VALUES (:IdCargoAbonoMovimiento, :IdPortafolio, :IdComprobante)
            """;

    private static final String SQL_SELECT_COMPROBANTE = """
            SELECT
                chm."IdCargoAbonoMovimiento",
                TO_VARCHAR(fac."FolioComprobante")  AS "FolioComprobante",
                por."CodigoPortafolio",
                fac."IdRespuestaDTE",
                fac."RespuestaPdfDTE",
                ident."DocumentoIdentidad"
            FROM "BSCL.CanalAgente.BD::ConsultarHistorialMovimientosCuentaInversion_Proceso" chm
            LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" por
                ON por."IdPortafolio" = chm."IdPortafolio"
            LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli
                ON cli."IdCliente" = por."IdCliente"
            LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" per
                ON per."IdPersona" = cli."IdPersona"
            LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" ident
                ON per."IdDocumentoIdentidad" = ident."IdDocumentoIdentidad"
            LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" fac
                ON fac."IdComprobante" = chm."IdComprobante"
            """;

    private static final String SQL_DELETE_PROCESO =
            "DELETE FROM \"BSCL.CanalAgente.BD::ConsultarHistorialMovimientosCuentaInversion_Proceso\"";

    private final NamedParameterJdbcTemplate jdbc;
    private final RestClient restClient;

    public CanalAgenteRepositoryAdapter(
            NamedParameterJdbcTemplate jdbc,
            @Value("${bcs.cuenta-inversion.url:http://bcs-cuenta-inversion-servicios:8080}") String cuentaInversionUrl) {
        this.jdbc = jdbc;
        this.restClient = RestClient.builder().baseUrl(cuentaInversionUrl).build();
    }

    /**
     * Legacy SQL:
     * SELECT TOP 1 "IdCliente" FROM "BSCL.Portafolio.BD::POR.Portafolio"
     * WHERE "IdPortafolio" = :IdPortafolio
     */
    @Override
    public Optional<Long> findIdClienteByPortafolio(Long idPortafolio) {
        var params = new MapSqlParameterSource("idPortafolio", idPortafolio);
        List<Long> results = jdbc.query(SQL_FIND_CLIENTE, params,
                (rs, rowNum) -> rs.getLong("ID_CLIENTE"));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    @Transactional
    public HistorialMovimientosResponse consultarHistorialCompleto(
            HistorialMovimientosRequest request, Long idCliente) {

        // Step 1 — retrieve cartola data from BcsCuentaInversionServicios
        JsonObject cartolaResult = callConsultarCartolaDeMovimiento(request, idCliente);

        BigDecimal saldoInicial = getBigDecimal(cartolaResult, "P_SALDOEFECTIVOINICIAL");
        BigDecimal saldoFinal   = getBigDecimal(cartolaResult, "P_SALDOEFECTIVOFINAL");

        JsonArray cartolas = cartolaResult.has("P_OUT_CARTOLADEMOVIMIENTO")
                && cartolaResult.get("P_OUT_CARTOLADEMOVIMIENTO").isJsonArray()
                ? cartolaResult.getAsJsonArray("P_OUT_CARTOLADEMOVIMIENTO")
                : new JsonArray();

        if (cartolas.isEmpty()) {
            return new HistorialMovimientosResponse(
                    saldoInicial, saldoFinal, List.of(), 0L, List.of(), 0L, List.of());
        }

        // Steps 2–4 touch a shared HANA table — serialise across threads
        synchronized (PROCESS_TABLE_LOCK) {
            // Step 2 — batch INSERT into process table
            jdbc.batchUpdate(SQL_INSERT_PROCESO, buildInsertBatch(cartolas));

            try {
                // Step 3 — JOIN to enrich with comprobante data
                Map<Long, Map<String, Object>> comprobanteMap = jdbc
                        .getJdbcTemplate()
                        .queryForList(SQL_SELECT_COMPROBANTE)
                        .stream()
                        .collect(Collectors.toMap(
                                r -> toLong(r.get("IdCargoAbonoMovimiento")),
                                r -> r,
                                (a, b) -> a));

                List<MovimientoCuentaInversion> movimientos =
                        buildMovimientos(cartolas, comprobanteMap);

                return new HistorialMovimientosResponse(
                        saldoInicial, saldoFinal,
                        movimientos, 0L, List.of(), 0L, List.of());

            } finally {
                // Step 4 — always clean up the shared process table
                jdbc.getJdbcTemplate().execute(SQL_DELETE_PROCESO);
            }
        }
    }

    // ─── Private helpers ────────────────────────────────────────────────────────

    private JsonObject callConsultarCartolaDeMovimiento(HistorialMovimientosRequest req, Long idCliente) {
        JsonObject params = new JsonObject();
        params.addProperty("p_IdEmpresa",             req.idEmpresa());
        params.addProperty("p_IdUsuario",             req.idUsuario());
        params.addProperty("p_IdModulo",              req.idModulo());
        params.addProperty("p_UsaReglasNegocio",      "S");
        params.add        ("p_IdCuentaInversion",     JsonNull.INSTANCE);
        params.addProperty("p_IdCliente",             idCliente);
        params.addProperty("p_IdPortafolio",          req.idPortafolio());
        params.addProperty("p_IdMoneda",              req.idMoneda());
        params.add        ("p_IdEstado",              JsonNull.INSTANCE);
        params.addProperty("p_FechaDesde",            req.fechaDesde());
        params.addProperty("p_FechaHasta",            req.fechaHasta());
        params.addProperty("p_SaldoPorFechaEfectiva", "N");
        if (req.idMatrizAgrupador() != null)
            params.addProperty("p_IdMatrizAgrupador", req.idMatrizAgrupador());
        else
            params.add("p_IdMatrizAgrupador", JsonNull.INSTANCE);
        if (req.esADC() != null)
            params.addProperty("p_EsADC", req.esADC());
        else
            params.add("p_EsADC", JsonNull.INSTANCE);

        JsonObject payload = new JsonObject();
        payload.addProperty("class", "cl.bcs.cuentaInversion.ConsultarCartolaDeMovimiento");
        payload.add("params", params);

        log.debug("Calling BcsCuentaInversionServicios /internal/java-call idPortafolio={}",
                req.idPortafolio());

        String raw = restClient.post()
                .uri("/internal/java-call")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload.toString())
                .retrieve()
                .body(String.class);

        return JsonParser.parseString(raw != null ? raw : "{}").getAsJsonObject();
    }

    private static MapSqlParameterSource[] buildInsertBatch(JsonArray cartolas) {
        MapSqlParameterSource[] batch = new MapSqlParameterSource[cartolas.size()];
        for (int i = 0; i < cartolas.size(); i++) {
            JsonObject c = cartolas.get(i).getAsJsonObject();
            batch[i] = new MapSqlParameterSource()
                    .addValue("IdCargoAbonoMovimiento", getJsonLong(c, "IdCargoAbonoMovimiento"))
                    .addValue("IdPortafolio",           getJsonLong(c, "IdPortafolio"))
                    .addValue("IdComprobante",          getJsonLong(c, "IdComprobante"));
        }
        return batch;
    }

    private static List<MovimientoCuentaInversion> buildMovimientos(
            JsonArray cartolas, Map<Long, Map<String, Object>> comprobanteMap) {
        List<MovimientoCuentaInversion> list = new ArrayList<>(cartolas.size());
        for (JsonElement el : cartolas) {
            JsonObject c = el.getAsJsonObject();
            Long id      = getJsonLong(c, "IdCargoAbonoMovimiento");
            Map<String, Object> comp = comprobanteMap.getOrDefault(id, Map.of());

            list.add(new MovimientoCuentaInversion(
                    id,
                    getJsonString    (c, "NombreCliente"),
                    getJsonLong      (c, "IdCliente"),
                    getJsonLong      (c, "IdPortafolio"),
                    parseDate        (c, "FechaSistema"),
                    parseDate        (c, "FechaLiquidacionOperacion"),
                    getJsonString    (c, "IdMoneda"),
                    getJsonString    (c, "IdConcepto"),
                    getJsonString    (c, "NombreConcepto"),
                    getJsonBigDecimal(c, "Cargo"),
                    getJsonBigDecimal(c, "Abono"),
                    (String) comp.get("FolioComprobante"),
                    getJsonString    (c, "IdEstado"),
                    getJsonString    (c, "NombreEstado"),
                    getJsonString    (c, "NombreTipoComprobante"),
                    getJsonString    (c, "NombreTipoProducto"),
                    getJsonString    (c, "NombreTipoOperacion"),
                    getJsonString    (c, "NombreTipoMovimiento"),
                    getJsonBigDecimal(c, "SaldoSimulado"),
                    (String) comp.get("CodigoPortafolio"),
                    getJsonLong      (c, "IdComprobante"),
                    toLong(comp.get("IdRespuestaDTE")),
                    (String) comp.get("RespuestaPdfDTE"),
                    (String) comp.get("DocumentoIdentidad"),
                    getJsonLong      (c, "IdMatrizAgrupador"),
                    getJsonString    (c, "EsADC")
            ));
        }
        return list;
    }

    // ─── JSON / type helpers ────────────────────────────────────────────────────

    private static String getJsonString(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return (e == null || e.isJsonNull()) ? null : e.getAsString();
    }

    private static Long getJsonLong(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return (e == null || e.isJsonNull()) ? null : e.getAsLong();
    }

    private static BigDecimal getJsonBigDecimal(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return (e == null || e.isJsonNull()) ? null : e.getAsBigDecimal();
    }

    private static BigDecimal getBigDecimal(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return (e == null || e.isJsonNull()) ? null : e.getAsBigDecimal();
    }

    private static LocalDate parseDate(JsonObject o, String key) {
        String s = getJsonString(o, key);
        if (s == null) return null;
        return LocalDate.parse(s.length() > 10 ? s.substring(0, 10) : s);
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Long l) return l;
        if (v instanceof Number n) return n.longValue();
        return null;
    }
}
