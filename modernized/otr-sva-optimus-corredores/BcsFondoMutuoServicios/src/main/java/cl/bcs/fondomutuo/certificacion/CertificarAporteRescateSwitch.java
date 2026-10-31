package cl.bcs.fondomutuo.certificacion;

import static cl.bcs.fondomutuo.certificacion.CerConstantes.APORTE_REINVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_RECHAZADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.NO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SI;

import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Salida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudReinversion;

/**
 * Front F-I (2: the switch) of legacy {@code CertificarSolicitudAporteRescate.logica} C:838-845, {@code certificarAportesSwitch} C:2764-2775 and
 * {@code cambiarMontoMonedaSistemaRescatesReinversionInternacionalesSinSpot} C:2820-2835, bug for bug (FM-CER-63, 64). Both steps run AFTER Spot and front J: the child certification of the
 * aportes of switch (ONCE per request, never when empty; the recursion is a port, its implementation is the facade of T5) and the sum of {@code MontoMonedaSistema} by related rescate (D-F3B-49),
 * the ONLY write of F-C..F-I, through its own write port. Works on the {@link CertificarAporteRescateContexto} and lets any exception go up untouched. No state.
 */
public final class CertificarAporteRescateSwitch {

    private final CertificacionHijaPort hija;
    private final CertificarAporteRescateDatos datos;
    private final CertificarAporteRescateEscrituraMonto escritura;

    public CertificarAporteRescateSwitch(CertificacionHijaPort hija, CertificarAporteRescateDatos datos, CertificarAporteRescateEscrituraMonto escritura) {
        this.hija = hija;
        this.datos = datos;
        this.escritura = escritura;
    }

    /** FM-CER-63 (C:838-843, 2764-2775): the child certification with the header of the request, {@code p_NotificaOWP = "N"} and the simulation flag copied; its counter, messages and results are added. */
    public void certificarAportesSwitch(CertificarAporteRescateContexto ctx) throws Exception {
        if (ctx.aportesSwitch.size() > 0) {
            Entrada original = ctx.entrada;
            Entrada entradaSwitch = new Entrada();
            entradaSwitch.p_IdEmpresa = original.p_IdEmpresa;
            entradaSwitch.p_IdUsuario = original.p_IdUsuario;
            entradaSwitch.p_IdModulo = original.p_IdModulo;
            entradaSwitch.p_SimularOperacion = original.p_SimularOperacion;
            entradaSwitch.p_UsaReglasNegocio = original.p_UsaReglasNegocio;
            entradaSwitch.p_FechaCertificacion = original.p_FechaCertificacion;
            entradaSwitch.p_IdTipoCanalIngreso = original.p_IdTipoCanalIngreso;
            entradaSwitch.p_NotificaOWP = NO;
            entradaSwitch.p_CertificacionAporteSwitchSimulado = entradaSwitch.p_SimularOperacion;
            entradaSwitch.p_Solicitudes = ctx.aportesSwitch;
            Salida salidaSwitch = hija.certificar(entradaSwitch);
            ctx.salida.P_OUT_ERRORESCONTADOR += salidaSwitch.P_OUT_ERRORESCONTADOR;
            ctx.salida.P_OUT_MENSAJES.addAll(salidaSwitch.P_OUT_MENSAJES);
            ctx.salida.P_OUT_RESULTADO.addAll(salidaSwitch.P_OUT_RESULTADO);
        }
    }

    /**
     * FM-CER-64 (C:845, 2820-2835): for the aportes APOREINV of the request that are not rejected, have a related rescate, a system currency other than their own and no Spot, the
     * rescate they come from takes the SUM of {@code MontoMonedaSistema} of ALL the rows related to it (D-F3B-49). A null related id is a NullPointerException.
     */
    public void cambiarMontoMonedaSistema(CertificarAporteRescateContexto ctx) throws Exception {
        List<Long> idsRelacionados = ctx.solicitudes.stream()
                .filter(sol -> !ESTADO_RECHAZADO.equals(sol.EstadoProceso) && APORTE_REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo) && sol.IdSolicitudAporteRescateRelacionado > 0
                        && !sol.IdMonedaSistema.equals(sol.IdMoneda) && !SI.equals(sol.GeneraSpot))
                .map(sol -> sol.IdSolicitudAporteRescateRelacionado)
                .distinct()
                .collect(Collectors.toList());
        if (idsRelacionados.size() > 0) {
            List<SolicitudReinversion> sumas = datos.sumaMontoMonedaSistema(idsRelacionados);
            escritura.actualizarMontoMonedaSistema(sumas);
        }
    }
}
