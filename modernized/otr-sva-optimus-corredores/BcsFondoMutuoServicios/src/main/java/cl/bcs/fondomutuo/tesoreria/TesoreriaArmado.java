package cl.bcs.fondomutuo.tesoreria;

import java.util.Map;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.tesoreria.TesoreriaDatos.ModalidadPago;
import cl.bcs.fondomutuo.tesoreria.TesoreriaDatos.PreferenciaPagoCorredor;
import cl.bcs.fondomutuo.tesoreria.TesoreriaDatos.PreferenciaPagoPortafolio;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.FormaDePago_Entrada;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService.IngresoEgreso_Entrada;

/**
 * The body of the loop of legacy {@code ProcesarIngresoEgresoTesoreria.logica} ({@code :195-309}): one Tesoreria movement and one payment form per request,
 * statement by statement and in the same order (FM-TES-06..11, FM-NOR-90/91). Defects kept bug for bug: D-TES-1 ({@code FechaLiquidacion} = issue date;
 * {@code TitularPago} compares the person with itself, always "N"; the titular portfolio assigned twice), D-TES-2 (a reinvestment contribution leaves with
 * concept {@code APORFM}: the concept depends only on the type of request), and the NullPointerExceptions of FM-TES-13 ({@code IdConcepto},
 * {@code IdPersona}, {@code FechaAceptacion}, {@code FechaEmision} null). No state.
 */
final class TesoreriaArmado {

    static final String SI = "S";
    static final String NO = "N";
    static final String APORTE = "APO";
    static final String RESCATE = "RES";
    static final String RESCATE_TRASPASO = "RESTRASP";
    static final String CONCEPTO_APORTE_DOLAR = "APODOL";
    static final String CONCEPTO_RESCATE_DOLAR = "RESDOL";
    static final String CONCEPTO_APORTE = "APORFM";
    static final String CONCEPTO_RESCATE_TRASPASO = "RESTRS";
    static final String CONCEPTO_RESCATE = "RESCFM";
    static final String INGRESO = "MOVING";
    static final String EGRESO = "MOVEGR";
    static final String PERSONA_CONTRAPARTE = "CNT";
    static final String PERSONA_CLIENTE = "CLI";
    static final String TIPO_PRODUCTO = "INFM";
    static final String OPERACION_APORTE = "APORFM";
    static final String OPERACION_RESCATE = "RESCFM";
    static final String TIPO_COMPROBANTE = "FMM";
    static final String T0 = "T0";
    static final String BOLSA = "BCS";
    static final String CUENTA_INVERSION = "CIN";
    static final String TRANSFERENCIA = "TRA";
    static final String MODCAJ = "MODCAJ";
    static final String FECHA_VACIA = "0001-01-01";

    private TesoreriaArmado() {
    }

