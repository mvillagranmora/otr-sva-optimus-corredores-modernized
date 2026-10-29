package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bills the commissions of the cash / custody patrimony (legacy {@code ProcesarCobroComisionPatrimonio}; CTA-B-109..116).
 *
 * <p>The VIG commissions of the filter (all of them in a mass run, CTA-B-109; otherwise only the selected portfolios, CTA-B-110) become
 * one voucher movement per portfolio through the billing collaborator ({@code CrearMovimientoComprobante}: concept CMMNRV / CMTAPV for
 * APV, voucher FOE / FOX). If billing answers errors they are returned and nothing else changes (CTA-B-113); otherwise the VIG
 * custody and cash patrimony rows of the portfolio inside the period and its ACT edited rows become PRO (CTA-B-115, 116).
 */
@Service
public class ProcesarCobroComisionPatrimonioService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCobroComisionPatrimonioService.class);

    private static final String SQL_COBRO_IVA = SqlReportes.recurso("ProcesarCobroComisionPatrimonio_SelectCobroIva");
    private static final String SQL_PROCESAR_CAJA = SqlReportes.recurso("ProcesarCobroComisionPatrimonio_UpdateEstadoPatrimonioCajaProcesado");
    private static final String SQL_PROCESAR_CUSTODIA = SqlReportes.recurso("ProcesarCobroComisionPatrimonio_UpdateEstadoPatrimonioCustodiaProcesado");
    private static final String SQL_PROCESAR_EDITADOS = SqlReportes.recurso("ProcesarCobroComisionPatrimonio_UpdateEstadoPatrimonioEditadosProcesado");

    private final DataSource dataSource;
    private final SqlReportes sql;
    private final ColaboradoresReportes colaboradores;

    public ProcesarCobroComisionPatrimonioService(DataSource dataSource, ColaboradoresReportes colaboradores) {
        this.dataSource = dataSource;
        this.sql = new SqlReportes(dataSource);
        this.colaboradores = colaboradores;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(procesar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida procesar(Entrada entrada) throws Exception {
        Salida salida = new Salida();
        Mensajeria mensajes = new Mensajeria(sql, entrada.p_IdEmpresa);

        if ("N".equals(entrada.p_EsProcesoMasivo) && entrada.p_PortafoliosSeleccionados.isEmpty()) {
            salida.error(mensajes.error("UI_ERROR_ENTRADA_SIN_PORTAFOLIOS"));
            return salida;
        }

        List<Registro> registros = consultarComisiones(entrada).stream()
                .filter(r -> "VIG".equals(r.EstadoComision))
                .filter(r -> "S".equals(entrada.p_EsProcesoMasivo) || seleccionado(entrada, r.IdPortafolio))
                .toList();
        if (registros.isEmpty()) {
            salida.error(mensajes.error("UI_ERROR_SIN_COMISIONES_A_PROCESAR"));
            return salida;
        }

        SalidaComprobante comprobante = facturar(registros, entrada, mensajes);
        if (comprobante.P_OUT_ERRORESCONTADOR > 0L) {
            comprobante.P_OUT_MENSAJES.stream().filter(m -> MensajeSalida.ERROR.equals(m.tipo())).forEach(salida.P_OUT_MENSAJES::add);
            salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
            return salida;
        }

        List<PatrimonioUpdate> patrimonios = comprobante.p_out_Movimientos.stream()
                .map(m -> new PatrimonioUpdate(m.NumeroMvtoCmbte, entrada.p_Filtros.Periodo)).toList();
        log.info("ProcesarCobroComisionPatrimonio: updating the state of the processed commissions");
        sql.lote(SQL_PROCESAR_CAJA, patrimonios);
        sql.lote(SQL_PROCESAR_CUSTODIA, patrimonios);
        sql.lote(SQL_PROCESAR_EDITADOS, patrimonios);
        salida.P_OUT_MENSAJES.add(mensajes.ok("UI_PATRIMONIOS_PROCESADOS_OK", Map.of("CantidadPatrimoniosProcesados", String.valueOf(patrimonios.size()))));
        return salida;
    }

    private static boolean seleccionado(Entrada entrada, Long idPortafolio) {
        return entrada.p_PortafoliosSeleccionados.stream().anyMatch(p -> p.IdPortafolio.equals(idPortafolio));
    }

    /** The commission register of the filter, from {@link ConsultarCobroComisionRegistrosService} (same JSON contract as the legacy call). */
    private List<Registro> consultarComisiones(Entrada entrada) {
        JsonObject pedido = new JsonObject();
        pedido.addProperty("p_IdEmpresa", entrada.p_IdEmpresa);
        pedido.addProperty("p_IdModulo", entrada.p_IdModulo);
        pedido.addProperty("p_IdUsuario", entrada.p_IdUsuario);
        pedido.addProperty("p_UsaReglasNegocio", entrada.p_UsaReglasNegocio);
        pedido.addProperty("p_FechaDesde", entrada.p_Filtros.FechaDesde);
        pedido.addProperty("p_FechaHasta", entrada.p_Filtros.FechaHasta);
        pedido.addProperty("p_Cliente", entrada.p_Filtros.IdCliente);
        pedido.addProperty("p_Portafolio", entrada.p_Filtros.IdPortafolio);
        pedido.addProperty("p_IdEstado", entrada.p_Filtros.IdEstado);
        pedido.addProperty("p_IdProductoAgrupador", entrada.p_Filtros.IdProductoAgrupador);
        pedido.addProperty("p_Periodo", entrada.p_Filtros.Periodo);
        JsonObject respuesta = new ConsultarCobroComisionRegistrosService(dataSource).execute(pedido);
        return JsonReportes.leer(respuesta, Consulta.class).P_OUT_TABLA;
    }

    private SalidaComprobante facturar(List<Registro> registros, Entrada entrada, Mensajeria mensajes) throws Exception {
        ConfiguracionSistema config = new ConfiguracionSistema(sql);
        String monedaSistema = config.cadena(entrada.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.MonedaSistema");
        String bolsa = config.cadena(entrada.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.BolsaDefectoSistema");
        String fechaSistema = config.cadena(entrada.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.FechaSistema");
        String glosaApv = mensajes.texto("UI_GLOSA_MOVIMIENTO_COMPROBANTE_APV", Map.of());
        String glosaNoApv = mensajes.texto("UI_GLOSA_MOVIMIENTO_COMPROBANTE_NO_APV", Map.of());

        // LEGACY DEFECT kept (see README CTA-B-114): the IVA row is required (get(0) throws IndexOutOfBoundsException without it) although
        // the resulting rate is never used.
        List<Cobro> cobros = sql.consultar(SQL_COBRO_IVA, Cobro.class);
        new BigDecimal(cobros.get(0).ValorCobro.toString()).divide(BigDecimal.valueOf(100));

        EntradaComprobante pedido = new EntradaComprobante();
        pedido.p_IdEmpresa = entrada.p_IdEmpresa;
        pedido.p_IdModulo = entrada.p_IdModulo;
        pedido.p_IdUsuario = entrada.p_IdUsuario;
        pedido.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
        long secuenciaFormaDePago = 0;
        for (Registro r : registros) {
            boolean apv = "S".equals(r.PortafolioAPV);
            Movimiento m = new Movimiento();
            m.NumeroMvtoCmbte = r.IdPortafolio;
            m.IdEmpresa = entrada.p_IdEmpresa;
            m.IdUsuario = entrada.p_IdUsuario;
            m.IdConcepto = apv ? "CMTAPV" : "CMMNRV";
            m.FechaMovimientoComprobante = fechaSistema;
            m.FechaLiquidacion = fechaSistema;
            m.IdCliente = r.IdCliente;
            m.IdPortafolio = r.IdPortafolio;
            m.IdMoneda = monedaSistema;
            m.IdMonedaLiquidacion = monedaSistema;
            m.IdTipoComprobante = apv ? "FOX" : "FOE";
            m.Glosa = apv ? glosaApv : glosaNoApv;
            m.IdBolsa = bolsa;
            m.Monto = r.MontoComision;
            m.MontoLiquidacion = r.MontoComision;
            m.MontoMovimientoComprobante = r.MontoComision;
            pedido.p_Movimientos.add(m);

            FormaDePago forma = new FormaDePago();
            forma.IdMoneda = monedaSistema;
            forma.MontoLiquidacionFormaDePago = r.MontoComision;
            forma.NumeroMvtoCmbte = m.NumeroMvtoCmbte;
            forma.NumeroMvtoCmbteFormaDePago = secuenciaFormaDePago++;
            pedido.p_MovimientosFormaDePago.add(forma);
        }
        return JsonReportes.leer(colaboradores.crearMovimientoComprobante(JsonReportes.escribir(pedido)), SalidaComprobante.class);
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa = 0L;
        public Long p_IdUsuario = 0L;
        public String p_IdModulo = "CTA";
        public String p_UsaReglasNegocio = "N";
        public String p_EsProcesoMasivo = "N";
        public Filtro p_Filtros = new Filtro();
        public List<ClientePortafolio> p_PortafoliosSeleccionados = new ArrayList<>();
    }

    public static class Filtro {
        public Long IdCliente = 0L;
        public Long IdPortafolio = 0L;
        public Long IdProductoAgrupador = 0L;
        public String IdEstado = "";
        public String FechaDesde = "0001-01-01";
        public String FechaHasta = "0001-01-01";
        public Long Periodo = 0L;
    }

    public static class ClientePortafolio {
        public Long IdCliente = 0L;
        public Long IdPortafolio = 0L;
        public BigDecimal MontoComision = BigDecimal.ZERO;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;

        void error(MensajeSalida m) {
            P_OUT_MENSAJES.add(m);
            P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(P_OUT_MENSAJES);
        }
    }

    /** Parameters of the three UPDATE statements: the portfolio (the voucher movement number) and the period. */
    public static class PatrimonioUpdate {
        public Long IdPortafolio = 0L;
        public Long IdPeriodo = 0L;

        PatrimonioUpdate(Long idPortafolio, Long idPeriodo) {
            this.IdPortafolio = idPortafolio;
            this.IdPeriodo = idPeriodo;
        }
    }

    /** Row of the commission register (the fields this service reads). */
    public static class Registro {
        public Long IdCliente;
        public Long IdPortafolio;
        public BigDecimal MontoComision;
        public String EstadoComision;
        public String PortafolioAPV;
    }

    static class Consulta {
        List<Registro> P_OUT_TABLA = new ArrayList<>();
    }

    public static class Cobro {
        public String IdCobro;
        public BigDecimal ValorCobro;
    }

    // ---- request / response of the billing collaborator (only the fields the legacy sends or reads; defaults as in its POJOs)

    public static class EntradaComprobante {
        public List<Movimiento> p_Movimientos = new ArrayList<>();
        public List<FormaDePago> p_MovimientosFormaDePago = new ArrayList<>();
        public List<Object> p_MovimientosCobros = new ArrayList<>();
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion = "N";
        public String p_UsaReglasNegocio;
        public String p_EsIngresoAutomatico = "N";
        public String p_EsCargaMasiva = "N";
        public boolean p_EsPauta = false;
    }

    public static class Movimiento {
        public String ActualizarCustodia = "N";
        public String ActualizarCuentaInversion = "S";
        public String ActualizarTesoreria = "N";
        public String ActualizarCustodiaRelacionado = "N";
        public BigDecimal Cantidad = BigDecimal.ZERO;
        public String CrearComprobanteAutomatico = "S";
        public BigDecimal DiasPlazo = BigDecimal.ZERO;
        public String EsAPlazo = "N";
        public String EsComprobIndividual = "S";
        public String EsCuentaPropia = "N";
        public String EsEnRueda = "S";
        public String EsFungible = "N";
        public String EsPrecioPrima = "N";
        public String EsRuteo = "N";
        public String EsVentaCorta = "N";
        public String EsClienteFinal = "S";
        public String FechaEmision = "0001-01-01";
        public String FechaLiquidacion;
        public String FechaMovimientoComprobante;
        public String FechaOrden = "0001-01-01";
        public String FechaParidad = "0001-01-01";
        public String FechaTipoCambio = "0001-01-01";
        public String FechaTransaccion = "0001-01-01";
        public String FechaVencimiento = "0001-01-01";
        public String FechaHoraMovimientoComprobante = "0001-01-01 00:00:00";
        public Long FolioCartera = 0L;
        public String FolioTransaccion = "";
        public String Glosa;
        public Long IdAsignacion = 0L;
        public Long IdAsignacionRef = 0L;
        public String IdBolsa;
        public String IdBoveda = "";
        public Long IdCliente;
        public String IdConcepto;
        public String IdCondicionLiquidacion = "T0";
        public Long IdCorredoraContraparte = 0L;
        public Long IdCuentaCustodioPortafolio = 0L;
        public String IdEmisor = "";
        public Long IdEmpleadoDigitador = 0L;
        public Long IdEmpresa;
        public String IdFormaCustodia = "";
        public Long IdInstrumentoRelacionado = 0L;
        public Long IdInstrumentoSubyacente = 0L;
        public Long IdMesa = 0L;
        public String IdMoneda;
        public String IdMonedaEmision = "";
        public String IdMonedaLiquidacion;
        public String IdMonedaSubyacente = "";
        public String IdMonedaSpot = "";
        public String IndicadorOrdenDirecta = "N";
        public Long IdOrden = 0L;
        public Long IdPortafolio;
        public String IdTipoComprobante;
        public String IdTipoOperacion = "COMOTC";
        public String IdTipoProducto = "";
        public String IdTipoReajuste = "CLP";
        public String IdTipoConvenio = "SCV";
        public String IdTipoCanalIngreso = "AGE";
        public String IdTipoCliente = "";
        public String IdTipoLiquidacionComisionFactura = "";
        public Long IdTransaccion = 0L;
        public Long IdUsuario;
        public BigDecimal Intereses = BigDecimal.ZERO;
        public String LiquidaBancoCustodio = "N";
        public BigDecimal Monto;
        public BigDecimal MontoMovimientoComprobante;
        public BigDecimal MontoLiquidacion;
        public BigDecimal MontoDescuento = BigDecimal.ZERO;
        public BigDecimal MontoInteres = BigDecimal.ZERO;
        public BigDecimal MontoAPlazo = BigDecimal.ZERO;
        public Long NumeroMvtoCmbte;
        public String NemoSVS = "";
        public BigDecimal Precio = BigDecimal.ZERO;
        public BigDecimal PrecioAPlazo = BigDecimal.ZERO;
        public BigDecimal ReajustesVAC = BigDecimal.ZERO;
        public String SecuenciaAgrupacion = "";
        public BigDecimal Spread = BigDecimal.ZERO;
        public BigDecimal Tasa = BigDecimal.ZERO;
        public BigDecimal TasaAgente = BigDecimal.ZERO;
        public BigDecimal TasaEmision = BigDecimal.ZERO;
        public BigDecimal ValorRescate = BigDecimal.ZERO;
        public BigDecimal ValorParidad = BigDecimal.ZERO;
        public BigDecimal ValorTipoCambio = BigDecimal.ZERO;
        public BigDecimal ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
        public Long IdOperacion = 0L;
        public String Contrapartida = "N";
    }

    public static class FormaDePago {
        public Long NumeroMvtoCmbte;
        public Long NumeroMvtoCmbteFormaDePago;
        public String IdMedioDePago = "CIN";
        public String NumeroSerie = "";
        public Long NumeroDocumento = 0L;
        public Long IdCuentaBancaria = 0L;
        public String IdTipoCuentaBancaria = "";
        public String NumeroCuentaBancaria = "";
        public Long IdTarjetaBancaria = 0L;
        public String IdTipoTarjetaBancaria = "";
        public String NumeroTarjetaBancaria = "";
        public String FechaVctoTarjetaBancaria = "0001-01-01";
        public String IdModalidadDePago = "MODDEP";
        public Long IdBanco = 0L;
        public String IdMoneda;
        public String FechaDocumentoPago = "0001-01-01";
        public BigDecimal MontoLiquidacionFormaDePago;
        public String IdTipoMovimientoSpot = "";
        public String AfectarTesoreria = "N";
        public String AfectarCuentaInversion = "S";
    }

    public static class SalidaComprobante {
        public List<MovimientoSalida> p_out_Movimientos = new ArrayList<>();
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    public static class MovimientoSalida {
        public Long IdMovimientoComprobante;
        public Long NumeroMvtoCmbte;
    }
}
