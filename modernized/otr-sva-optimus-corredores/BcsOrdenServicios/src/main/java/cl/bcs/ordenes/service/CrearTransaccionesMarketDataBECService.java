package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.ObtenerPlazoUtil;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Migrates cl.bcs.ordenes.CrearTransaccionesMarketDataBEC (JavaCall).
 * Processes BEC (Bolsa Electrónica de Chile) market data transactions:
 *   Mercado=0 → RV (Renta Variable)
 *   Mercado=1 → RF (Renta Fija)
 *   Mercado=2/4 → IF (Instrumentos Financieros)
 *   Mercado=5 → SIM (Simultáneas)
 */
@Service
public class CrearTransaccionesMarketDataBECService {

    private static final Logger log = LoggerFactory.getLogger(CrearTransaccionesMarketDataBECService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private static final Map<Integer, String> PLAZOS_MAP = Map.of(
        0, "T0", 1, "T1", 2, "T2", 3, "T3", 4, "T4", 5, "T5"
    );

    private static final String SQL_EMPRESA =
        "SELECT EMP.\"IdEmpresa\", CORR.\"IdCorredora\", EMP.\"IdPais\" " +
        "FROM \"BSCL.Plataforma.BD::PLA.Empresa\" EMP " +
        "LEFT JOIN \"BSCL.Plataforma.BD::PLA.Corredora\" CORR " +
        "  ON EMP.\"IdCorredora\" = CORR.\"IdCorredora\" " +
        "WHERE EMP.\"IdEmpresa\" = :IdEmpresa";

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil configuracion;
    private final ObtenerPlazoUtil obtenerPlazo;
    private final String sqlCorredoras;
    private final String sqlInsertWrapper;
    private final String sqlSelectTransacciones;
    private final String sqlSelectSimultanea;
    private final String sqlSelectLibros;
    private final String sqlLibroByDefault;
    private final String sqlLibroByOperador;

    @Autowired
    private CrearTransaccionesService crearTransaccionesService;

    public CrearTransaccionesMarketDataBECService(
            NamedParameterJdbcTemplate jdbc,
            ConfiguracionSistemaUtil configuracion,
            ObtenerPlazoUtil obtenerPlazo) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.obtenerPlazo = obtenerPlazo;
        this.sqlCorredoras       = loadSql("CrearTransaccionesMarketData_SelectCorredoras.sql");
        this.sqlInsertWrapper    = loadSql("CrearTransaccionesMarketDataBEC_InsertTransaccionesWrapper.sql");
        this.sqlSelectTransacciones = loadSql("CrearTransaccionesMarketDataBEC_SelectTransacciones.sql");
        this.sqlSelectSimultanea    = loadSql("CrearTransaccionesMarketDataBEC_SelectSimultanea.sql");
        this.sqlSelectLibros        = loadSql("CrearTransaccionesMarketData_SelectLibros.sql");
        this.sqlLibroByDefault   = loadSql("CrearTransacciones_SelectLibroOrdenesByDefault.sql");
        this.sqlLibroByOperador  = loadSql("CrearTransacciones_SelectLibroOrdenesByOperador.sql");
    }

    private static String loadSql(String name) {
        try (var is = CrearTransaccionesMarketDataBECService.class
                .getResourceAsStream("/cl/bcs/ordenes/sql/" + name)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + name);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load SQL: " + name, e);
        }
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        long idEmpresa  = params.has("p_IdEmpresa")  ? params.get("p_IdEmpresa").getAsLong()  : 0L;
        long idUsuario  = params.has("p_IdUsuario")  ? params.get("p_IdUsuario").getAsLong()  : 0L;

        JsonArray tranArr = params.has("transacciones") ? params.get("transacciones").getAsJsonArray() : new JsonArray();

        JsonObject salida = new JsonObject();
        salida.add("P_OUT_RESULTADO", new JsonArray());
        salida.add("P_OUT_MENSAJES",  new JsonArray());

        // ── Empresa info ──────────────────────────────────────────────────────
        Map<String, Object> empresaRow;
        try {
            empresaRow = jdbc.queryForMap(SQL_EMPRESA,
                new MapSqlParameterSource("IdEmpresa", idEmpresa));
        } catch (Exception e) {
            log.error("Cannot load empresa {}", idEmpresa, e);
            addMensaje(salida, null, "ERROR_EMPRESA", "Error al cargar empresa: " + e.getMessage());
            return salida;
        }
        Long idCorredoraEmpresa = toLong(empresaRow.get("IdCorredora"));
        Long idPais             = toLong(empresaRow.get("IdPais"));

