package cl.bcs.fondomutuo.certificacion;

import static cl.bcs.fondomutuo.certificacion.CerConstantes.APV;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.BCS;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.CANAL_AGENTE;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.CONCEPTO_COBRO_APV;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.CONCEPTO_COBRO_FM;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.CONCEPTO_COMISION_APV;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.CONCEPTO_COMISION_FM;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.CONDICION_LIQUIDACION_T0;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.COR;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.CUENTA_INVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_RECHAZADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.FORMA_CUSTODIA_CUS;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.NO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.REINVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.RESCATE_COMISION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.RESCATE_FONDO_MUTUO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.RESCATE_TRASPASO_PORTAFOLIO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SI;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_COMPROBANTE_COMISION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_CONVENIO_SCV;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_OPERACION_RESCATE_FONDO_MUTUO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_PRODUCTO_CUSTODIA;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.ComprobanteEntrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.ComprobanteSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.MovimientoCobro;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.MovimientoComprobante;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.MovimientoComprobanteEntrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.MovimientoComprobanteSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.MovimientoFormaDePago;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.MovimientoGenerado;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.MovimientoPorFacturar;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Cobro;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.ListaHash;

/**
 * Front F-G (Facturacion of the commission) of legacy {@code CertificarSolicitudAporteRescate.logica} C:724-759 and {@code creaMovimientoFacturacion} / {@code creaComprobanteFacturacion}
 * C:2159-2341, bug for bug (FM-CER-48..51): the movement of voucher of the rescates with commission (IVA of {@code COM.Cobro}, net = commission / (1 + IVA)) and, with the parameter, the
 * voucher. The counter of the VOUCHER is never read: the one of the MOVEMENT is added twice (D-F3B-04). Does nothing without rescates, writes nothing (the port does, gate G6 is the host's), works
 * on the {@link CertificarAporteRescateContexto} and lets any exception go up untouched. No state.
 */
public final class CertificarAporteRescateFacturacion {

    private final FacturacionPort facturacion;
    private final CertificarAporteRescateDatos datos;

    public CertificarAporteRescateFacturacion(FacturacionPort facturacion, CertificarAporteRescateDatos datos) {
        this.facturacion = facturacion;
        this.datos = datos;
    }

    /** Legacy C:724-759. */
    public void facturar(CertificarAporteRescateContexto ctx) throws Exception {
        if (ctx.rescates.size() == 0) return;
        MovimientoComprobanteSalida salidaMovimiento = crearMovimientos(ctx);
        ctx.salida.P_OUT_ERRORESCONTADOR += salidaMovimiento.P_OUT_ERRORESCONTADOR;
        CertificarAporteRescateCustodia.rechazarPorMensajes(ctx.salida, salidaMovimiento.P_OUT_MENSAJES, ctx.rescates);

        if (SI.equalsIgnoreCase(ctx.comisionComprobanteFacturacionAutomatico)) {
            ComprobanteSalida salidaComprobante = crearComprobantes(ctx, salidaMovimiento);
            // D-F3B-04: the counter added is the one of the MOVEMENT (again), never the one of the voucher
            ctx.salida.P_OUT_ERRORESCONTADOR += salidaMovimiento.P_OUT_ERRORESCONTADOR;
            CertificarAporteRescateCustodia.rechazarPorMensajes(ctx.salida, salidaComprobante.P_OUT_MENSAJES, ctx.rescates);
        }
    }

