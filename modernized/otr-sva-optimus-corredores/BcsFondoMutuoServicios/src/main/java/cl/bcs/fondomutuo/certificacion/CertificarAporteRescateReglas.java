package cl.bcs.fondomutuo.certificacion;

import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.cierre.FormatoMoneda;

/* RED SKELETON (F3 W3, T1 CER): the compiling shape the characterization tests expect (API_ASSUMED.md). The behaviour is NOT written yet: every entry point
 * fails closed with UnsupportedOperationException, so the CER tests are red until Step 2 replaces this body. Not registered as a bean, no handler, no list. */
/** Pure rules of F-B (FM-CER-18, 19, 23). */
public final class CertificarAporteRescateReglas {

    private CertificarAporteRescateReglas() {
    }

    public static java.sql.Date fechaCertificacionEfectiva(String fechaSistema, String fechaEntrada) {
        throw new UnsupportedOperationException("CertificarAporteRescateReglas.fechaCertificacionEfectiva: not written yet (F3 W3 T1, Step 2)");
    }

    public static boolean pendienteOAdvertencia(SolicitudProceso solicitud) {
        throw new UnsupportedOperationException("CertificarAporteRescateReglas.pendienteOAdvertencia: not written yet (F3 W3 T1, Step 2)");
    }

    public static List<SolicitudProceso> porTipoOrdenadas(List<SolicitudProceso> solicitudes, String tipoSolicitud) {
        throw new UnsupportedOperationException("CertificarAporteRescateReglas.porTipoOrdenadas: not written yet (F3 W3 T1, Step 2)");
    }

    public static void formatearMontos(FormatoMoneda formato, List<SolicitudProceso> solicitudes) throws Exception {
        throw new UnsupportedOperationException("CertificarAporteRescateReglas.formatearMontos: not written yet (F3 W3 T1, Step 2)");
    }
}