        // ── Config params ─────────────────────────────────────────────────────
        String operadorRuteoAgente  = configuracion.getParametro(idEmpresa, "ORDENES", "OperadorRuteoAgente");
        String operadorRuteoRetail  = configuracion.getParametro(idEmpresa, "ORDENES", "OperadorRuteoRetail");

        // ── Corredoras map ────────────────────────────────────────────────────
        Map<String, Long> corredoras = new HashMap<>(); // CodigoCorredora → IdCorredora
        try {
            jdbc.queryForList(sqlCorredoras, new MapSqlParameterSource())
                .forEach(r -> corredoras.put(str(r.get("CodigoCorredora")), toLong(r.get("IdCorredora"))));
        } catch (Exception e) {
            log.warn("Cannot load corredoras: {}", e.getMessage());
        }

        // ── Libro ordenes cache ───────────────────────────────────────────────
        List<Map<String, Object>> librosDefault   = queryLibros(sqlLibroByDefault,  idEmpresa);
        List<Map<String, Object>> librosPorOper   = queryLibros(sqlLibroByOperador, idEmpresa);

        // ── Separate SIM from regular ─────────────────────────────────────────
        List<JsonObject> regular = new ArrayList<>();
        Map<String, JsonObject> simMap = new LinkedHashMap<>();

        for (JsonElement el : tranArr) {
            JsonObject t = el.getAsJsonObject();
            int mercado = t.has("Mercado") ? t.get("Mercado").getAsInt() : -1;
            if (mercado == 5) {
                String key = str(t, "Folio") + str(t, "Fecha");
                simMap.put(key, t);
            } else {
                regular.add(t);
            }
        }

        // ── Process regular transactions ──────────────────────────────────────
        List<Map<String, Object>> txRows = new ArrayList<>();

