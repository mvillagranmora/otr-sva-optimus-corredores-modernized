package cl.bcs.fondomutuo.service.solicitud;

import cl.bcs.fondomutuo.port.out.HanaFondoMutuoPort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Orchestrates lifecycle operations on solicitudes de aporte/rescate (mutual fund orders).
 *
 * Operations mapped from legacy services:
 *   - CrearSolicitudAporteRescateNormal   → HANA SP (stub)
 *   - CrearSolicitudAportePorDividendos   → HANA SP (stub)
 *   - CrearSolicitudAporteOvernight       → HANA SP (stub)
 *   - CrearSolicitudAporteReinversion     → HANA SP (stub)
 *   - CrearSolicitudRescateNormalPorCliente → HANA SP (stub)
 *   - CertificarSolicitudAporteRescate    → HANA SP (stub)
 *   - AnulacionSolicitudAporteRescate     → HANA SP (stub)
 *   - LiquidacionSolicitudAporteRescate   → HANA SP (stub)
 *   - ModificarMontoNumeroCuotasSolicitud → HANA SP (stub)
 *   - ConsultarSolicitudAporteRescateUI   → HANA SP (stub)
 *   - ConsultarSolicitudesFFMM            → HANA SP (stub)
 *   - ConsultarDetalleSolicitud           → HANA SP (stub)
 *   - ConsultarFechaSolicitudAporteRescate→ HANA SP (stub)
 *   - ServicioCumpleRequisitosCrearSolicitudAporteRescate → HANA SP (stub)
 *   - ManejadorSolicitudAporteRescate     → HANA SP (stub)
 *   - ProcesarCargaSolicitudesAporteRescate → HANA SP (stub)
 *   - GenerarArchivoSolicitudAporteRescate → HANA SP (stub)
 *   - CertificarPorFiltrosSolicitudAporteRescate → HANA SP (stub)
 *   - ConsultarSolicitudesCertificacionMasiva → HANA SP (stub)
 *   - AgregarLogCertificacionAporteRescate → HANA SP (stub)
 *   - CambiarEstadoSolicitudAgf           → HANA SP (stub)
 *   - ValidaMontoInversion                → HANA SP (stub)
 */
@Service
public class SolicitudAporteRescateService {

    private final HanaFondoMutuoPort hanaPort;

    public SolicitudAporteRescateService(HanaFondoMutuoPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    /**
     * Creates a normal aporte/rescate solicitud.
     * Delegates to HANA stored procedure BCS_FM_CREAR_SOLICITUD_APORTE_RESCATE_NORMAL.
     */
    public Map<String, Object> crearSolicitudNormal(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CREAR_SOLICITUD_APORTE_RESCATE_NORMAL", params);
    }

    /**
     * Certifies a solicitud de aporte/rescate.
     * Delegates to HANA stored procedure BCS_FM_CERTIFICAR_SOLICITUD_APORTE_RESCATE.
     */
    public Map<String, Object> certificarSolicitud(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_CERTIFICAR_SOLICITUD_APORTE_RESCATE", params);
    }

    /**
     * Annuls a solicitud de aporte/rescate.
     * Delegates to HANA stored procedure BCS_FM_ANULAR_SOLICITUD_APORTE_RESCATE.
     */
    public Map<String, Object> anularSolicitud(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_ANULAR_SOLICITUD_APORTE_RESCATE", params);
    }

    /**
     * Liquidates a solicitud de aporte/rescate.
     * Delegates to HANA stored procedure BCS_FM_LIQUIDAR_SOLICITUD_APORTE_RESCATE.
     */
    public Map<String, Object> liquidarSolicitud(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_LIQUIDAR_SOLICITUD_APORTE_RESCATE", params);
    }
}