    /** FM-CER-48..50 (C:2159-2309). */
    private MovimientoComprobanteSalida crearMovimientos(CertificarAporteRescateContexto ctx) {
        MovimientoComprobanteSalida salida = new MovimientoComprobanteSalida();
        salida.P_OUT_ERRORESCONTADOR = 0L;

        // D-F3B-47: the IVA is read for EVERY request with rescates; no row is an IndexOutOfBoundsException, a null value a NullPointerException
        List<Cobro> cobros = datos.cobrosIva();
        BigDecimal iva = new BigDecimal(cobros.get(0).ValorCobro.toString()).divide(BigDecimal.valueOf(100));
        BigDecimal factorIva = iva.add(BigDecimal.ONE);

        MovimientoComprobanteEntrada entrada = new MovimientoComprobanteEntrada();
        for (SolicitudProceso sol : ctx.rescates) {
            if (!seFactura(sol)) continue;
            BigDecimal neto = sol.MontoComisionLocal.divide(factorIva, MathContext.DECIMAL128);
            MovimientoComprobante mov = movimiento(ctx, sol, neto);
            entrada.p_Movimientos.add(mov);

            if (mov.Monto.compareTo(BigDecimal.ZERO) == 1) {
                entrada.p_MovimientosCobros.add(cobro(sol, cobros.get(0), neto.multiply(iva)));
            }
            if (RESCATE_FONDO_MUTUO.equals(sol.IdTipoMovimientoFondoMutuo)) {
                entrada.p_MovimientosFormaDePago.add(formaDePago(sol));
            }
        }
        if (entrada.p_Movimientos.size() > 0) {
            entrada.p_IdEmpresa = ctx.entrada.p_IdEmpresa;
            entrada.p_IdUsuario = ctx.entrada.p_IdUsuario;
            entrada.p_IdModulo = ctx.entrada.p_IdModulo;
            entrada.p_simularOperacion = NO;
            entrada.p_UsaReglasNegocio = SI;
            entrada.p_EsIngresoAutomatico = NO;
            salida = CertificarAporteRescateColaboradores.crearMovimientoComprobante(facturacion, entrada);
        }
        return salida;
    }

    /** Not rejected, no Spot, commission type, commission (after the format of F-D) {@code > 0} and no traspaso (the short circuits are the legacy's). */
    private static boolean seFactura(SolicitudProceso sol) {
        return NO.equalsIgnoreCase(sol.GeneraSpot) && !ESTADO_RECHAZADO.equals(sol.EstadoProceso)
                && ((RESCATE_COMISION.equals(sol.IdTipoMovimientoFondoMutuo) || RESCATE_FONDO_MUTUO.equals(sol.IdTipoMovimientoFondoMutuo)
                        || RESCATE_TRASPASO_PORTAFOLIO.equals(sol.IdTipoMovimientoFondoMutuo))
                        && sol.MontoComisionLocal.compareTo(BigDecimal.ZERO) == 1 && !SI.equals(sol.RelacionTraspaso));
    }

