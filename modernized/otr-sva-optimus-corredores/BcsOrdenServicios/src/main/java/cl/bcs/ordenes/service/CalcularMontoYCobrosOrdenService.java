package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.ObtenerDiaSiguienteUtil;
import cl.bcs.ordenes.util.PlataformaCallClient;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Migrated from legacy cl.bcs.ordenes.CalcularMontoYCobrosOrden (2065 lines).
 * Calculates order amounts and charges.
 * Cross-service calls: CalcularParidad, CalcularCobros, ConsultarInstrumentoPrecioBolsa
 *   via PlataformaCallClient → BcsPlataformaServicios:8114
 * Local: ConsultarCobrosOrdenesService, ConsultarCobrosOrdenesPeService
 * BcsCustodia: ValorizarInstrumento via RestClient
 */
@Service
public class CalcularMontoYCobrosOrdenService {

    private static final Logger log = LoggerFactory.getLogger(CalcularMontoYCobrosOrdenService.class);
    private static final Gson GSON = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final PlataformaCallClient plataformaClient;
    private final ConfiguracionSistemaUtil configUtil;
    private final ConsultarCobrosOrdenesService consultarCobrosOrdenesService;
    private final ConsultarCobrosOrdenesPeService consultarCobrosOrdenesPeService;
    private final ObtenerDiaSiguienteUtil obtenerDiaSiguienteUtil;
    private final RestClient custodiaClient;

    private final String SQL_INSTRUMENTOS;
    private final String SQL_TIPO_OPERACION;
    private final String SQL_COBROS_ULTIMA_ORDEN;
    private static final String SQL_EMPRESA =
        "SELECT \"IdPais\" FROM \"BSCL.Plataforma.BD::PLA.Empresa\" WHERE \"IdEmpresa\"=:IdEmpresa";
    private static final String SQL_FORMATO_MONEDA =
        "SELECT \"Decimales\" FROM \"BSCL.Plataforma.BD::PLA.Moneda\" WHERE \"IdMoneda\"=:IdMoneda";

    // Tipo operación constants
    private static final String TIPO_COMSIM = "COMSIM";
    private static final String TIPO_VENSIM = "VENSIM";
    private static final String TIPO_COMRRV = "COMRRV";
    private static final String TIPO_VENRRV = "VENRRV";
    private static final String TIPO_COMRRF = "COMRRF";
    private static final String TIPO_VENRRF = "VENRRF";
    private static final String CLSIF_RV = "RV";
    private static final String CLSIF_DV = "DV";
    private static final String CLSIF_RF = "RF";
    private static final String CLSIF_IF = "IF";
    private static final String CLSIF_RE = "RE";
    private static final String ACCION_COMPRA = "COMPRA";
    private static final String ACCION_VENTA = "VENTA";
    private static final String CANAL_AGENTE = "AGENTE";
    private static final String TIPO_PROD_RERV = "RERV";
    private static final String TIPO_PROD_RERF = "RERF";
    private static final String ORIGEN_REFACTURACION = "REFACTURACION";

    public CalcularMontoYCobrosOrdenService(
            NamedParameterJdbcTemplate jdbc,
            PlataformaCallClient plataformaClient,
            ConfiguracionSistemaUtil configUtil,
            ConsultarCobrosOrdenesService consultarCobrosOrdenesService,
            ConsultarCobrosOrdenesPeService consultarCobrosOrdenesPeService,
            ObtenerDiaSiguienteUtil obtenerDiaSiguienteUtil,
            @Value("${bcs.custodia.url:http://bcs-custodia-servicios:8097}") String custodiaUrl) {
        this.jdbc = jdbc;
        this.plataformaClient = plataformaClient;
        this.configUtil = configUtil;
        this.consultarCobrosOrdenesService = consultarCobrosOrdenesService;
        this.consultarCobrosOrdenesPeService = consultarCobrosOrdenesPeService;
        this.obtenerDiaSiguienteUtil = obtenerDiaSiguienteUtil;
        this.custodiaClient = RestClient.builder().baseUrl(custodiaUrl).build();
        this.SQL_INSTRUMENTOS    = loadSql("cl/bcs/ordenes/sql/CalcularMontoYCobrosOrden_SelectInstrumentos.sql");
        this.SQL_TIPO_OPERACION  = loadSql("cl/bcs/ordenes/sql/CalcularMontoYCobrosOrden_SelectTipoOperacion.sql");
        this.SQL_COBROS_ULTIMA_ORDEN = loadSql("cl/bcs/ordenes/sql/CalcularMontoYCobrosOrden_SelectCobrosUltimaOrden.sql");
    }