        for (JsonObject tran : regular) {
            int mercado = tran.has("Mercado") ? tran.get("Mercado").getAsInt() : -1;
            Long idInterno = tran.has("IdInterno") ? tran.get("IdInterno").getAsLong() : null;

            String codeCompra  = str(tran, "BCSCodeComprador");
            String codeVenta   = str(tran, "BCSCodeVendedor");
            Long idCorrCompra  = corredoras.get(codeCompra);
            Long idCorrVenta   = corredoras.get(codeVenta);

            if (idCorrCompra == null) {
                addMensaje(salida, idInterno, "ERROR_CORREDOR", "Corredor " + codeCompra + " no cargado");
                continue;
            }
            if (idCorrVenta == null) {
                addMensaje(salida, idInterno, "ERROR_CORREDOR", "Corredor " + codeVenta + " no cargado");
                continue;
            }

            Map<String, Object> tx = new HashMap<>();
            tx.put("IdInterno", idInterno);

            String accion;
            if (idCorrCompra.equals(idCorredoraEmpresa)) {
                accion = "C";
                tx.put("CodigoOperadorCorredor",    resolveOperador(tran, "Comprador", true));
                tx.put("CodigoOperadorContraparte", resolveOperador(tran, "Vendedor",  false));
                tx.put("IdCorredoraContraparte",    idCorrVenta);
            } else if (idCorrVenta.equals(idCorredoraEmpresa)) {
                accion = "V";
                tx.put("CodigoOperadorCorredor",    resolveOperador(tran, "Vendedor",  false));
                tx.put("CodigoOperadorContraparte", resolveOperador(tran, "Comprador", true));
                tx.put("IdCorredoraContraparte",    idCorrCompra);
            } else {
                addMensaje(salida, idInterno, "INFO_OTROS_CORREDORES",
                    "Transaccion otros corredores Folio:" + str(tran, "Folio") +
                    " C:" + codeCompra + " V:" + codeVenta);
                continue;
            }
            tx.put("AccionOrden", accion);
            tx.put("IdBolsa", "BEC");

            // ── Shared fields ─────────────────────────────────────────────────
            tx.put("EsBilateral",  "N");
            tx.put("EsCuentaPropia", "N");
            tx.put("EsFueraRueda",   "N");
            tx.put("IdCorredoraCorresponsal", 0L);
            tx.put("IdEmpleado",    0L);
            tx.put("IdInstrumentoSubyacente", 0L);
            tx.put("IdMonedaLiquidacionCorresponsal", "");
            tx.put("IdSistemaBursatil", "BEC");
            tx.put("MontoLiquidacionCorresponsal", BigDecimal.ZERO);
            tx.put("TipoCambioCorresponsal",       BigDecimal.ZERO);
            tx.put("FlujoOrigen", "INGRESOORDEN");
            tx.put("Intereses",  BigDecimal.ZERO);
            tx.put("Reajustes",  BigDecimal.ZERO);
            tx.put("GastosCorresponsal", BigDecimal.ZERO);

            String codOper = str(tx.get("CodigoOperadorCorredor"));
            tx.put("IdLibroOrdenes", obtenerLibroOrdenes(codOper, librosDefault, librosPorOper));

            String fechaIn  = str(tran, "Fecha");           // yyyyMMdd
            String fechaFmt = reformatFecha(fechaIn);       // yyyy-MM-dd
            String fechaLiqIn  = str(tran, "FechaLiquidacion");
            String fechaLiqFmt = reformatFecha(fechaLiqIn);

            tx.put("FechaCreacion", fechaFmt);
            tx.put("HoraCreacion",  str(tran, "Hora"));
            tx.put("FechaEmision",  "0001-01-01");
            tx.put("FolioTransaccionPlazo", "");
            tx.put("NemotecnicoSVS", "");
            tx.put("NemotecnicoEspecificoBolson", "");
            tx.put("IdEmisor",  "");
            tx.put("MejorPrecioCompra", BigDecimal.valueOf(bd(tran, "puntaCompraPrecio")));
            tx.put("MejorPrecioVenta",  BigDecimal.valueOf(bd(tran, "puntaVentaPrecio")));
            tx.put("MontoPlazo",        BigDecimal.ZERO);

            String compradorIdCorredor = str(tran, "CompradorIdCorredor");
            String vendedorIdCorredor  = str(tran, "VendedorIdCorredor");
            String compradorIdOpdir    = str(tran, "CompradorIdOpdir");
            String vendedorIdOpdir     = str(tran, "VendedorIdOpdir");
            if (compradorIdCorredor.equalsIgnoreCase(vendedorIdCorredor)) {
                tx.put("IndicadorOrdenDirecta",
                    (!compradorIdOpdir.isEmpty() || !vendedorIdOpdir.isEmpty()) ? "OE" : "OD");
            } else {
                tx.put("IndicadorOrdenDirecta", "");
            }

            if (mercado == 0) {
                // ── RV ────────────────────────────────────────────────────────
                tx.put("Cantidad",   BigDecimal.valueOf(bd(tran, "Cantidad")));
                tx.put("EsFungible", "N");
                tx.put("EsVentaCorta", tran.has("ventaCorta") && tran.get("ventaCorta").getAsBoolean() ? "S" : "N");
                tx.put("FechaLiquidacionContado", "0001-01-01");
                tx.put("FechaVencimiento", "0001-01-01");
                tx.put("FolioTransaccion", str(tran, "Folio"));
                int plazo = tran.has("Plazo") ? tran.get("Plazo").getAsInt() : 0;
                tx.put("IdCondicionDeLiquidacionContado", PLAZOS_MAP.getOrDefault(plazo, null));
                tx.put("IdTipoReajuste", "");
                tx.put("IdMoneda", "CLP");
                tx.put("IndicadorDCV", "S");
                if ("C".equals(accion)) {
                    tx.put("MarcaNegociacion",            str(tran, "ClientIDCompra"));
                    tx.put("MarcaNegociacionContraparte", str(tran, "ClientIDVenta"));
                } else {
                    tx.put("MarcaNegociacion", str(tran, "ClientIDVenta"));
                    tx.put("MarcaNegociacionContraparte", "");
                }
                tx.put("Monto",        BigDecimal.valueOf(bd(tran, "Monto")));
                tx.put("Nemotecnico",  str(tran, "Instrumento"));
                tx.put("Plazo",        0L);
                tx.put("Precio",       BigDecimal.valueOf(bd(tran, "Precio")));
                tx.put("PrecioPlazo",  BigDecimal.valueOf(bd(tran, "PrecioPlazo")));
                tx.put("Tasa",         BigDecimal.ZERO);
                tx.put("ValorRescate", BigDecimal.ZERO);
                String codOperCorredor = str(tx.get("CodigoOperadorCorredor"));
                boolean esRuteo = codOperCorredor.equalsIgnoreCase(operadorRuteoAgente)
                    || codOperCorredor.equalsIgnoreCase(operadorRuteoRetail);
                tx.put("EsRuteo", esRuteo ? "S" : "N");

            } else if (mercado == 1) {
                // ── RF ────────────────────────────────────────────────────────
                tx.put("Cantidad",   BigDecimal.valueOf(bd(tran, "Cantidad")));
                tx.put("EsFungible", "N");
                tx.put("EsVentaCorta", "N");
                tx.put("FechaLiquidacionContado", fechaLiqFmt);
                tx.put("FechaVencimiento", "0001-01-01");
                tx.put("FolioTransaccion", str(tran, "Folio"));

                int plazo = calcularPlazoHabiles(idPais, idInterno, fechaFmt, fechaLiqFmt);
                tx.put("IdCondicionDeLiquidacionContado", PLAZOS_MAP.getOrDefault(plazo, null));
                tx.put("IdTipoReajuste", "");
                tx.put("IdMoneda", "CLP");
                tx.put("IndicadorDCV", "S");
                tx.put("MarcaNegociacion", "");
                tx.put("Monto",       BigDecimal.valueOf(bd(tran, "Monto")));
                tx.put("MontoPlazo",  BigDecimal.ZERO);

                String instrumento = str(tran, "Instrumento");
                String nemo;
                if (instrumento.startsWith("CERO"))      nemo = "CERO";
                else if (instrumento.startsWith("XERO")) nemo = "XERO";
                else if (instrumento.startsWith("ZERO")) nemo = "ZERO";
                else if (instrumento.startsWith("BR"))   nemo = "BR";
                else                                      nemo = instrumento;
                tx.put("Nemotecnico", nemo);
                tx.put("NemotecnicoEspecificoBolson", instrumento);
                tx.put("Plazo",       0L);
                tx.put("Precio",      BigDecimal.valueOf(bd(tran, "ValorPar")));
                tx.put("PrecioPlazo", BigDecimal.ZERO);
                tx.put("Tasa",        BigDecimal.valueOf(bd(tran, "Precio")));
                tx.put("ValorRescate", BigDecimal.ZERO);
                tx.put("EsRuteo", "N");

            } else if (mercado == 2 || mercado == 4) {
                // ── IF ────────────────────────────────────────────────────────
                String instrumento = str(tran, "Instrumento");
                boolean isPdbcPrbc = instrumento.equalsIgnoreCase("PRBC")
                    || instrumento.equalsIgnoreCase("PDBC");
                tx.put("Cantidad",   isPdbcPrbc ? BigDecimal.ZERO : BigDecimal.ONE);
                tx.put("EsFungible", isPdbcPrbc ? "N" : "S");
                tx.put("EsVentaCorta", "N");
                tx.put("FechaLiquidacionContado", fechaLiqFmt);
                tx.put("FechaVencimiento", fechaLiqFmt);
                tx.put("FolioTransaccion", str(tran, "Folio"));

                int plazo = calcularPlazoHabiles(idPais, idInterno, fechaFmt, fechaLiqFmt);
                tx.put("IdCondicionDeLiquidacionContado", PLAZOS_MAP.getOrDefault(plazo, null));

                String reaj = str(tran, "Reajustabilidad");
                if ("NR".equalsIgnoreCase(reaj)) {
                    tx.put("IdTipoReajuste", "CLP"); tx.put("IdMoneda", "CLP"); tx.put("IdMonedaEmision", "CLP");
                } else if ("UF".equalsIgnoreCase(reaj)) {
                    tx.put("IdTipoReajuste", "UF");  tx.put("IdMoneda", "CLP"); tx.put("IdMonedaEmision", "UF");
                } else if ("US$".equalsIgnoreCase(reaj)) {
                    tx.put("IdTipoReajuste", "USD"); tx.put("IdMoneda", "CLP"); tx.put("IdMonedaEmision", "USD");
                } else {
                    tx.put("IdTipoReajuste", null); tx.put("IdMoneda", null); tx.put("IdMonedaEmision", null);
                }
                tx.put("IndicadorDCV", "S");
                tx.put("MarcaNegociacion", "");
                tx.put("Monto", BigDecimal.valueOf(bd(tran, "Monto")));

                String nemo;
                if (instrumento.toUpperCase().contains("PDBC")) nemo = "PDBC";
                else if ("NR".equalsIgnoreCase(reaj))           nemo = "PAGARE NR";
                else                                             nemo = "PAGARE R";
                tx.put("Nemotecnico",    nemo);
                tx.put("NemotecnicoSVS", instrumento);
                String plazoDias = str(tran, "PlazoDias");
                tx.put("Plazo",        plazoDias.isEmpty() ? 0L : Long.parseLong(plazoDias));
                tx.put("Precio",       BigDecimal.ZERO);
                tx.put("PrecioPlazo",  BigDecimal.ZERO);
                tx.put("Tasa",         BigDecimal.valueOf(bd(tran, "Precio")));
                tx.put("ValorRescate", BigDecimal.valueOf(bd(tran, "Cantidad")));
                tx.put("IdEmisor", str(tran, "EmisorNombre"));
                tx.put("EsRuteo", "N");
            } else {
                continue;
            }

            txRows.add(tx);
        }