    /** {@code :197-258}: the movement ({@code IngresoEgreso_Entrada}). */
    static IngresoEgreso_Entrada ingreso(SolicitudAporteRescate sol, Long idEmpleadoDigitador) {
        IngresoEgreso_Entrada ie = new IngresoEgreso_Entrada();
        String idConcepto;
        if (SI.equalsIgnoreCase(sol.GeneraSpot)) {
            idConcepto = APORTE.equalsIgnoreCase(sol.TipoSolicitud) ? CONCEPTO_APORTE_DOLAR : RESCATE.equalsIgnoreCase(sol.TipoSolicitud) ? CONCEPTO_RESCATE_DOLAR : "";
        } else {
            idConcepto = APORTE.equalsIgnoreCase(sol.TipoSolicitud) ? CONCEPTO_APORTE
                    : (RESCATE.equalsIgnoreCase(sol.TipoSolicitud) ? (RESCATE_TRASPASO.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) ? CONCEPTO_RESCATE_TRASPASO : CONCEPTO_RESCATE) : "");
        }
        ie.NumeroReferencia = sol.IdInterno;
        ie.IdTipoMovimiento = APORTE.equalsIgnoreCase(sol.TipoSolicitud) ? INGRESO : RESCATE.equalsIgnoreCase(sol.TipoSolicitud) ? EGRESO : "";
        ie.IdMoneda = SI.equalsIgnoreCase(sol.GeneraSpot) ? sol.IdMonedaSistema : sol.IdMoneda;
        ie.Monto = SI.equalsIgnoreCase(sol.GeneraSpot) ? sol.MontoMonedaSistema : sol.MontoPagoLocal;
        ie.FechaLiquidacion = FechaLegacy.aTexto(sol.FechaEmision != null ? sol.FechaEmision.toLocalDate() : null);   // Fecha.toStringDate(java.sql.Date)
        ie.IdConcepto = (!sol.IdConcepto.equalsIgnoreCase("")) ? sol.IdConcepto : idConcepto;
        if (SI.equals(sol.RelacionTraspaso)) {
            ie.IdTipoPersona = PERSONA_CONTRAPARTE;
            ie.IdPortafolio = 0L;
            ie.IdPersona = sol.IdPersonaTraspaso;
        } else {
            ie.IdTipoPersona = PERSONA_CLIENTE;
            ie.IdPortafolio = sol.IdPortafolio;
            ie.IdPersona = sol.IdPersona;
        }
        ie.IdPortafolioTitularPago = sol.IdPortafolio;
        ie.IdTipoPersonaTitularPago = PERSONA_CLIENTE;
        ie.IdPersonaTitularPago = sol.IdPersona;
        ie.IdPortafolioTitularPago = sol.IdPortafolio;
        ie.IdCaja = 0L;
        ie.SecuenciaAgrupacion = "";
        ie.SecuenciaRelacionada = (sol.SecuenciaRelacionada != null) ? sol.SecuenciaRelacionada : 0L;
        ie.Glosa = "";
        ie.IdEmpleadoDigitador = idEmpleadoDigitador;
        ie.IdSucursalEmpleadoDigitador = 0L;
        ie.IdTipoProducto = TIPO_PRODUCTO;
        ie.IdTipoOperacion = APORTE.equalsIgnoreCase(sol.TipoSolicitud) ? OPERACION_APORTE : RESCATE.equalsIgnoreCase(sol.TipoSolicitud) ? OPERACION_RESCATE : "";
        ie.IdTipoComprobante = TIPO_COMPROBANTE;
        ie.IdComprobante = sol.IdSolicitudAporteRescate;
        ie.FechaComprobante = FechaLegacy.texto(sol.FechaAceptacion);   // Fecha.stringToSqlDate(java.util.Date): NPE when null
        ie.FechaLiquidacionComprobante = FECHA_VACIA;
        ie.IdOperacion = sol.IdSolicitudAporteRescate;
        ie.FechaOperacion = FechaLegacy.texto(sol.FechaEmision);
        ie.IdCondicionLiquidacion = T0;
        ie.IdBolsa = BOLSA;
        ie.IdMonedaOperacion = sol.IdMoneda;
        ie.MontoOperacion = sol.MontoPagoLocal;
        ie.IdTipoReajuste = "";
        ie.FechaParidad = FechaLegacy.texto(sol.FechaEmision);
        ie.ValorParidad = sol.ParidadFechaInversion;
        ie.FolioCartera = 0L;
        ie.IdInstrumento = sol.IdInstrumento;
        ie.IdTipoInstrumento = sol.IdTipoInstrumento;
        ie.IdClasificacionTipoInstrumento = "";
        ie.Bilateral = NO;
        ie.GeneraComprobante = SI;
        ie.AgruparEnComprobante = NO;
        ie.MueveCuentaInversion = SI;
        ie.TitularPago = sol.IdPersona.equals(ie.IdPersonaTitularPago) ? NO : SI;   // D-TES-1: always "N" (NPE when IdPersona is null)
        return ie;
    }

    /** {@code :260-306}: the payment form ({@code FormaDePago_Entrada}). */
    static FormaDePago_Entrada formaDePago(SolicitudAporteRescate sol, Map<String, ModalidadPago> hashModalidadPago,
            Map<String, PreferenciaPagoCorredor> hashPreferenciaPagoCorredor, PreferenciaPagoPortafolio infoPreferenciaPagoPortafolio) {
        FormaDePago_Entrada fp = new FormaDePago_Entrada();
        fp.NumeroReferencia = sol.IdInterno;
        fp.NumeroReferenciaFormaDePago = sol.IdInterno;
        fp.IdMedioDePago = SI.equalsIgnoreCase(sol.GeneraSpot) ? CUENTA_INVERSION.equalsIgnoreCase(sol.IdMedioDePago) ? TRANSFERENCIA : sol.IdMedioDePago : sol.IdMedioDePago;
        fp.IdModalidadDePago = hashModalidadPago.get(fp.IdMedioDePago) != null ? hashModalidadPago.get(fp.IdMedioDePago).IdModalidadDePago : MODCAJ;
        fp.IdMoneda = SI.equalsIgnoreCase(sol.GeneraSpot) ? sol.IdMonedaSistema : sol.IdMoneda;
        fp.Monto = SI.equalsIgnoreCase(sol.GeneraSpot) ? sol.MontoMonedaSistema : sol.MontoPagoLocal;
        if (sol.SecuenciaRelacionada != null) {
            // the legacy map is declared HashMap<Long, Object> but List2Hash keys it by IdMoneda (a String): get(Object) with the currency text
            PreferenciaPagoCorredor infoPreferenciaPagoCorredor = hashPreferenciaPagoCorredor.get(SI.equalsIgnoreCase(sol.GeneraSpot) ? sol.IdMonedaSistema : sol.IdMoneda);
            fp.IdCuentaBancariaCorredora = infoPreferenciaPagoCorredor == null || infoPreferenciaPagoCorredor.IdCuentaBanco == null ? 0L
                    : infoPreferenciaPagoCorredor.IdCuentaBanco;
        } else {
            fp.IdCuentaBancariaCorredora = 0L;
        }
        fp.IdTarjetaBancariaCorredora = 0L;
        fp.IdChequera = 0L;
        fp.NumeroDocumentoCorredora = 0L;
        fp.FechaDocumentoPagoCorredora = FECHA_VACIA;
        socio(sol, fp, infoPreferenciaPagoPortafolio);
        fp.IdTarjetaBancariaSocio = 0L;
        fp.IdTipoTarjetaBancariaSocio = "";
        fp.NombreTipoTarjetaBancariaSocio = "";
        fp.NumeroTarjetaBancariaSocio = "";
        fp.NumeroSerieSocio = "";
        fp.NumeroDocumentoSocio = 0L;
        fp.FechaDocumentoPagoSocio = FECHA_VACIA;
        return fp;
    }

    /** {@code :284-298}: the account of the partner, from the payment preference of the portfolio or (traspaso) from the request. */
    private static void socio(SolicitudAporteRescate sol, FormaDePago_Entrada fp, PreferenciaPagoPortafolio info) {
        if (!SI.equals(sol.RelacionTraspaso)) {
            fp.IdCuentaBancariaSocio = info != null ? info.IdCuentaBancaria : 0L;
            fp.IdBancoSocio = info != null ? info.IdBancoCuentaBancaria : 0L;
            fp.NombreCuentaBancariaSocio = "";
            fp.NumeroCuentaBancariaSocio = info != null ? info.NumeroCuentaBancaria : "";
            fp.IdTipoCuentaBancariaSocio = info != null ? info.IdTipoCuentaBancaria : "";
            fp.NombreTipoCuentaBancariaSocio = info != null ? info.NombreTipoCuentaBancaria : "";
        } else {
            fp.IdCuentaBancariaSocio = sol.IdCuentaBancaria;
            fp.IdBancoSocio = sol.IdBanco;
            fp.NombreCuentaBancariaSocio = "";
            fp.NumeroCuentaBancariaSocio = sol.NumeroCuentaBancaria;
            fp.IdTipoCuentaBancariaSocio = sol.IdTipoCuentaBancaria;
            fp.NombreTipoCuentaBancariaSocio = sol.NombreTipoCuentaBancaria;
        }
    }
}
