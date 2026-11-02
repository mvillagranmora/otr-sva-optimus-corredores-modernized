package cl.bcs.fondomutuo.certificacion;

import static cl.bcs.fondomutuo.certificacion.CerConstantes.CANAL_OWP;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_APROBADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_CERTIFICADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_PENDIENTE;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.MODULO_FONDO_MUTUO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.REINVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SI;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.InSolicitudes;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.NotificarEstadoOrdenEntrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.OrdenOwp;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.requisitos.contrato.LogContrato;

/**
 * Front J (the closing) of legacy {@code CertificarSolicitudAporteRescate.logica} C:835-836, 848-864 and {@code actualizarSolicitudes} / {@code notificar} / {@code notificarOWP} /
 * {@code agregarLogSistema} / {@code marcarSolicitudesExitosas} C:2738-2880, bug for bug (FM-CER-61, 62, 65, 66, 67, 68): the UPDATE of the pending and warned solicitudes to certified, the
 * asynchronous notification (always, also with an empty list and in simulation), the OpenWorkPlace notification, the system log, the success mark with the output and the log of certification,
 * and the forced counter of the simulation. The recursion of the switch and FM-CER-64 run between the first and the second step ({@link CertificarAporteRescateSwitch}); the order is the
 * facade's. Reads and writes ONLY the context and its ports: no connection, no gate (the facade evaluates them before the first write), no state. Any exception goes up UNTOUCHED (the legacy has
 * no try/catch here: the host rolls back the whole connection and nothing is compensated).
 */
public final class CertificarAporteRescateCierre {

    private final NotificacionCertificacionPort notificacion;
    private final NotificacionOrdenesOwpPort owp;
    private final CertificarAporteRescateEscrituraCierre escritura;
    private final CertificarAporteRescateDatos datos;
    private final LogPort agregarLog;
    private final CertificarAporteRescateSalida salida;

    public CertificarAporteRescateCierre(NotificacionCertificacionPort notificacion, NotificacionOrdenesOwpPort owp, CertificarAporteRescateEscrituraCierre escritura,
            CertificarAporteRescateDatos datos, LogPort agregarLog, CertificarAporteRescateSalida salida) {
        this.notificacion = notificacion;
        this.owp = owp;
        this.escritura = escritura;
        this.datos = datos;
        this.agregarLog = agregarLog;
        this.salida = salida;
    }

    /** FM-CER-61, 62 (C:835-836, 2738-2762): the UPDATE of the PEN / ADV solicitudes and the asynchronous notification with their ids (no guard of state in the UPDATE, D-F3B-16). */
    public void actualizarYNotificar(CertificarAporteRescateContexto ctx) throws Exception {
        List<SolicitudProceso> solActualizar = ctx.solicitudes.stream().filter(CertificarAporteRescateReglas::pendienteOAdvertencia).collect(Collectors.toList());
        if (solActualizar.size() > 0) {
            escritura.actualizarSolicitudes(solActualizar);
        }
        ctx.solicitudesActualizadas = solActualizar;
        NotificarEstadoOrdenEntrada entrada = new NotificarEstadoOrdenEntrada();
        entrada.p_IdEmpresa = ctx.entrada.p_IdEmpresa;
        entrada.p_IdUsuario = ctx.entrada.p_IdUsuario;
        entrada.p_IdModulo = ctx.entrada.p_IdModulo;
        entrada.p_Solicitudes = solActualizar.stream().map(sol -> sol.IdSolicitudAporteRescate).collect(Collectors.toList());
        notificacion.notificar(entrada);
    }