        // ── Process SIM ───────────────────────────────────────────────────────
        for (Map.Entry<String, JsonObject> entry : simMap.entrySet()) {
            JsonObject tran = entry.getValue();
            int plazoTran = tran.has("Plazo") ? tran.get("Plazo").getAsInt() : 0;
            if (plazoTran > 2) continue; // only contado leg (plazo <= 2) initiates SIM

            String folioRel = str(tran, "FolioRelacionado");
            String fecha    = str(tran, "Fecha");
            JsonObject related = simMap.get(folioRel + fecha);

            Long idInterno = tran.has("IdInterno") ? tran.get("IdInterno").getAsLong() : null;
            String codeCompra = str(tran, "BCSCodeComprador");
            String codeVenta  = str(tran, "BCSCodeVendedor");
            Long idCorrCompra = corredoras.get(codeCompra);
            Long idCorrVenta  = corredoras.get(codeVenta);

            if (idCorrCompra == null || idCorrVenta == null) {
                addMensaje(salida, idInterno, "ERROR_CORREDOR", "Corredor no cargado SIM");
                continue;
            }

            Map<String, Object> tx = new HashMap<>();
            tx.put("IdInterno", idInterno);
            tx.put("IdTipoInstrumento", "SIM");
            String accion;
            if (idCorrCompra.equals(idCorredoraEmpresa)) {
                accion = "C";
                tx.put("CodigoOperadorCorredor",    resolveOperador(tran, "Comprador", true));
                tx.put("CodigoOperadorContraparte", resolveOperador(tran, "Vendedor",  false));
                tx.put("IdCorredoraContraparte", idCorrVenta);
            } else if (idCorrVenta.equals(idCorredoraEmpresa)) {
                accion = "V";
                tx.put("CodigoOperadorCorredor",    resolveOperador(tran, "Vendedor",  false));
                tx.put("CodigoOperadorContraparte", resolveOperador(tran, "Comprador", true));
                tx.put("IdCorredoraContraparte", idCorrCompra);
            } else {
                addMensaje(salida, idInterno, "INFO_OTROS_CORREDORES",
                    "SIM otros corredores Folio:" + str(tran, "Folio"));
                continue;
            }
            tx.put("AccionOrden", accion);
            tx.put("IdBolsa", "BEC");
            tx.put("EsBilateral", "N"); tx.put("EsCuentaPropia", "N");
            tx.put("EsFueraRueda", "N"); tx.put("EsFungible", "N"); tx.put("EsVentaCorta", "N");
            tx.put("EsRuteo", "N"); tx.put("FlujoOrigen", "INGRESOORDEN");
            tx.put("IdCorredoraCorresponsal", 0L); tx.put("IdEmpleado", 0L); tx.put("IdEmisor", "");
            tx.put("IdInstrumentoSubyacente", 0L);
            tx.put("IdMonedaLiquidacionCorresponsal", "");
            tx.put("IdSistemaBursatil", "BEC");
            tx.put("IdTipoReajuste", ""); tx.put("IdMoneda", "CLP");
            tx.put("IndicadorDCV", "S");
            tx.put("Intereses", BigDecimal.ZERO); tx.put("Reajustes", BigDecimal.ZERO);
            tx.put("GastosCorresponsal", BigDecimal.ZERO);
            tx.put("MontoLiquidacionCorresponsal", BigDecimal.ZERO);
            tx.put("TipoCambioCorresponsal", BigDecimal.ZERO);
            tx.put("MejorPrecioCompra", BigDecimal.ZERO); tx.put("MejorPrecioVenta", BigDecimal.ZERO);
            tx.put("NemotecnicoSVS", ""); tx.put("NemotecnicoEspecificoBolson", "");

            String fechaFmt    = reformatFecha(fecha);
            String fechaLiqFmt = reformatFecha(str(tran, "FechaLiquidacion"));
            String fechaLiqPlazoFmt = related != null ? reformatFecha(str(related, "FechaLiquidacion")) : "0001-01-01";

            tx.put("FechaCreacion", fechaFmt);
            tx.put("FechaEmision",  "0001-01-01");
            tx.put("FechaLiquidacionContado", "0001-01-01");
            tx.put("FechaVencimiento", fechaLiqPlazoFmt);
            tx.put("FolioTransaccion",      str(tran, "Folio"));
            tx.put("FolioTransaccionPlazo", folioRel);
            tx.put("HoraCreacion", str(tran, "Hora"));

            int calcPlazo = calcularPlazoHabiles(idPais, idInterno, fechaFmt, fechaLiqFmt);
            int plazoActual = tran.has("Plazo") ? tran.get("Plazo").getAsInt() : 0;
            int plazoFinal  = plazoActual == 0 ? calcPlazo : plazoActual;
            tx.put("IdCondicionDeLiquidacionContado", PLAZOS_MAP.getOrDefault(plazoFinal, null));

            if ("C".equals(accion)) {
                tx.put("MarcaNegociacion", str(tran, "ClientIDCompra"));
                tx.put("MarcaNegociacionContraparte", str(tran, "ClientIDVenta"));
            } else {
                tx.put("MarcaNegociacion", str(tran, "ClientIDVenta"));
                tx.put("MarcaNegociacionContraparte", "");
            }
            tx.put("Cantidad",    BigDecimal.valueOf(bd(tran, "Cantidad")));
            tx.put("Monto",       BigDecimal.valueOf(bd(tran, "Monto")));
            tx.put("MontoPlazo",  related != null ? BigDecimal.valueOf(bd(related, "Monto")) : BigDecimal.ZERO);
            tx.put("Nemotecnico", str(tran, "Instrumento"));
            tx.put("Plazo",       -1L); // signal for CrearTransacciones to calculate
            tx.put("Precio",      BigDecimal.valueOf(bd(tran, "Precio")));
            tx.put("PrecioPlazo", related != null ? BigDecimal.valueOf(bd(related, "Precio")) : BigDecimal.ZERO);
            tx.put("Tasa",        BigDecimal.valueOf(bd(tran, "Variacion")));
            tx.put("ValorRescate", BigDecimal.ZERO);

            String compradorIdCorredor = str(tran, "CompradorIdCorredor");
            String vendedorIdCorredor  = str(tran, "VendedorIdCorredor");
            String compradorIdOpdir    = str(tran, "CompradorIdOpdir");
            String vendedorIdOpdir     = str(tran, "VendedorIdOpdir");
            if (compradorIdCorredor.equalsIgnoreCase(vendedorIdCorredor)) {
                tx.put("IndicadorOrdenDirecta",
                    (!compradorIdOpdir.isEmpty() || !vendedorIdOpdir.isEmpty()) ? "OE" : "OD");
            } else {
                tx.put("IndicadorOrdenDirecta", "");
            }
            String codOper = str(tx.get("CodigoOperadorCorredor"));
            tx.put("IdLibroOrdenes", obtenerLibroOrdenes(codOper, librosDefault, librosPorOper));
            txRows.add(tx);
        }

