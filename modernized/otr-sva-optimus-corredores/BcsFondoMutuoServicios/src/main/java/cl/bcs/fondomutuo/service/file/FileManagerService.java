package cl.bcs.fondomutuo.service.file;

import cl.bcs.fondomutuo.port.out.HanaFondoMutuoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Manages file generation operations for mutual funds.
 *
 * Operations mapped from legacy services:
 *   - TrxFileManager                     → HANA SP (stub)
 *   - TrxCFileManager                    → HANA SP (stub)
 *   - TrxPFileManager                    → HANA SP (stub)
 *   - ValorCuotaFileManager              → HANA SP (stub)
 *   - ValorCuotaInternacionalFileManager → HANA SP (stub)
 *   - InventarioFileManager              → HANA SP (stub)
 *   - GenerarArchivoMovimientos          → HANA SP (stub)
 *   - GenerarArchivoParticipes           → HANA SP (stub)
 *   - GenerarArchivoSolicitudAporteRescate → HANA SP (stub)
 *   - CargarArchivoCuadraturaNacionalUI  → HANA SP (stub)
 *   - CargarArchivoCuadraturaInternacionalUI → HANA SP (stub)
 *   - ProcesarArchivoCuadraturaNacional  → HANA SP (stub)
 *   - ProcesarArchivoCuadraturaInternacional → HANA SP (stub)
 *   - GenerarCertificado31Masivo         → Crystal Reports (stub, @CrystalReportsPort)
 *   - GenerarDataReporteTRXI             → HANA SP (stub)
 *   - GenerarDataReporteTRXIFolioContrato → HANA SP (stub)
 *   - GenerarInformeRentabilidadFFMMExtranjeroMasivo → HANA SP (stub)
 *   - SimulaCalculoSpreadRescate         → delegates to CalcularSpreadRescateService
 */
@Service
public class FileManagerService {

    private static final Logger log = LoggerFactory.getLogger(FileManagerService.class);

    private final HanaFondoMutuoPort hanaPort;

    public FileManagerService(HanaFondoMutuoPort hanaPort) {
        this.hanaPort = hanaPort;
    }

    public Map<String, Object> generarArchivoMovimientos(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_GENERAR_ARCHIVO_MOVIMIENTOS", params);
    }

    public Map<String, Object> generarArchivoParticipes(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_GENERAR_ARCHIVO_PARTICIPES", params);
    }

    public Map<String, Object> procesarArchivoCuadratura(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        Objects.requireNonNull(params.get("idEmpresa"), "idEmpresa");
        return hanaPort.callStoredProcedure("BCS", "BCS_FM_PROCESAR_ARCHIVO_CUADRATURA", params);
    }

    /**
     * Certificate generation via Crystal Reports is not available in this runtime.
     * Returns a graceful error response instead of throwing — callers must check "error" key.
     */
    public Map<String, Object> generarCertificado31Masivo(Map<String, Object> params) {
        Objects.requireNonNull(params, "params");
        log.warn("generarCertificado31Masivo: Crystal Reports engine not available in Spring Boot runtime; returning graceful error");
        return Map.of(
            "error", "CRYSTAL_REPORTS_NOT_AVAILABLE",
            "mensaje", "La generación del Certificado 31 masivo requiere Crystal Reports y no está disponible en esta instancia. Use el servicio TomEE legado hasta completar la migración."
        );
    }
}
