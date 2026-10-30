package cl.bcs.fondomutuo.certificacion;

import static cl.bcs.fondomutuo.certificacion.CerConstantes.APORTE_REINVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.CUENTA_INVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.DOLAR;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_RECHAZADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.MEDIO_PAGO_TRANSFERENCIA;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.NO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.RESCATE_COMISION_NN;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SI;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SPOT_CALCULA_MARGEN;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SPOT_COMPRA;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SPOT_ESTADO_RECHAZADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SPOT_MOVIMIENTO_EGRESO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SPOT_MOVIMIENTO_INGRESO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SPOT_VALUTA_DEFECTO_FORMA_DE_PAGO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SPOT_VENTA;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_COMISION_TRIMESTRAL;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_SOLICITUD_APORTE;

import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.fondomutuo.normal.ProcesosExternos.EntradaSpot;
import cl.bcs.fondomutuo.normal.ProcesosExternos.FormaPagoSpot;
import cl.bcs.fondomutuo.normal.ProcesosExternos.OperacionSpot;
import cl.bcs.fondomutuo.normal.ProcesosExternos.SalidaSpot;

/**
 * Front F-H (Spot) of legacy {@code CertificarSolicitudAporteRescate.logica}, bug for bug (FM-CER-59, 60): the Spot operation of the rescates that generate it, with its two payment forms,
 * and the reading of the answer (D-F3B-14, D-F3B-42). The branch of the aporte of reinversion in dollars (sale) is ported as it is although a list of rescates never reaches it (Q-T3-12).
 * The entry carries {@code p_PermiteOperarProspectos = "N"}, the default of the legacy class (see {@link EntradaSpot}).
 */
public final class CertificarAporteRescateSpot {

    private final ProcesosExternos.SpotPort spot;

    public CertificarAporteRescateSpot(ProcesosExternos.SpotPort spot) {
        this.spot = spot;
    }

    /** Legacy C:790-830. */
    public void moverSpot(CertificarAporteRescateContexto ctx) throws Exception {
        if (ctx.rescates.size() == 0) return;
        List<SolicitudProceso> rescates = ctx.rescates;
        SalidaSpot salidaSpot = mueveSpot(ctx);
        if (salidaSpot.P_OUT_ERRORESCONTADOR != null) {
            ctx.salida.P_OUT_ERRORESCONTADOR += salidaSpot.P_OUT_ERRORESCONTADOR;
        }
        for (OperacionSpot rs : salidaSpot.P_OUT_IDOPERACIONSPOT) {
            for (SolicitudProceso sol : rescates) {
                if (sol.IdSolicitudAporteRescate.equals(Long.parseLong(rs.IdInterno))) {
                    // D-F3B-42: a solicitud already rejected is not skipped
                    if (SPOT_ESTADO_RECHAZADO.equals(rs.Estado)) {
                        sol.EstadoProceso = ESTADO_RECHAZADO;
                        sol.Codigo = "ERROR_CREAR_SPOT";
                        break;
                    } else {
                        sol.IdOperacionSpot = rs.IdOperacionSpot;
                        break;
                    }
                }
            }
        }
        for (MensajeSalida rs : salidaSpot.P_OUT_MENSAJES) {
            if (MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(rs.tipo)) {
                for (SolicitudProceso sol : rescates) {
                    // D-F3B-14: an id 0 rejects the first rescate of the list
                    if (sol.IdSolicitudAporteRescate.equals(Math.abs(rs.id)) || rs.id.equals(0L)) {
                        sol.EstadoProceso = ESTADO_RECHAZADO;
                        sol.Codigo = rs.codigo;
                        sol.DescripcionCodigo = rs.descripcion;
                        break;
                    }
                }
            }
        }
    }

