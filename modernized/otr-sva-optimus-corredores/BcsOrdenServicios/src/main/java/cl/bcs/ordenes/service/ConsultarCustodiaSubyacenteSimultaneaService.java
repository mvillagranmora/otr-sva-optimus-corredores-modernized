package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.ObtenerDiaSiguienteUtil;
import cl.bcs.ordenes.util.PlataformaCallClient;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Migrated from legacy cl.bcs.ordenes.ConsultarCustodiaSubyacenteSimultanea (JavaCall).
 * Computes available custody for simultaneous operations by:
 *   1. Computing the settlement date via ObtenerDiaSiguienteUtil
 *   2. Querying custody via ConsultarCustodiaIngresoOrdenService
 *   3. Enriching with instrument data and prices from EntregarPreciosTasasUI
 *   4. Calculating PorcentajeVaR for each instrument
 */
@Service
public class ConsultarCustodiaSubyacenteSimultaneaService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCustodiaSubyacenteSimultaneaService.class);
    private static final Gson GSON = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final ObtenerDiaSiguienteUtil diaSiguienteUtil;
    private final ConfiguracionSistemaUtil configUtil;
    private final PlataformaCallClient plataformaClient;
    private final ConsultarCustodiaIngresoOrdenService custodiaIngresoService;

    private final String SQL_SELECT_INSTRUMENTO;

    public ConsultarCustodiaSubyacenteSimultaneaService(NamedParameterJdbcTemplate jdbc,
                                                         ObtenerDiaSiguienteUtil diaSiguienteUtil,
                                                         ConfiguracionSistemaUtil configUtil,
                                                         PlataformaCallClient plataformaClient,
                                                         ConsultarCustodiaIngresoOrdenService custodiaIngresoService) {
        this.jdbc = jdbc;
        this.diaSiguienteUtil = diaSiguienteUtil;
        this.configUtil = configUtil;
        this.plataformaClient = plataformaClient;
        this.custodiaIngresoService = custodiaIngresoService;
        this.SQL_SELECT_INSTRUMENTO = loadSql("cl/bcs/ordenes/sql/ConsultarCustodiaSubyacenteSimultanea_SelectInstrumento.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.debug("ConsultarCustodiaSubyacenteSimultaneaService.execute()");
        try {
            Long idEmpresa    = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario    = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo   = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            Long idPais       = params.has("p_IdPais") ? params.get("p_IdPais").getAsLong() : 1L;
            Long idPortafolio = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : null;
            String idBolsa    = params.has("p_IdBolsa") ? params.get("p_IdBolsa").getAsString() : "BCS";
            Long plazoDias    = params.has("p_plazoDias") ? params.get("p_plazoDias").getAsLong() : 0L;
            String idBoveda   = params.has("p_IdBoveda") ? params.get("p_IdBoveda").getAsString() : "DCV";
            BigDecimal tasa   = params.has("p_Tasa") ? params.get("p_Tasa").getAsBigDecimal() : BigDecimal.ZERO;

            JsonArray mensajes = new JsonArray();

            // Get system date and config
            String fechaSistema = configUtil.getParametroString(idEmpresa, "PLA.FechaSistema");
            String listasAyBSimultanea = "N";
            try {
                listasAyBSimultanea = configUtil.getParametroString(idEmpresa, "ListasAyBSimultanea");
            } catch (Exception e) {
                listasAyBSimultanea = "N";
            }

            // Compute settlement date
            String fechaCalculada = fechaSistema;
            try {
                ObtenerDiaSiguienteUtil.FechaIn fi = new ObtenerDiaSiguienteUtil.FechaIn();
                fi.fechaInicial = fechaSistema;
                fi.numeroDias = plazoDias;
                fi.pais = idPais;
                fi.IdInterno = 0L;
                ObtenerDiaSiguienteUtil.Salida sal = diaSiguienteUtil.calcular(idPais, "S", "N", List.of(fi));
                if (!sal.p_out_fechas.isEmpty()) fechaCalculada = sal.p_out_fechas.get(0).fechaCalculada;
            } catch (Exception e) {
                log.warn("ObtenerDiaSiguiente error: {}", e.getMessage());
            }

            // Query custody via ConsultarCustodiaIngresoOrdenService
            JsonObject custodiaParams = new JsonObject();
            custodiaParams.addProperty("P_IDEMPRESA", idEmpresa);
            custodiaParams.addProperty("P_IDUSUARIO", idUsuario);
            custodiaParams.addProperty("P_IDMODULO", idModulo);
            custodiaParams.addProperty("P_FECHASALDO", fechaCalculada);
            custodiaParams.addProperty("P_DIASPROYECCION", 0L);
            custodiaParams.addProperty("P_VALORIZARFLUJO", "S");
            JsonArray ordenesArr = new JsonArray();
            JsonObject ordenCustodia = new JsonObject();
            ordenCustodia.addProperty("IdPortafolio", idPortafolio);
            ordenCustodia.addProperty("IdTipoOperacion", "VENSIM");
            ordenCustodia.addProperty("IdBoveda", idBoveda);
            ordenCustodia.addProperty("CumpleParaVentaDescubierta", "N");
            ordenesArr.add(ordenCustodia);
            custodiaParams.add("P_ORDENES", ordenesArr);

            JsonObject custodiaResult = custodiaIngresoService.execute(custodiaParams);

            // Check errors from custody query
            if (custodiaResult.has("P_OUT_ERRORESDETALLE")) {
                JsonArray errores = custodiaResult.get("P_OUT_ERRORESDETALLE").getAsJsonArray();
                if (errores.size() > 0) {
                    JsonObject out = new JsonObject();
                    out.addProperty("P_OUT_ERRORESCONTADOR", (long) errores.size());
                    out.add("P_OUT_MENSAJES", errores);
                    out.add("P_CUSTODIA", new JsonArray());
                    return out;
                }
            }

            JsonArray custodiaList = new JsonArray();
            if (custodiaResult.has("P_OUT_SALIDA")) {
                for (var el : custodiaResult.get("P_OUT_SALIDA").getAsJsonArray()) {
                    JsonObject item = el.getAsJsonObject();
                    Long idInstr = item.has("IdInstrumento") ? item.get("IdInstrumento").getAsLong() : null;
                    if (idInstr != null && idInstr > 0) custodiaList.add(item);
                }
            }

            if (custodiaList.size() == 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"tipo\":\"ERROR\",\"codigo\":\"SIN_CUSTODIA_DISPONIBLE\"}]"));
                out.add("P_CUSTODIA", new JsonArray());
                return out;
            }

            // Collect instrument IDs for extra data
            List<Long> lstIdInstrumentos = new ArrayList<>();
            for (var el : custodiaList) {
                Long idInstr = el.getAsJsonObject().has("IdInstrumento") ? el.getAsJsonObject().get("IdInstrumento").getAsLong() : null;
                if (idInstr != null && !lstIdInstrumentos.contains(idInstr)) lstIdInstrumentos.add(idInstr);
            }

            List<Map<String, Object>> datosInstrumento = jdbc.queryForList(SQL_SELECT_INSTRUMENTO,
                new MapSqlParameterSource("IdBolsa", idBolsa)
                    .addValue("IdInstrumentos", lstIdInstrumentos));
            Map<Long, Map<String, Object>> hashInstr = new HashMap<>();
            for (Map<String, Object> di : datosInstrumento) hashInstr.put(toLong(di.get("IdInstrumento")), di);

            // Get prices via EntregarPreciosTasasUI
            JsonArray datosInstrumentosEntrada = new JsonArray();
            for (Map<String, Object> instr : datosInstrumento) {
                Long idInstr = toLong(instr.get("IdInstrumento"));
                // Precio entry (IdRegistro=0, IdUso=ORDEN)
                JsonObject precioOrd = new JsonObject();
                precioOrd.addProperty("FechaConsulta", fechaSistema);
                precioOrd.addProperty("IdRegistro", 0L);
                precioOrd.addProperty("TipoCartera", "0");
                precioOrd.addProperty("IdUso", "ORDEN");
                precioOrd.addProperty("IdTipoCriterioTasa", "0");
                precioOrd.addProperty("IdClasificacionTipoInstrumento", str(instr, "IdClasificacionTipoInstrumento"));
                precioOrd.addProperty("IdTipoInstrumento", str(instr, "IdTipoInstrumento"));
                precioOrd.addProperty("Nemotecnico", str(instr, "Instrumento"));
                precioOrd.addProperty("IdInstrumento", idInstr);
                precioOrd.addProperty("IdMoneda", str(instr, "IdMonedaInstrumento"));
                precioOrd.addProperty("PlazoResidual", 0L);
                precioOrd.addProperty("IdTrabajo", 0L);
                datosInstrumentosEntrada.add(precioOrd);
                // Precio VaR entry (IdRegistro=1, IdUso=GARANTIA)
                JsonObject precioGar = new JsonObject();
                precioGar.addProperty("FechaConsulta", fechaSistema);
                precioGar.addProperty("IdRegistro", 1L);
                precioGar.addProperty("TipoCartera", "0");
                precioGar.addProperty("IdUso", "GARANTIA");
                precioGar.addProperty("IdTipoCriterioTasa", "0");
                precioGar.addProperty("IdClasificacionTipoInstrumento", str(instr, "IdClasificacionTipoInstrumento"));
                precioGar.addProperty("IdTipoInstrumento", str(instr, "IdTipoInstrumento"));
                precioGar.addProperty("Nemotecnico", str(instr, "Instrumento"));
                precioGar.addProperty("IdInstrumento", idInstr);
                precioGar.addProperty("IdMoneda", str(instr, "IdMonedaInstrumento"));
                precioGar.addProperty("PlazoResidual", 0L);
                precioGar.addProperty("IdTrabajo", 1L);
                datosInstrumentosEntrada.add(precioGar);
            }

            JsonObject precioParams = new JsonObject();
            precioParams.addProperty("p_IdEmpresa", idEmpresa);
            precioParams.addProperty("p_IdModulo", "CUS");
            precioParams.addProperty("p_IdPais", idPais);
            precioParams.addProperty("p_IdUsuario", idUsuario);
            precioParams.add("p_DatosInstrumentos", datosInstrumentosEntrada);

            // hash prices by instrumento and idRegistro
            Map<Long, BigDecimal> hashPrecio = new HashMap<>();
            Map<Long, BigDecimal> hashPrecioVar = new HashMap<>();
            try {
                JsonObject precioResult = plataformaClient.call("cl.bcs.plataforma.tas.EntregarPreciosTasasUI", precioParams);
                if (precioResult.has("P_OUT_PRECIOSTASAS")) {
                    for (var el : precioResult.get("P_OUT_PRECIOSTASAS").getAsJsonArray()) {
                        JsonObject p = el.getAsJsonObject();
                        Long idInstr = p.has("IdInstrumento") ? p.get("IdInstrumento").getAsLong() : null;
                        Long idReg = p.has("IdRegistro") ? p.get("IdRegistro").getAsLong() : null;
                        BigDecimal precio = p.has("Precio") ? p.get("Precio").getAsBigDecimal() : BigDecimal.ZERO;
                        if (idInstr != null) {
                            if (Long.valueOf(0L).equals(idReg)) hashPrecio.put(idInstr, precio);
                            else if (Long.valueOf(1L).equals(idReg)) hashPrecioVar.put(idInstr, precio);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("EntregarPreciosTasasUI error: {}", e.getMessage());
            }

            // Build output
            JsonArray custodia = new JsonArray();
            long idInterno = 0L;
            BigDecimal bg1 = BigDecimal.ONE;
            BigDecimal bg2 = new BigDecimal(2);
            BigDecimal bg30 = new BigDecimal(30);
            BigDecimal bg100 = new BigDecimal(100);

            for (var el : custodiaList) {
                JsonObject sal = el.getAsJsonObject();
                Long idInstr = sal.has("IdInstrumento") ? sal.get("IdInstrumento").getAsLong() : null;
                if (idInstr == null) continue;

                Map<String, Object> infoInstr = hashInstr.get(idInstr);
                BigDecimal precio = hashPrecio.getOrDefault(idInstr, BigDecimal.ZERO);
                BigDecimal precioVar = hashPrecioVar.getOrDefault(idInstr, BigDecimal.ZERO);

                String clasificacion = infoInstr != null ? str(infoInstr, "ValorClasificacionRiesgo") : "";
                if ("S".equalsIgnoreCase(listasAyBSimultanea)
                    && !"A".equalsIgnoreCase(clasificacion.trim())
                    && !"B".equalsIgnoreCase(clasificacion.trim())) {
                    continue;
                }

                JsonObject c = new JsonObject();
                c.addProperty("IdInterno", idInterno);
                c.addProperty("IdInstrumento", idInstr);
                c.addProperty("Instrumento", infoInstr != null ? str(infoInstr, "Instrumento") : "");
                c.addProperty("IdTipoInstrumento", infoInstr != null ? str(infoInstr, "IdTipoInstrumento") : "");
                c.addProperty("Precio", precio);
                c.addProperty("CantidadDisponible", sal.has("Cantidad") ? sal.get("Cantidad").getAsBigDecimal() : BigDecimal.ZERO);
                c.addProperty("MontoDisponible", sal.has("Monto") ? sal.get("Monto").getAsBigDecimal() : BigDecimal.ZERO);
                c.addProperty("IdMonedaInstrumento", infoInstr != null ? str(infoInstr, "IdMonedaInstrumento") : "");
                c.addProperty("ValorClasificacionRiesgo", clasificacion);
                c.addProperty("PrecioVaR", precioVar);
                c.addProperty("IdClasificacionTipoInstrumento", infoInstr != null ? str(infoInstr, "IdClasificacionTipoInstrumento") : "");

                // PorcentajeVaR = (2 - precioVar/precio) - 1/(1 + tasa/100 * plazoDias/30)
                BigDecimal porcentaje = BigDecimal.ZERO;
                try {
                    if (precio.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal valor1 = bg2.subtract(precioVar.divide(precio, 20, RoundingMode.HALF_UP));
                        BigDecimal valor2 = bg1.divide(
                            bg1.add(tasa.divide(bg100).multiply(BigDecimal.valueOf(plazoDias).divide(bg30, 20, RoundingMode.HALF_UP))),
                            20, RoundingMode.HALF_UP);
                        porcentaje = valor1.subtract(valor2).multiply(bg100);
                    }
                } catch (Exception e) {
                    log.warn("PorcentajeVaR calc error for instr {}: {}", idInstr, e.getMessage());
                }
                c.addProperty("PorcentajeVaR", porcentaje);

                custodia.add(c);
                idInterno++;
            }

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", mensajes);
            out.add("P_CUSTODIA", custodia);
            return out;
        } catch (Exception e) {
            log.error("ConsultarCustodiaSubyacenteSimultaneaService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m == null ? null : m.get(key);
        return v != null ? v.toString() : "";
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static String loadSql(String path) {
        try (var is = ConsultarCustodiaSubyacenteSimultaneaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