        if (txRows.isEmpty()) return salida;

        // ── Batch insert wrapper ──────────────────────────────────────────────
        try {
            MapSqlParameterSource[] batchParams = txRows.stream()
                .map(tx -> {
                    MapSqlParameterSource ps = new MapSqlParameterSource();
                    tx.forEach(ps::addValue);
                    return ps;
                }).toArray(MapSqlParameterSource[]::new);
            jdbc.batchUpdate(sqlInsertWrapper, batchParams);
        } catch (Exception e) {
            log.error("Batch insert wrapper failed", e);
            addMensaje(salida, null, "ERROR_INSERT_WRAPPER", e.getMessage());
            return salida;
        }

        // ── Query instrument info ─────────────────────────────────────────────
        Map<Long, Map<String, Object>> infoTx     = queryInfoMap(sqlSelectTransacciones, new MapSqlParameterSource());
        Map<Long, Map<String, Object>> infoSimTx  = queryInfoMap(sqlSelectSimultanea,    new MapSqlParameterSource());
        List<Map<String, Object>> libros           = queryLibros(sqlSelectLibros,
            new MapSqlParameterSource("idEmpresa", idEmpresa));

        for (Map<String, Object> tx : txRows) {
            Long idInterno = (Long) tx.get("IdInterno");
            boolean esSIM  = "SIM".equals(tx.get("IdTipoInstrumento"));

            Map<String, Object> info = esSIM ? infoSimTx.get(idInterno) : infoTx.get(idInterno);
            if (info == null) {
                addMensaje(salida, idInterno, "ERROR_INSTRUMENTO",
                    "Error al obtener instrumento " + tx.get("Nemotecnico"));
                continue;
            }

            if (esSIM) {
                tx.put("IdTipoOperacion", info.get("IdTipoOperacion"));
                tx.put("IdInstrumento",   info.get("IdInstrumento"));
                Map<String, Object> infoSuby = infoTx.get(idInterno);
                if (infoSuby != null) tx.put("IdInstrumentoSubyacente", infoSuby.get("IdInstrumento"));
            } else {
                tx.put("IdTipoOperacion",   info.get("IdTipoOperacion"));
                tx.put("IdTipoInstrumento", info.get("IdTipoInstrumento"));
                tx.put("IdInstrumento",     info.get("IdInstrumento"));
                // PDBC/PRBC: recalculate quantity
                String nemo = str(tx.get("Nemotecnico"));
                if ("PDBC".equalsIgnoreCase(nemo) || "PRBC".equalsIgnoreCase(nemo)) {
                    BigDecimal corteMin = toBD(info.get("CorteMinimo"));
                    BigDecimal valorRescate = toBD(tx.get("ValorRescate"));
                    if (corteMin != null && corteMin.compareTo(BigDecimal.ZERO) != 0) {
                        tx.put("Cantidad", valorRescate.divide(corteMin));
                    }
                }
            }

            // Book assignment from libros process table
            for (Map<String, Object> lib : libros) {
                String codOper = str(lib.get("CodigoOperador"));
                if (codOper.equalsIgnoreCase(str(tx.get("CodigoOperadorCorredor")))) {
                    tx.put("IdLibroOrdenes",
                        obtenerLibroOrdenes(codOper, librosDefault, librosPorOper));
                    break;
                }
            }
        }