    private MovimientoComprobante movimiento(CertificarAporteRescateContexto ctx, SolicitudProceso sol, BigDecimal neto) {
        boolean esApv = APV.equals(sol.PropositoPortafolio);
        MovimientoComprobante mov = new MovimientoComprobante();
        mov.NumeroMvtoCmbte = sol.IdSolicitudAporteRescate;
        mov.IdEmpresa = ctx.entrada.p_IdEmpresa;
        mov.IdCliente = sol.IdCliente;
        mov.IdPortafolio = sol.IdPortafolio;
        mov.IdInstrumento = sol.IdInstrumento;
        mov.IdInstrumentoRelacionado = 0L;
        mov.IdMoneda = sol.IdMoneda;
        mov.IdMonedaLiquidacion = sol.IdMoneda;
        mov.IdBoveda = COR;
        mov.IdBolsa = BCS;
        mov.IdEmisor = sol.IdEmisor;
        mov.Cantidad = BigDecimal.ONE;
        mov.Precio = neto;
        mov.FechaLiquidacion = FechaLegacy.texto(sol.FechaLiquidacion);
        String certificacion = FechaLegacy.texto(sol.FechaCertificacion);
        mov.FechaOrden = certificacion;
        mov.FechaTransaccion = certificacion;
        mov.FechaMovimientoComprobante = certificacion;
        mov.FechaEmision = certificacion;
        mov.IdUsuario = ctx.entrada.p_IdUsuario;
        mov.IdTipoProducto = TIPO_PRODUCTO_CUSTODIA;

        if (RESCATE_FONDO_MUTUO.equals(sol.IdTipoMovimientoFondoMutuo) || RESCATE_TRASPASO_PORTAFOLIO.equals(sol.IdTipoMovimientoFondoMutuo)) {
            mov.IdTipoComprobante = TIPO_COMPROBANTE_COMISION;
            mov.IdConcepto = esApv ? CONCEPTO_COMISION_APV : CONCEPTO_COMISION_FM;
            mov.ActualizarCuentaInversion = SI;
            mov.Monto = neto;
            mov.MontoMovimientoComprobante = neto;
            mov.MontoLiquidacion = neto;
        } else {
            mov.IdTipoComprobante = esApv ? ctx.tipoComprobanteFacturaApv : ctx.tipoComprobanteFacturaNoApv;
            mov.IdConcepto = esApv ? CONCEPTO_COBRO_APV : CONCEPTO_COBRO_FM;
            mov.ActualizarCuentaInversion = NO;
            mov.Monto = sol.CostoMonedaLocal;
            mov.MontoMovimientoComprobante = sol.CostoMonedaLocal;
            mov.MontoLiquidacion = sol.CostoMonedaLocal;
        }
        mov.IdTipoOperacion = TIPO_OPERACION_RESCATE_FONDO_MUTUO;
        mov.IdCondicionLiquidacion = CONDICION_LIQUIDACION_T0;
        mov.IdTipoReajuste = "";
        mov.IdTipoConvenio = TIPO_CONVENIO_SCV;
        mov.IdFormaCustodia = FORMA_CUSTODIA_CUS;
        mov.IdTipoCanalIngreso = CANAL_AGENTE;
        mov.EsComprobIndividual = NO;
        mov.EsCuentaPropia = NO;
        mov.EsVentaCorta = NO;
        mov.EsPrecioPrima = NO;
        mov.EsEnRueda = NO;
        mov.EsRuteo = NO;
        mov.EsAPlazo = NO;
        mov.EsFungible = NO;
        mov.ActualizarCustodia = NO;
        mov.ActualizarTesoreria = NO;
        mov.ActualizarCustodiaRelacionado = NO;
        mov.CrearComprobanteAutomatico = NO;
        mov.SecuenciaAgrupacion = "";
        mov.ModPrefFact = "MODPREFNORM";
        mov.IdEmpleadoDigitador = 0L;
        mov.IdInstrumentoSubyacente = 0L;
        mov.IdMonedaSubyacente = "";
        mov.IdCorredoraContraparte = 0L;
        mov.IdOrden = 0L;
        mov.IdAsignacion = 0L;
        mov.IdAsignacionRef = 0L;
        mov.IdTransaccion = 0L;
        mov.FolioTransaccion = "";
        mov.FechaVencimiento = "0001-01-01";
        mov.PrecioAPlazo = BigDecimal.ZERO;
        mov.MontoAPlazo = BigDecimal.ZERO;
        mov.FechaParidad = "0001-01-01";
        mov.ValorParidad = BigDecimal.ZERO;
        mov.FechaTipoCambio = "0001-01-01";
        mov.ValorTipoCambio = BigDecimal.ZERO;
        mov.ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
        mov.Tasa = BigDecimal.ZERO;
        mov.MontoDescuento = BigDecimal.ZERO;
        mov.MontoInteres = BigDecimal.ZERO;
        mov.Glosa = "";
        mov.ValorRescate = BigDecimal.ZERO;
        mov.TasaAgente = BigDecimal.ZERO;
        mov.Spread = BigDecimal.ZERO;
        mov.DiasPlazo = BigDecimal.ZERO;
        mov.FolioCartera = 0L;
        mov.TasaEmision = BigDecimal.ZERO;
        return mov;
    }

