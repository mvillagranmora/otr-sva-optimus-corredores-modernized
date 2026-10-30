package cl.bcs.fondomutuo.certificacion;

import static cl.bcs.fondomutuo.certificacion.CerConstantes.APV_AN;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.APV_B;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.BONIFICACION_FISCAL;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.CUENTA_INVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_RECHAZADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_RECHAZADO_TESORERIA;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.MEDIO_PAGO_TRANSFERENCIA;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.NO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.NUMERO_REFERENCIA_TESORERIA;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.REINVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SI;

import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Persona;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Traspaso;
import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.fondomutuo.normal.ProcesosExternos.EntradaTesoreria;
import cl.bcs.fondomutuo.normal.ProcesosExternos.IngresoEgresoGenerado;
import cl.bcs.fondomutuo.normal.ProcesosExternos.ResultadoProceso;
import cl.bcs.fondomutuo.normal.ProcesosExternos.SalidaTesoreria;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

/**
 * Front F-F (Tesoreria) of legacy {@code CertificarSolicitudAporteRescate.logica}, bug for bug (FM-CER-44..47): the entries of Tesoreria for the rescates (path a, path b of the traspaso, the payment
 * of the retention to the SII, the TGR of the fiscal bonus) and the reading of its answer. The counter and the messages of Tesoreria are NEVER read (D-F3B-03).
 */
public final class CertificarAporteRescateTesoreria {

    private final ProcesosExternos.TesoreriaPort tesoreria;
    private final ConfiguracionSistemaPort configuracion;

    public CertificarAporteRescateTesoreria(ProcesosExternos.TesoreriaPort tesoreria, ConfiguracionSistemaPort configuracion) {
        this.tesoreria = tesoreria;
        this.configuracion = configuracion;
    }

    /** Legacy C:698-721. */
    public void moverTesoreria(CertificarAporteRescateContexto ctx) throws Exception {
        if (ctx.rescates.size() == 0) return;
        List<SolicitudProceso> rescates = ctx.rescates;
        SalidaTesoreria salida = mueveTesoreria(ctx);
        for (ResultadoProceso rs : salida.P_OUT_RESULTADOPROCESO) {
            if (ESTADO_RECHAZADO_TESORERIA.equals(rs.Resultado)) {
                Long id = Math.abs(rs.Id);
                for (SolicitudProceso sol : rescates) {
                    if (id.longValue() == sol.IdSolicitudAporteRescate.longValue() && rs.Campo.equals(NUMERO_REFERENCIA_TESORERIA)) {
                        sol.EstadoProceso = ESTADO_RECHAZADO;
                        break;
                    }
                }
            }
        }
        for (IngresoEgresoGenerado rs : salida.P_OUT_INGRESOEGRESOGENERADO) {
            for (SolicitudProceso sol : rescates) {
                if (rs.NumeroReferencia.longValue() == sol.IdSolicitudAporteRescate.longValue()) {
                    sol.IdIngresoEgreso = rs.IdIngresoEgreso;
                    break;
                }
            }
        }
    }