        // ── Call CrearTransacciones ───────────────────────────────────────────
        JsonObject ctEntrada = new JsonObject();
        ctEntrada.addProperty("p_IdEmpresa",        idEmpresa);
        ctEntrada.addProperty("p_IdUsuario",         idUsuario);
        ctEntrada.addProperty("p_IdModulo",          "ORDENES");
        ctEntrada.addProperty("p_simularOperacion",  "N");
        ctEntrada.addProperty("p_UsaReglasNegocio",  "S");
        JsonArray ctTrans = new JsonArray();
        for (Map<String, Object> tx : txRows) {
            if (tx.get("IdTipoOperacion") == null) continue;
            ctTrans.add(GSON.toJsonTree(tx));
        }
        ctEntrada.add("p_Transacciones", ctTrans);

        try {
            JsonObject ctResult = crearTransaccionesService.execute(ctEntrada);
            if (ctResult.has("P_OUT_RESULTADO")) {
                for (JsonElement e : ctResult.get("P_OUT_RESULTADO").getAsJsonArray()) {
                    salida.get("P_OUT_RESULTADO").getAsJsonArray().add(e);
                }
            }
            if (ctResult.has("P_OUT_MENSAJES")) {
                for (JsonElement e : ctResult.get("P_OUT_MENSAJES").getAsJsonArray()) {
                    salida.get("P_OUT_MENSAJES").getAsJsonArray().add(e);
                }
            }
        } catch (UnsupportedOperationException ue) {
            log.warn("CrearTransaccionesService still a stub — TomEE fallback for final step: {}", ue.getMessage());
            addMensaje(salida, null, "WARN_CREAR_TX_STUB",
                "CrearTransacciones deferred; operaciones staged in wrapper table");
        } catch (Exception e) {
            log.error("CrearTransacciones failed", e);
            addMensaje(salida, null, "ERROR_CREAR_TX", e.getMessage());
        }

