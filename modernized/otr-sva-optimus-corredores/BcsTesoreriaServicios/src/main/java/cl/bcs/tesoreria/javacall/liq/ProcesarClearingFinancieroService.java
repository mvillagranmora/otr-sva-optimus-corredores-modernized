package cl.bcs.tesoreria.javacall.liq;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.javacall.kit.colaborador.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.tesoreria.javacall.liq.ClearingFinanciero.Resumen;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Legacy {@code cl.bcs.tesoreria.liq.ProcesarClearingFinanciero} (TES-B-47..63): runs the clearing query, turns the net balance of every chamber group
 * into an ingreso/egreso (collaborator CrearIngresoEgreso) and stamps the generated comprobante on the transactions, dividends and derivative liquidations.
 *
 * <p>LEGACY DEFECT kept (see README TES-B-47): every transaction, dividend and liquidation is stamped with the comprobante of the LAST group, also the
 * ones already processed upstream; with no group left (zero net balances, TES-B-58) they are stamped NULL.
 * LEGACY DEFECT kept (see README TES-B-57): only the CL localization is migrated; PE (or any other value) dereferences a null clearing result
 * (NullPointerException).
 * The chamber references are drawn from {@link Random} (legacy); the goldens mask them by order of appearance.
 */
@Service
public class ProcesarClearingFinancieroService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarClearingFinancieroService.class);

    private static final String SELECT_CAMARA_COMPENSACION = SqlNombrado.recurso("ProcesarClearingFinanciero_SelectCamaraDeCompensacion");
    private static final String SELECT_CAMARAS = SqlNombrado.recurso("ProcesarClearingFinanciero_SelectCamaras");
    private static final String SELECT_PREFERENCIAS_DE_PAGO = SqlNombrado.recurso("ProcesarClearingFinanciero_SelectPreferenciasDePago");
    private static final String SELECT_PREFERENCIAS_DE_PAGO_PORTAFOLIO = SqlNombrado.recurso("ProcesarClearingFinanciero_SelectPreferenciasDePagoPortafolioClearing");
    private static final String SELECT_COMPROBANTE = SqlNombrado.recurso("ProcesarClearingFinanciero_SelectComprobante");
    private static final String UPDATE_TRANSACCIONES = SqlNombrado.recurso("ProcesarClearingFinanciero_UpdateTransacciones");
    private static final String UPDATE_EVENTO_CAPITAL_CLIENTE = SqlNombrado.recurso("ProcesarClearingFinanciero_UpdateEventoCapitalCliente");
    private static final String UPDATE_LIQUIDACIONES = SqlNombrado.recurso("ProcesarClearingFinanciero_UpdateLiquidaciones");

    // ------------------------------------------------------------------------------------------------ request / response (the JavaCall contract)

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_FechaLiquidacion;
        /** Country of the settlement house. */
        public Long p_IdPais;
        public List<Camara> p_Camaras;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    // ------------------------------------------------------------------------------------------------ rows

    public static class PreferenciaPago {
        public String IdTipoMovimiento;
        public String IdMedioDePago;
        public String IdModalidadDePago;
        public Long IdCuentaBanco;
        public String IdMoneda;
        public String IdConcepto;
    }

    public static class PreferenciaPagoPortafolioClearing {
        public String IdPreferenciaPago;
        public Long IdCuentaBancaria;
        public Long IdBancoCuentaBancaria;
        public String NombreBancoCuentaBancaria;
        public String NumeroCuentaBancaria;
        public String IdEstado;
        public String TipoMovimiento;
        public String TipoOperacion;
        public String NombreTipoCuentaBancaria;
        public String IdTipoCuentaBancaria;
        public String IdMedioDePago;
        public String MedioDePago;
        public String IdMoneda;
        public String RecibePagaPortafolio;
    }

    public record IngresoEgresoComprobante(Long IdIngresoEgreso, Long IdComprobante) {
    }

    public record TransaccionUpd(Long IdTransaccion, Long IdComprobanteTesoreria) {
    }

    public record EventoCapitalClienteUpd(Long IdEventoCapitalCliente, Long IdComprobanteTesoreria) {
    }

    public record LiquidacionUpd(Long IdCargoAbonoMovimiento, Long IdComprobanteTesoreria) {
    }

    /** Net balance of one chamber / currency group; its reference identifies the generated movement. */
    private static final class GrupoCamara {
        Long numeroReferencia;
        Long codigoGrupoCamara;
        String nombreGrupoCamara;
        String idMoneda;
        BigDecimal saldoCamara = BigDecimal.ZERO;
    }

    /** No payment preference for a movement type / concept / currency (the legacy signalled it with a RuntimeException message). */
    private static final class SinPreferenciaPagoException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final String clave;

        SinPreferenciaPagoException(String clave) {
            super("SIN_PREFERENCIA_PAGO|" + clave);
            this.clave = clave;
        }
    }

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final ConfiguracionSistema configuracion;
    private final ConsultarClearingFinancieroService consultarClearing;
    private final Colaboradores colaboradores;

    public ProcesarClearingFinancieroService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, ConfiguracionSistema configuracion,
            ConsultarClearingFinancieroService consultarClearing, Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.configuracion = configuracion;
        this.consultarClearing = consultarClearing;
        this.colaboradores = colaboradores;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        var gson = JsonLegacy.gson();
        Entrada in = gson.fromJson(entrada.toString(), Entrada.class);
        Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        return gson.toJsonTree(out).getAsJsonObject();
    }

    private static String concepto(Long codigoGrupoCamara, String localizacionSistema) {
        String concepto = "";
        if (codigoGrupoCamara.equals(1L) && localizacionSistema.equalsIgnoreCase("CL")) {
            concepto = "CLFIPM";
        }
        if (codigoGrupoCamara.equals(2L) && localizacionSistema.equalsIgnoreCase("CL")) {
            concepto = "CFPHSM";
        }
        if (codigoGrupoCamara.equals(3L) && localizacionSistema.equalsIgnoreCase("CL")) {
            concepto = "CLFIRV";
        }
        if (codigoGrupoCamara.equals(4L) && localizacionSistema.equalsIgnoreCase("CL")) {
            concepto = "CLFIDV";
        }
        if (codigoGrupoCamara.equals(5L) && localizacionSistema.equalsIgnoreCase("CL")) {
            concepto = "CLFIHS";
        }
        if (codigoGrupoCamara.equals(1L) && localizacionSistema.equalsIgnoreCase("PE")) {
            concepto = "CLFIRV";
        }
        if (codigoGrupoCamara.equals(2L) && localizacionSistema.equalsIgnoreCase("PE")) {
            concepto = "CLFIRF";
        }
        if (codigoGrupoCamara.equals(3L) && localizacionSistema.equalsIgnoreCase("PE")) {
            concepto = "CLFIRP";
        }
        if (codigoGrupoCamara.equals(4L) && localizacionSistema.equalsIgnoreCase("PE")) {
            concepto = "CLFIRM";
        }
        return concepto;
    }

    private Salida logica(Entrada in) throws Exception {
        Salida salida = new Salida();
        long id = 0L;
        try {
            log.info("INICIO  ProcesarClearingFinanciero");
            String localizacionSistema = Plataforma.parametro(configuracion, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");

            Map<String, Object> params = new HashMap<>();
            params.put("IdPaisCamara", in.p_IdPais);
            List<CamaraCompensacion> camarasEntidadLiquidadora = sql.consultar(SELECT_CAMARA_COMPENSACION, params, CamaraCompensacion.class);
            List<String> idsCamara = camarasEntidadLiquidadora.stream().map(s -> s.IdCamaraDeCompensacion).toList();
            List<CamaraCompensacion> sinPersona = camarasEntidadLiquidadora.stream().filter(s -> s.IdPersona == null || s.IdPersona.equals(0L)).toList();

            // payment preferences of the broker for the clearing
            Map<String, Object> paramsPrefPago = new HashMap<>();
            paramsPrefPago.put("IdEmpresa", in.p_IdEmpresa);
            List<PreferenciaPago> prefPagoList = sql.consultar(SELECT_PREFERENCIAS_DE_PAGO, paramsPrefPago, PreferenciaPago.class);

            Map<String, Object> paramsPrefPortafolio = new HashMap<>();
            paramsPrefPortafolio.put("IdEmpresa", in.p_IdEmpresa);
            paramsPrefPortafolio.put("IdPaisCamara", in.p_IdPais);
            List<PreferenciaPagoPortafolioClearing> prefPagoPortafolio = sql.consultar(SELECT_PREFERENCIAS_DE_PAGO_PORTAFOLIO, paramsPrefPortafolio,
                    PreferenciaPagoPortafolioClearing.class);

            if (idsCamara.isEmpty()) {
                salida.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, in.p_IdEmpresa, id++, MensajeSalida.MENSAJE_ERROR, "EMPRESA_LIQUIDADORA_NO_EXISTE", null));
            }
            if (!sinPersona.isEmpty()) {
                salida.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, in.p_IdEmpresa, id++, MensajeSalida.MENSAJE_ERROR, "EMPRESA_LIQUIDADORA_SIN_PERSONA_RELACIONADA", null));
            }
            if (prefPagoList.isEmpty()) {
                salida.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, in.p_IdEmpresa, id++, MensajeSalida.MENSAJE_ERROR, "PREFERENCIAS_DE_PAGO_CLEARING_NO_CONFIGURADAS", null));
            }
            if (MensajeSalida.contarErrores(salida.P_OUT_MENSAJES) > 0) {
                return salida;
            }

            List<String> camarasPedidas = in.p_Camaras.stream().map(s -> s.Camara).toList();
            params = new HashMap<>();
            params.put("IdCamaraDeCompensacion", idsCamara);
            params.put("Camaras", camarasPedidas);
            List<Camara> camaras = sql.consultar(SELECT_CAMARAS, params, Camara.class);

            ClearingFinanciero.Salida clearing = null;
            if (localizacionSistema.equalsIgnoreCase("CL")) {
                ClearingFinanciero.Entrada inClearing = new ClearingFinanciero.Entrada();
                inClearing.p_IdEmpresa = in.p_IdEmpresa;
                inClearing.p_IdUsuario = in.p_IdUsuario;
                inClearing.p_IdModulo = in.p_IdModulo;
                inClearing.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
                inClearing.p_FechaLiquidacion = in.p_FechaLiquidacion;
                inClearing.p_Camaras.addAll(camaras);
                clearing = consultarClearing.consultar(inClearing);
            }
            // LEGACY DEFECT kept (see README TES-B-57): the Peruvian query is not migrated, so 'clearing' stays null for PE and the next line throws

            salida.P_OUT_MENSAJES.addAll(clearing.P_OUT_MENSAJES);
            List<Resumen> resumenPendiente = clearing.P_OUT_CLEARINGFINANCIERORESUMEN.stream().filter(s -> s.IdComprobanteTesoreria.compareTo(0L) < 1).toList();
            if (resumenPendiente.isEmpty()) {
                salida.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, in.p_IdEmpresa, id++, MensajeSalida.MENSAJE_ERROR, "NO_EXISTEN_MOVIMIENTOS_A_PROCESAR", null));
            }
            // only the movements not yet processed take part (transactions and dividends, per the original queries)
            if (MensajeSalida.contarErrores(salida.P_OUT_MENSAJES) > 0) {
                return salida;
            }

            List<GrupoCamara> saldosGrupoCamara = saldosPorGrupo(resumenPendiente);

            // ---- treasury movements: one ingreso/egreso per chamber group
            CrearIngresoEgreso_Entrada inTeso = new CrearIngresoEgreso_Entrada();
            inTeso.p_IdEmpresa = in.p_IdEmpresa;
            inTeso.p_IdUsuario = in.p_IdUsuario;
            inTeso.p_IdModulo = in.p_IdModulo;
            inTeso.p_SimularOperacion = "N";
            inTeso.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
            inTeso.p_EsIngresoAutomatico = "S";
            inTeso.p_FechaMovimiento = null; // null: the service assigns the system date
            try {
                for (GrupoCamara saldo : saldosGrupoCamara) {
                    IngresoEgreso_Entrada ingresoEgreso = ingresoEgresoDe(saldo, camarasEntidadLiquidadora.get(0).IdPersona, in.p_FechaLiquidacion, localizacionSistema);
                    inTeso.p_IngresoEgresoGenerar.add(ingresoEgreso);
                    inTeso.p_FormaDePago.add(formaDePagoDe(ingresoEgreso, prefPagoList, prefPagoPortafolio));
                }
            } catch (SinPreferenciaPagoException e) {
                String codigoMensaje = "UI_SIN_PREFERENCIA_PAGO_CLEARING";
                MensajeSalida mensaje = Plataforma.mensaje(diccionario, in.p_IdEmpresa, null, MensajeSalida.MENSAJE_ERROR, codigoMensaje,
                        Plataforma.valores().con("Clave", e.clave));
                salida.P_OUT_MENSAJES.add(mensaje);
                salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR + 1L;
                return salida;
            }

            CrearIngresoEgreso_Salida ingEgrTesoSalida = CrearIngresoEgreso.invocar(colaboradores, inTeso);
            salida.P_OUT_MENSAJES.addAll(ingEgrTesoSalida.P_OUT_MENSAJES);
            if (MensajeSalida.contarErrores(salida.P_OUT_MENSAJES) > 0) {
                return salida;
            }

            // comprobantes generated for those movements
            params.clear();
            params.put("IdIngresoEgreso", ingEgrTesoSalida.P_OUT_INGRESOEGRESOGENERADO.stream().map(s -> s.IdIngresoEgreso).toList());
            List<IngresoEgresoComprobante> comprobanteTeso = sql.consultar(SELECT_COMPROBANTE, params, IngresoEgresoComprobante.class);

            // ---- stamp the comprobante on transactions, dividends (CL) and derivative liquidations
            List<TransaccionUpd> updateTransaccion = new ArrayList<>();
            for (var det : clearing.P_OUT_CLEARINGFINANCIERODETALLE) {
                updateTransaccion.add(new TransaccionUpd(det.IdTransaccion, comprobanteDelUltimoGrupo(saldosGrupoCamara, ingEgrTesoSalida, comprobanteTeso)));
            }
            sql.lote(UPDATE_TRANSACCIONES, updateTransaccion);

            if (localizacionSistema.equalsIgnoreCase("CL")) {
                List<EventoCapitalClienteUpd> updateEventoCapitalCliente = new ArrayList<>();
                for (var det : clearing.P_OUT_CLEARINGFINANCIERODIVIDENDOS) {
                    updateEventoCapitalCliente.add(new EventoCapitalClienteUpd(det.IdEventoCapitalCliente,
                            comprobanteDelUltimoGrupo(saldosGrupoCamara, ingEgrTesoSalida, comprobanteTeso)));
                }
                sql.lote(UPDATE_EVENTO_CAPITAL_CLIENTE, updateEventoCapitalCliente);
            }

            List<LiquidacionUpd> updateLiquidaciones = new ArrayList<>();
            for (var det : clearing.P_OUT_CLEARINGFINANCIEROLIQUIDACIONES) {
                updateLiquidaciones.add(new LiquidacionUpd(det.IdMovimiento, comprobanteDelUltimoGrupo(saldosGrupoCamara, ingEgrTesoSalida, comprobanteTeso)));
            }
            sql.lote(UPDATE_LIQUIDACIONES, updateLiquidaciones);

            log.info("TERMINO ProcesarClearingFinanciero");
        } finally {
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
        }
        return salida;
    }

    /**
     * Net balance (sales minus purchases) per chamber code / name / currency, in the order the groups first appear; groups with a zero balance are dropped.
     * The reference is a random long (legacy {@code new Random().nextLong()}).
     */
    private static List<GrupoCamara> saldosPorGrupo(List<Resumen> resumen) {
        Map<String, GrupoCamara> grupos = new LinkedHashMap<>();
        for (Resumen r : resumen) {
            String ref = r.CodigoGrupoCamara + "-" + r.NombreGrupoCamara + "-" + r.IdMonedaTransaccion;
            GrupoCamara grupo = grupos.get(ref);
            if (grupo == null) {
                grupo = new GrupoCamara();
                grupo.numeroReferencia = new Random().nextLong();
                grupo.codigoGrupoCamara = r.CodigoGrupoCamara;
                grupo.nombreGrupoCamara = r.NombreGrupoCamara;
                grupo.idMoneda = r.IdMonedaTransaccion;
                grupos.put(ref, grupo);
            }
            grupo.saldoCamara = grupo.saldoCamara.add(r.TotalVenta.subtract(r.TotalCompra));
        }
        return grupos.values().stream().filter(g -> g.saldoCamara.compareTo(BigDecimal.ZERO) != 0).toList();
    }

    private static IngresoEgreso_Entrada ingresoEgresoDe(GrupoCamara saldo, Long idPersonaCamara, String fechaLiquidacion, String localizacionSistema) {
        IngresoEgreso_Entrada ie = new IngresoEgreso_Entrada();
        ie.NumeroReferencia = saldo.numeroReferencia;
        ie.IdTipoMovimiento = saldo.saldoCamara.compareTo(BigDecimal.ZERO) == -1 ? "MOVEGR" : "MOVING";
        ie.IdTipoPersona = "CNT";
        ie.IdPersona = idPersonaCamara;
        ie.IdPortafolio = 0L;
        ie.IdMoneda = saldo.idMoneda;
        ie.Monto = saldo.saldoCamara.abs();
        ie.FechaLiquidacion = fechaLiquidacion;
        ie.IdConcepto = concepto(saldo.codigoGrupoCamara, localizacionSistema);
        ie.IdTipoPersonaTitularPago = "CNT";
        ie.IdPersonaTitularPago = idPersonaCamara;
        ie.IdPortafolioTitularPago = 0L;
        ie.IdCaja = 0L;
        ie.SecuenciaAgrupacion = "";
        ie.SecuenciaRelacionada = 0L;
        ie.Glosa = "CLEARING FINANCIERO - CAMARA: " + saldo.nombreGrupoCamara;
        ie.IdEmpleadoDigitador = 0L;
        ie.IdSucursalEmpleadoDigitador = 0L;
        ie.IdTipoProducto = "";
        ie.IdTipoOperacion = "";
        ie.IdTipoComprobante = "";
        ie.IdComprobante = 0L;
        ie.FechaComprobante = "0001-01-01";
        ie.FechaLiquidacionComprobante = "0001-01-01";
        ie.IdOperacion = 0L;
        ie.FechaOperacion = "0001-01-01";
        ie.IdCondicionLiquidacion = "";
        ie.IdBolsa = "";
        ie.IdMonedaOperacion = "";
        ie.MontoOperacion = BigDecimal.ZERO;
        ie.IdTipoReajuste = "";
        ie.FechaParidad = "0001-01-01";
        ie.ValorParidad = BigDecimal.ZERO;
        ie.FolioCartera = 0L;
        ie.IdInstrumento = 0L;
        ie.IdTipoInstrumento = "";
        ie.IdClasificacionTipoInstrumento = "";
        ie.Bilateral = "N";
        ie.GeneraComprobante = "S";
        ie.AgruparEnComprobante = "S";
        ie.MueveCuentaInversion = "S";
        ie.TitularPago = "S";
        return ie;
    }

    private static FormaDePago_Entrada formaDePagoDe(IngresoEgreso_Entrada ie, List<PreferenciaPago> preferencias,
            List<PreferenciaPagoPortafolioClearing> preferenciasPortafolio) {
        PreferenciaPagoPortafolioClearing port = preferenciasPortafolio.stream()
                .filter(q -> q.TipoMovimiento.equalsIgnoreCase(ie.IdTipoMovimiento) && q.IdMoneda.equalsIgnoreCase(ie.IdMoneda)).findAny().orElse(null);
        PreferenciaPago pag = preferencias.stream()
                .filter(p -> p.IdTipoMovimiento.equalsIgnoreCase(ie.IdTipoMovimiento) && p.IdConcepto.equalsIgnoreCase(ie.IdConcepto)
                        && p.IdMoneda.equalsIgnoreCase(ie.IdMoneda))
                .findAny().orElseThrow(() -> new SinPreferenciaPagoException(ie.IdTipoMovimiento + "-" + ie.IdConcepto + "-" + ie.IdMoneda));

        FormaDePago_Entrada fp = new FormaDePago_Entrada();
        fp.NumeroReferencia = ie.NumeroReferencia;
        fp.NumeroReferenciaFormaDePago = ie.NumeroReferencia;
        fp.IdMedioDePago = pag != null ? pag.IdMedioDePago : "";
        fp.IdModalidadDePago = pag != null ? pag.IdModalidadDePago : "";
        fp.IdMoneda = ie.IdMoneda;
        fp.Monto = ie.Monto;
        fp.IdCuentaBancariaCorredora = pag != null ? pag.IdCuentaBanco : 0L;
        fp.IdTarjetaBancariaCorredora = 0L;
        fp.IdChequera = 0L;
        fp.NumeroDocumentoCorredora = 0L;
        fp.FechaDocumentoPagoCorredora = "0001-01-01";
        fp.IdCuentaBancariaSocio = port != null ? port.IdCuentaBancaria : 0L;
        fp.IdBancoSocio = port != null ? port.IdBancoCuentaBancaria : 0L;
        fp.NombreCuentaBancariaSocio = port != null ? port.NombreBancoCuentaBancaria : "";
        fp.NumeroCuentaBancariaSocio = port != null ? port.NumeroCuentaBancaria : "";
        fp.IdTipoCuentaBancariaSocio = port != null ? port.IdTipoCuentaBancaria : "";
        fp.NombreTipoCuentaBancariaSocio = port != null ? port.NombreTipoCuentaBancaria : "";
        fp.IdTarjetaBancariaSocio = 0L;
        fp.IdTipoTarjetaBancariaSocio = "";
        fp.NombreTipoTarjetaBancariaSocio = "";
        fp.NumeroTarjetaBancariaSocio = "";
        fp.NumeroSerieSocio = "";
        fp.NumeroDocumentoSocio = 0L;
        fp.FechaDocumentoPagoSocio = "0001-01-01";
        return fp;
    }

    /**
     * Comprobante to stamp on a movement. LEGACY DEFECT kept (see README TES-B-47): the legacy looks the comprobante up for EVERY group and keeps the
     * last one, so every movement gets the last group's comprobante; with no group the result is null (TES-B-58). A group whose movement was not generated
     * (or has no comprobante) throws NoSuchElementException, as the legacy {@code Optional.get()} did.
     */
    private static Long comprobanteDelUltimoGrupo(List<GrupoCamara> grupos, CrearIngresoEgreso_Salida generados, List<IngresoEgresoComprobante> comprobantes) {
        Long idComprobante = null;
        for (GrupoCamara saldo : grupos) {
            IngresoEgresoGenerado_Salida teso = generados.P_OUT_INGRESOEGRESOGENERADO.stream().filter(t -> t.NumeroReferencia.equals(saldo.numeroReferencia)).findAny()
                    .get();
            idComprobante = comprobantes.stream().filter(c -> c.IdIngresoEgreso().equals(teso.IdIngresoEgreso)).map(IngresoEgresoComprobante::IdComprobante).findFirst().get();
        }
        return idComprobante;
    }
}