    private SalidaTesoreria mueveTesoreria(CertificarAporteRescateContexto ctx) throws Exception {
        SalidaTesoreria salida = new SalidaTesoreria();
        EntradaTesoreria entrada = new EntradaTesoreria();
        java.sql.Date fechaSistema = FechaLegacy.aFechaSql(ctx.fechaSistema);
        for (SolicitudProceso sol : ctx.rescates) {
            if (!ESTADO_RECHAZADO.equals(sol.EstadoProceso) && !SI.equals(sol.GeneraSpot)) {
                if (!CUENTA_INVERSION.equals(sol.IdMedioDePago) && !REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo) && NO.equals(sol.IndicadorMLT) && !SI.equals(sol.RelacionTraspaso)) {
                    SolicitudAporteRescate s = pojo(sol);
                    s.IdInterno = sol.IdSolicitudAporteRescate;
                    s.FechaEmision = fechaEmision(fechaSistema, sol);
                    s.ParidadFechaInversion = sol.ParidadFechaCertificacion;
                    s.RelacionTraspaso = NO;
                    if (SI.equals(sol.TieneMontoRetencion)) s.MontoPagoLocal = s.MontoPagoLocal.subtract(sol.MontoRetencion);

                    if (BONIFICACION_FISCAL.equals(sol.IdRegimenTributarioAPV)) {
                        s.ParidadFechaInversion = sol.ParidadFechaCertificacion;
                        persona(s, ctx.personaTGR);
                        s.RelacionTraspaso = SI;
                        s.IdMedioDePago = MEDIO_PAGO_TRANSFERENCIA;
                    }
                    entrada.p_Solicitudes.add(s);

                    if (APV_B.equals(sol.IdRegimenTributarioAPV) || APV_AN.equals(sol.IdRegimenTributarioAPV)) {
                        String idConceptoRetencionPagoSII = configuracion.texto(ctx.entrada.p_IdEmpresa, "FONDOMUTUO", "CONCEPTO_RETENCION_PAGO_SII");
                        SolicitudAporteRescate so = pojo(sol);
                        so.IdInterno = sol.IdSolicitudAporteRescate * -1;
                        so.FechaEmision = fechaEmision(fechaSistema, sol);
                        so.ParidadFechaInversion = sol.ParidadFechaCertificacion;
                        so.MontoPagoLocal = sol.MontoRetencion;
                        persona(so, ctx.personaSII);
                        so.RelacionTraspaso = SI;
                        so.IdMedioDePago = MEDIO_PAGO_TRANSFERENCIA;
                        so.IdConcepto = idConceptoRetencionPagoSII;
                        entrada.p_Solicitudes.add(so);
                    }
                } else if (SI.equals(sol.RelacionTraspaso)) {
                    Traspaso traspaso = traspaso(sol, ctx);
                    if (traspaso != null) {
                        SolicitudAporteRescate s = pojo(sol);
                        s.IdInterno = sol.IdSolicitudAporteRescate;
                        s.FechaEmision = fechaEmision(fechaSistema, sol);
                        s.ParidadFechaInversion = sol.ParidadFechaCertificacion;
                        s.RelacionTraspaso = SI;
                        s.IdPersonaTraspaso = traspaso.IdPersona;
                        s.IdCuentaBancaria = traspaso.IdCuentaBancaria;
                        s.IdBanco = traspaso.IdBanco;
                        s.IdTipoCuentaBancaria = traspaso.IdTipoCuentaBancaria;
                        s.NumeroCuentaBancaria = traspaso.NumeroCuentaBancaria;
                        s.NombreTipoCuentaBancaria = traspaso.NombreTipoCuentaBancaria;
                        s.IdMedioDePago = MEDIO_PAGO_TRANSFERENCIA;
                        entrada.p_Solicitudes.add(s);
                    }
                }
            }
        }
        if (entrada.p_Solicitudes.size() > 0) {
            entrada.p_IdEmpresa = ctx.entrada.p_IdEmpresa;
            entrada.p_IdUsuario = ctx.entrada.p_IdUsuario;
            entrada.p_IdModulo = ctx.entrada.p_IdModulo;
            entrada.p_SimularOperacion = NO;
            entrada.p_UsaReglasNegocio = ctx.entrada.p_UsaReglasNegocio;
            entrada.p_EsIngresoAutomatico = NO;
            salida = tesoreria.procesar(entrada);
        }
        return salida;
    }

    /** D-F3B-07: {@code compareTo(...) > 1} is never true (compareTo gives -1 / 0 / 1): the emission date is always the liquidation date. */
    private static java.sql.Date fechaEmision(java.sql.Date fechaSistema, SolicitudProceso sol) {
        return (fechaSistema.compareTo(sol.FechaLiquidacion) > 1) ? fechaSistema : sol.FechaLiquidacion;
    }

    private static void persona(SolicitudAporteRescate s, Persona p) {
        s.IdPersonaTraspaso = p.IdPersona;
        s.IdCuentaBancaria = p.IdCuentaBancaria;
        s.IdBanco = p.IdBanco;
        s.IdTipoCuentaBancaria = p.IdTipoCuentaBancaria;
        s.NumeroCuentaBancaria = p.NumeroCuentaBancaria;
        s.NombreTipoCuentaBancaria = p.NombreTipoCuentaBancaria;
    }

    private static Traspaso traspaso(SolicitudProceso solicitud, CertificarAporteRescateContexto ctx) {
        Traspaso traspaso = ctx.traspasos.get(solicitud.IdSolicitudAporteRescate);
        if (traspaso == null) traspaso = ctx.traspasos.get(solicitud.IdSolicitudAporteRescateRelacionado);
        return traspaso;
    }

    /** Legacy {@code getPojoSolicitudAporteRescate} (C:2638-2704), field by field; {@code IdAutorizadoOrigen} takes the payment preference (D-F3B-41). */
    static SolicitudAporteRescate pojo(SolicitudProceso sol) {
        SolicitudAporteRescate s = new SolicitudAporteRescate();
        s.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
        s.IdTipoCanalIngreso = sol.IdTipoCanalIngreso;
        s.IdCliente = sol.IdCliente;
        s.IdPortafolio = sol.IdPortafolio;
        s.TipoSolicitud = sol.TipoSolicitud;
        s.IdSerieFondo = sol.IdSerieFondo;
        s.IdTipoMovimientoFondoMutuo = sol.IdTipoMovimientoFondoMutuo;
        s.IdSerieFondoDestino = sol.IdSerieFondoDestino;
        s.IdEmisorDestino = sol.IdEmisorDestino;
        s.IndicadorMLT = sol.IndicadorMLT;
        s.Overnight = sol.Overnight;
        s.IdSolicitudAporteRescateRelacionado = sol.IdSolicitudAporteRescateRelacionado;
        s.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
        s.FechaAceptacion = sol.FechaAceptacion;
        s.FechaCertificacion = sol.FechaCertificacion;
        s.FechaLiquidacion = sol.FechaLiquidacion;
        s.EsPorMontoLiquido = sol.EsPorMontoLiquido;
        s.Monto = sol.Monto;
        s.NumeroCuotas = sol.NumeroCuotas;
        s.ValorCuota = sol.ValorCuota;
        s.CostoUF = sol.CostoUF;
        s.CostoMonedaLocal = sol.CostoMonedaLocal;
        s.ParidadFechaCertificacion = sol.ParidadFechaCertificacion;
        s.ValorUFFechaInversion = sol.ValorUFFechaInversion;
        s.IdMoneda = sol.IdMoneda;
        s.IdMonedaPago = sol.IdMonedaPago;
        s.GeneraSpot = sol.GeneraSpot;
        s.MontoComision = sol.MontoComision;
        s.ComisionInformada = sol.ComisionInformada;
        s.MontoComisionLocal = sol.MontoComisionLocal;
        s.MontoLiquido = sol.MontoLiquido;
        s.MontoLiquidoLocal = sol.MontoLiquidoLocal;
        s.MontoPago = sol.MontoPago;
        s.MontoPagoLocal = sol.MontoPagoLocal;
        s.Spread = sol.Spread;
        s.EstadoLiquidacion = sol.EstadoLiquidacion;
        s.MueveCta = sol.MueveCta;
        s.FolioCartaLiquidacion = sol.FolioCartaLiquidacion;
        s.IdMedioDePago = sol.IdMedioDePago;
        s.Glosa = sol.Glosa;
        s.IdUsuario = sol.IdUsuario;
        s.OrigenOperacion = sol.OrigenOperacion;
        s.SolicitudBatch = sol.SolicitudBatch;
        s.IdRegimenTributario = sol.IdRegimenTributario;
        s.TipoCarteraNormativa = sol.TipoCarteraNormativa;
        s.IdRegimenTributarioDestino = sol.IdRegimenTributarioDestino;
        s.TipoCarteraNormativaDestino = sol.TipoCarteraNormativaDestino;
        s.IdPreferenciaPagoPortafolio = sol.IdPreferenciaPagoPortafolio;
        s.IdAutorizadoOrigen = sol.IdPreferenciaPagoPortafolio;
        s.IdMonedaSistema = sol.IdMonedaSistema;
        s.MontoMonedaSistema = sol.MontoMonedaSistema;
        s.Switch = sol.Switch;
        s.IdEmpleadoEjecutivo = sol.IdEmpleadoEjecutivo;
        s.IdPersona = sol.IdPersona;
        s.IdInstrumento = sol.IdInstrumento;
        s.IdTipoInstrumento = sol.IdTipoInstrumento;
        s.EstadoProceso = sol.EstadoProceso;
        s.IdIngresoEgreso = sol.IdIngresoEgreso;
        s.IdOperacionSpot = sol.IdOperacionSpot;
        s.IdCartera = sol.IdCartera;
        s.IdAgrupador = sol.IdAgrupador;
        s.RelacionTraspaso = sol.RelacionTraspaso;
        s.IdPersonaEmisorDestino = sol.IdPersonaEmisorDestino;
        s.RazonRetiro = sol.RazonRetiro;
        return s;
    }
}