        return salida;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int calcularPlazoHabiles(Long idPais, Long idInterno, String fechaDesde, String fechaHasta) {
        ObtenerPlazoUtil.FechasIn fi = new ObtenerPlazoUtil.FechasIn();
        fi.IdInterno      = idInterno;
        fi.FechaDesde     = fechaDesde;
        fi.FechaHasta     = fechaHasta;
        fi.SoloDiasHabiles = "S";
        ObtenerPlazoUtil.Salida s = obtenerPlazo.calcular(idPais, List.of(fi));
        if (!s.p_out_Fechas.isEmpty()) {
            Long p = s.p_out_Fechas.get(0).Plazo;
            return p == null ? 0 : p.intValue();
        }
        return 0;
    }

    private Long obtenerLibroOrdenes(String codOperador,
                                     List<Map<String, Object>> defaults,
                                     List<Map<String, Object>> porOperador) {
        if (codOperador != null && !codOperador.isEmpty()) {
            for (Map<String, Object> lib : porOperador) {
                String cod = str(lib.get("CodigoOperador"));
                if (cod != null && cod.equalsIgnoreCase(codOperador)) {
                    return toLong(lib.get("IdLibroOrdenes"));
                }
            }
        }
        return defaults.isEmpty() ? 0L : toLong(defaults.get(0).get("IdLibroOrdenes"));
    }

