package cl.bcs.fondomutuo.certificacion;

import static cl.bcs.fondomutuo.certificacion.CerConstantes.ABONO_CUENTA_INVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.CARGO_CUENTA_INVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_RECHAZADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.NO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ORIGEN_MANUAL;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.REINVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.RESCATE_COMISION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SI;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_COMPROBANTE_CUENTA_INVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_OPERACION_APORTE_FONDO_MUTUO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_OPERACION_RESCATE_FONDO_MUTUO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_PRODUCTO_CUSTODIA;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_SOLICITUD_APORTE;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_SOLICITUD_RESCATE;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.CargoAbonoEntrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.CargoAbonoSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.ObjCargoAbono;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.kit.MensajeSalida;

/**
 * Front F-E (cuenta de inversion) of legacy {@code CertificarSolicitudAporteRescate.logica}, bug for bug (FM-CER-29, 30, 42, 43): one call to {@code CrearCargoAbono} for the aportes (after the
 * entry of custody) and another for the rescates (after the retirement and its costs). Each step does nothing when its list is empty, works on the {@link CertificarAporteRescateContexto}
 * and its ports only (no write, no gate: T5) and lets any exception go up untouched.
 */
public final class CertificarAporteRescateCuentaInversion {

    private final CrearCargoAbonoPort cuenta;
    private final CertificarAporteRescateDatos datos;

    public CertificarAporteRescateCuentaInversion(CrearCargoAbonoPort cuenta, CertificarAporteRescateDatos datos) {
        this.cuenta = cuenta;
        this.datos = datos;
    }

    /** Legacy C:622-640 (the aportes, after the custody entry). */
    public void moverAportes(CertificarAporteRescateContexto ctx) throws Exception {
        if (ctx.aportes.size() > 0) mover(ctx, ctx.aportes);
    }

    /** Legacy C:679-694 (the rescates, after the retirement and the costs). */
    public void moverRescates(CertificarAporteRescateContexto ctx) throws Exception {
        if (ctx.rescates.size() > 0) mover(ctx, ctx.rescates);
    }

    private void mover(CertificarAporteRescateContexto ctx, List<SolicitudProceso> solicitudes) {
        CargoAbonoSalida salida = mueveCuentaInversion(ctx, solicitudes);
        ctx.salida.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
        CertificarAporteRescateCustodia.rechazarPorMensajes(ctx.salida, salida.P_OUT_MENSAJES, solicitudes);
    }

