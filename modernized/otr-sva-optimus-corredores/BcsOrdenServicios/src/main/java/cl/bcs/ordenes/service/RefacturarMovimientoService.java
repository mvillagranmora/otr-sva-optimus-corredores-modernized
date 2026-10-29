package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.FacturacionCallClient;
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
 * Migrated from legacy cl.bcs.ordenes.RefacturarMovimiento (JavaCall).
 * Retrieves refacturation details, cancels existing comprobantes (DTE notes),
 * and re-invoices via BcsFacturacionServicios.
 */
@Service
public class RefacturarMovimientoService {

    private static final Logger log = LoggerFactory.getLogger(RefacturarMovimientoService.class);
    private static final Gson GSON = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final ConfiguracionSistemaUtil configUtil;
    private final ObtenerDetalleRefacturacionService obtenerDetalleService;
    private final EnviarAsignacionesFacturarService enviarAsigService;
    private final FacturacionCallClient facturacionClient;

    private final String SQL_SELECT_TRANSACCIONES;
    private final String SQL_UPDATE_ASIGNACIONES;
    private final String SQL_UPDATE_ASIGNACIONES_ENV_FAC;

    public RefacturarMovimientoService(NamedParameterJdbcTemplate jdbc,
                                        RequisitosBasicosUtil requisitosUtil,
                                        ConfiguracionSistemaUtil configUtil,
                                        ObtenerDetalleRefacturacionService obtenerDetalleService,
                                        EnviarAsignacionesFacturarService enviarAsigService,
                                        FacturacionCallClient facturacionClient) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.configUtil = configUtil;
        this.obtenerDetalleService = obtenerDetalleService;
        this.enviarAsigService = enviarAsigService;
        this.facturacionClient = facturacionClient;
        this.SQL_SELECT_TRANSACCIONES    = loadSql("cl/bcs/ordenes/sql/RefacturarMovimiento_SelectTransacciones.sql");
        this.SQL_UPDATE_ASIGNACIONES     = loadSql("cl/bcs/ordenes/sql/RefacturarMovimiento_UpdateAsignacion.sql");
        this.SQL_UPDATE_ASIGNACIONES_ENV_FAC = loadSql("cl/bcs/ordenes/sql/RefacturarMovimiento_UpdateAsignacionEnviadaFacturar.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("RefacturarMovimientoService.execute()");
        try {
            Long idEmpresa   = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario   = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo  = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            String esEjecucionBatch = params.has("p_EsEjecucionBatch") ? params.get("p_EsEjecucionBatch").getAsString() : "N";
            String usaReglas = params.has("p_UsaReglasNegocio") ? params.get("p_UsaReglasNegocio").getAsString() : "S";
            String origen    = params.has("p_Origen") ? params.get("p_Origen").getAsString() : "";
            JsonArray asigIn = params.has("p_Asignaciones") ? params.get("p_Asignaciones").getAsJsonArray() : new JsonArray();
            JsonArray entidadesIn = params.has("p_Entidades") ? params.get("p_Entidades").getAsJsonArray() : new JsonArray();

            JsonArray mensajes = new JsonArray();

            // Validate basic requirements
            RequisitosBasicosUtil.Salida rb = requisitosUtil.validar(idEmpresa, idUsuario, idModulo);
            if (rb.contarErrores() > 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", rb.contarErrores());
                out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(rb.p_out_Mensajes)));
                return out;
            }