    private List<Map<String, Object>> queryLibros(String sql, Long idEmpresa) {
        try {
            return jdbc.queryForList(sql, new MapSqlParameterSource("p_IdEmpresa", idEmpresa));
        } catch (Exception e) {
            log.warn("queryLibros failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> queryLibros(String sql, MapSqlParameterSource params) {
        try {
            return jdbc.queryForList(sql, params);
        } catch (Exception e) {
            log.warn("queryLibros failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Map<Long, Map<String, Object>> queryInfoMap(String sql, MapSqlParameterSource params) {
        Map<Long, Map<String, Object>> map = new HashMap<>();
        try {
            jdbc.queryForList(sql, params).forEach(r -> {
                Long id = toLong(r.get("IdInterno"));
                if (id != null) map.put(id, r);
            });
        } catch (Exception e) {
            log.warn("queryInfoMap failed: {}", e.getMessage());
        }
        return map;
    }

    private String resolveOperador(JsonObject tran, String side, boolean isComprador) {
        String idCorredor = str(tran, side.equals("Comprador") ? "BCSCodeComprador" : "BCSCodeVendedor");
        String idOpdir    = str(tran, side.equals("Comprador") ? "CompradorIdOpdir" : "VendedorIdOpdir");
        String traderID   = str(tran, side.equals("Comprador") ? "CompradorTraderID" : "VendedorTraderID");
        if (!idCorredor.equalsIgnoreCase(idOpdir) && !idOpdir.trim().isEmpty()) {
            return idOpdir;
        }
        return traderID;
    }

    private static String reformatFecha(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) return "0001-01-01";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd");
            return out.format(in.parse(yyyymmdd));
        } catch (Exception e) {
            return "0001-01-01";
        }
    }

    private static void addMensaje(JsonObject salida, Long idInterno, String codigo, String descripcion) {
        JsonObject m = new JsonObject();
        if (idInterno != null) m.addProperty("IdInterno", idInterno);
        m.addProperty("Codigo",      codigo);
        m.addProperty("Descripcion", descripcion);
        salida.get("P_OUT_MENSAJES").getAsJsonArray().add(m);
    }

    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return null; }
    }

    private static double bd(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) return 0.0;
        try { return o.get(key).getAsDouble(); } catch (Exception e) { return 0.0; }
    }

    private static BigDecimal toBD(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        try { return new BigDecimal(o.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static String str(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) return "";
        return o.get(key).getAsString();
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }
}