    private SalidaSpot mueveSpot(CertificarAporteRescateContexto ctx) throws Exception {
        SalidaSpot salida = new SalidaSpot();
        EntradaSpot entrada = new EntradaSpot();
        for (SolicitudProceso sol : ctx.rescates) {
            if (!ESTADO_RECHAZADO.equals(sol.EstadoProceso) && SI.equals(sol.GeneraSpot)) {
                SolicitudAporteRescate s = CertificarAporteRescateTesoreria.pojo(sol);
                s.IdInterno = sol.IdSolicitudAporteRescate;
                entrada.p_DatosOperacion.add(s);
            }
        }
        if (entrada.p_DatosOperacion.size() > 0) {
            entrada.p_IdEmpresa = ctx.entrada.p_IdEmpresa;
            entrada.p_IdUsuario = ctx.entrada.p_IdUsuario;
            entrada.p_IdModulo = ctx.entrada.p_IdModulo;
            entrada.p_CalculaMargen = SPOT_CALCULA_MARGEN;
            entrada.p_UsaReglasNegocio = ctx.entrada.p_UsaReglasNegocio;
            for (SolicitudProceso sol : ctx.rescates) {
                if (!ESTADO_RECHAZADO.equals(sol.EstadoProceso) && SI.equals(sol.GeneraSpot)) {
                    String idTipoOperacionSpot = "";
                    if (APORTE_REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo)) {
                        idTipoOperacionSpot = sol.IdMoneda.equals(DOLAR) ? SPOT_VENTA : SPOT_COMPRA;
                    } else {
                        idTipoOperacionSpot = TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud) ? SPOT_VENTA : SPOT_COMPRA;
                    }

                    FormaPagoSpot pagRec = new FormaPagoSpot();
                    pagRec.IdInternoPagoOperacion = 1L;
                    pagRec.IdComprobantePagoOperacion = 0L;
                    pagRec.IdOperacion = sol.IdSolicitudAporteRescate;
                    pagRec.RecibePagaPortafolio = "R";
                    if (SPOT_COMPRA.equals(idTipoOperacionSpot)) {
                        pagRec.IdTipoMovimiento = SPOT_MOVIMIENTO_EGRESO;
                        pagRec.IdMoneda = sol.IdMoneda;
                        pagRec.IdMonedaLiquidacion = sol.IdMoneda;
                        pagRec.MontoFormaDePago = sol.Monto;
                        pagRec.AfectarTesoreria = RESCATE_COMISION_NN.equals(sol.IdTipoMovimientoFondoMutuo) ? SI : NO;
                        if (TIPO_COMISION_TRIMESTRAL.equalsIgnoreCase(sol.TipoComision)) {
                            pagRec.IdFormaDePago = SPOT_COMPRA + "_" + MEDIO_PAGO_TRANSFERENCIA + "_" + sol.IdMoneda + "_" + SPOT_VALUTA_DEFECTO_FORMA_DE_PAGO;
                        } else {
                            pagRec.IdFormaDePago = SPOT_COMPRA + "_" + CUENTA_INVERSION + "_" + sol.IdMoneda + "_" + SPOT_VALUTA_DEFECTO_FORMA_DE_PAGO;
                        }
                    } else {
                        String idMedioDePago = "".equals(sol.IdMedioDePago) || sol.IdMedioDePago == null ? CUENTA_INVERSION : sol.IdMedioDePago;
                        pagRec.IdTipoMovimiento = SPOT_MOVIMIENTO_INGRESO;
                        pagRec.IdFormaDePago = SPOT_COMPRA + "_" + idMedioDePago + "_" + sol.IdMonedaSistema + "_" + SPOT_VALUTA_DEFECTO_FORMA_DE_PAGO;
                        pagRec.IdMoneda = sol.IdMonedaSistema;
                        pagRec.IdMonedaLiquidacion = sol.IdMonedaSistema;
                        pagRec.MontoFormaDePago = sol.MontoMonedaSistema;
                        pagRec.AfectarTesoreria = CUENTA_INVERSION.equals(idMedioDePago) ? NO : SI;
                    }
                    if (TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud)) {
                        pagRec.FechaLiquidacion = sol.FechaAceptacion;
                    } else {
                        pagRec.FechaLiquidacion = sol.FechaLiquidacion;
                    }
                    entrada.p_DatosFormaPago.add(pagRec);

                    FormaPagoSpot pagEnt = new FormaPagoSpot();
                    pagEnt.IdInternoPagoOperacion = 1L;
                    pagEnt.IdComprobantePagoOperacion = 0L;
                    pagEnt.IdOperacion = sol.IdSolicitudAporteRescate;
                    pagEnt.RecibePagaPortafolio = "P";
                    if (SPOT_COMPRA.equals(idTipoOperacionSpot)) {
                        String idMedioDePago = "".equals(sol.IdMedioDePago) || sol.IdMedioDePago == null ? CUENTA_INVERSION : sol.IdMedioDePago;
                        pagEnt.IdTipoMovimiento = SPOT_MOVIMIENTO_INGRESO;
                        pagEnt.IdFormaDePago = SPOT_VENTA + "_" + idMedioDePago + "_" + sol.IdMonedaSistema + "_" + SPOT_VALUTA_DEFECTO_FORMA_DE_PAGO;
                        pagEnt.IdMoneda = sol.IdMonedaSistema;
                        pagEnt.IdMonedaLiquidacion = sol.IdMonedaSistema;
                        pagEnt.MontoFormaDePago = sol.MontoMonedaSistema;
                        pagEnt.AfectarTesoreria = CUENTA_INVERSION.equals(idMedioDePago) ? NO : SI;
                    } else {
                        pagEnt.IdTipoMovimiento = SPOT_MOVIMIENTO_INGRESO;
                        pagEnt.IdFormaDePago = SPOT_VENTA + "_" + CUENTA_INVERSION + "_" + sol.IdMoneda + "_" + SPOT_VALUTA_DEFECTO_FORMA_DE_PAGO;
                        pagEnt.IdMoneda = sol.IdMoneda;
                        pagEnt.IdMonedaLiquidacion = sol.IdMoneda;
                        pagEnt.MontoFormaDePago = sol.Monto;
                        pagEnt.AfectarTesoreria = NO;
                    }
                    if (TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud)) {
                        pagEnt.FechaLiquidacion = sol.FechaAceptacion;
                    } else {
                        pagEnt.FechaLiquidacion = sol.FechaLiquidacion;
                    }
                    entrada.p_DatosFormaPago.add(pagEnt);
                }
            }
            salida = spot.procesar(entrada);
        }
        return salida;
    }
}
