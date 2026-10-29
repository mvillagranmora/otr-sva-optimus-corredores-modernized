package cl.bcs.fondomutuo.certificacion;

import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_ADVERTENCIA;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_PENDIENTE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.FormatoMoneda;

/** Pure rules of F-B (FM-CER-18, 19, 23). */
public final class CertificarAporteRescateReglas {

    /** Legacy comparator: equal ids are 0, otherwise 1 when the first is greater and -1 when not (a null id is a NullPointerException). */
    private static final Comparator<SolicitudProceso> ASCENDENTE = (primera, segunda) -> {
        if (primera.IdSolicitudAporteRescate.compareTo(segunda.IdSolicitudAporteRescate) == 0) {
            return 0;
        }
        return primera.IdSolicitudAporteRescate.compareTo(segunda.IdSolicitudAporteRescate) == 1 ? 1 : -1;
    };

    private CertificarAporteRescateReglas() {
    }

    /** FM-CER-18: the later of the system date and the date of the request (the system date wins when the request date is before it). */
    public static java.sql.Date fechaCertificacionEfectiva(String fechaSistema, String fechaEntrada) {
        return FechaLegacy.aFecha(fechaEntrada).compareTo(FechaLegacy.aFecha(fechaSistema)) < 0
                ? FechaLegacy.aFechaSql(fechaSistema)
                : FechaLegacy.aFechaSql(fechaEntrada);
    }

    /** Legacy {@code filtrarSolicitudesPendientesYAdvertencias}. */
    public static boolean pendienteOAdvertencia(SolicitudProceso solicitud) {
        return ESTADO_PENDIENTE.equals(solicitud.EstadoProceso) || ESTADO_ADVERTENCIA.equals(solicitud.EstadoProceso);
    }

    /** FM-CER-19: the PEN / ADV solicitudes of the type, ascending by id (stable). */
    public static List<SolicitudProceso> porTipoOrdenadas(List<SolicitudProceso> solicitudes, String tipoSolicitud) {
        List<SolicitudProceso> desordenadas = new ArrayList<>();
        for (SolicitudProceso s : solicitudes) {
            if (pendienteOAdvertencia(s) && tipoSolicitud.equals(s.TipoSolicitud)) {
                desordenadas.add(s);
            }
        }
        desordenadas.sort(ASCENDENTE);
        return desordenadas;
    }

    /** FM-CER-23: the 11 amounts take the decimals of the fund currency, the investment currency and the system currency. */
    public static void formatearMontos(FormatoMoneda formato, List<SolicitudProceso> solicitudes) throws Exception {
        for (SolicitudProceso s : solicitudes) {
            s.Monto = formato.formatear(s.Monto, FormatoMoneda.MONTO, s.IdMonedaFondo);
            s.MontoComision = formato.formatear(s.MontoComision, FormatoMoneda.MONTO, s.IdMonedaFondo);
            s.MontoPago = formato.formatear(s.MontoPago, FormatoMoneda.MONTO, s.IdMonedaFondo);
            s.MontoLiquido = formato.formatear(s.MontoLiquido, FormatoMoneda.MONTO, s.IdMonedaFondo);
            s.MontoRetencion = formato.formatear(s.MontoRetencion, FormatoMoneda.MONTO, s.IdMonedaFondo);

            s.CostoMonedaLocal = formato.formatear(s.CostoMonedaLocal, FormatoMoneda.MONTO, s.IdMoneda);
            s.MontoComisionLocal = formato.formatear(s.MontoComisionLocal, FormatoMoneda.MONTO, s.IdMoneda);
            s.MontoPagoLocal = formato.formatear(s.MontoPagoLocal, FormatoMoneda.MONTO, s.IdMoneda);
            s.MontoLiquidoLocal = formato.formatear(s.MontoLiquidoLocal, FormatoMoneda.MONTO, s.IdMoneda);
            s.MontoRetencionLocal = formato.formatear(s.MontoRetencionLocal, FormatoMoneda.MONTO, s.IdMoneda);

            s.MontoMonedaSistema = formato.formatear(s.MontoMonedaSistema, FormatoMoneda.MONTO, s.IdMonedaSistema);
        }
    }
}