    /** The IVA charge of the movement ({@code MontoCobro = neto x IVA}); only built when the amount of the movement is positive. */
    private static MovimientoCobro cobro(SolicitudProceso sol, Cobro iva, BigDecimal montoIva) {
        MovimientoCobro cobro = new MovimientoCobro();
        cobro.NumeroMvtoCmbte = sol.IdSolicitudAporteRescate;
        cobro.NumeroMvtoCmbteCobro = sol.IdSolicitudAporteRescate;
        cobro.IdCobro = iva.IdCobro;
        cobro.IdMonedaCobro = sol.IdMoneda;
        cobro.IdMonedaLiquidacionCobro = sol.IdMoneda;
        cobro.MontoCobro = montoIva;
        cobro.MontoMovimientoComprobanteCobro = montoIva;
        cobro.MontoLiquidacionCobro = montoIva;
        cobro.Porcentaje = new BigDecimal(iva.ValorCobro.toString());
        return cobro;
    }

    /** Only for {@code RESNORM}: the WHOLE commission (net plus IVA) charged to the cuenta de inversion. */
    private static MovimientoFormaDePago formaDePago(SolicitudProceso sol) {
        MovimientoFormaDePago forma = new MovimientoFormaDePago();
        forma.AfectarCuentaInversion = SI;
        forma.AfectarTesoreria = NO;
        forma.IdMedioDePago = CUENTA_INVERSION;
        forma.IdMoneda = sol.IdMoneda;
        forma.MontoLiquidacionFormaDePago = sol.MontoComisionLocal;
        forma.NumeroMvtoCmbteFormaDePago = sol.IdSolicitudAporteRescate;
        forma.NumeroMvtoCmbte = sol.IdSolicitudAporteRescate;
        forma.FechaVctoTarjetaBancaria = "1900-01-01";
        forma.FechaDocumentoPago = FechaLegacy.texto(sol.FechaCertificacion);
        return forma;
    }

    /** FM-CER-51 (C:2311-2341): the voucher of every generated movement whose rescate (looked up by id: a missing one raises) is not rejected, is {@code RESNORM} or {@code RESREINV}, has commission and no traspaso. */
    private ComprobanteSalida crearComprobantes(CertificarAporteRescateContexto ctx, MovimientoComprobanteSalida movimientos) throws Exception {
        ComprobanteSalida salida = new ComprobanteSalida();
        ComprobanteEntrada entrada = new ComprobanteEntrada();
        Map<Long, SolicitudProceso> rescatesPorId = ListaHash.aHash(ctx.rescates, r -> r.IdSolicitudAporteRescate);
        for (MovimientoGenerado mov : movimientos.p_out_Movimientos) {
            SolicitudProceso res = rescatesPorId.get(mov.NumeroMvtoCmbte);
            if (!ESTADO_RECHAZADO.equals(res.EstadoProceso)
                    && ((RESCATE_FONDO_MUTUO.equals(res.IdTipoMovimientoFondoMutuo) || REINVERSION.equals(res.IdTipoMovimientoFondoMutuo))
                            && res.MontoComisionLocal.compareTo(BigDecimal.ZERO) == 1 && !SI.equals(res.RelacionTraspaso))) {
                MovimientoPorFacturar porFacturar = new MovimientoPorFacturar();
                porFacturar.IdMovimientoComprobante = mov.IdMovimientoComprobante;
                porFacturar.FechaComprobante = ctx.fechaSistema;
                entrada.P_MOVIMIENTOFACTURAR.add(porFacturar);
            }
        }
        if (entrada.P_MOVIMIENTOFACTURAR.size() > 0) {
            entrada.p_IdEmpresa = ctx.entrada.p_IdEmpresa;
            entrada.p_IdUsuario = ctx.entrada.p_IdUsuario;
            entrada.p_IdModulo = ctx.entrada.p_IdModulo;
            entrada.p_simularOperacion = NO;
            entrada.p_UsaReglasNegocio = SI;
            entrada.p_EsIngresoAutomatico = NO;
            salida = CertificarAporteRescateColaboradores.crearComprobante(facturacion, entrada);
        }
        return salida;
    }
}
