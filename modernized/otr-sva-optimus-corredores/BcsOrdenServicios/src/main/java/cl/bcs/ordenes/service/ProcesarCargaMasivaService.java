package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.RequisitosBasicosUtil;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Migrated from legacy cl.bcs.ordenes.ProcesarCargaMasiva (JavaCall).
 * Processes bulk loads of orders and instructions from staging table,
 * delegating to CrearOrdenesMasivaService and ProcesarCargaMasivaInstruccionService.
 */
@Service
public class ProcesarCargaMasivaService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCargaMasivaService.class);
    private static final Gson GSON = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final CrearOrdenesMasivaService crearOrdenesMasivaService;
    private final ProcesarCargaMasivaInstruccionService procesarInstruccionService;

    private final String SQL_SELECT_CARGA_MASIVA;
    private final String SQL_SELECT_CARGA_MASIVA_DETALLE;
    private final String SQL_UPDATE_CARGA_MASIVA;
    private final String SQL_UPDATE_CARGA_MASIVA_DETALLE;

    public ProcesarCargaMasivaService(NamedParameterJdbcTemplate jdbc,
                                       RequisitosBasicosUtil requisitosUtil,
                                       CrearOrdenesMasivaService crearOrdenesMasivaService,
                                       ProcesarCargaMasivaInstruccionService procesarInstruccionService) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.crearOrdenesMasivaService = crearOrdenesMasivaService;
        this.procesarInstruccionService = procesarInstruccionService;
        this.SQL_SELECT_CARGA_MASIVA        = loadSql("cl/bcs/ordenes/sql/ProcesarCargaMasiva_SelectCargaMasiva.sql");
        this.SQL_SELECT_CARGA_MASIVA_DETALLE = loadSql("cl/bcs/ordenes/sql/ProcesarCargaMasiva_SelectCargaMasivaDetalle.sql");
        this.SQL_UPDATE_CARGA_MASIVA        = loadSql("cl/bcs/ordenes/sql/ProcesarCargaMasiva_UpdateCargaMasiva.sql");
        this.SQL_UPDATE_CARGA_MASIVA_DETALLE = loadSql("cl/bcs/ordenes/sql/ProcesarCargaMasiva_UpdateCargaMasivaDetalle.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("ProcesarCargaMasivaService.execute()");
        try {
            Long idEmpresa   = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario   = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo  = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            String simular   = params.has("p_SimularOperacion") ? params.get("p_SimularOperacion").getAsString() : "N";
            String usaReglas = params.has("p_UsaReglasNegocio") ? params.get("p_UsaReglasNegocio").getAsString() : "S";
            String fechaProceso = params.has("p_FechaProceso") ? params.get("p_FechaProceso").getAsString() : "";
            JsonArray cargasIn = params.has("p_CargaMasivaProcesar") ? params.get("p_CargaMasivaProcesar").getAsJsonArray() : new JsonArray();

            // Validate basic requirements
            RequisitosBasicosUtil.Salida rb = requisitosUtil.validar(idEmpresa, idUsuario, idModulo);
            if (rb.contarErrores() > 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", rb.contarErrores());
                out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(rb.p_out_Mensajes)));
                return out;
            }

            if (cargasIn.size() == 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"tipo\":\"ERROR\",\"codigo\":\"NO_SE_ENVIARON_CARGAS\"}]"));
                return out;
            }

            // Build map of carga → metadata
            Map<Long, JsonObject> hashCargasMeta = new HashMap<>();
            List<Long> idsCargasMasivas = new ArrayList<>();
            for (var el : cargasIn) {
                JsonObject c = el.getAsJsonObject();
                Long id = c.has("IdCargaMasiva") ? c.get("IdCargaMasiva").getAsLong() : null;
                if (id != null) {
                    idsCargasMasivas.add(id);
                    hashCargasMeta.put(id, c);
                }
            }

            // Load CargaMasiva records
            List<Map<String, Object>> lstCargaMasiva = jdbc.queryForList(SQL_SELECT_CARGA_MASIVA,
                new MapSqlParameterSource()
                    .addValue("IdsCargasMasivas", idsCargasMasivas)
                    .addValue("FechaProceso", fechaProceso));

            JsonArray mensajes = new JsonArray();
            List<Map<String, Object>> cargasValidas = new ArrayList<>();
            for (Map<String, Object> carga : lstCargaMasiva) {
                String estado = getStr(carga, "IdEstado");
                Long idCarga = toLong(carga.get("IdCargaMasiva"));
                if ("PRO".equalsIgnoreCase(estado)) {
                    JsonObject m = new JsonObject();
                    m.addProperty("id", idCarga);
                    m.addProperty("tipo", "ERROR");
                    m.addProperty("codigo", "CARGA_MASIVA_PROCESADA");
                    mensajes.add(m);
                } else if ("INV".equalsIgnoreCase(estado)) {
                    JsonObject m = new JsonObject();
                    m.addProperty("id", idCarga);
                    m.addProperty("tipo", "ERROR");
                    m.addProperty("codigo", "CARGA_MASIVA_INVALIDA");
                    mensajes.add(m);
                } else {
                    cargasValidas.add(carga);
                }
            }

            // Check errors before proceeding
            long errores = 0;
            for (var el : mensajes) {
                if ("ERROR".equals(el.getAsJsonObject().has("tipo") ? el.getAsJsonObject().get("tipo").getAsString() : ""))
                    errores++;
            }
            if (errores > 0 || cargasValidas.isEmpty()) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", errores);
                out.add("P_OUT_MENSAJES", mensajes);
                return out;
            }

            // Load detail records
            List<Long> idsValidos = new ArrayList<>();
            for (Map<String, Object> c : cargasValidas) {
                Long id = toLong(c.get("IdCargaMasiva"));
                if (id != null) idsValidos.add(id);
            }

            List<Map<String, Object>> lstDetalle = jdbc.queryForList(SQL_SELECT_CARGA_MASIVA_DETALLE,
                new MapSqlParameterSource("IdsCargasMasivas", idsValidos));

            // Process ORDER-type details
            JsonArray detalleOrdenes = new JsonArray();
            for (Map<String, Object> det : lstDetalle) {
                String tipo = getStr(det, "CodigoTipoCargaMasiva");
                if (!"CARGA_INGRESO_ORDEN_MASIVO".equals(tipo)) continue;
                Long idCargaDet = toLong(det.get("IdCargaMasiva"));
                JsonObject meta = hashCargasMeta.get(idCargaDet);

                JsonObject d = new JsonObject();
                d.addProperty("IdInterno", toLong(det.get("IdCargaMasivaDetalle")));
                d.addProperty("DocumentoIdentidad", getStr(det, "DocumentoIdentidad"));
                d.addProperty("CodigoPortafolio", getStr(det, "CodigoPortafolio"));
                d.addProperty("TipoOperacion", getStr(det, "TipoOperacion"));
                d.addProperty("IdMecanismoCaptacion", getStr(det, "IdMecanismoCaptacion"));
                d.addProperty("Cantidad", getBD(det, "Cantidad"));
                d.addProperty("Nemotecnico", getStr(det, "Nemotecnico"));
                d.addProperty("Precio", getBD(det, "Precio"));
                d.addProperty("IdCondicionDeLiquidacion", getStr(det, "IdCondicionDeLiquidacion"));
                d.addProperty("IdTipoVigenciaOrden", getStr(det, "IdTipoVigenciaOrden"));
                d.addProperty("FechaVigencia", getStr(det, "FechaVigencia"));
                d.addProperty("IdFormaCustodia", getStr(det, "IdFormaCustodia"));
                d.addProperty("IdBolsa", getStr(det, "IdBolsa"));
                d.addProperty("IdBoveda", getStr(det, "IdBoveda"));
                d.addProperty("IdTipoPrecioOrden", getStr(det, "IdTipoPrecioOrden"));
                d.addProperty("IdPersona", toLong(det.get("IdPersona")));
                d.addProperty("IdPortafolio", toLong(det.get("IdPortafolio")));
                if (meta != null) {
                    d.addProperty("IdTipoCanalDistribucion", meta.has("CanalDistribucion") ? meta.get("CanalDistribucion").getAsString() : "MAN");
                    d.addProperty("Negociacion", meta.has("Negociacion") ? meta.get("Negociacion").getAsString() : "CTD");
                    d.addProperty("UsarPlantillaCobros", meta.has("UsarPlantillaCobros") ? meta.get("UsarPlantillaCobros").getAsString() : "S");
                    d.addProperty("FechaOperacion", meta.has("FechaOperacion") ? meta.get("FechaOperacion").getAsString() : "0001-01-01");
                }
                detalleOrdenes.add(d);
            }

            List<Map<String, Object>> resultadosProceso = new ArrayList<>();

            if (detalleOrdenes.size() > 0) {
                JsonObject crearParams = new JsonObject();
                crearParams.addProperty("p_IdEmpresa", idEmpresa);
                crearParams.addProperty("p_IdUsuario", idUsuario);
                crearParams.addProperty("p_IdModulo", idModulo);
                crearParams.addProperty("p_SimularOperacion", simular);
                crearParams.addProperty("p_UsaReglasNegocio", usaReglas);
                crearParams.add("p_DetalleOrden", detalleOrdenes);
                JsonObject crearResult = crearOrdenesMasivaService.execute(crearParams);

                errores = crearResult.has("P_OUT_ERRORESCONTADOR") ? crearResult.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
                if (crearResult.has("P_OUT_MENSAJES")) {
                    for (var m : crearResult.get("P_OUT_MENSAJES").getAsJsonArray()) mensajes.add(m);
                }
                if (errores == 0 && crearResult.has("P_OUT_RESULTADO")) {
                    for (var r : crearResult.get("P_OUT_RESULTADO").getAsJsonArray()) {
                        JsonObject res = r.getAsJsonObject();
                        Map<String, Object> rp = new HashMap<>();
                        rp.put("IdInterno", res.has("IdInterno") ? res.get("IdInterno").getAsLong() : null);
                        rp.put("IdOperacionGenerada", res.has("IdOrden") ? res.get("IdOrden").getAsLong() : null);
                        rp.put("Mensaje", "Orden creada: " + (res.has("IdOrden") ? res.get("IdOrden").getAsLong() : ""));
                        resultadosProceso.add(rp);
                    }
                }
            }

            // Process INSTRUCTION-type details
            JsonArray detalleInstrucciones = new JsonArray();
            for (Map<String, Object> det : lstDetalle) {
                String tipo = getStr(det, "CodigoTipoCargaMasiva");
                if (!"CARGA_INGRESO_INSTRUCCIONES_MASIVA".equals(tipo)) continue;
                JsonObject d = new JsonObject();
                d.addProperty("IdInterno", toLong(det.get("IdCargaMasivaDetalle")));
                d.addProperty("TipoInstruccion", getStr(det, "TipoInstruccion"));
                d.addProperty("Operacion", getStr(det, "TipoOperacion"));
                d.addProperty("Nemotecnico", getStr(det, "Nemotecnico"));
                d.addProperty("IdEmisor", getStr(det, "Emisor"));
                d.addProperty("FechaVencimiento", getStr(det, "FechaVencimiento"));
                d.addProperty("Tasa", getBD(det, "Tasa"));
                d.addProperty("ValorRescate", getBD(det, "ValorRescate"));
                d.addProperty("FechaTrade", getStr(det, "FechaTrade"));
                d.addProperty("CondicionLiquidacion", getStr(det, "IdCondicionDeLiquidacion"));
                d.addProperty("Intermediario", toLong(det.get("Intermediario")) != null ? toLong(det.get("Intermediario")) : 0L);
                d.addProperty("NumeroFactura", getStr(det, "FolioFactura"));
                d.addProperty("Cantidad", getBD(det, "Cantidad"));
                d.addProperty("MontoTotal", getBD(det, "MontoLiquidacion"));
                d.addProperty("TotalCobros", getBD(det, "TotalCobros"));
                d.addProperty("IdTipoReajuste", getStr(det, "IdTipoReajuste"));
                d.addProperty("Precio", getBD(det, "Precio"));
                d.addProperty("DocumentoIdentidad", getStr(det, "DocumentoIdentidad"));
                d.addProperty("CodigoPortafolio", getStr(det, "CodigoPortafolio"));
                detalleInstrucciones.add(d);
            }

            if (detalleInstrucciones.size() > 0) {
                try {
                    JsonObject instrParams = new JsonObject();
                    instrParams.addProperty("p_IdEmpresa", idEmpresa);
                    instrParams.addProperty("p_IdUsuario", idUsuario);
                    instrParams.addProperty("p_IdModulo", idModulo);
                    instrParams.addProperty("p_UsaReglasNegocio", usaReglas);
                    instrParams.addProperty("p_IdTipoCanalIngreso", "AGE");
                    instrParams.add("p_Instrucciones", detalleInstrucciones);
                    JsonObject instrResult = procesarInstruccionService.execute(instrParams);

                    errores = instrResult.has("P_OUT_ERRORESCONTADOR") ? instrResult.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
                    if (instrResult.has("P_OUT_MENSAJES")) {
                        for (var m : instrResult.get("P_OUT_MENSAJES").getAsJsonArray()) mensajes.add(m);
                    }
                } catch (Exception e) {
                    log.warn("ProcesarCargaMasivaInstruccion error (non-fatal): {}", e.getMessage());
                }
            }

            // Update CargaMasiva and CargaMasivaDetalle status
            for (Map<String, Object> carga : cargasValidas) {
                try {
                    jdbc.update(SQL_UPDATE_CARGA_MASIVA,
                        new MapSqlParameterSource()
                            .addValue("IdCargaMasiva", toLong(carga.get("IdCargaMasiva")))
                            .addValue("IdEstado", errores > 0 ? "ERR" : "PRO"));
                } catch (Exception e) {
                    log.warn("Error updating CargaMasiva: {}", e.getMessage());
                }
            }
            for (Map<String, Object> rp : resultadosProceso) {
                try {
                    jdbc.update(SQL_UPDATE_CARGA_MASIVA_DETALLE,
                        new MapSqlParameterSource()
                            .addValue("IdInterno", rp.get("IdInterno"))
                            .addValue("IdOperacionGenerada", rp.get("IdOperacionGenerada"))
                            .addValue("Mensaje", rp.get("Mensaje")));
                } catch (Exception e) {
                    log.warn("Error updating CargaMasivaDetalle: {}", e.getMessage());
                }
            }

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", errores);
            out.add("P_OUT_MENSAJES", mensajes);
            return out;
        } catch (Exception e) {
            log.error("ProcesarCargaMasivaService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String getStr(Map<String, Object> m, String key) {
        Object v = m == null ? null : m.get(key);
        return v != null ? v.toString() : "";
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static BigDecimal getBD(Map<String, Object> m, String key) {
        Object v = m == null ? null : m.get(key);
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static String loadSql(String path) {
        try (var is = ProcesarCargaMasivaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
