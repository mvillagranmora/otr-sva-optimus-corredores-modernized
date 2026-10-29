package cl.bcs.mesa.javacall.spt;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallInfra.MensajeSalida;
import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Modernized from legacy cl.bcs.mesa.SPT.ProcesarCargaMasivaArchivoOperacionSpot (JavaCall).
 *
 * Business logic preserved verbatim:
 *   1. Query carga masiva header and detalle rows by IdCargaMasiva list.
 *   2. Reject immediately if IdEstado != "ING".
 *   3. Delegate to CrearOperacionSpot (via PlataformaBridge) to create spot operations;
 *      collect P_OUT_MENSAJES from the result.
 *   4. Propagate per-row validation descriptions from error messages into DetalleValidacion.
 *   5. Fetch system date from Configuracion, mark header as "ERR" or "PRO", batch-update
 *      both the carga masiva header and the detalle rows.
 *   6. Reset P_OUT_ERRORESCONTADOR to 0 when the DB updates succeed (legacy behaviour).
 *
 * SQL files: all 6 exist in resources (no new files were created).
 * Output contract: { P_OUT_ERRORESCONTADOR, P_OUT_MENSAJES } — identical to legacy Salida.
 */
@Service
public class ProcesarCargaMasivaArchivoOperacionSpotService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCargaMasivaArchivoOperacionSpotService.class);

    private static final String SQL_SEL_CARGA_MASIVA =
            JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ProcesarCargaMasivaArchivoOperacionSpot_SelectCargaMasiva.sql");
    private static final String SQL_SEL_DETALLE =
            JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ProcesarCargaMasivaArchivoOperacionSpot_SelectOperacionDetalle.sql");
    private static final String SQL_UPD_ESTADO =
            JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ProcesarCargaMasivaArchivoOperacionSpot_UpdateOperacionesSpotEstado.sql");
    private static final String SQL_UPD_DETALLE =
            JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ProcesarCargaMasivaArchivoOperacionSpot_UpdateOperacionesSpotDetalleEstado.sql");
    private static final String SQL_SEL_FORMA_PAGO_MESA =
            JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ProcesarCargaMasivaArchivoOperacionSpot_SelectFormaPagoMesa.sql");
    private static final String SQL_SEL_FORMA_PAGO_CLIENTE =
            JavaCallInfra.loadSql("cl/bcs/mesa/SPT/sql/ProcesarCargaMasivaArchivoOperacionSpot_SelectFormaPagoCliente.sql");

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    private static final String FQN_CREAR_SPOT = "cl.bcs.mesa.SPT.CrearOperacionSpot";
    private static final String FQN_CONFIG = "cl.bcs.plataforma.pla.ConfiguracionSistema.getParametroConfiguracionString";

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public ProcesarCargaMasivaArchivoOperacionSpotService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override
    public String fqn() { return "cl.bcs.mesa.SPT.ProcesarCargaMasivaArchivoOperacionSpot"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ProcesarCargaMasivaArchivoOperacionSpot");
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);
            if (in.p_CargaMasivaProcesar == null) in.p_CargaMasivaProcesar = new ArrayList<>();

            Salida out = new Salida();

            // 1. Fetch carga masiva header and detalle
            List<Long> idsCarga = in.p_CargaMasivaProcesar.stream()
                    .map(op -> op.IdCargaMasiva)
                    .collect(Collectors.toList());
            Map<String, Object> byId = new HashMap<>();
            byId.put("IdCargaMasiva", idsCarga);

            List<OperacionCargaMasiva> cargaMasiva = JavaCallInfra.query(
                    conn, SQL_SEL_CARGA_MASIVA, byId, JavaCallInfra.fieldMapper(OperacionCargaMasiva.class));
            List<OperacionSpotDetalle> detalle = JavaCallInfra.query(
                    conn, SQL_SEL_DETALLE, byId, JavaCallInfra.fieldMapper(OperacionSpotDetalle.class));

            if (cargaMasiva.isEmpty()) {
                out.P_OUT_MENSAJES.add(MensajeSalida.error(0L, "CARGA_MASIVA_NO_ENCONTRADA", "No se encontró la carga masiva"));
                out.P_OUT_ERRORESCONTADOR = MensajeSalida.countErrors(out.P_OUT_MENSAJES);
                return GSON.fromJson(GSON.toJson(out), JsonObject.class);
            }

            // 2. Validate state (legacy: "ESATADO_CARGA_MASIVA_DEBE_SER_INGRESADO" — typo preserved)
            if (!"ING".equals(cargaMasiva.get(0).IdEstado)) {
                out.P_OUT_MENSAJES.add(MensajeSalida.error(
                        cargaMasiva.get(0).IdCargaMasiva,
                        "ESATADO_CARGA_MASIVA_DEBE_SER_INGRESADO",
                        "El estado de la carga masiva debe ser INGRESADO"));
                out.P_OUT_ERRORESCONTADOR = MensajeSalida.countErrors(out.P_OUT_MENSAJES);
                return GSON.fromJson(GSON.toJson(out), JsonObject.class);
            }

            // 3. Delegate to CrearOperacionSpot and collect messages
            JsonObject crearResult = crearOperaciones(conn, detalle, cargaMasiva, in);
            if (crearResult.has("P_OUT_MENSAJES")) {
                crearResult.getAsJsonArray("P_OUT_MENSAJES").forEach(el ->
                        out.P_OUT_MENSAJES.add(GSON.fromJson(el, MensajeSalida.class)));
            }
            out.P_OUT_ERRORESCONTADOR = MensajeSalida.countErrors(out.P_OUT_MENSAJES);

            // 4. Propagate validation details into each detalle row (legacy obtenerMensajeProcesado logic)
            Map<Long, String> descPorId = new HashMap<>();
            for (MensajeSalida m : out.P_OUT_MENSAJES) {
                if (MensajeSalida.ERROR.equalsIgnoreCase(m.tipo) && m.id != null) {
                    descPorId.merge(m.id, m.descripcion, (a, b) -> a + " ; " + b);
                }
            }
            for (OperacionSpotDetalle d : detalle) {
                String desc = descPorId.get(d.IdCargaMasivaDetalle);
                d.DetalleValidacion = (desc != null && !desc.isEmpty()) ? desc : "OK";
            }

            // 5. Get system date and update estados
            String fechaSistema = obtenerFechaSistema(in.p_IdEmpresa);

            OperacionCargaMasiva header = cargaMasiva.get(0);
            header.IdEstado = out.P_OUT_ERRORESCONTADOR > 0 ? "ERR" : "PRO";
            header.FechaProceso = fechaSistema + " 00:00:00.000";
            JavaCallInfra.batch(conn, SQL_UPD_ESTADO, cargaMasiva);
            JavaCallInfra.batch(conn, SQL_UPD_DETALLE, detalle);

            // Legacy: resets counter to 0 when DB updates succeed (batch didn't throw)
            out.P_OUT_ERRORESCONTADOR = 0L;

            log.info("FIN::ProcesarCargaMasivaArchivoOperacionSpot");
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("ProcesarCargaMasivaArchivoOperacionSpot failed", e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    /**
     * Builds the CrearOperacionSpot entrada identical to the legacy crearOperaciones() method,
     * then delegates via PlataformaBridge (same routing as the gateway).
     */
    private JsonObject crearOperaciones(Connection conn, List<OperacionSpotDetalle> detalle,
                                        List<OperacionCargaMasiva> cargaMasiva, Entrada entrada) throws Exception {

        List<String> idFormasPago = detalle.stream().map(d -> d.FormaPagoRecibimos).collect(Collectors.toList());
        List<Long> idPortafolios = detalle.stream().map(d -> d.IdPortafolio).collect(Collectors.toList());

        Map<String, Object> p1 = new HashMap<>();
        p1.put("IdFormaDePago", idFormasPago);
        List<FormasDePagoMesa> formasPagoMesa = JavaCallInfra.query(
                conn, SQL_SEL_FORMA_PAGO_MESA, p1, JavaCallInfra.fieldMapper(FormasDePagoMesa.class));

        Map<String, Object> p2 = new HashMap<>();
        p2.put("IdFormaDePago", idFormasPago);
        p2.put("IdPortafolio", idPortafolios);
        List<FormasDePagoCliente> formasDePagoCliente = JavaCallInfra.query(
                conn, SQL_SEL_FORMA_PAGO_CLIENTE, p2, JavaCallInfra.fieldMapper(FormasDePagoCliente.class));

        JsonObject crearEntrada = new JsonObject();
        crearEntrada.addProperty("p_IdEmpresa", entrada.p_IdEmpresa);
        crearEntrada.addProperty("p_IdUsuario", entrada.p_IdUsuario);
        crearEntrada.addProperty("p_IdModulo",         entrada.p_IdModulo != null         ? entrada.p_IdModulo         : "MSP");
        crearEntrada.addProperty("p_CalculaMargen",    entrada.p_CalculaMargen != null    ? entrada.p_CalculaMargen    : "N");
        crearEntrada.addProperty("p_UsaReglasNegocio", entrada.p_UsaReglasNegocio != null ? entrada.p_UsaReglasNegocio : "S");
        crearEntrada.addProperty("p_SimularOperacion", entrada.p_SimularOperacion != null ? entrada.p_SimularOperacion : "N");
        crearEntrada.addProperty("p_EsCargaMasiva", "S");

        JsonArray operaciones = new JsonArray();
        JsonArray formasPago = new JsonArray();
        long idInterno = 1L;

        for (OperacionSpotDetalle d : detalle) {
            String monedaPrincipal  = moneda(d.FormaPagoEntregamos);
            String monedaSecundaria = moneda(d.FormaPagoRecibimos);

            boolean esCompra = "COMSPT".equalsIgnoreCase(d.TipoOperacion);
            BigDecimal tipoPrincipal  = esCompra ? d.TipoCambioCosto   : d.TipoCambioCierre;
            BigDecimal tipoSecundario = esCompra ? d.TipoCambioCierre  : d.TipoCambioCosto;
            BigDecimal monto2 = d.MontoMonedaPrincipal != null && d.TipoCambioCierre != null
                    ? d.MontoMonedaPrincipal.multiply(d.TipoCambioCierre) : BigDecimal.ZERO;
            BigDecimal margen = (tipoPrincipal != null && tipoSecundario != null && d.MontoMonedaPrincipal != null)
                    ? tipoPrincipal.multiply(d.MontoMonedaPrincipal).subtract(tipoSecundario.multiply(d.MontoMonedaPrincipal))
                    : BigDecimal.ZERO;

            JsonObject op = new JsonObject();
            op.addProperty("NumeroOperacion",          d.IdCargaMasivaDetalle != null ? d.IdCargaMasivaDetalle.toString() : "");
            op.addProperty("IdInstrumento",            d.IdInstrumento);
            op.addProperty("IdTipoOperacion",          d.TipoOperacion);
            op.addProperty("ProductoAgrupador",        d.ProductoAgrupador);
            op.addProperty("IdTipoProducto",           "MEME");
            op.addProperty("IdCliente",                d.IdCliente);
            op.addProperty("IdPortafolio",             d.IdPortafolio);
            op.addProperty("CodigoPortafolio",         d.CodigoPortafolio);
            op.addProperty("FechaOperacion",           d.FechaOperacion);
            op.addProperty("HoraOperacion",            d.HoraOperacion);
            op.addProperty("IdMonedaPrincipal",        monedaPrincipal);
            op.addProperty("IdMonedaSecundaria",       monedaSecundaria);
            op.add("MontoMonedaPrincipal",             GSON.toJsonTree(d.MontoMonedaPrincipal));
            op.add("MontoMonedaSecundaria",            GSON.toJsonTree(monto2));
            op.add("TipoCambioCierre",                 GSON.toJsonTree(d.TipoCambioCierre));
            op.add("TipoCambioCierreForward",          GSON.toJsonTree(BigDecimal.ZERO));
            op.add("ParidadCierre",                    GSON.toJsonTree(BigDecimal.ZERO));
            op.add("ParidadCierreForward",             GSON.toJsonTree(BigDecimal.ZERO));
            op.add("TipoCambioCosto",                  GSON.toJsonTree(d.TipoCambioCosto));
            op.add("ParidadCosto",                     GSON.toJsonTree(BigDecimal.ZERO));
            op.add("MontoEquivalente",                 GSON.toJsonTree(monto2));
            op.add("MontoRentabilidad",                GSON.toJsonTree(BigDecimal.ZERO));
            op.add("MontoRentabilidadCanal",           GSON.toJsonTree(BigDecimal.ZERO));
            op.add("Margen",                           GSON.toJsonTree(margen));
            op.add("Utilidad",                         GSON.toJsonTree(margen));
            op.add("PuntaCompra",                      GSON.toJsonTree(d.PuntaCompra));
            op.add("PuntaVenta",                       GSON.toJsonTree(d.PuntaVenta));
            op.addProperty("SolicitudTipoCambio",      "N");
            op.addProperty("IdMesa",                   d.IdMesa);
            op.addProperty("IdModulo",                 entrada.p_IdModulo != null ? entrada.p_IdModulo : "MSP");
            op.addProperty("IdTipoDolar",              1L);
            op.addProperty("TipoComprobanteFacturacion", d.TipoComprobanteFacturacion);
            op.addProperty("IdAgente",                 d.IdPersonaEjecutivo);
            op.addProperty("BenOur",                   "BEN");
            op.add("EquivalenteConGasto",              GSON.toJsonTree(BigDecimal.ZERO));
            op.add("TipoCambioCierreConGasto",         GSON.toJsonTree(BigDecimal.ZERO));
            op.addProperty("Observacion",              d.Observacion);
            op.addProperty("CodigoComercio",           d.CodigoComercio);
            op.addProperty("FolioPlanilla",            0L);
            op.addProperty("FolioInterfaz",            "");
            op.addProperty("OperacionFicticia",        "N");
            op.addProperty("IdTipoCanalIngreso",       d.CanalIngreso);
            op.addProperty("IdDatatec",                "0");
            op.addProperty("Estado",                   "VIG");
            op.addProperty("IdSolicitudAporteRescate", 0L);
            op.addProperty("AfectoADerivado",  blankToN(d.AfectoADerivado));
            op.addProperty("DolarObservado",   blankToN(d.DolarObservado));
            operaciones.add(op);

            // Forma de pago 1: MOVEGR (entregamos)
            JsonObject fp1 = new JsonObject();
            fp1.addProperty("IdInternoPagoOperacion",    idInterno++);
            fp1.addProperty("IdComprobantePagoOperacion", 0L);
            fp1.addProperty("IdTipoMovimiento",           "MOVEGR");
            fp1.addProperty("IdFormaDePago",              d.FormaPagoEntregamos);
            fp1.addProperty("IdOperacion",                d.IdCargaMasivaDetalle);
            fp1.addProperty("RecibePagaPortafolio",       esCompra ? "R" : "P");
            fp1.addProperty("IdMoneda",                   monedaPrincipal);
            fp1.addProperty("IdMonedaLiquidacion",        monedaPrincipal);
            fp1.add("MontoFormaDePago",                   GSON.toJsonTree(d.MontoMonedaPrincipal));
            fp1.addProperty("AfectarTesoreria",           contieneCI(d.FormaPagoEntregamos) ? "N" : "S");
            fp1.addProperty("IdCuentaBancaria",           d.IdCuentaBancariaCliente);
            formasPago.add(fp1);

            // Forma de pago 2: MOVING (recibimos)
            Long cuentaRecibimos;
            if ("VENSPT".equals(d.TipoOperacion)) {
                cuentaRecibimos = formasPagoMesa.stream()
                        .filter(fpm -> fpm.IdFormaDePago != null && fpm.IdFormaDePago.equals(d.FormaPagoRecibimos))
                        .map(fpm -> fpm.IdCuentaBancaria)
                        .findFirst().orElse(0L);
            } else {
                cuentaRecibimos = formasDePagoCliente.stream()
                        .filter(fpc -> fpc.IdFormaDePago != null && fpc.IdFormaDePago.equals(d.FormaPagoRecibimos)
                                    && fpc.IdPortafolio != null && fpc.IdPortafolio.equals(d.IdPortafolio))
                        .map(fpc -> fpc.IdCuentaBancaria)
                        .findFirst().orElse(0L);
            }
            JsonObject fp2 = new JsonObject();
            fp2.addProperty("IdInternoPagoOperacion",    idInterno++);
            fp2.addProperty("IdComprobantePagoOperacion", 0L);
            fp2.addProperty("IdTipoMovimiento",           "MOVING");
            fp2.addProperty("IdFormaDePago",              d.FormaPagoRecibimos);
            fp2.addProperty("IdOperacion",                d.IdCargaMasivaDetalle);
            fp2.addProperty("RecibePagaPortafolio",       esCompra ? "P" : "R");
            fp2.addProperty("IdMoneda",                   monedaSecundaria);
            fp2.addProperty("IdMonedaLiquidacion",        monedaSecundaria);
            fp2.add("MontoFormaDePago",                   GSON.toJsonTree(monto2));
            fp2.addProperty("AfectarTesoreria",           contieneCI(d.FormaPagoRecibimos) ? "N" : "S");
            fp2.addProperty("IdCuentaBancaria",           cuentaRecibimos);
            formasPago.add(fp2);
        }

        crearEntrada.add("p_DatosOperacion", operaciones);
        crearEntrada.add("p_DatosFormaPago",  formasPago);

        return plataforma.call(FQN_CREAR_SPOT, crearEntrada);
    }

    private String obtenerFechaSistema(Long idEmpresa) {
        try {
            JsonObject p = new JsonObject();
            p.addProperty("p_IdEmpresa",  idEmpresa);
            p.addProperty("p_IdSistema",  "PLATAFORMA");
            p.addProperty("p_IdParametro","PLA.FechaSistema");
            JsonObject result = plataforma.call(FQN_CONFIG, p);
            if (result.has("valor")) return result.get("valor").getAsString();
        } catch (Exception e) {
            log.warn("No se pudo obtener FechaSistema, usando fecha actual: {}", e.getMessage());
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private static String moneda(String formaPago) {
        return (formaPago != null && formaPago.length() >= 14) ? formaPago.substring(11, 14) : "";
    }

    private static boolean contieneCI(String formaPago) {
        return formaPago != null && formaPago.contains("_CIN_");
    }

    private static String blankToN(String val) {
        return (val != null && !val.trim().isEmpty()) ? val.trim() : "N";
    }

    // ─── Inner classes — same contract as legacy ─────────────────────────────────

    public static class Entrada {
        public Long   p_IdEmpresa;
        public Long   p_IdUsuario;
        public String p_IdModulo           = "MSP";
        public String p_UsaReglasNegocio   = "S";
        public String p_SimularOperacion   = "N";
        public String p_CalculaMargen      = "N";
        public String p_IdTipoCanalIngreso = "AGE";
        public List<OperacionSpot> p_CargaMasivaProcesar = new ArrayList<>();
    }

    public static class OperacionSpot {
        public Long IdCargaMasiva;
    }

    public static class FormasDePagoCliente {
        public Long   IdPortafolio;
        public Long   IdCuentaBancaria;
        public String IdFormaDePago;
    }

    public static class FormasDePagoMesa {
        public Long   IdCuentaBancaria = 0L;
        public String IdFormaDePago;
    }

    public static class OperacionCargaMasiva {
        public Long   IdCargaMasiva;
        public String FechaCarga;
        public String FechaProceso;
        public Long   CantidadRegistros;
        public Long   IdEmpresa;
        public Long   IdUsuario;
        public String IdEstado;
        public String NombreArchivo;
        public Long   IdTipoCargaMasiva;
        public String CodigoTipoCargaMasiva;
    }

    public static class OperacionSpotDetalle {
        public Long       IdCargaMasivaDetalle;
        public String     DocumentoIdentidad;
        public String     CodigoPortafolio;
        public String     ProductoAgrupador;
        public String     TipoOperacion;
        public String     FechaOperacion;
        public String     HoraOperacion;
        public String     CanalIngreso;
        public BigDecimal MontoMonedaPrincipal;
        public BigDecimal TipoCambioCierre;
        public BigDecimal TipoCambioCosto;
        public BigDecimal PuntaCompra;
        public BigDecimal PuntaVenta;
        public String     FormaPagoEntregamos;
        public String     FormaPagoRecibimos;
        public String     Observacion;
        public String     AfectoADerivado;
        public String     DolarObservado;
        public Long       CodigoComercio;
        public String     DetalleValidacion   = "";
        public Long       IdCargaMasiva;
        public Long       IdTipoCargaMasiva;
        public Long       IdCliente;
        public Long       IdPortafolio;
        public String     TipoComprobanteFacturacion;
        public Long       IdMesa                  = 0L;
        public Long       IdCuentaBancariaCorredora = 0L;
        public Long       IdCuentaBancariaCliente   = 0L;
        public Long       IdInstrumento             = 0L;
        public Long       IdPersonaEjecutivo        = 0L;
    }

    /** Output contract matches legacy SalidaProcesarCargaMasivaArchivoOperacionSpot. */
    public static class Salida {
        public Long                P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES        = new ArrayList<>();
    }
}