    private CargoAbonoSalida mueveCuentaInversion(CertificarAporteRescateContexto ctx, List<SolicitudProceso> solicitudes) {
        Entrada entrada = ctx.entrada;
        CargoAbonoEntrada entradaCuenta = new CargoAbonoEntrada();
        CargoAbonoSalida salida = new CargoAbonoSalida();
        Long i = 0L;
        HashMap<Long, Long> ids = new HashMap<>();
        java.sql.Date fechaSistema = FechaLegacy.aFechaSql(ctx.fechaSistema);
        for (SolicitudProceso sol : solicitudes) {
            if (ESTADO_RECHAZADO.equals(sol.EstadoProceso)) continue;
            if (TIPO_SOLICITUD_APORTE.equals(sol.TipoSolicitud)) {
                if (SI.equals(sol.MueveCta) && ORIGEN_MANUAL.equals(sol.OrigenOperacion) && SI.equals(datos.generaCargoAbono(sol.IdTipoMovimientoFondoMutuo))) {
                    ObjCargoAbono obj = new ObjCargoAbono();
                    obj.NumeroCargoAbono = i;
                    obj.IdPortafolio = sol.IdPortafolio;
                    obj.IdMoneda = sol.IdMonedaFondo;
                    obj.IdTipoMovimiento = CARGO_CUENTA_INVERSION;
                    obj.Monto = sol.MontoPago;
                    obj.IdMonedaOperacion = sol.IdMonedaFondo;
                    obj.MontoOperacion = sol.MontoPago;
                    obj.IdConcepto = sol.IdConcepto;
                    obj.Descripcion = "APORTE_FONDO_MUTUO";
                    obj.IdCondicionDeLiquidacion = "";
                    obj.FechaCondicionDeLiquidacion = (fechaSistema.compareTo(sol.FechaLiquidacion) > 0 || sol.Switch.equalsIgnoreCase("S")) ? ctx.fechaSistema : FechaLegacy.texto(sol.FechaLiquidacion);
                    obj.IdEjecutivo = sol.IdEmpleadoEjecutivo;
                    obj.IdSucursal = 0L;
                    obj.IdTipoProducto = TIPO_PRODUCTO_CUSTODIA;
                    obj.IdTipoOperacion = TIPO_OPERACION_APORTE_FONDO_MUTUO;
                    obj.IdOperacion = sol.IdSolicitudAporteRescate;
                    obj.FechaOperacion = FechaLegacy.texto(sol.FechaCertificacion);
                    obj.FechaLiquidacionOperacion = (fechaSistema.compareTo(sol.FechaLiquidacion) > 0) ? ctx.fechaSistema : FechaLegacy.texto(sol.FechaLiquidacion);
                    obj.TipoCambioOperacion = sol.ParidadFechaCertificacion;
                    obj.IdTipoComprobante = TIPO_COMPROBANTE_CUENTA_INVERSION;
                    obj.IdComprobante = sol.IdSolicitudAporteRescate;
                    obj.FolioComprobante = sol.IdSolicitudAporteRescate.toString();
                    obj.IdComprobanteDetalle = sol.IdSolicitudAporteRescate;
                    obj.IdTipoMovimientoAsociado = "";
                    obj.IdMovimientoAsociado = 0L;
                    obj.FechaComprobante = FechaLegacy.texto(sol.FechaCertificacion);
                    obj.IdInstrumento = sol.IdInstrumento;
                    obj.IdTipoInstrumento = sol.IdTipoInstrumento;
                    obj.Nemo = sol.IdSerieFondo;
                    obj.EsCargoAbonoPorTraspaso = NO;
                    obj.GeneroContraparteEnTesoreria = NO;
                    obj.CertificadoAutomatico = SI;
                    ids.put(i, sol.IdSolicitudAporteRescate);
                    i++;
                    if (!sol.IdMoneda.equals(sol.IdMonedaFondo)) {
                        ObjCargoAbono cardif = copiar(obj);
                        cardif.NumeroCargoAbono = i;
                        cardif.IdMoneda = sol.IdMoneda;
                        cardif.IdTipoMovimiento = CARGO_CUENTA_INVERSION;
                        cardif.Monto = sol.MontoPagoLocal;
                        cardif.IdMonedaOperacion = sol.IdMoneda;
                        cardif.MontoOperacion = sol.MontoPagoLocal;
                        entradaCuenta.p_CargoAbonoAGenerar.add(cardif);
                        ids.put(i, sol.IdSolicitudAporteRescate);
                        i++;
                    }
                    if (!sol.IdMoneda.equals(sol.IdMonedaFondo)) {
                        ObjCargoAbono cardif = copiar(obj);
                        cardif.NumeroCargoAbono = i;
                        cardif.IdMoneda = sol.IdMonedaFondo;
                        cardif.IdTipoMovimiento = ABONO_CUENTA_INVERSION;
                        cardif.Monto = sol.MontoPago;
                        cardif.IdMonedaOperacion = sol.IdMonedaFondo;
                        cardif.MontoOperacion = sol.MontoPago;
                        entradaCuenta.p_CargoAbonoAGenerar.add(cardif);
                        ids.put(i, sol.IdSolicitudAporteRescate);
                        i++;
                    }
                    entradaCuenta.p_CargoAbonoAGenerar.add(obj);
                }
            }
            if (TIPO_SOLICITUD_RESCATE.equals(sol.TipoSolicitud)) {
                // D-F3B-17: a rescate does not ask for MueveCta nor the manual origin
                if (SI.equals(datos.generaCargoAbono(sol.IdTipoMovimientoFondoMutuo))) {
                    ObjCargoAbono obj = new ObjCargoAbono();
                    obj.NumeroCargoAbono = i;
                    obj.IdPortafolio = sol.IdPortafolio;
                    obj.IdMoneda = sol.IdMonedaFondo;
                    obj.IdTipoMovimiento = ABONO_CUENTA_INVERSION;
                    obj.Monto = sol.Monto;
                    obj.IdMonedaOperacion = sol.IdMonedaFondo;
                    obj.MontoOperacion = sol.MontoPago;
                    obj.IdConcepto = sol.IdConcepto;
                    obj.Descripcion = "RESCATE FONDO MUTUO";
                    obj.IdCondicionDeLiquidacion = "";
                    obj.FechaCondicionDeLiquidacion = (fechaSistema.compareTo(sol.FechaLiquidacion) > 0 || sol.Switch.equalsIgnoreCase("S")) ? ctx.fechaSistema : FechaLegacy.texto(sol.FechaLiquidacion);
                    obj.IdEjecutivo = sol.IdEmpleadoEjecutivo;
                    obj.IdSucursal = 0L;
                    obj.IdTipoProducto = TIPO_PRODUCTO_CUSTODIA;
                    obj.IdTipoOperacion = TIPO_OPERACION_RESCATE_FONDO_MUTUO;
                    obj.IdOperacion = sol.IdSolicitudAporteRescate;
                    obj.FechaOperacion = FechaLegacy.texto(sol.FechaAceptacion);
                    obj.FechaLiquidacionOperacion = (fechaSistema.compareTo(sol.FechaLiquidacion) > 0) ? ctx.fechaSistema : FechaLegacy.texto(sol.FechaLiquidacion);
                    obj.TipoCambioOperacion = sol.ParidadFechaCertificacion;
                    obj.IdTipoComprobante = TIPO_COMPROBANTE_CUENTA_INVERSION;
                    obj.IdComprobante = sol.IdSolicitudAporteRescate;
                    obj.FolioComprobante = sol.IdSolicitudAporteRescate.toString();
                    obj.IdComprobanteDetalle = sol.IdSolicitudAporteRescate;
                    obj.IdTipoMovimientoAsociado = "";
                    obj.IdMovimientoAsociado = 0L;
                    obj.FechaComprobante = FechaLegacy.texto(sol.FechaCertificacion);
                    obj.IdInstrumento = sol.IdInstrumento;
                    obj.IdTipoInstrumento = sol.IdTipoInstrumento;
                    obj.Nemo = sol.IdSerieFondo;
                    obj.EsCargoAbonoPorTraspaso = NO;
                    obj.GeneroContraparteEnTesoreria = NO;
                    obj.CertificadoAutomatico = SI;
                    ids.put(i, sol.IdSolicitudAporteRescate);
                    i++;
                    entradaCuenta.p_CargoAbonoAGenerar.add(obj);
                    if (!sol.IdMoneda.equals(sol.IdMonedaFondo)) {
                        ObjCargoAbono cardif = copiar(obj);
                        cardif.NumeroCargoAbono = i;
                        cardif.IdMoneda = sol.IdMonedaFondo;
                        cardif.IdTipoMovimiento = CARGO_CUENTA_INVERSION;
                        cardif.Monto = sol.MontoPago;
                        cardif.IdMonedaOperacion = sol.IdMonedaFondo;
                        cardif.MontoOperacion = sol.MontoPago;
                        entradaCuenta.p_CargoAbonoAGenerar.add(cardif);
                        ids.put(i, sol.IdSolicitudAporteRescate);
                        i++;
                    }
                    if (!sol.IdMoneda.equals(sol.IdMonedaFondo)) {
                        ObjCargoAbono cardif = copiar(obj);
                        cardif.NumeroCargoAbono = i;
                        cardif.IdMoneda = sol.IdMoneda;
                        cardif.IdTipoMovimiento = ABONO_CUENTA_INVERSION;
                        cardif.Monto = sol.MontoPagoLocal;
                        cardif.IdMonedaOperacion = sol.IdMoneda;
                        cardif.MontoOperacion = sol.MontoPagoLocal;
                        entradaCuenta.p_CargoAbonoAGenerar.add(cardif);
                        ids.put(i, sol.IdSolicitudAporteRescate);
                        i++;
                    }
                    if ((REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo) || RESCATE_COMISION.equals(sol.IdTipoMovimientoFondoMutuo)) && sol.MontoComisionLocal.compareTo(BigDecimal.ZERO) == 1) {
                        ObjCargoAbono cardif = copiar(obj);
                        cardif.NumeroCargoAbono = i;
                        cardif.IdMoneda = sol.IdMoneda;
                        cardif.IdTipoMovimiento = CARGO_CUENTA_INVERSION;
                        cardif.Monto = sol.MontoComisionLocal;
                        cardif.IdMonedaOperacion = sol.IdMoneda;
                        cardif.MontoOperacion = sol.MontoComisionLocal;
                        entradaCuenta.p_CargoAbonoAGenerar.add(cardif);
                        ids.put(i, sol.IdSolicitudAporteRescate);
                        i++;
                    }
                }
            }
        }