    /**
     * FM-CER-65 (C:848-850, 2777-2818): with the parameter {@code PLA.NotificarEstadoOrdenOWP} AND the entry {@code p_NotificaOWP} both {@code S} (ignoring case), the solicitudes of channel OWP
     * (the aportes of switch first, then the updated ones) are read again and notified in ONE call; an empty list reads but sends nothing. It runs also with a counter above 0 and in
     * simulation; the child never notifies (its {@code p_NotificaOWP} is {@code N}).
     */
    public void notificarOwp(CertificarAporteRescateContexto ctx) throws Exception {
        if (!(SI.equalsIgnoreCase(ctx.notificaOWP) && SI.equalsIgnoreCase(ctx.entrada.p_NotificaOWP))) {
            return;
        }
        List<Long> idSolicitudes = new ArrayList<>();
        idSolicitudes.addAll(ctx.aportesSwitch.stream().filter(sol -> CANAL_OWP.equals(sol.IdTipoCanalIngreso)).map(sol -> sol.IdSolicitudAporteRescate).distinct()
                .collect(Collectors.toList()));
        idSolicitudes.addAll(ctx.solicitudesActualizadas.stream().filter(sol -> CANAL_OWP.equals(sol.IdTipoCanalIngreso)).map(sol -> sol.IdSolicitudAporteRescate).distinct()
                .collect(Collectors.toList()));
        List<SolicitudProceso> modificadas = datos.solicitudes(ctx.entrada.p_IdEmpresa, idSolicitudes);
        List<OrdenOwp> ordenes = new ArrayList<>();
        for (SolicitudProceso sol : modificadas) {
            OrdenOwp o = new OrdenOwp();
            o.IdEmpresa = ctx.entrada.p_IdEmpresa;
            o.IdUsuario = ctx.entrada.p_IdUsuario;
            o.Solsec = sol.IdSolicitudAporteRescate;
            o.NTitulos = sol.NumeroCuotas;
            o.Precio = sol.ValorCuota;
            o.Fecha = sol.FechaAceptacion.toString();   // legacy: a null date or related id is a NullPointerException here, after the UPDATE, the notification and the child
            o.Estado = sol.EstadoSolicitud;
            o.Comision = sol.MontoComision;
            o.Gastos = new BigDecimal(0);
            o.Mercado = "F";
            o.Divisa = sol.IdMoneda;
            o.TipoCambio = sol.CostoMonedaLocal;
            o.SolsecAsoc = REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo) ? "" : sol.IdSolicitudAporteRescateRelacionado.toString();
            o.Nemo = sol.IdSerieFondo;
            o.IdClasificacionTipoInstrumento = "FM";
            ordenes.add(o);
        }
        if (ordenes.size() > 0) {
            owp.notificar(ordenes);
        }
    }

    /**
     * FM-CER-66, 67, 68 (C:852-864, 2837-2880): the system log (its answer is never read), the pending solicitudes become approved with {@code UI_EXITO}, the output of EVERY solicitud and one row of
     * the log of certification each, and, only with {@code p_SimularOperacion} exactly {@code S}, the counter is forced to 1 (even without errors) at the very end.
     */
    public void cerrar(CertificarAporteRescateContexto ctx) {
        agregarLogSistema(ctx);
        for (SolicitudProceso sol : ctx.solicitudes) {
            if (ESTADO_PENDIENTE.equals(sol.EstadoProceso)) {
                sol.EstadoProceso = ESTADO_APROBADO;
                sol.Codigo = "UI_EXITO";
            }
        }
        ctx.salida.P_OUT_RESULTADO.addAll(salida.procesarSalida(ctx.entrada, ctx.solicitudes, ctx.estados));
        salida.insertarLog(ctx.solicitudes, ctx.entrada, ctx.fechaSistema);
        if (SI.equals(ctx.entrada.p_SimularOperacion)) {
            ctx.salida.P_OUT_ERRORESCONTADOR = 1L;
        }
    }

    private void agregarLogSistema(CertificarAporteRescateContexto ctx) {
        LogContrato.InAgregarLog entradaLog = new LogContrato.InAgregarLog();
        LogContrato.Log l = new LogContrato.Log();
        l.IdCabeceraLog = 0L;
        l.IdTipoLog = "CERTIFICACION APORTE RESCATE";
        l.IdModulo = MODULO_FONDO_MUTUO;
        l.IdUsuario = ctx.entrada.p_IdUsuario;
        l.IdEmpresa = ctx.entrada.p_IdEmpresa;
        l.FechaSistemaLog = ctx.fechaSistema;
        l.IdUsuarioAtendido = ctx.entrada.p_IdUsuario;
        l.IdCliente = 0L;
        l.EstadoCliente = "";
        l.NombreCliente = "";
        l.IdTipoDocumentoIdentidad = "";
        l.IdDocumentoIdentidad = "";
        l.IdPortafolio = 0L;
        l.EstadoPortafolio = "";
        l.IdTipoRegistro = "CERTIFICACION_APORTE_RESCATE";
        l.EstadoRegistro = ESTADO_CERTIFICADO;
        l.IdTipoOperacion = "";
        l.Folio = 0L;
        l.Cantidad = BigDecimal.ZERO;
        l.PrecioTasa = BigDecimal.ZERO;
        l.Monto = BigDecimal.ZERO;
        l.IdMonedaOrigen = "";
        l.IdMonedaLiquidacion = "";
        l.TipoCambio = "";
        l.Nemo = "";
        l.IdProceso = 0L;
        l.EstadoProceso = "";
        l.NombreProceso = "";
        entradaLog.p_Log.add(l);
        agregarLog.agregar(JsonLegacy.conNulos().toJsonTree(entradaLog).getAsJsonObject());
    }
}
