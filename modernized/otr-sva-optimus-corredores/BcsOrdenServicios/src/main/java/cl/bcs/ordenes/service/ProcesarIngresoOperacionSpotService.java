package cl.bcs.ordenes.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.PlataformaCallClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Migrates cl.bcs.ordenes.ProcesarIngresoOperacionSpot.
 *
 * Orchestrates: ConfiguracionSistema → CalcularParidad (plataforma) →
 * ConsultarDatosInstrumentos (plataforma) → ConsultarPuntasPorTramo (mesa) →
 * CrearOperacionSpot (mesa).
 */
@Service
public class ProcesarIngresoOperacionSpotService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarIngresoOperacionSpotService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    // Constants from ConstantesOrdenes
    private static final String DOLAR              = "USD";
    private static final String CANAL_PUNTA        = "Punta";
    private static final String SPOT_COMPRA        = "COMSPT";
    private static final String SPOT_VENTA         = "VENSPT";
    private static final String SPOT_PRODUCTO      = "MEME";
    private static final String SPOT_TIPO_CPTE_FAC = "FEE";
    private static final String SPOT_BEN_OUR       = "BEN";
    private static final String SPOT_ESTADO_VIG    = "VIG";
    private static final String SPOT_CALCULA_MRG   = "S";
    private static final String SPOT_TIPO_INSTR    = "SPT";
    private static final String SPOT_NEMO          = "DISTRI";
    private static final long   SPOT_COD_COMERCIO  = 300_206L;
    private static final String TIPO_CANAL_ING     = "MEB";
    private static final String ID_MODULO_ORDENES  = "ORD";

    private static final String SQL_MESA =
        "SELECT TOP 1 \"IdMesa\" FROM BSCL.\"BSCL.Mesa.BD::MES.Mesa\" " +
        "WHERE \"IdTipoProducto\" = 'MEME' AND \"IdEstadoRegistro\" = 'ACT' AND \"PorDefecto\" = 'S'";

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil   configUtil;
    private final PlataformaCallClient       plataformaClient;
    private final RestClient                 mesaClient;

    public ProcesarIngresoOperacionSpotService(
            NamedParameterJdbcTemplate jdbc,
            ConfiguracionSistemaUtil configUtil,
            PlataformaCallClient plataformaClient,
            @Value("${bcs.mesa.url:http://bcs-mesa-servicios:8085}") String mesaUrl) {
        this.jdbc             = jdbc;
        this.configUtil       = configUtil;
        this.plataformaClient = plataformaClient;
        this.mesaClient       = RestClient.builder().baseUrl(mesaUrl).build();
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ProcesarIngresoOperacionSpot");

        long   idEmpresa              = longOf(params, "p_IdEmpresa");
        long   idUsuario              = longOf(params, "p_IdUsuario");
        String idModulo               = strOf(params, "p_IdModulo");
        String usaReglasNegocio       = strOf(params, "p_UsaReglasNegocio",       "N");
        String permiteOperarProsp     = strOf(params, "p_PermiteOperarProspectos", "N");
        String calculaMargen          = strOf(params, "p_CalculaMargen",           SPOT_CALCULA_MRG);
        JsonArray datosOperacion      = params.has("p_DatosOperacion")
                                        ? params.get("p_DatosOperacion").getAsJsonArray() : new JsonArray();
        JsonArray datosFormaPago      = params.has("p_DatosFormaPago")
                                        ? params.get("p_DatosFormaPago").getAsJsonArray() : new JsonArray();

        JsonObject result = new JsonObject();
        result.addProperty("P_OUT_ERRORESCONTADOR",    0L);
        result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0L);
        result.add("P_OUT_MENSAJES",         new JsonArray());
        result.add("P_OUT_IDOPERACIONSPOT",  new JsonArray());

        try {
            String fechaSistema  = configUtil.getParametroString(idEmpresa, "PLA.FechaSistema");
            String monedaSistema = configUtil.getParametroString(idEmpresa, "PLA.MonedaSistema");

            long       idMesaDefecto  = obtenerMesa();
            BigDecimal dolarObservado = obtenerDolarObservado(idEmpresa, fechaSistema, monedaSistema);
            long       instrSpot      = obtenerIdInstrumentoSpot(idEmpresa, idUsuario, idModulo);
            JsonArray  puntas         = obtenerPuntas(idEmpresa);

            JsonArray operaciones = buildOperaciones(datosOperacion, dolarObservado,
                                                     idMesaDefecto, instrSpot, puntas, monedaSistema);

            JsonObject crearParams = new JsonObject();
            crearParams.addProperty("p_IdEmpresa",              idEmpresa);
            crearParams.addProperty("p_IdUsuario",              idUsuario);
            crearParams.addProperty("p_CalculaMargen",          calculaMargen);
            crearParams.addProperty("p_IdModulo",               idModulo);
            crearParams.addProperty("p_UsaReglasNegocio",       usaReglasNegocio);
            crearParams.addProperty("p_PermiteOperarProspectos", permiteOperarProsp);
            crearParams.add("p_DatosOperacion",  operaciones);
            crearParams.add("p_DatosFormaPago",  datosFormaPago);

            JsonObject salida = callMesa("cl.bcs.mesa.SPT.CrearOperacionSpot", crearParams);

            if (salida.has("P_OUT_ERRORESCONTADOR"))
                result.addProperty("P_OUT_ERRORESCONTADOR", salida.get("P_OUT_ERRORESCONTADOR").getAsLong());
            if (salida.has("P_OUT_ADVERTENCIASCONTADOR"))
                result.addProperty("P_OUT_ADVERTENCIASCONTADOR", salida.get("P_OUT_ADVERTENCIASCONTADOR").getAsLong());
            if (salida.has("P_OUT_MENSAJES"))        result.add("P_OUT_MENSAJES",        salida.get("P_OUT_MENSAJES"));
            if (salida.has("P_OUT_IDOPERACIONSPOT")) result.add("P_OUT_IDOPERACIONSPOT", salida.get("P_OUT_IDOPERACIONSPOT"));

        } catch (Exception e) {
            log.error("Error ProcesarIngresoOperacionSpot", e);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            JsonArray msgs = new JsonArray();
            JsonObject m = new JsonObject();
            m.addProperty("Codigo",      "ERROR_PROCESAR_INGRESO_SPOT");
            m.addProperty("Descripcion", e.getMessage());
            m.addProperty("TipoMensaje", "E");
            msgs.add(m);
            result.add("P_OUT_MENSAJES", msgs);
        }

        log.info("FIN::ProcesarIngresoOperacionSpot");
        return result;
    }

    // ─── Lookups ────────────────────────────────────────────────────────────

    private long obtenerMesa() {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(SQL_MESA, new MapSqlParameterSource());
            if (!rows.isEmpty()) {
                Object id = rows.get(0).get("IdMesa");
                return id instanceof Number n ? n.longValue() : 0L;
            }
        } catch (Exception e) {
            log.warn("obtenerMesa failed: {}", e.getMessage());
        }
        return 0L;
    }

    private BigDecimal obtenerDolarObservado(long idEmpresa, String fechaSistema, String monedaSistema) {
        try {
            JsonObject calculo = new JsonObject();
            calculo.addProperty("IdTrabajo",                    1L);
            calculo.addProperty("IdInterno",                   -1L);
            calculo.addProperty("Fecha",                        fechaSistema);
            calculo.addProperty("IdMonedaOrigen",               DOLAR);
            calculo.addProperty("IdMonedaDestino",              monedaSistema);
            calculo.addProperty("MontoOrigen",                  1);
            calculo.addProperty("ValorCambioLocalModificado",   0);
            calculo.addProperty("IdUso",                        "");
            JsonArray arr = new JsonArray(); arr.add(calculo);

            JsonObject p = new JsonObject();
            p.addProperty("p_IdEmpresa", idEmpresa);
            p.addProperty("p_IdTrabajo", 1L);
            p.add("p_CalculosEntrada", arr);

            JsonObject resp = plataformaClient.call("cl.bcs.plataforma.mer.CalcularParidad", p);
            if (resp.has("p_out_CalculosSalida")) {
                JsonArray salidas = resp.get("p_out_CalculosSalida").getAsJsonArray();
                for (int i = 0; i < salidas.size(); i++) {
                    JsonObject s = salidas.get(i).getAsJsonObject();
                    if (s.has("IdInterno") && s.get("IdInterno").getAsLong() == -1L && s.has("MontoDestino"))
                        return s.get("MontoDestino").getAsBigDecimal();
                }
            }
        } catch (Exception e) {
            log.warn("obtenerDolarObservado failed: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private long obtenerIdInstrumentoSpot(long idEmpresa, long idUsuario, String idModulo) {
        try {
            JsonObject filtro = new JsonObject();
            filtro.addProperty("Correlativo",        1L);
            filtro.addProperty("IdTipoInstrumento",  SPOT_TIPO_INSTR);
            JsonArray tipos = new JsonArray(); tipos.add(filtro);

            JsonObject instr = new JsonObject();
            instr.addProperty("Correlativo",  1L);
            instr.addProperty("Nemotecnico",  SPOT_NEMO);
            JsonArray instrs = new JsonArray(); instrs.add(instr);

            JsonObject p = new JsonObject();
            p.addProperty("p_IdEmpresa",            idEmpresa);
            p.addProperty("p_IdUsuario",             idUsuario);
            p.addProperty("p_IdModulo",              idModulo);
            p.addProperty("p_CreaOpcionAutomatica",  "N");
            p.add("p_IdTipoInstrumentos",            tipos);
            p.add("p_Instrumentos",                  instrs);
            p.add("p_IdClasificacionTipoInstrumentos", new JsonArray());

            JsonObject resp = plataformaClient.call("cl.bcs.plataforma.ins.ConsultarDatosInstrumentos", p);
            if (resp.has("P_OUT_INSTRUMENTOS")) {
                JsonArray list = resp.get("P_OUT_INSTRUMENTOS").getAsJsonArray();
                if (list.size() > 0 && list.get(0).getAsJsonObject().has("IdInstrumento"))
                    return list.get(0).getAsJsonObject().get("IdInstrumento").getAsLong();
            }
        } catch (Exception e) {
            log.warn("obtenerIdInstrumentoSpot failed: {}", e.getMessage());
        }
        return 0L;
    }

    private JsonArray obtenerPuntas(long idEmpresa) {
        try {
            JsonObject p = new JsonObject();
            p.addProperty("p_IdEmpresa",    idEmpresa);
            p.addProperty("p_MontoPorTramo", 0L);
            p.addProperty("p_IdMoneda",     DOLAR);
            p.addProperty("p_Canal",        CANAL_PUNTA);

            JsonObject resp = callMesa("cl.bcs.mesa.SPT.ConsultarPuntasPorTramo", p);
            if (resp.has("P_OUT_PUNTAS")) return resp.get("P_OUT_PUNTAS").getAsJsonArray();
        } catch (Exception e) {
            log.warn("obtenerPuntas failed: {}", e.getMessage());
        }
        return new JsonArray();
    }

    private JsonObject callMesa(String fqn, JsonObject params) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("class", fqn);
            body.add("params", params);
            String resp = mesaClient.post()
                .uri("/internal/java-call")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toString())
                .retrieve()
                .body(String.class);
            if (resp == null || resp.isBlank()) return new JsonObject();
            return JsonParser.parseString(resp).getAsJsonObject();
        } catch (Exception e) {
            log.warn("callMesa({}) failed: {}", fqn, e.getMessage());
            return new JsonObject();
        }
    }

    // ─── Operation builder ──────────────────────────────────────────────────

    private JsonArray buildOperaciones(JsonArray asigs, BigDecimal dolarObservado,
                                       long idMesaDefecto, long instrSpot,
                                       JsonArray puntas, String monedaSistema) {
        JsonArray result = new JsonArray();
        for (int i = 0; i < asigs.size(); i++) {
            JsonObject a    = asigs.get(i).getAsJsonObject();
            BigDecimal monto = a.has("MontoAsignadoLiquidacion")
                              ? a.get("MontoAsignadoLiquidacion").getAsBigDecimal() : BigDecimal.ZERO;

            BigDecimal[] pv = calcPunta(puntas, monto, SPOT_VENTA);

            JsonObject op = new JsonObject();
            op.addProperty("IdTipoOperacion",           SPOT_VENTA);
            op.addProperty("NumeroOperacion",            a.has("IdAsignacion") ? a.get("IdAsignacion").getAsString() : "0");
            op.addProperty("IdInstrumento",              instrSpot);
            op.addProperty("IdTipoProducto",             SPOT_PRODUCTO);
            op.addProperty("IdCliente",                  longOf(a, "IdCliente"));
            op.addProperty("IdPortafolio",               longOf(a, "IdPortafolio"));
            op.addProperty("FechaOperacion",             a.has("FechaMovimientoAsignacion")
                                                         ? a.get("FechaMovimientoAsignacion").getAsString() : "");
            op.addProperty("IdMonedaPrincipal",          DOLAR);
            op.addProperty("IdMonedaSecundaria",         monedaSistema);
            op.addProperty("MontoMonedaPrincipal",       monto);
            op.addProperty("MontoMonedaSecundaria",      monto.multiply(dolarObservado));
            op.addProperty("TipoCambioCierre",           dolarObservado);
            op.addProperty("TipoCambioCierreForward",    BigDecimal.ZERO);
            op.addProperty("ParidadCierre",              BigDecimal.ZERO);
            op.addProperty("ParidadCierreForward",       BigDecimal.ZERO);
            op.addProperty("TipoCambioCosto",            pv[2]);
            op.addProperty("ParidadCosto",               BigDecimal.ZERO);
            op.addProperty("MontoEquivalente",           BigDecimal.ZERO);
            op.addProperty("MontoRentabilidad",          BigDecimal.ZERO);
            op.addProperty("MontoRentabilidadCanal",     BigDecimal.ZERO);
            op.addProperty("Margen",                     BigDecimal.ZERO);
            op.addProperty("Utilidad",                   BigDecimal.ZERO);
            op.addProperty("PuntaCompra",                pv[0]);
            op.addProperty("PuntaVenta",                 pv[1]);
            op.addProperty("SolicitudTipoCambio",        "N");
            op.addProperty("IdMesa",                     idMesaDefecto);
            op.addProperty("IdModulo",                   ID_MODULO_ORDENES);
            op.addProperty("IdTipoDolar",                4L);
            op.addProperty("TipoComprobanteFacturacion", SPOT_TIPO_CPTE_FAC);
            op.addProperty("IdAgente",                   0L);
            op.addProperty("BenOur",                     SPOT_BEN_OUR);
            op.addProperty("EquivalenteConGasto",        BigDecimal.ZERO);
            op.addProperty("TipoCambioCierreConGasto",   BigDecimal.ZERO);
            op.addProperty("Observacion",                strOf(a, "Glosa"));
            op.addProperty("CodigoComercio",             SPOT_COD_COMERCIO);
            op.addProperty("FolioPlanilla",              0L);
            op.addProperty("FolioInterfaz",              "0");
            op.addProperty("OperacionFicticia",          "N");
            op.addProperty("IdTipoCanalIngreso",         TIPO_CANAL_ING);
            op.addProperty("IdDatatec",                  "0");
            op.addProperty("Estado",                     SPOT_ESTADO_VIG);
            result.add(op);
        }
        return result;
    }

    /** Returns [puntaCompra, puntaVenta, tipoCambioCosto] */
    private BigDecimal[] calcPunta(JsonArray puntas, BigDecimal monto, String tipoOp) {
        BigDecimal pc = BigDecimal.ZERO, pv = BigDecimal.ZERO, tc = BigDecimal.ZERO;
        if (puntas.size() == 0) return new BigDecimal[]{pc, pv, tc};

        JsonObject principal = puntas.get(0).getAsJsonObject();
        BigDecimal tramo0    = principal.has("MontoPorTramo")
                               ? principal.get("MontoPorTramo").getAsBigDecimal() : BigDecimal.ZERO;

        JsonObject elegida = null;
        if (monto.compareTo(tramo0) < 0) {
            elegida = principal;
        } else {
            List<JsonObject> list = new ArrayList<>();
            for (int i = 0; i < puntas.size(); i++) list.add(puntas.get(i).getAsJsonObject());
            Collections.reverse(list);
            for (JsonObject p : list) {
                BigDecimal t = p.has("MontoPorTramo") ? p.get("MontoPorTramo").getAsBigDecimal() : BigDecimal.ZERO;
                if (monto.compareTo(t) >= 0) { elegida = p; break; }
            }
        }

        if (elegida != null) {
            pc = elegida.has("PuntaCompra") ? elegida.get("PuntaCompra").getAsBigDecimal() : BigDecimal.ZERO;
            pv = elegida.has("PuntaVenta")  ? elegida.get("PuntaVenta").getAsBigDecimal()  : BigDecimal.ZERO;
            tc = SPOT_VENTA.equals(tipoOp) ? pv : SPOT_COMPRA.equals(tipoOp) ? pc : BigDecimal.ZERO;
        }
        return new BigDecimal[]{pc, pv, tc};
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private static long   longOf(JsonObject o, String k)              { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsLong()   : 0L;  }
    private static String strOf (JsonObject o, String k)              { return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString()  : "";  }
    private static String strOf (JsonObject o, String k, String def)  { String v = strOf(o, k); return v.isEmpty() ? def : v; }
}