        if (entradaCuenta.p_CargoAbonoAGenerar.size() > 0) {
            entradaCuenta.p_IdEmpresa = entrada.p_IdEmpresa;
            entradaCuenta.p_IdUsuario = entrada.p_IdUsuario;
            entradaCuenta.p_IdModulo = entrada.p_IdModulo;
            entradaCuenta.p_simularOperacion = NO;
            entradaCuenta.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
            entradaCuenta.p_FechaSistema = ctx.fechaSistema;
            entradaCuenta.p_EsIngresoManual = "N";
            salida = CertificarAporteRescateColaboradores.crearCargoAbono(cuenta, entradaCuenta);
            // the id of the collaborator is the number of the line: it goes back to the id of the solicitud (a number without entry is a null id: D-NOR-9)
            for (MensajeSalida mje : salida.P_OUT_MENSAJES) {
                if (MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
                    mje.id = ids.get(mje.id);
                }
            }
        }
        return salida;
    }

    /** Legacy {@code getPojoCardif}: the same fields, one by one. */
    private static ObjCargoAbono copiar(ObjCargoAbono o) {
        ObjCargoAbono c = new ObjCargoAbono();
        c.NumeroCargoAbono = o.NumeroCargoAbono;
        c.IdPortafolio = o.IdPortafolio;
        c.IdMoneda = o.IdMoneda;
        c.IdTipoMovimiento = o.IdTipoMovimiento;
        c.IdConcepto = o.IdConcepto;
        c.Descripcion = o.Descripcion;
        c.IdCondicionDeLiquidacion = o.IdCondicionDeLiquidacion;
        c.FechaCondicionDeLiquidacion = o.FechaCondicionDeLiquidacion;
        c.Monto = o.Monto;
        c.IdEjecutivo = o.IdEjecutivo;
        c.IdSucursal = o.IdSucursal;
        c.IdTipoProducto = o.IdTipoProducto;
        c.IdTipoOperacion = o.IdTipoOperacion;
        c.IdOperacion = o.IdOperacion;
        c.FechaOperacion = o.FechaOperacion;
        c.FechaLiquidacionOperacion = o.FechaLiquidacionOperacion;
        c.FechaVencimientoOperacion = o.FechaVencimientoOperacion;
        c.IdMonedaOperacion = o.IdMonedaOperacion;
        c.MontoOperacion = o.MontoOperacion;
        c.TipoCambioOperacion = o.TipoCambioOperacion;
        c.IdContraparte = o.IdContraparte;
        c.IdTipoComprobante = o.IdTipoComprobante;
        c.IdComprobante = o.IdComprobante;
        c.FolioComprobante = o.FolioComprobante;
        c.IdComprobanteDetalle = o.IdComprobanteDetalle;
        c.IdComprobanteFormaDePago = o.IdComprobanteFormaDePago;
        c.IdTipoMovimientoAsociado = o.IdTipoMovimientoAsociado;
        c.IdMovimientoAsociado = o.IdMovimientoAsociado;
        c.FechaComprobante = o.FechaComprobante;
        c.IdPersonaPagoCobro = o.IdPersonaPagoCobro;
        c.IdInstrumento = o.IdInstrumento;
        c.IdTipoInstrumento = o.IdTipoInstrumento;
        c.Cantidad = o.Cantidad;
        c.Nemo = o.Nemo;
        c.EsIndicadorBilateral = o.EsIndicadorBilateral;
        c.EsCargoAbonoPorTraspaso = o.EsCargoAbonoPorTraspaso;
        c.IdPortafolioTraspaso = o.IdPortafolioTraspaso;
        c.GeneroContraparteEnTesoreria = o.GeneroContraparteEnTesoreria;
        c.CertificadoAutomatico = o.CertificadoAutomatico;
        return c;
    }
}
