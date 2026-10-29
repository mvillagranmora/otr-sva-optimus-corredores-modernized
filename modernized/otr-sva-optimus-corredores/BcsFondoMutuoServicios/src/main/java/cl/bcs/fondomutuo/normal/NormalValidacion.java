package cl.bcs.fondomutuo.normal;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.normal.NormalDatos.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.patrimonio.ValidaPatrimonioEmisorService;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService;
import cl.bcs.fondomutuo.requisitos.ValidaMontoInversionService;

/** The checks of the requests before the flows: requirements, investment amounts, issuer equity (legacy :1081-1162). */
final class NormalValidacion {

    private final ServicioCumpleRequisitosService requisitos;
    private final ValidaMontoInversionService montoInversion;
    private final ValidaPatrimonioEmisorService patrimonio;

    NormalValidacion(ServicioCumpleRequisitosService requisitos, ValidaMontoInversionService montoInversion, ValidaPatrimonioEmisorService patrimonio) {
        this.requisitos = requisitos;
        this.montoInversion = montoInversion;
        this.patrimonio = patrimonio;
    }

    /** Legacy {@code esOrigenProcesosManuales}: the origin is not PAC, REC or BONIF. */
    static boolean esOrigenProcesosManuales(NormalContrato.Entrada entrada) {
        return !NormalConstantes.ORIGEN_PAC.equals(entrada.p_IdOrigenProceso) && !NormalConstantes.ORIGEN_RECAUDACION.equals(entrada.p_IdOrigenProceso)
                && !NormalConstantes.ORIGEN_BONIFICACION.equals(entrada.p_IdOrigenProceso);
    }

    /** @return false when no request is left to process (the caller releases the lock and answers). */
    boolean requisitos(NormalContexto c) throws Exception {
        ServicioCumpleRequisitosService.Salida salida = requisitos.logica(c.entradaCumpleRequisitos);
        Map<Long, List<MensajeSalida>> porSolicitud = salida.P_OUT_MENSAJES.stream().collect(Collectors.groupingBy(mje -> Math.abs(mje.id)));
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            List<MensajeSalida> mensajes = porSolicitud.get(sol.IdInterno);
            if (mensajes != null) {
                c.out.P_OUT_MENSAJES.addAll(mensajes);
                Long errores = mensajes.stream().filter(mje -> MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)).count();
                c.out.P_OUT_ERRORESCONTADOR += errores;
                if (!errores.equals(0L)) {
                    sol.EstadoProceso = NormalConstantes.ESTADO_RECHAZADO;
                }
            }
        }
        long porProcesar = c.solicitudesProcesar.stream().filter(sol -> NormalConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)).count();
        return porProcesar != 0;
    }

    void montoInversion(NormalContexto c) throws Exception {
        ValidaMontoInversionService.Entrada entrada = new ValidaMontoInversionService.Entrada();
        entrada.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entrada.p_IdUsuario = c.entrada.p_IdUsuario;
        entrada.p_Solicitudes = new ArrayList<>();
        for (SolicitudAporteRescate solicitud : c.solicitudesProcesar) {
            if (!NormalConstantes.TIPO_SOLICITUD_APORTE.equals(solicitud.TipoSolicitud)) continue;
            SerieFondoSolicitudes serieFondoSolicitud = c.hseries.get(solicitud.IdSerieFondo);
            RegimenSerieDeFondo regimenSerieFondo = c.hseriesRegimen.get(solicitud.IdSerieFondo + "_" + solicitud.IdRegimenTributario);
            ValidaMontoInversionService.Solicitud aValidar = new ValidaMontoInversionService.Solicitud();
            aValidar.IdInterno = solicitud.IdInterno;
            aValidar.FolioContrato = solicitud.FolioContrato;
            aValidar.AportesAltos = serieFondoSolicitud.AportesAltos;   // legacy: no null check (a missing series is a NullPointerException, D-NOR-9)
            aValidar.Monto = solicitud.MontoMonedaSistema;
            aValidar.ValidaMontoMinimo = regimenSerieFondo != null ? regimenSerieFondo.ValidaMontoMinimoInstrumento : NormalConstantes.SI;
            entrada.p_Solicitudes.add(aValidar);
        }
        if (entrada.p_Solicitudes.size() > 0) {
            ValidaMontoInversionService.Salida salida = montoInversion.logica(entrada);
            for (MensajeSalida mje : salida.P_OUT_MENSAJES) {
                c.out.P_OUT_MENSAJES.add(mje);
                if (MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)) {
                    c.out.P_OUT_ERRORESCONTADOR++;
                    rechazar(c, mje.id);
                }
            }
        }
    }

    void patrimonioEmisor(NormalContexto c) throws Exception {
        if (c.entradaValidaPatrimonioEmisor.p_Solicitudes.size() > 0 && esOrigenProcesosManuales(c.entrada)) {
            ValidaPatrimonioEmisorService.Salida salida = patrimonio.logica(c.entradaValidaPatrimonioEmisor);
            for (MensajeSalida mje : salida.P_OUT_MENSAJES) {
                if (MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)) {
                    c.out.P_OUT_ERRORESCONTADOR++;
                    c.out.P_OUT_MENSAJES.add(mje);
                    rechazar(c, mje.id);
                }
            }
        }
    }

    /** The latest certification date of the pending requests (legacy :1141-1146); it is the end of the cash-flow query. */
    static Date fechaMaximaCertificacion(NormalContexto c) {
        Date maxima = FechaLegacy.aFechaSql("0001-01-01");
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            if (NormalConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso) && sol.FechaCertificacion.compareTo(maxima) == 1) {
                maxima = sol.FechaCertificacion;
            }
        }
        return maxima;
    }

    private static void rechazar(NormalContexto c, Long idInterno) {
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            if (sol.IdInterno.equals(idInterno)) {
                sol.EstadoProceso = NormalConstantes.ESTADO_RECHAZADO;
                break;
            }
        }
    }
}