            if (asigIn.size() == 0 && entidadesIn.size() == 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"tipo\":\"ERROR\",\"codigo\":\"NO_SE_ENVIARON_ASIGNACIONES\"}]"));
                return out;
            }

            // Collect transaction IDs for reservation check
            List<Long> idsTransaccion = new ArrayList<>();
            for (var el : asigIn) {
                JsonObject a = el.getAsJsonObject();
                Long idTx = a.has("IdTransaccion") ? a.get("IdTransaccion").getAsLong() : null;
                if (idTx != null && !idsTransaccion.contains(idTx)) idsTransaccion.add(idTx);
            }

            List<Map<String, Object>> infoTransacciones = new ArrayList<>();
            if (!idsTransaccion.isEmpty()) {
                infoTransacciones = jdbc.queryForList(SQL_SELECT_TRANSACCIONES,
                    new MapSqlParameterSource("idsTransaccion", idsTransaccion));
            }

            // Validate transaction reservation
            for (Map<String, Object> tran : infoTransacciones) {
                String reservado = getStr(tran, "Reservado");
                Long idUsuarioReserva = toLong(tran.get("IdUsuarioReserva"));
                if ("S".equalsIgnoreCase(reservado) && !Objects.equals(idUsuarioReserva, idUsuario)) {
                    JsonObject m = new JsonObject();
                    m.addProperty("id", toLong(tran.get("IdTransaccion")));
                    m.addProperty("tipo", "ERROR");
                    m.addProperty("codigo", "TRANSACCION_RESERVADA_POR_OTRO_USAURIO");
                    mensajes.add(m);
                }
            }

            long errores = 0;
            for (var el : mensajes) errores += "ERROR".equals(el.getAsJsonObject().get("tipo").getAsString()) ? 1 : 0;
            if (errores > 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", errores);
                out.add("P_OUT_MENSAJES", mensajes);
                return out;
            }

            // Get refacturation detail if entidades are provided
            List<Map<String, Object>> detalleAsignaciones = new ArrayList<>();
            List<Map<String, Object>> detalleComprobantes = new ArrayList<>();
            if (entidadesIn.size() > 0) {
                JsonObject detParams = new JsonObject();
                detParams.addProperty("p_IdEmpresa", idEmpresa);
                detParams.addProperty("p_IdModulo", idModulo);
                detParams.addProperty("p_IdUsuario", idUsuario);
                detParams.addProperty("p_Origen", origen);
                detParams.add("p_Entidades", entidadesIn);
                JsonObject detResult = obtenerDetalleService.execute(detParams);

                errores = detResult.has("P_OUT_ERRORESCONTADOR") ? detResult.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
                if (detResult.has("P_OUT_MENSAJES")) {
                    for (var m : detResult.get("P_OUT_MENSAJES").getAsJsonArray()) mensajes.add(m);
                }
                if (errores > 0) {
                    JsonObject out = new JsonObject();
                    out.addProperty("P_OUT_ERRORESCONTADOR", errores);
                    out.add("P_OUT_MENSAJES", mensajes);
                    return out;
                }
            }

            // Get localizacion to determine if CL
            String localizacionSistema = "CL";
            try {
                localizacionSistema = configUtil.getParametroString(idEmpresa, "PLA.LocalizacionSistema");
            } catch (Exception e) {
                log.warn("Could not get LocalizacionSistema: {}", e.getMessage());
            }

            // Cancel DTE comprobantes via BcsFacturacionServicios
            if ("CL".equalsIgnoreCase(localizacionSistema) && !detalleComprobantes.isEmpty()) {
                try {
                    JsonArray comprobantesAnular = new JsonArray();
                    for (Map<String, Object> c : detalleComprobantes) {
                        if ("ENV".equalsIgnoreCase(getStr(c, "IdEstadoComprobante"))) {
                            JsonObject comprobante = new JsonObject();
                            comprobante.addProperty("IdComprobante", toLong(c.get("IdComprobante")));
                            comprobante.addProperty("IdCliente", toLong(c.get("IdCliente")));
                            comprobante.addProperty("IdPortafolio", toLong(c.get("IdPortafolio")));
                            comprobante.addProperty("IdTipoComprobante", getStr(c, "IdTipoComprobante"));
                            comprobantesAnular.add(comprobante);
                        }
                    }
                    if (comprobantesAnular.size() > 0) {
                        JsonObject anularParams = new JsonObject();
                        anularParams.addProperty("p_IdEmpresa", idEmpresa);
                        anularParams.addProperty("p_IdModulo", idModulo);
                        anularParams.addProperty("p_IdUsuario", idUsuario);
                        anularParams.add("P_COMPROBANTE", comprobantesAnular);
                        anularParams.addProperty("p_CrearComprobanteAutomatico", "S");
                        anularParams.addProperty("p_EsIngresoAutomatico", "N");
                        anularParams.addProperty("p_simularOperacion", "N");
                        anularParams.addProperty("p_UsaReglasNegocio", usaReglas);
                        JsonObject anularResult = facturacionClient.call("cl.bcs.facturacion.CrearComprobanteNotaDeCredito", anularParams);
                        errores = anularResult.has("P_OUT_ERRORESCONTADOR") ? anularResult.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
                        if (errores > 0) {
                            if (anularResult.has("P_OUT_MENSAJES")) {
                                for (var m : anularResult.get("P_OUT_MENSAJES").getAsJsonArray()) mensajes.add(m);
                            }
                            JsonObject out = new JsonObject();
                            out.addProperty("P_OUT_ERRORESCONTADOR", errores);
                            out.add("P_OUT_MENSAJES", mensajes);
                            return out;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error cancelling comprobantes: {}", e.getMessage());
                }
            }

            // Update assignments back to state that allows re-invoicing
            if (!asigIn.isEmpty()) {
                List<Long> idsAsig = new ArrayList<>();
                for (var el : asigIn) {
                    JsonObject a = el.getAsJsonObject();
                    Long id = a.has("IdAsignacion") ? a.get("IdAsignacion").getAsLong() : null;
                    if (id != null) idsAsig.add(id);
                }
                for (Long idA : idsAsig) {
                    try {
                        jdbc.update(SQL_UPDATE_ASIGNACIONES,
                            new MapSqlParameterSource("IdAsignacion", idA));
                    } catch (Exception e) {
                        log.warn("Error updating asignacion {}: {}", idA, e.getMessage());
                    }
                }
            }

            // Re-invoice via EnviarAsignacionesFacturar
            if (!asigIn.isEmpty()) {
                JsonObject enviarParams = new JsonObject();
                enviarParams.addProperty("p_IdEmpresa", idEmpresa);
                enviarParams.addProperty("p_IdUsuario", idUsuario);
                enviarParams.addProperty("p_IdModulo", idModulo);
                enviarParams.add("p_Asignaciones", asigIn);
                JsonObject enviarResult = enviarAsigService.execute(enviarParams);

                errores = enviarResult.has("P_OUT_ERRORESCONTADOR") ? enviarResult.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
                if (enviarResult.has("P_OUT_MENSAJES")) {
                    for (var m : enviarResult.get("P_OUT_MENSAJES").getAsJsonArray()) mensajes.add(m);
                }
            }

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", errores);
            out.add("P_OUT_MENSAJES", mensajes);
            out.addProperty("P_OUT_RUTAARCHIVOLOG", "");
            return out;
        } catch (Exception e) {
            log.error("RefacturarMovimientoService error: {}", e.getMessage(), e);
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

    private static String loadSql(String path) {
        try (var is = RefacturarMovimientoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
