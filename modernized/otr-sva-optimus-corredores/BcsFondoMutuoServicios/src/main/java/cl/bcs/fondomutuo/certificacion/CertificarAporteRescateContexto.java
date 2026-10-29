package cl.bcs.fondomutuo.certificacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Estado;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.InSolicitudes;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.IndicadorPortafolio;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Persona;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Salida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Traspaso;

/* RED SKELETON (F3 W3, T1 CER): the compiling shape the characterization tests expect (API_ASSUMED.md). The behaviour is NOT written yet: every entry point
 * fails closed with UnsupportedOperationException, so the CER tests are red until Step 2 replaces this body. Not registered as a bean, no handler, no list. */
/** What F-A and F-B leave for the next phases: public mutable fields (the legacy mutates the same objects in place). */
public final class CertificarAporteRescateContexto {

    public Entrada entrada;
    public Long paisSistema;
    public String fechaSistema;
    public String tipoComprobanteFacturaNoApv;
    public String tipoComprobanteFacturaApv;
    public String calcularComisionCertificacion;
    public String validarCertificacion;
    public String rescateGeneraVentaSpotDolar;
    public String comisionComprobanteFacturacionAutomatico;
    public String notificaOWP;
    public Long porcentajeRetencion;
    public List<SolicitudProceso> solicitudes = new ArrayList<>();
    public List<SolicitudProceso> aportes = new ArrayList<>();
    public List<SolicitudProceso> rescates = new ArrayList<>();
    public List<InSolicitudes> aportesSwitch = new ArrayList<>();
    public Map<String, Estado> estados;
    public Map<Long, Traspaso> traspasos;
    public Persona personaTGR;
    public Persona personaSII;
    public Map<Long, IndicadorPortafolio> indicadoresPortafolio;
    public java.sql.Date fechaCertificacion;
    public Salida salida;
}