    public JsonObject execute(JsonObject params) {
        long init = System.currentTimeMillis();
        log.info("Inicio CalcularMontoYCobrosOrdenService");

        JsonArray ordenesIn = params.has("p_Ordenes") ? params.get("p_Ordenes").getAsJsonArray() : new JsonArray();
        if (ordenesIn.size() == 0) {
            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", new JsonArray());
            out.add("p_out_Ordenes", new JsonArray());
            out.add("p_out_Cobros", new JsonArray());
            return out;
        }

        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : 0L;
        String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
        JsonArray cobrosModIn = params.has("p_CobrosModificados") ? params.get("p_CobrosModificados").getAsJsonArray() : new JsonArray();
        JsonArray cobrosFijosIn = params.has("p_CobrosFijos") ? params.get("p_CobrosFijos").getAsJsonArray() : new JsonArray();

        JsonArray mensajes = new JsonArray();

        // Build order process list
        Map<Long, OrdenProceso> ordenesHash = new HashMap<>();
        List<OrdenProceso> ordenes = new ArrayList<>();
        List<Long> idsInstrumento = new ArrayList<>();
        List<String> idsTipoOperacion = new ArrayList<>();

        for (var el : ordenesIn) {
            JsonObject o = el.getAsJsonObject();
            OrdenProceso ord = new OrdenProceso();
            ord.IdInterno            = getLong(o, "IdInterno");
            ord.IdPortafolio         = getLong(o, "IdPortafolio");
            ord.IdTipoOperacion      = getStr(o, "IdTipoOperacion");
            ord.IdTipoProducto       = getStr(o, "IdTipoProducto");
            ord.IdTipoCanalIngreso   = getStr(o, "IdTipoCanalIngreso");
            ord.IdBolsa              = getStr(o, "IdBolsa");
            ord.IdCorredorCorresponsal = getLong(o, "IdCorredorCorresponsal");
            ord.IdInstrumento        = getLong(o, "IdInstrumento");
            ord.IdInstrumentoSubyacente = getLong(o, "IdInstrumentoSubyacente");
            ord.CantidadOrden        = getBD(o, "CantidadOrden");
            ord.MontoOrden           = getBD(o, "MontoOrden");
            ord.ValorRescateOrden    = getBD(o, "ValorRescateOrden");
            ord.EsFungible           = getStr(o, "EsFungible", "N");
            ord.TasaSimultanea       = getBD(o, "TasaSimultanea");
            ord.TasaRFIF             = getBD(o, "TasaRFIF");
            ord.TasaVentaCorta       = getBD(o, "TasaVentaCorta");
            ord.TasaReporte          = getBD(o, "TasaReporte");
            ord.TasaReportado        = getBD(o, "TasaReportado");
            ord.PlazoReporte         = getLong(o, "PlazoReporte");
            ord.PlazoIF              = getLong(o, "PlazoIF");
            ord.PlazoSimultanea      = getLong(o, "PlazoSimultanea");
            ord.PlazoVentaCorta      = getLong(o, "PlazoVentaCorta");
            ord.IdMonedaLiquidacionOrden = getStr(o, "IdMonedaLiquidacionOrden");
            ord.IdMonedaOrden        = getStr(o, "IdMonedaOrden");
            ord.PrecioOrden          = getBD(o, "PrecioOrden");
            ord.TipoCambio           = getBD(o, "TipoCambio");
            ord.IdEmisor             = getStr(o, "IdEmisor");
            ord.EsAPlazo             = getStr(o, "EsAPlazo", "N");
            ord.MontoTotal           = getBD(o, "MontoTotal");
            ord.EsPorMonto           = getStr(o, "EsPorMonto", "N");
            ord.TipoInteres          = getStr(o, "TipoInteres");
            ord.BaseTasa             = getLong(o, "BaseTasa");
            ord.PorcentajeAllIn      = getBD(o, "PorcentajeAllIn");
            ord.MontoAllIn           = getBD(o, "MontoAllIn");
            ord.GlosaRefactura       = getStr(o, "GlosaRefactura");
            ord.FechaOperacion       = getStr(o, "FechaOperacion");
            ord.FechaOperacionOriginal = getStr(o, "FechaOperacionOriginal");
            ord.IdClasificacionTipoInstrumento = getStr(o, "IdClasificacionTipoInstrumento");

            ordenesHash.put(ord.IdInterno, ord);
            ordenes.add(ord);
            idsInstrumento.add(ord.IdInstrumento);
            if (ord.IdInstrumentoSubyacente != null) idsInstrumento.add(ord.IdInstrumentoSubyacente);
            idsTipoOperacion.add(ord.IdTipoOperacion);
        }

        // Query instruments
        Map<Long, Instrumento> instrumentos = new HashMap<>();
        Map<Object, Instrumento> instrumentosHash = new HashMap<>();
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_INSTRUMENTOS,
                new MapSqlParameterSource("idsInstrumento", idsInstrumento));
            for (Map<String, Object> r : rows) {
                Instrumento inst = new Instrumento();
                inst.IdInstrumento = toLong(r.get("IdInstrumento"));
                inst.Tamano = toLong(r.get("Tamano"));
                inst.IdMoneda = str(r, "IdMoneda");
                inst.IdClasificacionTipoInstrumento = str(r, "IdClasificacionTipoInstrumento");
                inst.IdTipoInstrumento = str(r, "IdTipoInstrumento");
                inst.IdTipoReajuste = str(r, "IdTipoReajuste");
                instrumentos.put(inst.IdInstrumento, inst);
                instrumentosHash.put(inst.IdInstrumento, inst);
                instrumentosHash.put(String.valueOf(inst.IdInstrumento), inst);
            }
        } catch (Exception e) {
            log.error("Error querying instruments: {}", e.getMessage());
        }

        // Query operation types
        Map<String, String> tiposOperacion = new HashMap<>();
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_TIPO_OPERACION,
                new MapSqlParameterSource("idsTipoOperacion", idsTipoOperacion));
            for (Map<String, Object> r : rows) {
                tiposOperacion.put(str(r, "IdTipoOperacion"), str(r, "AccionOrden"));
            }
        } catch (Exception e) {
            log.error("Error querying op types: {}", e.getMessage());
        }

        // Config params
        String idMonedaSimultanea = configUtil.getParametroString(idEmpresa, "MonedaSimultanea");
        if (idMonedaSimultanea.isEmpty()) idMonedaSimultanea = configUtil.getParametroString(idEmpresa, "ORDENES.MonedaSimultanea");
        String idMonedaSistema = configUtil.getParametroString(idEmpresa, "PLA.MonedaSistema");
        String fechaSistema = configUtil.getParametroString(idEmpresa, "PLA.FechaSistema");
        String localizacion = configUtil.getParametroString(idEmpresa, "PLA.LocalizacionSistema");
        String parametroCalculoPrecioPunta;
        try { parametroCalculoPrecioPunta = configUtil.getParametroString(idEmpresa, "CalculoPrecioPunta"); }
        catch (Exception e) { parametroCalculoPrecioPunta = "N"; }
        if (parametroCalculoPrecioPunta.isEmpty()) parametroCalculoPrecioPunta = "N";

        // Get empresa IdPais
        long idPaisEmpresa = 0L;
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_EMPRESA,
                new MapSqlParameterSource("IdEmpresa", idEmpresa));
            if (!rows.isEmpty()) idPaisEmpresa = toLong(rows.get(0).get("IdPais"));
        } catch (Exception e) {
            log.warn("Error getting empresa: {}", e.getMessage());
        }

        // Set IdMonedaOrden per order
        for (OrdenProceso ord : ordenes) {
            if (TIPO_COMSIM.equalsIgnoreCase(ord.IdTipoOperacion)
                    || TIPO_VENSIM.equalsIgnoreCase(ord.IdTipoOperacion)) {
                ord.IdMonedaOrden = idMonedaSimultanea;
            } else if (instrumentos.get(ord.IdInstrumentoSubyacente) != null
                    && (TIPO_COMRRV.equalsIgnoreCase(ord.IdTipoOperacion) || TIPO_COMRRF.equalsIgnoreCase(ord.IdTipoOperacion)
                        || TIPO_VENRRV.equalsIgnoreCase(ord.IdTipoOperacion) || TIPO_VENRRF.equalsIgnoreCase(ord.IdTipoOperacion))) {
                ord.IdMonedaOrden = ord.IdMonedaLiquidacionOrden;
            } else if (instrumentos.get(ord.IdInstrumento) != null
                    && CLSIF_IF.equalsIgnoreCase(instrumentos.get(ord.IdInstrumento).IdClasificacionTipoInstrumento)) {
                // keep moneda
            } else if (ord.IdMonedaOrden != null && !ord.IdMonedaOrden.isBlank()) {
                // keep moneda
            } else if (instrumentos.get(ord.IdInstrumento) != null) {
                ord.IdMonedaOrden = instrumentos.get(ord.IdInstrumento).IdMoneda;
            }
            ord.AccionOrden = tiposOperacion.getOrDefault(ord.IdTipoOperacion, "");
        }

        // ── Cobros CL ──────────────────────────────────────────────────────────
        Map<Long, CobroProceso> cobrosHash = new HashMap<>();
        if ("CL".equalsIgnoreCase(localizacion)) {
            try {
                if (cobrosFijosIn.size() == 0) {
                    // Call ConsultarCobrosOrdenes
                    JsonObject ccParams = new JsonObject();
                    ccParams.addProperty("p_IdEmpresa", idEmpresa);
                    ccParams.addProperty("p_IdModulo", idModulo);
                    ccParams.addProperty("p_IdUsuario", idUsuario);
                    JsonArray ops = new JsonArray();
                    for (OrdenProceso ord : ordenes) {
                        JsonObject op = new JsonObject();
                        op.addProperty("IdBolsa", ord.IdBolsa);
                        op.addProperty("IdCorredoraCorresponsal", ord.IdCorredorCorresponsal);
                        op.addProperty("IdInterno", ord.IdInterno);
                        op.addProperty("IdPortafolio", ord.IdPortafolio);
                        op.addProperty("IdTipoCanalIngreso", ord.IdTipoCanalIngreso);
                        op.addProperty("IdTipoOperacion", ord.IdTipoOperacion);
                        op.addProperty("IdTipoProducto", ord.IdTipoProducto);
                        ops.add(op);
                    }
                    ccParams.add("p_Operaciones", ops);
                    JsonObject ccResult = consultarCobrosOrdenesService.execute(ccParams);

                    long idInternoCobro = 0L;
                    if (ccResult.has("p_out_MatrizOperacional")) {
                        for (var el : ccResult.get("p_out_MatrizOperacional").getAsJsonArray()) {
                            JsonObject mat = el.getAsJsonObject();
                            Long idInterno = getLong(mat, "IdInterno");
                            OrdenProceso ord2 = ordenesHash.get(idInterno);
                            if (ord2 == null) continue;
                            CobroProceso cob = new CobroProceso();
                            cob.IdInterno            = idInterno;
                            cob.IdInternoCobro       = idInternoCobro++;
                            cob.IdCobro              = getStr(mat, "IdCobro");
                            cob.CodigoCobroClasificacion = getStr(mat, "CodigoCobroClasificacion");
                            cob.IdMoneda             = getStr(mat, "IdMoneda");
                            cob.NombreCobro          = getStr(mat, "NombreCobro");
                            cob.ValorCobroPerfil     = getBD(mat, "ValorCobro");
                            cob.PermiteCambiarValor  = getStr(mat, "PermiteCambiarValor", "N");
                            cob.FormaCobro           = getStr(mat, "FormaCobro", "V");
                            cob.MontoMinimo          = getBD(mat, "ValorTopeMinimoOrden");
                            cob.MontoOperacionMonedaCobro = BigDecimal.ZERO;
                            cob.MontoCobroCalculado  = BigDecimal.ZERO;
                            cob.MontoCobroCalculadoLocal = BigDecimal.ZERO;
                            cob.MontoCobroCalculadoLiquidacion = BigDecimal.ZERO;
                            cob.ValorCobroAplicado   = cob.ValorCobroPerfil;
                            cob.IdOrigenCobro        = getStr(mat, "IdOrigenCobro");
                            cob.IdMonedaLiquidacionOrden = ord2.IdMonedaLiquidacionOrden;
                            ord2.cobros.add(cob);
                            cobrosHash.put(cob.IdInternoCobro, cob);
                        }
                    }
                } else {
                    // Cobros fijos
                    long idInternoCobro = 0L;
                    for (var el : cobrosFijosIn) {
                        JsonObject cf = el.getAsJsonObject();
                        Long idInterno = getLong(cf, "IdInterno");
                        OrdenProceso ord2 = ordenesHash.get(idInterno);
                        if (ord2 == null) continue;
                        CobroProceso cob = new CobroProceso();
                        cob.IdOrigenCobro = "PORTAFOLIO";
                        cob.IdInterno = idInterno;
                        cob.IdInternoCobro = idInternoCobro++;
                        cob.IdMonedaLiquidacionOrden = getStr(cf, "IdMoneda");
                        cob.MontoOperacionMonedaCobro = BigDecimal.ZERO;
                        cob.IdCobro = getStr(cf, "IdCobro");
                        cob.CodigoCobroClasificacion = getStr(cf, "CodigoCobroClasificacion");
                        cob.IdMoneda = getStr(cf, "IdMoneda");
                        cob.NombreCobro = "COMISION VARIABLE";
                        cob.ValorCobroPerfil = getBD(cf, "ValorCobro");
                        cob.PermiteCambiarValor = getStr(cf, "PermiteCambiarValor", "N");
                        cob.FormaCobro = getStr(cf, "FormaCobro", "V");
                        cob.MontoMinimo = BigDecimal.ZERO;
                        cob.ValorCobroAplicado = cob.ValorCobroPerfil;
                        cob.MontoCobroCalculado = BigDecimal.ZERO;
                        cob.MontoCobroCalculadoLocal = BigDecimal.ZERO;
                        cob.MontoCobroCalculadoLiquidacion = BigDecimal.ZERO;
                        ord2.cobros.add(cob);
                        cobrosHash.put(cob.IdInternoCobro, cob);
                    }
                    // Apply cobros fijos values
                    for (var el : cobrosFijosIn) {
                        JsonObject cf = el.getAsJsonObject();
                        Long idInterno = getLong(cf, "IdInterno");
                        OrdenProceso ord2 = ordenesHash.get(idInterno);
                        if (ord2 == null) continue;
                        String idCobro = getStr(cf, "IdCobro");
                        BigDecimal valor = getBD(cf, "ValorCobro");
                        for (CobroProceso c : ord2.cobros) {
                            if (idCobro.equalsIgnoreCase(c.IdCobro)) c.ValorCobroAplicado = valor;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("ConsultarCobrosOrdenes error: {}", e.getMessage());
            }

            // Apply cobros modificados
            if (cobrosFijosIn.size() == 0) {
                for (var el : cobrosModIn) {
                    JsonObject cm = el.getAsJsonObject();
                    Long idInterno = getLong(cm, "IdInterno");
                    String idCobro = getStr(cm, "IdCobro");
                    BigDecimal valor = getBD(cm, "ValorCobroAplicado");
                    OrdenProceso ord = ordenesHash.get(idInterno);
                    if (ord == null) continue;
                    for (CobroProceso c : ord.cobros) {
                        if (idCobro.equalsIgnoreCase(c.IdCobro)) c.ValorCobroAplicado = valor;
                    }
                }
            }
        }

        // ── Cobros PE ──────────────────────────────────────────────────────────
        Map<Long, DefinicionCobroProceso> cobrosPeruHash = new HashMap<>();
        if ("PE".equalsIgnoreCase(localizacion)) {
            try {
                JsonObject ccpParams = new JsonObject();
                ccpParams.addProperty("p_IdEmpresa", idEmpresa);
                ccpParams.addProperty("p_IdModulo", idModulo);
                ccpParams.addProperty("p_IdUsuario", idUsuario);
                JsonArray ordenesArr = new JsonArray();
                for (OrdenProceso ord : ordenes) {
                    JsonObject op = new JsonObject();
                    op.addProperty("EsAPlazo", ord.EsAPlazo != null ? ord.EsAPlazo : "N");
                    op.addProperty("IdBolsa", ord.IdBolsa);
                    op.addProperty("IdCorredoraCorresponsal", ord.IdCorredorCorresponsal);
                    op.addProperty("IdEmisor", ord.IdEmisor);
                    op.addProperty("IdInstrumento", ord.IdInstrumento);
                    op.addProperty("IdInterno", ord.IdInterno);
                    op.addProperty("IdPortafolio", ord.IdPortafolio);
                    op.addProperty("IdTipoCanalIngreso", ord.IdTipoCanalIngreso);
                    op.addProperty("IdTipoOperacion", ord.IdTipoOperacion);
                    op.addProperty("IdTipoProducto", ord.IdTipoProducto);
                    ordenesArr.add(op);
                }
                ccpParams.add("p_Ordenes", ordenesArr);
                JsonObject ccpResult = consultarCobrosOrdenesPeService.execute(ccpParams);

                if (ccpResult.has("P_OUT_ERRORESCONTADOR") && ccpResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                    addMensaje(mensajes, "ERROR", "PORTAFOLIO_SIN_COBROS_CONFIGURADOS");
                    return buildSalida(0L, mensajes, new JsonArray(), new JsonArray());
                }

                long idInternoCobro = 0L;
                if (ccpResult.has("p_out_Cobros")) {
                    for (var el : ccpResult.get("p_out_Cobros").getAsJsonArray()) {
                        JsonObject def = el.getAsJsonObject();
                        Long idInterno = getLong(def, "IdInterno");
                        OrdenProceso ord = ordenesHash.get(idInterno);
                        if (ord == null) continue;
                        DefinicionCobroProceso dcob = new DefinicionCobroProceso();
                        dcob.IdInternoCobro = idInternoCobro++;
                        dcob.IdInterno = idInterno;
                        dcob.IdOrigenCobro = getStr(def, "IdOrigenCobro");
                        dcob.CodigoDefinicionCobro = getStr(def, "CodigoDefinicionCobro");
                        dcob.IdTipoCobro = getStr(def, "IdTipoCobro");
                        dcob.NombreTipoCobro = getStr(def, "NombreTipoCobro");
                        dcob.IdClaseCobro = getStr(def, "IdClaseCobro");
                        dcob.IdClaseCobroBase = getStr(def, "IdClaseCobroBase");
                        dcob.ValorCobroPerfil = getBD(def, "ValorCobroPerfil");
                        dcob.ValorCobroAplicado = dcob.ValorCobroPerfil;
                        dcob.MontoCobroCalculado = BigDecimal.ZERO;
                        dcob.MontoCobroCalculadoLocal = BigDecimal.ZERO;
                        dcob.MontoCobroCalculadoLiquidacion = BigDecimal.ZERO;
                        dcob.IdMonedaCobro = getStr(def, "IdMonedaCobro");
                        dcob.Otros_CheckOperacionPlazo = getStr(def, "Otros_CheckOperacionPlazo", "N");
                        dcob.Otros_CheckPermiteAumentarValor = getStr(def, "Otros_CheckPermiteAumentarValor", "N");
                        dcob.Otros_CheckPermiteDisminuirValor = getStr(def, "Otros_CheckPermiteDisminuirValor", "N");
                        dcob.Otros_CheckAllIn = getStr(def, "Otros_CheckAllIn", "N");
                        dcob.Descripcion = getStr(def, "Descripcion");
                        dcob.PorcentualFijo_ValorMontoMinimo = getBD(def, "PorcentualFijo_ValorMontoMinimo");
                        dcob.PorcentualRango_IdMonedaRangoMonto = getStr(def, "PorcentualRango_IdMonedaRangoMonto");
                        dcob.PorcentualRango_IdTipoRangoCobro = getStr(def, "PorcentualRango_IdTipoRangoCobro");
                        ord.definicionCobros.add(dcob);
                        cobrosPeruHash.put(dcob.IdInternoCobro, dcob);
                    }
                }
            } catch (Exception e) {
                log.warn("ConsultarCobrosOrdenesPe error: {}", e.getMessage());
            }

            // Apply cobros modificados PE
            for (var el : cobrosModIn) {
                JsonObject cm = el.getAsJsonObject();
                Long idInterno = getLong(cm, "IdInterno");
                String idCobro = getStr(cm, "IdCobro");
                BigDecimal valor = getBD(cm, "ValorCobroAplicado");
                OrdenProceso ord = ordenesHash.get(idInterno);
                if (ord == null) continue;
                for (DefinicionCobroProceso dc : ord.definicionCobros) {
                    if (idCobro.equalsIgnoreCase(dc.CodigoDefinicionCobro)) dc.ValorCobroAplicado = valor;
                }
            }
        }

        // ── Orden por monto ────────────────────────────────────────────────────
        long idTrabajo = 5000L;
        for (OrdenProceso ord : ordenes) {
            if (!"S".equalsIgnoreCase(ord.EsPorMonto)) continue;

            if ("PE".equalsIgnoreCase(localizacion)) {
                if (ord.PrecioOrden != null && ord.PrecioOrden.compareTo(BigDecimal.ZERO) != 0) {
                    ord.CantidadOrden = ord.MontoTotal.divide(ord.PrecioOrden, 0, RoundingMode.FLOOR);
                }
                continue;
            }

            // CL: convert price to liquidation currency via CalcularParidad
            BigDecimal precioLiq = BigDecimal.ZERO;
            try {
                idTrabajo++;
                JsonObject parIn = buildParidadRequest(idEmpresa, idTrabajo, List.of(
                    buildCalculo(0L, fechaSistema, idTrabajo, ord.IdMonedaLiquidacionOrden, ord.IdMonedaOrden,
                        ord.PrecioOrden, ord.TipoCambio, idUso(ord.IdTipoOperacion, localizacion, ord.IdBolsa))
                ));
                JsonObject parOut = plataformaClient.call("cl.bcs.plataforma.mer.CalcularParidad", parIn);
                precioLiq = getFirstCalcMonto(parOut);
            } catch (Exception e) {
                log.warn("CalcularParidad (precio) error: {}", e.getMessage());
                precioLiq = ord.PrecioOrden != null ? ord.PrecioOrden : BigDecimal.ONE;
            }

            // Sum cobros
            BigDecimal sumaVar = BigDecimal.ZERO, sumaFijos = BigDecimal.ZERO, sumaImp = BigDecimal.ZERO;
            for (CobroProceso cob : ord.cobros) {
                if ("IMPUESTO".equalsIgnoreCase(cob.CodigoCobroClasificacion)) {
                    sumaImp = sumaImp.add(cob.ValorCobroPerfil.divide(new BigDecimal(100)));
                } else if ("V".equalsIgnoreCase(cob.FormaCobro)) {
                    BigDecimal vApl = "S".equalsIgnoreCase(cob.PermiteCambiarValor)
                        ? cob.ValorCobroAplicado : cob.ValorCobroPerfil;
                    sumaVar = sumaVar.add(vApl.divide(new BigDecimal(100)));
                } else if ("F".equalsIgnoreCase(cob.FormaCobro)) {
                    BigDecimal vFijo = "S".equalsIgnoreCase(cob.PermiteCambiarValor)
                        ? (cob.ValorCobroAplicado != null ? cob.ValorCobroAplicado : BigDecimal.ZERO)
                        : (cob.ValorCobroPerfil != null ? cob.ValorCobroPerfil : BigDecimal.ZERO);
                    try {
                        idTrabajo++;
                        JsonObject parIn2 = buildParidadRequest(idEmpresa, idTrabajo, List.of(
                            buildCalculo(cob.IdInternoCobro, fechaSistema, idTrabajo,
                                ord.IdMonedaLiquidacionOrden, cob.IdMoneda, vFijo,
                                ord.TipoCambio, idUso(ord.IdTipoOperacion, localizacion, ord.IdBolsa))
                        ));
                        JsonObject parOut2 = plataformaClient.call("cl.bcs.plataforma.mer.CalcularParidad", parIn2);
                        sumaFijos = sumaFijos.add(getFirstCalcMonto(parOut2));
                    } catch (Exception e) {
                        sumaFijos = sumaFijos.add(vFijo);
                    }
                }
            }

            // Get puntas
            BigDecimal puntaCompra = BigDecimal.ZERO, puntaVenta = BigDecimal.ZERO, precioCierre = BigDecimal.ZERO;
            try {
                JsonObject pInstr = new JsonObject();
                pInstr.addProperty("p_IdEmpresa", idEmpresa);
                JsonArray instrArr = new JsonArray();
                JsonObject instr = new JsonObject();
                instr.addProperty("Fecha", ord.FechaOperacion != null ? ord.FechaOperacion.substring(0, 10) : fechaSistema.substring(0, 10));
                instr.addProperty("IdInstrumento", ord.IdInstrumento);
                instr.addProperty("IdEmpresa", idEmpresa);
                instrArr.add(instr);
                pInstr.add("p_Instrumentos", instrArr);
                JsonObject pOut = plataformaClient.call("cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa", pInstr);
                if (pOut.has("P_OUT_INSTRUMENTO") && pOut.get("P_OUT_INSTRUMENTO").isJsonArray()) {
                    JsonArray arr = pOut.get("P_OUT_INSTRUMENTO").getAsJsonArray();
                    if (arr.size() > 0) {
                        JsonObject p0 = arr.get(0).getAsJsonObject();
                        puntaCompra = getBD(p0, "PuntaCompra");
                        puntaVenta = getBD(p0, "PuntaVenta");
                        precioCierre = getBD(p0, "PrecioCierre");
                    }
                }
            } catch (Exception e) {
                log.warn("ConsultarInstrumentoPrecioBolsa error: {}", e.getMessage());
            }

            if (ACCION_COMPRA.equalsIgnoreCase(ord.AccionOrden)) {
                if ("S".equalsIgnoreCase(parametroCalculoPrecioPunta)) {
                    BigDecimal precio = precioCierre.compareTo(puntaVenta) > 0 ? precioCierre : puntaVenta;
                    if (precio.compareTo(BigDecimal.ZERO) == 0) precio = precioCierre;
                    if (precio.compareTo(BigDecimal.ZERO) != 0) {
                        ord.CantidadOrden = new BigDecimal(Math.floor(ord.MontoTotal.divide(precio, 20, RoundingMode.HALF_UP).doubleValue()));
                    }
                } else {
                    BigDecimal k = BigDecimal.ONE.add(sumaVar.multiply(BigDecimal.ONE.add(sumaImp)));
                    BigDecimal fijoBruto = sumaFijos.multiply(BigDecimal.ONE.add(sumaImp));
                    BigDecimal num = ord.MontoTotal.subtract(fijoBruto);
                    BigDecimal den = precioLiq.multiply(k);
                    BigDecimal q = den.compareTo(BigDecimal.ZERO) != 0 ? num.divideToIntegralValue(den) : BigDecimal.ZERO;
                    if (q.signum() < 0) q = BigDecimal.ZERO;
                    // Try Q+1
                    BigDecimal qPlus = q.add(BigDecimal.ONE);
                    BigDecimal subtot = qPlus.multiply(precioLiq).setScale(0, RoundingMode.HALF_UP);
                    BigDecimal comVar = subtot.multiply(sumaVar).setScale(0, RoundingMode.HALF_UP);
                    BigDecimal ivaFijo = sumaFijos.multiply(sumaImp).setScale(0, RoundingMode.HALF_UP);
                    BigDecimal ivaComVar = comVar.multiply(sumaImp).setScale(0, RoundingMode.HALF_UP);
                    BigDecimal totalQ = subtot.add(sumaFijos).add(ivaFijo).add(comVar).add(ivaComVar).setScale(0, RoundingMode.HALF_UP);
                    if (totalQ.compareTo(ord.MontoTotal) <= 0) q = qPlus;
                    ord.CantidadOrden = q;
                }
            } else if (ACCION_VENTA.equalsIgnoreCase(ord.AccionOrden)) {
                if ("S".equalsIgnoreCase(parametroCalculoPrecioPunta)) {
                    BigDecimal precio = precioCierre.compareTo(puntaCompra) < 0 ? precioCierre : puntaCompra;
                    if (precio.compareTo(BigDecimal.ZERO) == 0) precio = precioCierre;
                    if (precio.compareTo(BigDecimal.ZERO) != 0) {
                        ord.CantidadOrden = new BigDecimal(Math.floor(ord.MontoTotal.divide(precio, 20, RoundingMode.HALF_UP).doubleValue()));
                    }
                } else {
                    BigDecimal a = BigDecimal.ONE.subtract(sumaVar.multiply(BigDecimal.ONE.add(sumaImp)));
                    BigDecimal fijoBruto = sumaFijos.multiply(BigDecimal.ONE.add(sumaImp));
                    BigDecimal num = ord.MontoTotal.add(fijoBruto);
                    BigDecimal den = precioLiq.multiply(a);
                    BigDecimal q = den.compareTo(BigDecimal.ZERO) != 0 ? num.divideToIntegralValue(den) : BigDecimal.ZERO;
                    if (q.signum() < 0) q = BigDecimal.ZERO;
                    BigDecimal qPlus = q.add(BigDecimal.ONE);
                    BigDecimal netoQ = qPlus.multiply(precioLiq).multiply(a).subtract(fijoBruto).setScale(0, RoundingMode.DOWN);
                    if (netoQ.compareTo(ord.MontoTotal) <= 0) q = qPlus;
                    ord.CantidadOrden = q;
                }
            }
            if (ord.CantidadOrden.compareTo(BigDecimal.ZERO) < 0) ord.CantidadOrden = BigDecimal.ZERO;
        }

        // ── Correccion de cantidades, montos ───────────────────────────────────
        for (OrdenProceso ord : ordenes) {
            Instrumento inst = instrumentos.get(ord.IdInstrumento);
            if (inst == null) continue;

            if (CLSIF_RV.equalsIgnoreCase(inst.IdClasificacionTipoInstrumento)) {
                ord.ValorRescateOrden = BigDecimal.ZERO;
                if (TIPO_COMSIM.equalsIgnoreCase(ord.IdTipoOperacion) && CANAL_AGENTE.equalsIgnoreCase(ord.IdTipoCanalIngreso)) {
                    ord.CantidadOrden = BigDecimal.ONE;
                }
                if (!(TIPO_COMSIM.equalsIgnoreCase(ord.IdTipoOperacion) && CANAL_AGENTE.equalsIgnoreCase(ord.IdTipoCanalIngreso))) {
                    if (ord.CantidadOrden.compareTo(BigDecimal.ZERO) != 0) {
                        ord.MontoOrden = ord.CantidadOrden.multiply(ord.PrecioOrden);
                    }
                }
            } else if (CLSIF_DV.equalsIgnoreCase(inst.IdClasificacionTipoInstrumento)) {
                ord.ValorRescateOrden = BigDecimal.ZERO;
                ord.MontoOrden = ord.CantidadOrden.multiply(ord.PrecioOrden)
                    .multiply(new BigDecimal(inst.Tamano != null ? inst.Tamano : 1L));
            } else if (CLSIF_RF.equalsIgnoreCase(inst.IdClasificacionTipoInstrumento)) {
                ord.ValorRescateOrden = BigDecimal.ZERO;
            } else if (CLSIF_IF.equalsIgnoreCase(inst.IdClasificacionTipoInstrumento)) {
                if ("S".equalsIgnoreCase(ord.EsFungible)) ord.CantidadOrden = BigDecimal.ONE;
            } else if (CLSIF_RE.equalsIgnoreCase(inst.IdClasificacionTipoInstrumento)) {
                ord.ValorRescateOrden = BigDecimal.ZERO;
                if (TIPO_VENRRV.equalsIgnoreCase(ord.IdTipoOperacion)) {
                    ord.MontoOrden = ord.CantidadOrden.multiply(ord.PrecioOrden.setScale(2, RoundingMode.HALF_UP));
                }
            }
            // MonedaFormato — simplified: round to 0 decimals for CLP-like currencies
            ord.MontoOrden = redondearMonto(ord.MontoOrden);
        }

        // ── Valorizacion RF PE (ObtenerDiaSiguiente + ValorizarInstrumento) ───
        {
            List<JsonObject> rfOrdenes = new ArrayList<>();
            for (OrdenProceso ord : ordenes) {
                if (TIPO_VENRRF.equalsIgnoreCase(ord.IdTipoOperacion) || TIPO_COMRRF.equalsIgnoreCase(ord.IdTipoOperacion)) {
                    rfOrdenes.add(GSON.fromJson(GSON.toJson(ord), JsonObject.class));
                }
            }
            if (!rfOrdenes.isEmpty()) {
                try {
                    // ObtenerDiaSiguiente
                    Map<String, String> fechasCalculadas = new HashMap<>();
                    JsonObject dsParams = new JsonObject();
                    dsParams.addProperty("p_idPais", idPaisEmpresa);
                    dsParams.addProperty("p_diaHabil", "N");
                    JsonArray fechasArr = new JsonArray();
                    for (OrdenProceso ord : ordenes) {
                        if (!TIPO_VENRRF.equalsIgnoreCase(ord.IdTipoOperacion) && !TIPO_COMRRF.equalsIgnoreCase(ord.IdTipoOperacion)) continue;
                        JsonObject f = new JsonObject();
                        f.addProperty("fechaInicial", ord.FechaOperacion);
                        f.addProperty("numeroDias", ord.PlazoReporte);
                        fechasArr.add(f);
                    }
                    dsParams.add("p_fechas", fechasArr);
                    JsonObject dsOut = plataformaClient.call("cl.bcs.plataforma.pla.ObtenerDiaSiguiente", dsParams);
                    if (dsOut.has("p_out_fechas")) {
                        for (var el : dsOut.get("p_out_fechas").getAsJsonArray()) {
                            JsonObject f = el.getAsJsonObject();
                            String key = getStr(f, "fechaInicial") + "|" + getLong(f, "numeroDiasInicial");
                            fechasCalculadas.put(key, getStr(f, "fechaCalculada"));
                        }
                    }

                    // ValorizarInstrumento (BcsCustodia)
                    JsonObject valParams = new JsonObject();
                    valParams.addProperty("p_IdEmpresa", idEmpresa);
                    valParams.addProperty("p_IdUsuario", idUsuario);
                    valParams.addProperty("p_IdModulo", idModulo);
                    valParams.addProperty("p_IdPais", idPaisEmpresa);
                    valParams.addProperty("p_OrigenProcesamiento", "MONTO_COBRO_ORDEN");
                    JsonArray instrValArr = new JsonArray();
                    int iVal = 0;
                    for (OrdenProceso ord : ordenes) {
                        if (!TIPO_VENRRF.equalsIgnoreCase(ord.IdTipoOperacion) && !TIPO_COMRRF.equalsIgnoreCase(ord.IdTipoOperacion)) continue;
                        String keyInstr = ord.IdInstrumentoSubyacente != null
                            ? String.valueOf(ord.IdInstrumentoSubyacente) : String.valueOf(ord.IdInstrumento);
                        Instrumento inst = (Instrumento) instrumentosHash.get(keyInstr);
                        if (inst == null) inst = instrumentos.get(ord.IdInstrumento);
                        if (inst == null) continue;

                        String fechaKey = ord.FechaOperacion + "|" + ord.PlazoReporte;
                        String fechaVto = fechasCalculadas.getOrDefault(fechaKey, ord.FechaOperacion);

                        JsonObject iv = new JsonObject();
                        iv.addProperty("IdValorizacion", ord.IdInterno);
                        iv.addProperty("FechaOperacion", ord.FechaOperacion);
                        iv.addProperty("FechaVencimientoOperacion", fechaVto);
                        iv.addProperty("PlazoEnDiasOperacion", ord.PlazoReporte);
                        iv.addProperty("FechaValorizacion", ord.FechaOperacion);
                        iv.addProperty("IdClasificacionTipoInstrumento", inst.IdClasificacionTipoInstrumento);
                        iv.addProperty("IdTipoInstrumento", inst.IdTipoInstrumento);
                        iv.addProperty("IdInstrumento", inst.IdInstrumento);
                        iv.addProperty("IdMonedaInstrumento", inst.IdMoneda);
                        iv.addProperty("IdTipoReajusteInstrumento", inst.IdTipoReajuste);
                        iv.addProperty("Cantidad", ord.CantidadOrden);
                        iv.addProperty("PrecioCompra", ord.PrecioOrden);
                        instrValArr.add(iv);
                        iVal++;
                    }
                    if (iVal > 0) {
                        valParams.add("p_Instrumentos", instrValArr);
                        try {
                            JsonObject valBody = new JsonObject();
                            valBody.addProperty("class", "cl.bcs.custodia.ValorizarInstrumento");
                            valBody.add("params", valParams);
                            String valResp = custodiaClient.post()
                                .uri("/internal/java-call")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .body(valBody.toString())
                                .retrieve()
                                .body(String.class);
                            if (valResp != null && !valResp.isBlank()) {
                                JsonObject valOut = JsonParser.parseString(valResp).getAsJsonObject();
                                if (valOut.has("P_OUT_INSTRUMENTOSVALORIZADOS")) {
                                    for (var el : valOut.get("P_OUT_INSTRUMENTOSVALORIZADOS").getAsJsonArray()) {
                                        JsonObject iv2 = el.getAsJsonObject();
                                        String dimTasa = getStr(iv2, "DimensionTasa");
                                        String dimTiempo = getStr(iv2, "DimensionTiempo");
                                        if ("COMP".equalsIgnoreCase(dimTasa) && "AC".equalsIgnoreCase(dimTiempo)) {
                                            Long idVal = getLong(iv2, "IdValorizacion");
                                            OrdenProceso ord = ordenesHash.get(idVal);
                                            if (ord != null) {
                                                ord.MontoInteresContado = getBD(iv2, "MontoInteresCorteCuponMonedaInstrumento");
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn("ValorizarInstrumento (BcsCustodia) error: {}", e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    log.warn("RF valorization error: {}", e.getMessage());
                }
            }
        }

        // ── Operaciones a plazo SIM/REPORTE ───────────────────────────────────
        for (OrdenProceso ord : ordenes) {
            if (ord.TipoInteres != null && "SIMPLE".equalsIgnoreCase(ord.TipoInteres)
                    && TIPO_VENSIM.equalsIgnoreCase(ord.IdTipoOperacion)) {
                BigDecimal factor = ord.TasaSimultanea.multiply(new BigDecimal(ord.PlazoSimultanea))
                    .divide(new BigDecimal(ord.BaseTasa), 20, RoundingMode.HALF_UP)
                    .divide(new BigDecimal(100), 20, RoundingMode.HALF_UP);
                ord.PrecioPlazo = ord.PrecioOrden.add(ord.PrecioOrden.multiply(factor));
                ord.MontoOperacionPlazo = ord.CantidadOrden.multiply(ord.PrecioPlazo);
            } else if (ord.TipoInteres != null && "SIMPLE".equalsIgnoreCase(ord.TipoInteres)
                    && TIPO_COMSIM.equalsIgnoreCase(ord.IdTipoOperacion)) {
                ord.PrecioPlazo = BigDecimal.ZERO;
                BigDecimal factor = ord.TasaSimultanea.multiply(new BigDecimal(ord.PlazoSimultanea))
                    .divide(new BigDecimal(ord.BaseTasa), 20, RoundingMode.HALF_UP)
                    .divide(new BigDecimal(100), 20, RoundingMode.HALF_UP);
                ord.MontoOperacionPlazo = ord.MontoOrden.add(ord.MontoOrden.multiply(factor));
            } else if (ord.TipoInteres != null && "COMPUESTO".equalsIgnoreCase(ord.TipoInteres)
                    && (TIPO_VENRRV.equalsIgnoreCase(ord.IdTipoOperacion) || TIPO_COMRRV.equalsIgnoreCase(ord.IdTipoOperacion))) {
                BigDecimal tasa = ord.TasaReporte.divide(new BigDecimal(100), 20, RoundingMode.HALF_UP);
                BigDecimal periodos = new BigDecimal(ord.PlazoReporte).divide(new BigDecimal(ord.BaseTasa), 20, RoundingMode.HALF_UP);
                double factor = Math.pow(BigDecimal.ONE.add(tasa).doubleValue(), periodos.doubleValue());
                ord.MontoOperacionPlazo = ord.MontoOrden.multiply(new BigDecimal(factor));
                if (ord.CantidadOrden.compareTo(BigDecimal.ZERO) != 0)
                    ord.PrecioPlazo = ord.MontoOperacionPlazo.divide(ord.CantidadOrden, 20, RoundingMode.HALF_UP);
            } else if (ord.TipoInteres != null && "COMPUESTO".equalsIgnoreCase(ord.TipoInteres)
                    && (TIPO_VENRRF.equalsIgnoreCase(ord.IdTipoOperacion) || TIPO_COMRRF.equalsIgnoreCase(ord.IdTipoOperacion))) {
                BigDecimal tasa = ord.TasaReporte.divide(new BigDecimal(100), 20, RoundingMode.HALF_UP);
                BigDecimal periodos = new BigDecimal(ord.PlazoReporte).divide(new BigDecimal(ord.BaseTasa), 20, RoundingMode.HALF_UP);
                BigDecimal montoMasInteres = ord.MontoOrden.add(ord.MontoInteresContado);
                double factor = Math.pow(BigDecimal.ONE.add(tasa).doubleValue(), periodos.doubleValue());
                BigDecimal montoFactor = montoMasInteres.multiply(new BigDecimal(factor));
                ord.PrecioPlazo = (montoFactor.subtract(ord.MontoInteresPlazo))
                    .divide(ord.CantidadOrden, 8, RoundingMode.HALF_UP);
                ord.MontoOperacionPlazo = ord.PrecioPlazo.multiply(ord.CantidadOrden);
            } else {
                ord.PrecioPlazo = BigDecimal.ZERO;
                ord.MontoOperacionPlazo = BigDecimal.ZERO;
            }
            ord.MontoOperacionPlazo = redondearMonto(ord.MontoOperacionPlazo);
        }

        // ── CalcularCobros CL ─────────────────────────────────────────────────
        if ("CL".equalsIgnoreCase(localizacion)) {
            // Monto orden a moneda cobro
            try {
                idTrabajo++;
                List<JsonObject> calculos = new ArrayList<>();
                for (OrdenProceso ord : ordenes) {
                    for (CobroProceso cob : ord.cobros) {
                        calculos.add(buildCalculo(cob.IdInternoCobro, fechaSistema, idTrabajo,
                            cob.IdMoneda, ord.IdMonedaOrden, ord.MontoOrden, ord.TipoCambio,
                            idUso(ord.IdTipoOperacion, localizacion, ord.IdBolsa)));
                    }
                }
                if (!calculos.isEmpty()) {
                    JsonObject parIn = buildParidadRequest(idEmpresa, idTrabajo, calculos);
                    JsonObject parOut = plataformaClient.call("cl.bcs.plataforma.mer.CalcularParidad", parIn);
                    if (parOut.has("p_out_CalculosSalida")) {
                        for (var el : parOut.get("p_out_CalculosSalida").getAsJsonArray()) {
                            JsonObject calc = el.getAsJsonObject();
                            Long idInt = getLong(calc, "IdInterno");
                            CobroProceso cob = cobrosHash.get(idInt);
                            if (cob != null && "OK".equals(getStr(calc, "Resultado"))) {
                                cob.MontoOperacionMonedaCobro = getBD(calc, "MontoDestino");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("CalcularParidad (monto cobro) error: {}", e.getMessage());
            }

            // CalcularCobros
            try {
                for (OrdenProceso ord : ordenes) {
                    JsonObject ccParams = new JsonObject();
                    ccParams.addProperty("DiasPlazo", ord.PlazoSimultanea);
                    ccParams.addProperty("Tasa", ord.TasaSimultanea);
                    JsonArray cobArr = new JsonArray();
                    for (CobroProceso cob : ord.cobros) {
                        JsonObject c = new JsonObject();
                        c.addProperty("IdInternoCobro", cob.IdInternoCobro);
                        c.addProperty("MontoOperacionMonedaCobro", cob.MontoOperacionMonedaCobro);
                        c.addProperty("IdCobro", cob.IdCobro);
                        c.addProperty("ValorCobroPerfil", cob.ValorCobroPerfil);
                        c.addProperty("MontoMinimo", cob.MontoMinimo);
                        c.addProperty("CodigoCobroClasificacion", cob.CodigoCobroClasificacion);
                        c.addProperty("PermiteCambiarValor", cob.PermiteCambiarValor);
                        c.addProperty("ValorCobroAplicado", cob.ValorCobroAplicado);
                        c.addProperty("FormaCobro", cob.FormaCobro);
                        c.addProperty("IdMonedaCobro", cob.IdMoneda);
                        cobArr.add(c);
                    }
                    ccParams.add("cobros", cobArr);
                    JsonObject ccOut = plataformaClient.call("cl.bcs.plataforma.com.CalcularCobros", ccParams);
                    if (ccOut.has("p_out_Cobros")) {
                        for (var el : ccOut.get("p_out_Cobros").getAsJsonArray()) {
                            JsonObject cs = el.getAsJsonObject();
                            Long idInt = getLong(cs, "IdInternoCobro");
                            CobroProceso cob = cobrosHash.get(idInt);
                            if (cob != null) {
                                cob.ValorCobroFinal = getBD(cs, "ValorCobroFinal");
                                cob.ValorCobroAplicado = cob.ValorCobroFinal;
                                cob.MontoCobroCalculado = redondearMonto(getBD(cs, "MontoCobroCalculado"));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("CalcularCobros error: {}", e.getMessage());
            }
        }

        // ── Monto orden a moneda liquidacion + local ───────────────────────────
        try {
            idTrabajo++;
            List<JsonObject> calculos = new ArrayList<>();
            for (OrdenProceso ord : ordenes) {
                Instrumento inst = instrumentos.get(ord.IdInstrumento);
                String fecha = ORIGEN_REFACTURACION.equalsIgnoreCase(ord.GlosaRefactura)
                    && inst != null && CLSIF_IF.equalsIgnoreCase(inst.IdClasificacionTipoInstrumento)
                    ? ord.FechaOperacionOriginal : fechaSistema;
                String destino = TIPO_PROD_RERV.equalsIgnoreCase(ord.IdTipoProducto) || TIPO_PROD_RERF.equalsIgnoreCase(ord.IdTipoProducto)
                    ? ord.IdMonedaOrden : ord.IdMonedaLiquidacionOrden;
                calculos.add(buildCalculo(ord.IdInterno, fecha, idTrabajo, destino,
                    ord.IdMonedaOrden, ord.MontoOrden, ord.TipoCambio, idUso(ord.IdTipoOperacion, localizacion, ord.IdBolsa)));
            }
            if (!calculos.isEmpty()) {
                JsonObject parIn = buildParidadRequest(idEmpresa, idTrabajo, calculos);
                JsonObject parOut = plataformaClient.call("cl.bcs.plataforma.mer.CalcularParidad", parIn);
                if (parOut.has("p_out_CalculosSalida")) {
                    for (var el : parOut.get("p_out_CalculosSalida").getAsJsonArray()) {
                        JsonObject calc = el.getAsJsonObject();
                        Long idInt = getLong(calc, "IdInterno");
                        OrdenProceso ord = ordenesHash.get(idInt);
                        if (ord == null) continue;
                        if (!"OK".equals(getStr(calc, "Resultado"))) {
                            addMensaje(mensajes, "ERROR", getStr(calc, "CodigoMensaje"), idInt);
                            return buildSalida(1L, mensajes, new JsonArray(), new JsonArray());
                        }
                        if (TIPO_PROD_RERV.equalsIgnoreCase(ord.IdTipoProducto)) {
                            ord.MontoOperacionLiquidacion = redondearMonto(
                                calculaMontoParidad(ord.PrecioOrden, ord.CantidadOrden, getBD(calc, "FactorOrigenDestino")));
                        } else {
                            ord.MontoOperacionLiquidacion = redondearMonto(getBD(calc, "MontoDestino"));
                        }
                        if ("COP".equals(ord.IdMonedaOrden) || "PEN".equals(ord.IdMonedaOrden)) {
                            ord.MontoOrden = ord.MontoOperacionLiquidacion;
                        }
                        ord.MontoOperacionLocal = redondearMonto(getBD(calc, "MontoLocal"));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("CalcularParidad (liq+local) error: {}", e.getMessage());
        }

        // ── Monto plazo a liquidacion + local ─────────────────────────────────
        try {
            idTrabajo++;
            List<JsonObject> calculos = new ArrayList<>();
            for (OrdenProceso ord : ordenes) {
                if (ord.MontoOperacionPlazo.compareTo(BigDecimal.ZERO) <= 0) continue;
                String destino = TIPO_PROD_RERV.equalsIgnoreCase(ord.IdTipoProducto) || TIPO_PROD_RERF.equalsIgnoreCase(ord.IdTipoProducto)
                    ? ord.IdMonedaOrden : ord.IdMonedaLiquidacionOrden;
                calculos.add(buildCalculo(ord.IdInterno, fechaSistema, idTrabajo, destino,
                    ord.IdMonedaOrden, ord.MontoOperacionPlazo, ord.TipoCambio, idUso(ord.IdTipoOperacion, localizacion, ord.IdBolsa)));
            }
            if (!calculos.isEmpty()) {
                JsonObject parIn = buildParidadRequest(idEmpresa, idTrabajo, calculos);
                JsonObject parOut = plataformaClient.call("cl.bcs.plataforma.mer.CalcularParidad", parIn);
                if (parOut.has("p_out_CalculosSalida")) {
                    for (var el : parOut.get("p_out_CalculosSalida").getAsJsonArray()) {
                        JsonObject calc = el.getAsJsonObject();
                        Long idInt = getLong(calc, "IdInterno");
                        OrdenProceso ord = ordenesHash.get(idInt);
                        if (ord == null || !"OK".equals(getStr(calc, "Resultado"))) continue;
                        if (TIPO_PROD_RERV.equalsIgnoreCase(ord.IdTipoProducto)) {
                            ord.MontoOperacionLiquidacionPlazo = redondearMonto(
                                calculaMontoParidad(ord.PrecioOrden, ord.CantidadOrden, getBD(calc, "FactorOrigenDestino")));
                        } else {
                            ord.MontoOperacionLiquidacionPlazo = redondearMonto(getBD(calc, "MontoDestino"));
                        }
                        ord.MontoOperacionLocalPlazo = redondearMonto(getBD(calc, "MontoLocal"));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("CalcularParidad (plazo) error: {}", e.getMessage());
        }

        // ── CalcularCobrosPE ──────────────────────────────────────────────────
        if ("PE".equalsIgnoreCase(localizacion)) {
            try {
                JsonObject ccpParams = new JsonObject();
                ccpParams.addProperty("p_IdEmpresa", idEmpresa);
                JsonArray opsArr = new JsonArray();
                JsonArray cobsArr = new JsonArray();
                for (OrdenProceso ord : ordenes) {
                    JsonObject op = new JsonObject();
                    op.addProperty("CantidadOperacion", ord.CantidadOrden);
                    op.addProperty("MontoOperacionPlazo", ord.MontoOperacionPlazo);
                    op.addProperty("FechaOperacion", ord.FechaOperacion);
                    op.addProperty("IdInterno", ord.IdInterno);
                    op.addProperty("IdMonedaLiquidacion", ord.IdMonedaLiquidacionOrden);
                    op.addProperty("IdMonedaOperacion", ord.IdMonedaLiquidacionOrden);
                    op.addProperty("MontoOperacion", ord.MontoOrden);
                    op.addProperty("PlazoEnDias", ord.PlazoReporte);
                    op.addProperty("Tasa", ord.TasaReporte);
                    op.addProperty("TasaReportado", ord.TasaReportado);
                    op.addProperty("TipoCambio", ord.TipoCambio);
                    String liqBase = TIPO_PROD_RERV.equalsIgnoreCase(ord.IdTipoProducto) || TIPO_PROD_RERF.equalsIgnoreCase(ord.IdTipoProducto)
                        ? ord.MontoOrden.toPlainString() : ord.MontoOperacionLiquidacion.toPlainString();
                    op.addProperty("MontoLiquidacionOperacion", liqBase);
                    op.addProperty("BaseTasa", ord.BaseTasa);
                    op.addProperty("PorcentajeAllIn", ord.PorcentajeAllIn);
                    op.addProperty("MontoAllIn", ord.MontoAllIn);
                    op.addProperty("MontoInteresContado", ord.MontoInteresContado);
                    op.addProperty("MontoInteresPlazo", ord.MontoInteresPlazo);
                    op.addProperty("AccionOrden", ord.AccionOrden);
                    opsArr.add(op);
                    for (DefinicionCobroProceso dc : ord.definicionCobros) {
                        JsonObject c = GSON.fromJson(GSON.toJson(dc), JsonObject.class);
                        cobsArr.add(c);
                    }
                }
                ccpParams.add("p_Operaciones", opsArr);
                ccpParams.add("p_Cobros", cobsArr);
                JsonObject ccpOut = plataformaClient.call("cl.bcs.plataforma.com.pe.CalcularCobrosPe", ccpParams);
                if (ccpOut.has("p_out_Cobros")) {
                    for (var el : ccpOut.get("p_out_Cobros").getAsJsonArray()) {
                        JsonObject cs = el.getAsJsonObject();
                        Long idInt = getLong(cs, "IdInternoCobro");
                        DefinicionCobroProceso dc = cobrosPeruHash.get(idInt);
                        if (dc != null) {
                            dc.ValorCobroFinal = getBD(cs, "ValorCobroFinal");
                            dc.ValorCobroPerfil = getBD(cs, "ValorCobroPerfil");
                            dc.IdMonedaCobro = getStr(cs, "IdMonedaCobro");
                            dc.MontoCobroCalculado = getBD(cs, "MontoCobroCalculado");
                            dc.MontoCobroCalculadoLiquidacion = getBD(cs, "MontoCobroCalculadoLiquidacion");
                            dc.MontoCobroCalculadoLocal = getBD(cs, "MontoCobroCalculadoLocal");
                        }
                    }
                }
                if (ccpOut.has("p_out_operaciones")) {
                    for (var el : ccpOut.get("p_out_operaciones").getAsJsonArray()) {
                        JsonObject op = el.getAsJsonObject();
                        Long idInt = getLong(op, "IdInterno");
                        OrdenProceso ord = ordenesHash.get(idInt);
                        if (ord == null) continue;
                        ord.MontoAllIn = getBD(op, "MontoAllIn");
                        ord.PorcentajeAllIn = getBD(op, "PorcentajeAllIn");
                        if (TIPO_PROD_RERV.equalsIgnoreCase(ord.IdTipoProducto) || TIPO_PROD_RERF.equalsIgnoreCase(ord.IdTipoProducto)) {
                            ord.MontoOperacionLocal = getBD(op, "MontoOperacion");
                            ord.MontoOperacionLocalPlazo = getBD(op, "MontoOperacionPlazo");
                        } else {
                            ord.MontoOrden = getBD(op, "MontoOperacion");
                            ord.MontoOperacionPlazo = getBD(op, "MontoOperacionPlazo");
                        }
                        ord.MontoOperacionLiquidacion = getBD(op, "MontoLiquidacionOperacion");
                        ord.MontoOperacionLiquidacionPlazo = getBD(op, "MontoLiquidacionOperacionPlazo");
                    }
                }
            } catch (Exception e) {
                log.warn("CalcularCobrosPe error: {}", e.getMessage());
            }
        }

        // ── Cobros CL → moneda liq + local ────────────────────────────────────
        if ("CL".equalsIgnoreCase(localizacion)) {
            try {
                idTrabajo++;
                List<JsonObject> calculos = new ArrayList<>();
                for (OrdenProceso ord : ordenes) {
                    for (CobroProceso cob : ord.cobros) {
                        calculos.add(buildCalculo(cob.IdInternoCobro, fechaSistema, idTrabajo,
                            ord.IdMonedaLiquidacionOrden, cob.IdMoneda, cob.MontoCobroCalculado,
                            ord.TipoCambio, idUso(ord.IdTipoOperacion, localizacion, ord.IdBolsa)));
                    }
                }
                if (!calculos.isEmpty()) {
                    JsonObject parIn = buildParidadRequest(idEmpresa, idTrabajo, calculos);
                    JsonObject parOut = plataformaClient.call("cl.bcs.plataforma.mer.CalcularParidad", parIn);
                    if (parOut.has("p_out_CalculosSalida")) {
                        for (var el : parOut.get("p_out_CalculosSalida").getAsJsonArray()) {
                            JsonObject calc = el.getAsJsonObject();
                            Long idInt = getLong(calc, "IdInterno");
                            CobroProceso cob = cobrosHash.get(idInt);
                            if (cob == null) continue;
                            if (!"OK".equals(getStr(calc, "Resultado"))) {
                                addMensaje(mensajes, "ERROR", getStr(calc, "CodigoMensaje"), null);
                                return buildSalida(1L, mensajes, new JsonArray(), new JsonArray());
                            }
                            cob.MontoCobroCalculadoLiquidacion = redondearMonto(getBD(calc, "MontoDestino"));
                            cob.MontoCobroCalculadoLocal = redondearMonto(getBD(calc, "MontoLocal"));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("CalcularParidad (cobros liq) error: {}", e.getMessage());
            }
        }

        // ── Totales por orden ─────────────────────────────────────────────────
        for (OrdenProceso ord : ordenes) {
            for (CobroProceso cob : ord.cobros) {
                ord.TotalCobrosLiquidacion = ord.TotalCobrosLiquidacion.add(cob.MontoCobroCalculadoLiquidacion);
                ord.TotalCobrosLocal = ord.TotalCobrosLocal.add(cob.MontoCobroCalculadoLocal);
            }
            for (DefinicionCobroProceso dc : ord.definicionCobros) {
                if ("S".equalsIgnoreCase(dc.Otros_CheckOperacionPlazo)) {
                    ord.TotalCobrosLiquidacionPlazo = ord.TotalCobrosLiquidacionPlazo.add(dc.MontoCobroCalculadoLiquidacion);
                } else {
                    ord.TotalCobrosLiquidacion = ord.TotalCobrosLiquidacion.add(dc.MontoCobroCalculadoLiquidacion);
                    ord.TotalCobrosLocal = ord.TotalCobrosLocal.add(dc.MontoCobroCalculadoLocal);
                }
            }
            if (ACCION_VENTA.equalsIgnoreCase(ord.AccionOrden)) {
                ord.MontoTotalLocal = ord.MontoOperacionLocal.subtract(ord.TotalCobrosLocal);
                ord.MontoTotalLiquidacion = ord.MontoOperacionLiquidacion.subtract(ord.TotalCobrosLiquidacion);
                ord.MontoTotalLiquidacionPlazo = ord.MontoOperacionLiquidacionPlazo.add(ord.TotalCobrosLiquidacionPlazo);
            } else if (ACCION_COMPRA.equalsIgnoreCase(ord.AccionOrden)) {
                ord.MontoTotalLocal = ord.MontoOperacionLocal.add(ord.TotalCobrosLocal);
                ord.MontoTotalLiquidacion = ord.MontoOperacionLiquidacion.add(ord.TotalCobrosLiquidacion);
                ord.MontoTotalLiquidacionPlazo = ord.MontoOperacionLiquidacionPlazo.subtract(ord.TotalCobrosLiquidacionPlazo);
            }
        }

        // ── Cobros ultima orden ────────────────────────────────────────────────
        try {
            List<String> idsTiposOp = new ArrayList<>();
            List<Long> idsPortafolio = new ArrayList<>();
            for (OrdenProceso ord : ordenes) {
                idsTiposOp.add(ord.IdTipoOperacion);
                idsPortafolio.add(ord.IdPortafolio);
            }
            List<Map<String, Object>> ultimasOrdenes = jdbc.queryForList(SQL_COBROS_ULTIMA_ORDEN,
                new MapSqlParameterSource()
                    .addValue("idsTipoOperacion", idsTiposOp)
                    .addValue("idsPortafolio", idsPortafolio));
            for (OrdenProceso ord : ordenes) {
                for (Map<String, Object> cobUlt : ultimasOrdenes) {
                    if (ord.IdPortafolio.equals(toLong(cobUlt.get("IdPortafolio")))
                            && ord.IdTipoOperacion.equalsIgnoreCase(str(cobUlt, "IdTipoOperacion"))) {
                        ord.PorcentajeCobrosUltimaOrden = getBD2(cobUlt, "PorcentajeCobros");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("CobrосUltimaOrden query error: {}", e.getMessage());
        }

        // ── Build output ───────────────────────────────────────────────────────
        JsonArray outOrdenes = new JsonArray();
        JsonArray outCobros = new JsonArray();

        for (OrdenProceso ord : ordenes) {
            JsonObject os = new JsonObject();
            os.addProperty("IdInterno", ord.IdInterno);
            os.addProperty("PorcentajeCobrosUltimaOrden", ord.PorcentajeCobrosUltimaOrden);
            os.addProperty("PrecioPlazo", ord.PrecioPlazo);
            os.addProperty("IdMonedaLiquidacionOrden", ord.IdMonedaLiquidacionOrden);
            os.addProperty("CantidadOperacion", ord.CantidadOrden);
            os.addProperty("MontoTotalLiquidacion", ord.MontoTotalLiquidacion);
            os.addProperty("MontoOperacion", ord.MontoOrden);
            os.addProperty("MontoOperacionLocal", ord.MontoOperacionLocal);
            os.addProperty("MontoOperacionLiquidacion", ord.MontoOperacionLiquidacion);
            os.addProperty("MontoOperacionPlazo", ord.MontoOperacionPlazo);
            os.addProperty("MontoTotalLocal", ord.MontoTotalLocal);
            os.addProperty("TotalCobrosLocal", ord.TotalCobrosLocal);
            os.addProperty("TotalCobrosLiquidacion", ord.TotalCobrosLiquidacion);
            os.addProperty("MontoOperacionLiquidacionPlazo", ord.MontoOperacionLiquidacionPlazo);
            os.addProperty("MontoOperacionLocalPlazo", ord.MontoOperacionLocalPlazo);
            os.addProperty("MontoTotalLiquidacionPlazo", ord.MontoTotalLiquidacionPlazo);
            os.addProperty("TotalCobrosLiquidacionPlazo", ord.TotalCobrosLiquidacionPlazo);
            os.addProperty("IdMonedaOrden", ord.IdMonedaOrden);
            os.addProperty("TasaReporte", ord.TasaReporte);
            os.addProperty("ValorRescateOrden", ord.ValorRescateOrden);
            os.addProperty("MontoInteresContado", ord.MontoInteresContado);
            os.addProperty("MontoInteresPlazo", ord.MontoInteresPlazo);
            os.addProperty("MontoAllIn", ord.MontoAllIn);
            os.addProperty("PorcentajeAllIn", ord.PorcentajeAllIn);
            outOrdenes.add(os);

            if ("CL".equalsIgnoreCase(localizacion)) {
                for (CobroProceso cob : ord.cobros) {
                    JsonObject cs = new JsonObject();
                    cs.addProperty("IdInterno", cob.IdInterno);
                    cs.addProperty("IdInternoCobro", cob.IdInternoCobro);
                    cs.addProperty("IdCobro", cob.IdCobro);
                    cs.addProperty("NombreCobro", cob.NombreCobro);
                    cs.addProperty("CodigoCobroClasificacion", cob.CodigoCobroClasificacion);
                    cs.addProperty("CodigoCobroClasificacionBase", cob.CodigoCobroClasificacion);
                    cs.addProperty("IdMonedaCobro", cob.IdMoneda);
                    cs.addProperty("ValorCobroPerfil", cob.ValorCobroPerfil);
                    cs.addProperty("PermiteCambiarValor", cob.PermiteCambiarValor);
                    cs.addProperty("FormaCobro", cob.FormaCobro);
                    cs.addProperty("IdMonedaLiquidacionOrden", cob.IdMonedaLiquidacionOrden);
                    cs.addProperty("ValorCobroAplicado", cob.ValorCobroAplicado != null ? cob.ValorCobroAplicado : cob.ValorCobroPerfil);
                    cs.addProperty("ValorCobroFinal", cob.ValorCobroFinal);
                    cs.addProperty("MontoCobroCalculado", cob.MontoCobroCalculado);
                    cs.addProperty("MontoCobroCalculadoLocal", cob.MontoCobroCalculadoLocal);
                    cs.addProperty("MontoCobroCalculadoLiquidacion", cob.MontoCobroCalculadoLiquidacion);
                    cs.addProperty("IdOrigenCobro", cob.IdOrigenCobro);
                    cs.addProperty("MontoMinimo", cob.MontoMinimo);
                    outCobros.add(cs);
                }
            } else if ("PE".equalsIgnoreCase(localizacion)) {
                for (DefinicionCobroProceso dc : ord.definicionCobros) {
                    JsonObject cs = new JsonObject();
                    cs.addProperty("IdInterno", dc.IdInterno);
                    cs.addProperty("IdInternoCobro", dc.IdInternoCobro);
                    cs.addProperty("IdCobro", dc.CodigoDefinicionCobro);
                    cs.addProperty("CodigoCobroClasificacionBase", dc.IdClaseCobroBase);
                    cs.addProperty("NombreCobro", dc.Descripcion);
                    cs.addProperty("PermiteCambiarValor", ("S".equalsIgnoreCase(dc.Otros_CheckPermiteAumentarValor) || "S".equalsIgnoreCase(dc.Otros_CheckPermiteDisminuirValor)) ? "S" : "N");
                    cs.addProperty("Otros_CheckAllIn", dc.Otros_CheckAllIn);
                    cs.addProperty("FormaCobro", dc.IdTipoCobro);
                    cs.addProperty("NombreFormaCobro", dc.NombreTipoCobro);
                    cs.addProperty("MontoMinimo", dc.PorcentualFijo_ValorMontoMinimo);
                    cs.addProperty("CodigoCobroClasificacion", dc.IdClaseCobro);
                    cs.addProperty("IdMonedaCobro", dc.IdMonedaCobro);
                    cs.addProperty("ValorCobroPerfil", dc.ValorCobroPerfil);
                    cs.addProperty("ValorCobroAplicado", dc.ValorCobroAplicado != null ? dc.ValorCobroAplicado : dc.ValorCobroPerfil);
                    cs.addProperty("MontoCobroCalculado", dc.MontoCobroCalculado);
                    cs.addProperty("MontoCobroCalculadoLocal", dc.MontoCobroCalculadoLocal);
                    cs.addProperty("MontoCobroCalculadoLiquidacion", dc.MontoCobroCalculadoLiquidacion);
                    cs.addProperty("IdOrigenCobro", dc.IdOrigenCobro);
                    cs.addProperty("Otros_CheckOperacionPlazo", dc.Otros_CheckOperacionPlazo);
                    cs.addProperty("PorcentualRango_IdMonedaRangoMonto", dc.PorcentualRango_IdMonedaRangoMonto);
                    cs.addProperty("PorcentualRango_IdTipoRangoCobro", dc.PorcentualRango_IdTipoRangoCobro);
                    outCobros.add(cs);
                }
            }
        }

        log.info("CalcularMontoYCobrosOrdenService completado en {}ms", System.currentTimeMillis() - init);
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", mensajes);
        out.add("p_out_Ordenes", outOrdenes);
        out.add("p_out_Cobros", outCobros);
        return out;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private BigDecimal calculaMontoParidad(BigDecimal precio, BigDecimal cantidad, BigDecimal factor) {
        BigDecimal puntoCinco = new BigDecimal("0.5");
        BigDecimal y = precio.multiply(factor);
        if (y.compareTo(puntoCinco) < 0) {
            y = y.setScale(3, RoundingMode.DOWN);
        } else {
            y = y.setScale(2, RoundingMode.HALF_UP);
        }
        return y.multiply(cantidad);
    }

    private BigDecimal redondearMonto(BigDecimal monto) {
        if (monto == null) return BigDecimal.ZERO;
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    private String idUso(String idTipoOperacion, String localizacion, String idBolsa) {
        if ("CL".equalsIgnoreCase(localizacion)) {
            if ("COMMIL".equalsIgnoreCase(idTipoOperacion)) return "COMPRA MILA";
            if ("VENMIL".equalsIgnoreCase(idTipoOperacion)) return "VENTA MILA";
            if ("COMETX".equalsIgnoreCase(idTipoOperacion) || "COMCFX".equalsIgnoreCase(idTipoOperacion)
                    || "COMACX".equalsIgnoreCase(idTipoOperacion)) return "COMPRA EXTRANJEROS";
            if ("VENETX".equalsIgnoreCase(idTipoOperacion) || "VENCFX".equalsIgnoreCase(idTipoOperacion)
                    || "VENACX".equalsIgnoreCase(idTipoOperacion)) return "VENTA EXTRANJEROS";
            return "ORDENES";
        } else if ("PE".equalsIgnoreCase(localizacion)) {
            if ("COMACN".equalsIgnoreCase(idTipoOperacion) && "MIL".equalsIgnoreCase(idBolsa)) return "COMPRA MILA";
            if ("VENACN".equalsIgnoreCase(idTipoOperacion) && "MIL".equalsIgnoreCase(idBolsa)) return "VENTA MILA";
            if ("COMACN".equalsIgnoreCase(idTipoOperacion) && "OUT".equalsIgnoreCase(idBolsa)) return "COMPRA EXTRANJEROS";
            if ("VENACN".equalsIgnoreCase(idTipoOperacion) && "OUT".equalsIgnoreCase(idBolsa)) return "VENTA EXTRANJEROS";
            if (idTipoOperacion != null && (idTipoOperacion.startsWith("VEN") || idTipoOperacion.startsWith("COM"))
                    && (idTipoOperacion.contains("RRV") || idTipoOperacion.contains("RRF")
                        || idTipoOperacion.contains("VRV") || idTipoOperacion.contains("VRF"))) return "REPORTES";
            return "ORDENES";
        }
        return "ORDENES";
    }

    private JsonObject buildParidadRequest(long idEmpresa, long idTrabajo, List<JsonObject> calculos) {
        JsonObject req = new JsonObject();
        req.addProperty("p_IdEmpresa", idEmpresa);
        req.addProperty("p_IdTrabajo", idTrabajo);
        JsonArray arr = new JsonArray();
        for (JsonObject c : calculos) arr.add(c);
        req.add("p_CalculosEntrada", arr);
        return req;
    }

    private JsonObject buildCalculo(Long idInterno, String fecha, long idTrabajo,
                                    String monedaDest, String monedaOrig,
                                    BigDecimal monto, BigDecimal tipoCambio, String idUso) {
        JsonObject c = new JsonObject();
        c.addProperty("Fecha", fecha != null ? fecha : "");
        c.addProperty("IdInterno", idInterno);
        c.addProperty("IdMonedaDestino", monedaDest != null ? monedaDest : "");
        c.addProperty("IdMonedaOrigen", monedaOrig != null ? monedaOrig : "");
        c.addProperty("IdTrabajo", idTrabajo);
        c.addProperty("IdUso", idUso);
        c.addProperty("MontoOrigen", monto != null ? monto : BigDecimal.ZERO);
        c.addProperty("ValorCambioLocalModificado", tipoCambio != null ? tipoCambio : BigDecimal.ZERO);
        return c;
    }

    private BigDecimal getFirstCalcMonto(JsonObject parOut) {
        if (parOut.has("p_out_CalculosSalida")) {
            JsonArray arr = parOut.get("p_out_CalculosSalida").getAsJsonArray();
            if (arr.size() > 0) {
                JsonObject c = arr.get(0).getAsJsonObject();
                if ("OK".equals(getStr(c, "Resultado"))) return getBD(c, "MontoDestino");
            }
        }
        return BigDecimal.ZERO;
    }

    private JsonObject buildSalida(long errores, JsonArray mensajes, JsonArray ordenes, JsonArray cobros) {
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", errores);
        out.add("P_OUT_MENSAJES", mensajes);
        out.add("p_out_Ordenes", ordenes);
        out.add("p_out_Cobros", cobros);
        return out;
    }

    private void addMensaje(JsonArray mensajes, String tipo, String codigo) {
        addMensaje(mensajes, tipo, codigo, null);
    }

    private void addMensaje(JsonArray mensajes, String tipo, String codigo, Long idInterno) {
        JsonObject m = new JsonObject();
        m.addProperty("tipo", tipo);
        m.addProperty("codigo", codigo);
        if (idInterno != null) m.addProperty("id", idInterno);
        mensajes.add(m);
    }

    private static Long getLong(JsonObject o, String k) {
        if (o == null || !o.has(k) || o.get(k).isJsonNull()) return null;
        try { return o.get(k).getAsLong(); } catch (Exception e) { return null; }
    }

    private static String getStr(JsonObject o, String k) { return getStr(o, k, ""); }
    private static String getStr(JsonObject o, String k, String def) {
        if (o == null || !o.has(k) || o.get(k).isJsonNull()) return def;
        return o.get(k).getAsString();
    }

    private static BigDecimal getBD(JsonObject o, String k) {
        if (o == null || !o.has(k) || o.get(k).isJsonNull()) return BigDecimal.ZERO;
        try { return o.get(k).getAsBigDecimal(); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static String str(Map<String, Object> m, String k) {
        Object v = m.get(k); return v != null ? v.toString() : "";
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static BigDecimal getBD2(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static String loadSql(String path) {
        try (var is = CalcularMontoYCobrosOrdenService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }

    // ── Inner POJOs ───────────────────────────────────────────────────────────

    private static class OrdenProceso {
        String TipoInteres;
        Long BaseTasa = 0L;
        BigDecimal PorcentajeCobrosUltimaOrden = BigDecimal.ZERO;
        String IdClasificacionTipoInstrumento;
        String FechaOperacion;
        Long IdInterno;
        Long IdPortafolio;
        String IdTipoOperacion;
        String IdTipoProducto = "";
        String IdTipoCanalIngreso;
        String IdBolsa;
        Long IdCorredorCorresponsal;
        Long IdInstrumento;
        Long IdInstrumentoSubyacente;
        BigDecimal CantidadOrden = BigDecimal.ZERO;
        BigDecimal MontoOrden = BigDecimal.ZERO;
        BigDecimal MontoOperacionPlazo = BigDecimal.ZERO;
        BigDecimal PrecioPlazo = BigDecimal.ZERO;
        BigDecimal ValorRescateOrden = BigDecimal.ZERO;
        String EsFungible = "N";
        BigDecimal TasaSimultanea = BigDecimal.ZERO;
        BigDecimal TasaRFIF = BigDecimal.ZERO;
        BigDecimal TasaVentaCorta = BigDecimal.ZERO;
        BigDecimal TasaReporte = BigDecimal.ZERO;
        BigDecimal TasaReportado = BigDecimal.ZERO;
        Long PlazoReporte = 0L;
        Long PlazoIF = 0L;
        Long PlazoSimultanea = 0L;
        Long PlazoVentaCorta = 0L;
        String IdMonedaLiquidacionOrden;
        BigDecimal PrecioOrden = BigDecimal.ZERO;
        BigDecimal TipoCambio = BigDecimal.ZERO;
        String AccionOrden = "";
        String IdEmisor = "";
        String EsAPlazo = "N";
        BigDecimal MontoTotal = BigDecimal.ZERO;
        String EsPorMonto = "N";
        String IdMonedaOrden = "";
        BigDecimal MontoTotalLocal = BigDecimal.ZERO;
        BigDecimal MontoTotalLiquidacion = BigDecimal.ZERO;
        BigDecimal MontoTotalLiquidacionPlazo = BigDecimal.ZERO;
        BigDecimal MontoOperacionLiquidacionPlazo = BigDecimal.ZERO;
        BigDecimal MontoOperacionLocalPlazo = BigDecimal.ZERO;
        BigDecimal TotalCobrosLiquidacionPlazo = BigDecimal.ZERO;
        BigDecimal MontoOperacionLocal = BigDecimal.ZERO;
        BigDecimal MontoOperacionLiquidacion = BigDecimal.ZERO;
        BigDecimal TotalCobrosLocal = BigDecimal.ZERO;
        BigDecimal TotalCobrosLiquidacion = BigDecimal.ZERO;
        BigDecimal MontoInteresContado = BigDecimal.ZERO;
        BigDecimal MontoInteresPlazo = BigDecimal.ZERO;
        BigDecimal PorcentajeAllIn = BigDecimal.ZERO;
        BigDecimal MontoAllIn = BigDecimal.ZERO;
        String GlosaRefactura = "";
        String FechaOperacionOriginal = "";
        List<CobroProceso> cobros = new ArrayList<>();
        List<DefinicionCobroProceso> definicionCobros = new ArrayList<>();
    }

    private static class CobroProceso {
        String IdOrigenCobro = "";
        Long IdInterno;
        Long IdInternoCobro;
        String IdMonedaLiquidacionOrden = "";
        BigDecimal MontoOperacionMonedaCobro = BigDecimal.ZERO;
        String IdCobro = "";
        String CodigoCobroClasificacion = "";
        String IdMoneda = "";
        String NombreCobro = "";
        BigDecimal ValorCobroPerfil = BigDecimal.ZERO;
        String PermiteCambiarValor = "N";
        String FormaCobro = "V";
        BigDecimal MontoMinimo = BigDecimal.ZERO;
        BigDecimal ValorCobroAplicado = BigDecimal.ZERO;
        BigDecimal ValorCobroFinal = BigDecimal.ZERO;
        BigDecimal MontoCobroCalculado = BigDecimal.ZERO;
        BigDecimal MontoCobroCalculadoLocal = BigDecimal.ZERO;
        BigDecimal MontoCobroCalculadoLiquidacion = BigDecimal.ZERO;
    }

    private static class DefinicionCobroProceso {
        String IdOrigenCobro = "";
        String Descripcion = "";
        BigDecimal ValorCobroPerfil = BigDecimal.ZERO;
        String IdMonedaCobro = "";
        Long IdInternoCobro;
        Long IdInterno;
        String IdClaseCobro = "";
        String IdClaseCobroBase = "";
        String IdTipoCobro = "";
        String NombreTipoCobro = "";
        String CodigoDefinicionCobro = "";
        BigDecimal PorcentualFijo_ValorMontoMinimo = BigDecimal.ZERO;
        String Otros_CheckOperacionPlazo = "N";
        String Otros_CheckPermiteAumentarValor = "N";
        String Otros_CheckPermiteDisminuirValor = "N";
        String Otros_CheckAllIn = "N";
        String PorcentualRango_IdMonedaRangoMonto = "";
        String PorcentualRango_IdTipoRangoCobro = "";
        BigDecimal ValorCobroFinal;
        BigDecimal ValorCobroAplicado;
        BigDecimal MontoCobroCalculado = BigDecimal.ZERO;
        BigDecimal MontoCobroCalculadoLocal = BigDecimal.ZERO;
        BigDecimal MontoCobroCalculadoLiquidacion = BigDecimal.ZERO;
    }

    private static class Instrumento {
        Long IdInstrumento;
        Long Tamano;
        String IdMoneda = "";
        String IdClasificacionTipoInstrumento = "";
        String IdTipoInstrumento = "";
        String IdTipoReajuste = "";
    }
}
