package cl.bcs.plataforma.jobs.service.archivos;

import org.springframework.stereotype.Service;

/**
 * Stub service for file-management and extractor jobs.
 *
 * Legacy classes stubbed:
 *   - CargarDiasFeriados (connects to external Ruebas DB + HANA)
 *   - CargarDiasFeriadosJob
 *   - ExtractorArchivosJob / FileExtractor (SFTP extraction)
 *   - FileManagerJob (file management operations)
 *   - ExtraerPreciosRuebasDB (Ruebas external DB + HANA)
 *   - GenerarDocumentosAutomaticos (Crystal Reports + HANA)
 *   - GenerarCartolaEnvioAutomatico (reports + email)
 *   - GenerarInformeParametrosSistemaJob
 *   - ExportarGrillaCsv
 *   - ExportarPerfilesJob
 *   - ExportarRolesJob
 *   - ProcesadorArchivosJob
 */
@Service
public class OtrosArchivosStubs {

    private static final String MSG =
            " requires a live HANA connection or external service — "
            + "annotate test with @Disabled(\"requires HANA\")";

    /** legacy: CargarDiasFeriados + CargarDiasFeriadosJob */
    public void cargarDiasFeriados(Long idEmpresa, Long idUsuario) {
        throw new UnsupportedOperationException("CargarDiasFeriados" + MSG);
    }

    /** legacy: ExtractorArchivosJob / FileExtractor */
    public void extractorArchivos(Long idEmpresa, String rutaOrigen, String rutaDestino) {
        throw new UnsupportedOperationException("ExtractorArchivosJob" + MSG);
    }

    /** legacy: FileManagerJob */
    public void fileManager(Long idEmpresa) {
        throw new UnsupportedOperationException("FileManagerJob" + MSG);
    }

    /** legacy: ExtraerPreciosRuebasDB */
    public void extraerPreciosRuebasDB(Long idEmpresa, String fechaSistema) {
        throw new UnsupportedOperationException("ExtraerPreciosRuebasDB" + MSG);
    }

    /** legacy: GenerarDocumentosAutomaticos */
    public void generarDocumentosAutomaticos(Long idEmpresa, Long idUsuario, String fechaSistema) {
        throw new UnsupportedOperationException("GenerarDocumentosAutomaticos" + MSG);
    }

    /** legacy: GenerarCartolaEnvioAutomatico */
    public void generarCartolaEnvioAutomatico(Long idEmpresa, Long idUsuario, String fechaSistema) {
        throw new UnsupportedOperationException("GenerarCartolaEnvioAutomatico" + MSG);
    }

    /** legacy: GenerarInformeParametrosSistemaJob */
    public void generarInformeParametrosSistema(Long idEmpresa) {
        throw new UnsupportedOperationException("GenerarInformeParametrosSistemaJob" + MSG);
    }

    /** legacy: ExportarGrillaCsv */
    public void exportarGrillaCsv(Long idEmpresa, String idReporte) {
        throw new UnsupportedOperationException("ExportarGrillaCsv" + MSG);
    }

    /** legacy: ExportarPerfilesJob */
    public void exportarPerfiles(Long idEmpresa) {
        throw new UnsupportedOperationException("ExportarPerfilesJob" + MSG);
    }

    /** legacy: ExportarRolesJob */
    public void exportarRoles(Long idEmpresa) {
        throw new UnsupportedOperationException("ExportarRolesJob" + MSG);
    }

    /** legacy: ProcesadorArchivosJob */
    public void procesadorArchivos(Long idEmpresa, String rutaArchivos) {
        throw new UnsupportedOperationException("ProcesadorArchivosJob" + MSG);
    }
}
