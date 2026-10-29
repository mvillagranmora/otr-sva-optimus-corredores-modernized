package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.PlataformaCallClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.CambiarEstadoOrdenes (JavaCall).
 */
@Service
public class CambiarEstadoOrdenesService {

    private static final Logger log = LoggerFactory.getLogger(CambiarEstadoOrdenesService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    // Order state constants
    private static final String ESTADO_PDA  = "PDA";
    private static final String ESTADO_ING  = "ING";
    private static final String ESTADO_OFE  = "OFE";
    private static final String ESTADO_ANU  = "ANU";
    private static final String ESTADO_RZD  = "RZD";
    private static final String ESTADO_ANP  = "ANP";
    private static final String ESTADO_NAU  = "NAU";
    private static final String ESTADO_ENR  = "ENR";
    private static final String TIPO_CANAL_MANUAL = "MAN";
    private static final String TIPO_CANAL_TRADER  = "TRA";
    private static final String TIPO_CANAL_OWP     = "OWP";

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil configUtil;
    private final PlataformaCallClient plataformaClient;
    private final NotificarEstadoOrdenService notificarService;
    private final String SQL_SELECT_ORDEN;
    private final String SQL_UPDATE_ESTADO_ORDEN;
    private final String SQL_SELECT_ORDENES_MODIFICADAS;

    public CambiarEstadoOrdenesService(NamedParameterJdbcTemplate jdbc,
                                        ConfiguracionSistemaUtil configUtil,
                                        PlataformaCallClient plataformaClient,
                                        NotificarEstadoOrdenService notificarService) {
        this.jdbc = jdbc;
        this.configUtil = configUtil;
        this.plataformaClient = plataformaClient;
        this.notificarService = notificarService;
        this.SQL_SELECT_ORDEN              = loadSql("cl/bcs/ordenes/sql/CambiarEstadoOrdenes_SelectOrdenes.sql");
        this.SQL_UPDATE_ESTADO_ORDEN       = loadSql("cl/bcs/ordenes/sql/CambiarEstadoOrdenes_UpdateEstadoOrdenes.sql");
        this.SQL_SELECT_ORDENES_MODIFICADAS = loadSql("cl/bcs/ordenes/sql/OrdenesModificadas_Select.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("CambiarEstadoOrdenesService.execute()");
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            JsonArray ordenesIn = params.has("p_Ordenes") ? params.get("p_Ordenes").getAsJsonArray() : new JsonArray();

            // Load current order states
            List<Long> idsOrden = new ArrayList<>();
            for (var el : ordenesIn) {
                JsonObject ord = el.getAsJsonObject();
                if (ord.has("IdOrden")) idsOrden.add(ord.get("IdOrden").getAsLong());
            }

            Map<Long, Map<String, Object>> hashOrdenes = new java.util.HashMap<>();
            if (!idsOrden.isEmpty()) {
                List<Map<String, Object>> listaOrdenes = jdbc.queryForList(SQL_SELECT_ORDEN,
                    new MapSqlParameterSource("idsOrden", idsOrden));
                for (Map<String, Object> ord : listaOrdenes) {
                    Long id = toLong(ord.get("IdOrden"));
                    if (id != null) hashOrdenes.put(id, ord);
                }
            }

            List<Map<String, Object>> mensajes = new ArrayList<>();
            List<Map<String, Object>> ordenesActualizar = new ArrayList<>();
            List<Long> ordenesNotificar = new ArrayList<>();

            for (var el : ordenesIn) {
                JsonObject ordIn = el.getAsJsonObject();
                Long idOrden = ordIn.has("IdOrden") ? ordIn.get("IdOrden").getAsLong() : null;
                String idNuevoEstado = ordIn.has("IdNuevoEstado") ? ordIn.get("IdNuevoEstado").getAsString() : null;

                Map<String, Object> o1 = hashOrdenes.get(idOrden);
                if (o1 == null) {
                    mensajes.add(Map.of("IdInterno", idOrden != null ? idOrden : 0L,
                        "tipo", "ERROR", "codigo", "ORDEN_NO_EXISTE"));
                    continue;
                }

                String estadoActual = getStr(o1, "IdEstadoOrden");
                String canalDistrib = getStr(o1, "IdTipoCanalDistribucion");
                String canalIngreso = getStr(o1, "IdTipoCanalIngreso");

                boolean ok = esTransicionValida(estadoActual, idNuevoEstado, canalDistrib, canalIngreso);

                if (ok) {
                    boolean anulada = ESTADO_ANU.equals(idNuevoEstado) || ESTADO_ANP.equals(idNuevoEstado) || ESTADO_RZD.equals(idNuevoEstado);
                    ordenesActualizar.add(Map.of(
                        "IdOrden", idOrden,
                        "IdEstadoOrden", idNuevoEstado,
                        "OrdenVigente", anulada ? "N" : getStr(o1, "OrdenVigente")
                    ));
                    if (anulada) ordenesNotificar.add(idOrden);
                } else {
                    mensajes.add(Map.of("IdInterno", idOrden, "tipo", "OK", "codigo", "CAMBIO_ESTADO_INVALIDO"));
                }
            }

            // Batch update
            for (Map<String, Object> ord : ordenesActualizar) {
                jdbc.update(SQL_UPDATE_ESTADO_ORDEN,
                    new MapSqlParameterSource()
                        .addValue("IdOrden", ord.get("IdOrden"))
                        .addValue("IdEstadoOrden", ord.get("IdEstadoOrden"))
                        .addValue("OrdenVigente", ord.get("OrdenVigente")));
                mensajes.add(Map.of("IdInterno", ord.get("IdOrden"), "tipo", "OK", "codigo", "CAMBIO_DE_ESTADO_REALIZADO"));
            }

            // Notify local service about cancelled/rejected orders
            if (!ordenesNotificar.isEmpty()) {
                try {
                    JsonObject notifParams = new JsonObject();
                    notifParams.addProperty("p_IdEmpresa", idEmpresa);
                    notifParams.addProperty("p_IdUsuario", 0L);
                    notifParams.addProperty("p_IdModulo", "ORD");
                    JsonArray ordArray = new JsonArray();
                    for (Long idOrd : ordenesNotificar) {
                        JsonObject o = new JsonObject();
                        o.addProperty("IdOrden", idOrd);
                        ordArray.add(o);
                    }
                    notifParams.add("p_Ordenes", ordArray);
                    notificarService.execute(notifParams);
                } catch (Exception e) {
                    log.warn("Error notifying order state change: {}", e.getMessage());
                }
            }

            // OWP notification if configured
            try {
                String notificaOWP = configUtil.getParametroString(idEmpresa, "PLA.NotificarEstadoOrdenOWP");
                if ("S".equalsIgnoreCase(notificaOWP)) {
                    List<Long> idsOWP = new ArrayList<>();
                    for (Map<String, Object> ord : ordenesActualizar) {
                        Long idOrd = toLong(ord.get("IdOrden"));
                        Map<String, Object> orig = hashOrdenes.get(idOrd);
                        if ("RV".equalsIgnoreCase(getStr(orig, "IdClasificacionTipoInstrumento"))
                            && TIPO_CANAL_OWP.equals(getStr(orig, "IdTipoCanalIngreso"))) {
                            idsOWP.add(idOrd);
                        }
                    }
                    if (!idsOWP.isEmpty()) {
                        List<Map<String, Object>> ordenesModificadas = jdbc.queryForList(SQL_SELECT_ORDENES_MODIFICADAS,
                            new MapSqlParameterSource("p_IdOrdenes", idsOWP));
                        if (!ordenesModificadas.isEmpty()) {
                            JsonArray owpList = new JsonArray();
                            for (Map<String, Object> om : ordenesModificadas) {
                                JsonObject owpOrden = new JsonObject();
                                owpOrden.addProperty("IdEmpresa", idEmpresa);
                                owpOrden.addProperty("IdUsuario", idUsuario);
                                owpOrden.addProperty("Solsec", toLong(om.get("IdOrden")));
                                owpOrden.addProperty("NTitulos", om.get("Cantidad") != null ? om.get("Cantidad").toString() : "0");
                                owpOrden.addProperty("Precio", om.get("PrecioOrden") != null ? om.get("PrecioOrden").toString() : "0");
                                owpOrden.addProperty("Fecha", om.get("FechaOperacion") != null ? om.get("FechaOperacion").toString() : "");
                                String estadoFac = getStr(om, "IdEstadoFacturacion");
                                owpOrden.addProperty("Estado", "FAC".equalsIgnoreCase(estadoFac) ? "FAC" : getStr(om, "IdEstadoOrden"));
                                owpOrden.addProperty("Mercado", "A");
                                owpOrden.addProperty("Divisa", getStr(om, "IdMonedaOrden"));
                                owpOrden.addProperty("Nemo", getStr(om, "Nemotecnico"));
                                owpList.add(owpOrden);
                            }
                            JsonObject owpParams = new JsonObject();
                            owpParams.addProperty("p_IdEmpresa", idEmpresa);
                            owpParams.add("p_Ordenes", owpList);
                            plataformaClient.call("cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP", owpParams);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error in OWP notification: {}", e.getMessage());
            }

            long errores = mensajes.stream().filter(m -> "ERROR".equals(m.get("tipo"))).count();
            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", errores);
            out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(mensajes)));
            return out;
        } catch (Exception e) {
            log.error("CambiarEstadoOrdenesService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private boolean esTransicionValida(String desde, String hacia, String canalDistrib, String canalIngreso) {
        if (desde == null || hacia == null) return false;
        if (ESTADO_PDA.equals(desde) && (ESTADO_ANU.equals(hacia) || ESTADO_NAU.equals(hacia) || ESTADO_ING.equals(hacia))) return true;
        if (ESTADO_ING.equals(desde) && ESTADO_ANU.equals(hacia)) return true;
        if (ESTADO_ING.equals(desde) && TIPO_CANAL_MANUAL.equals(canalDistrib) && ESTADO_OFE.equals(hacia)) return true;
        if (ESTADO_ING.equals(desde) && !TIPO_CANAL_MANUAL.equals(canalDistrib) && ESTADO_ENR.equals(hacia)) return true;
        if (ESTADO_OFE.equals(desde) && TIPO_CANAL_MANUAL.equals(canalDistrib) && ESTADO_ING.equals(hacia)) return true;
        if (ESTADO_OFE.equals(desde) && TIPO_CANAL_TRADER.equals(canalIngreso) && TIPO_CANAL_MANUAL.equals(canalDistrib) && ESTADO_ANU.equals(hacia)) return true;
        if (ESTADO_OFE.equals(desde) && !TIPO_CANAL_MANUAL.equals(canalDistrib) && ESTADO_ANU.equals(hacia)) return true;
        if (ESTADO_OFE.equals(desde) && !TIPO_CANAL_MANUAL.equals(canalDistrib) && ESTADO_ANP.equals(hacia)) return true;
        if (ESTADO_ENR.equals(desde) && ESTADO_OFE.equals(hacia)) return true;
        if (ESTADO_ENR.equals(desde) && ESTADO_ANU.equals(hacia)) return true;
        if (ESTADO_ENR.equals(desde) && ESTADO_RZD.equals(hacia)) return true;
        return false;
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
        try (var is = CambiarEstadoOrdenesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
