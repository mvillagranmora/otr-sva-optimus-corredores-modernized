package cl.bcs.fondomutuo.jobs.adapter.in;

import cl.bcs.fondomutuo.jobs.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller exposing Fondo Mutuo batch job endpoints.
 *
 * All endpoints accept job parameters as JSON body and return the service result.
 * Auth/session is handled at the gateway level.
 *
 * Port: 8107 (configurable via SERVER_PORT env var).
 */
@RestController
@RequestMapping("/ffmm-jobs")
public class JobController {

    private final AdministracionLiquidezJob               administracionLiquidezJob;
    private final BloqueoCierreSeriesJob                   bloqueoCierreSeriesJob;
    private final CargaMasivaSolicitudesJob                cargaMasivaSolicitudesJob;
    private final CargaOnlineAGFJob                        cargaOnlineAGFJob;
    private final CertificacionMasivaSolicitudesJob        certificacionMasivaSolicitudesJob;
    private final ConfirmarTraspasoJob                     confirmarTraspasoJob;
    private final GenerarArchivoAGFJob                     generarArchivoAGFJob;
    private final GenerarArchivosFFMMJob                   generarArchivosFFMMJob;
    private final GenerarArchivoSolicitudAporteRescateJob  generarArchivoSolicitudAporteRescateJob;
    private final GenerarCertificado31FFMMJob              generarCertificado31FFMMJob;
    private final GenerarInformeRentabilidadFFMMExtranjeroJob generarInformeRentabilidadJob;
    private final IngresarOrdenBonificacionFiscalReinversionJob ingresarOrdenBFReinversionJob;
    private final NotificarTraspasoJob                     notificarTraspasoJob;
    private final ProcesarComisionesNNJob                  procesarComisionesNNJob;
    private final TrxCFileManagerJob                       trxCFileManagerJob;
    private final TrxPFileManagerJob                       trxPFileManagerJob;
    private final ValorCuotaFileManagerJob                 valorCuotaFileManagerJob;
    private final ValorCuotaInternacionalFileManagerJob    valorCuotaInternacionalFileManagerJob;

    public JobController(
            AdministracionLiquidezJob administracionLiquidezJob,
            BloqueoCierreSeriesJob bloqueoCierreSeriesJob,
            CargaMasivaSolicitudesJob cargaMasivaSolicitudesJob,
            CargaOnlineAGFJob cargaOnlineAGFJob,
            CertificacionMasivaSolicitudesJob certificacionMasivaSolicitudesJob,
            ConfirmarTraspasoJob confirmarTraspasoJob,
            GenerarArchivoAGFJob generarArchivoAGFJob,
            GenerarArchivosFFMMJob generarArchivosFFMMJob,
            GenerarArchivoSolicitudAporteRescateJob generarArchivoSolicitudAporteRescateJob,
            GenerarCertificado31FFMMJob generarCertificado31FFMMJob,
            GenerarInformeRentabilidadFFMMExtranjeroJob generarInformeRentabilidadJob,
            IngresarOrdenBonificacionFiscalReinversionJob ingresarOrdenBFReinversionJob,
            NotificarTraspasoJob notificarTraspasoJob,
            ProcesarComisionesNNJob procesarComisionesNNJob,
            TrxCFileManagerJob trxCFileManagerJob,
            TrxPFileManagerJob trxPFileManagerJob,
            ValorCuotaFileManagerJob valorCuotaFileManagerJob,
            ValorCuotaInternacionalFileManagerJob valorCuotaInternacionalFileManagerJob) {

        this.administracionLiquidezJob              = administracionLiquidezJob;
        this.bloqueoCierreSeriesJob                 = bloqueoCierreSeriesJob;
        this.cargaMasivaSolicitudesJob              = cargaMasivaSolicitudesJob;
        this.cargaOnlineAGFJob                      = cargaOnlineAGFJob;
        this.certificacionMasivaSolicitudesJob      = certificacionMasivaSolicitudesJob;
        this.confirmarTraspasoJob                   = confirmarTraspasoJob;
        this.generarArchivoAGFJob                   = generarArchivoAGFJob;
        this.generarArchivosFFMMJob                 = generarArchivosFFMMJob;
        this.generarArchivoSolicitudAporteRescateJob = generarArchivoSolicitudAporteRescateJob;
        this.generarCertificado31FFMMJob            = generarCertificado31FFMMJob;
        this.generarInformeRentabilidadJob          = generarInformeRentabilidadJob;
        this.ingresarOrdenBFReinversionJob          = ingresarOrdenBFReinversionJob;
        this.notificarTraspasoJob                   = notificarTraspasoJob;
        this.procesarComisionesNNJob                = procesarComisionesNNJob;
        this.trxCFileManagerJob                     = trxCFileManagerJob;
        this.trxPFileManagerJob                     = trxPFileManagerJob;
        this.valorCuotaFileManagerJob               = valorCuotaFileManagerJob;
        this.valorCuotaInternacionalFileManagerJob  = valorCuotaInternacionalFileManagerJob;
    }

    // ── AdministracionLiquidez ───────────────────────────────────────────────

    @PostMapping("/administracion-liquidez")
    public ResponseEntity<Map<String, Object>> administracionLiquidez(@RequestBody Map<String, Object> body) {
        Long idEmpresa = toLong(body.get("idEmpresa"));
        Long idUsuario = toLong(body.get("idUsuario"));
        return ResponseEntity.ok(administracionLiquidezJob.ejecutar(idEmpresa, idUsuario));
    }

    // ── BloqueoCierreSeries ──────────────────────────────────────────────────

    @PostMapping("/bloqueo-cierre-series")
    public ResponseEntity<Map<String, Object>> bloqueoCierreSeries(@RequestBody Map<String, Object> body) {
        Long idEmpresa = toLong(body.get("idEmpresa"));
        Long idUsuario = toLong(body.get("idUsuario"));
        return ResponseEntity.ok(bloqueoCierreSeriesJob.ejecutar(idEmpresa, idUsuario));
    }

    // ── CargaMasivaSolicitudes ───────────────────────────────────────────────

    @PostMapping("/carga-masiva-solicitudes")
    public ResponseEntity<Map<String, Object>> cargaMasivaSolicitudes(@RequestBody Map<String, Object> body) {
        Long idEmpresa      = toLong(body.get("idEmpresa"));
        Long idUsuario      = toLong(body.get("idUsuario"));
        String transacciones = (String) body.get("transacciones");
        return ResponseEntity.ok(cargaMasivaSolicitudesJob.ejecutar(idEmpresa, idUsuario, transacciones));
    }

    // ── CargaOnlineAGF ───────────────────────────────────────────────────────

    @PostMapping("/carga-online-agf")
    public ResponseEntity<List<Map<String, Object>>> cargaOnlineAGF(@RequestBody Map<String, Object> body) {
        Long idEmpresa = toLong(body.get("idEmpresa"));
        Long idUsuario = toLong(body.get("idUsuario"));
        return ResponseEntity.ok(cargaOnlineAGFJob.ejecutar(idEmpresa, idUsuario));
    }

    // ── CertificacionMasivaSolicitudes ───────────────────────────────────────

    @PostMapping("/certificacion-masiva-solicitudes")
    public ResponseEntity<Map<String, Object>> certificacionMasiva(@RequestBody Map<String, Object> body) {
        Long   idEmpresa              = toLong(body.get("idEmpresa"));
        Long   idUsuario              = toLong(body.get("idUsuario"));
        String simularOperacion       = str(body, "simularOperacion");
        String usaReglasNegocio       = str(body, "usaReglasNegocio");
        String fechaCertificacion     = str(body, "fechaCertificacion");
        String esMasivo               = str(body, "esMasivo");
        String idInternoFiltro        = str(body, "idInternoFiltro");
        String idEmisor               = str(body, "idEmisor");
        String idMoneda               = str(body, "idMoneda");
        String idSerieFondo           = str(body, "idSerieFondo");
        String nacional               = str(body, "nacional");
        String tipoSolicitud          = str(body, "tipoSolicitud");
        String idTipoMovimientoFondoMutuo = str(body, "idTipoMovimientoFondoMutuo");
        String moneyMarket            = str(body, "moneyMarket");
        String idCarga                = str(body, "idCarga");
        String idSolicitudes          = str(body, "idSolicitudes");

        return ResponseEntity.ok(certificacionMasivaSolicitudesJob.ejecutar(
                idEmpresa, idUsuario, simularOperacion, usaReglasNegocio, fechaCertificacion,
                esMasivo, idInternoFiltro, idEmisor, idMoneda, idSerieFondo, nacional,
                tipoSolicitud, idTipoMovimientoFondoMutuo, moneyMarket, idCarga, idSolicitudes));
    }

    // ── ConfirmarTraspaso ────────────────────────────────────────────────────

    @PostMapping("/confirmar-traspaso")
    public ResponseEntity<Map<String, Object>> confirmarTraspaso(@RequestBody Map<String, Object> body) {
        Long idEmpresa = toLong(body.get("idEmpresa"));
        Long idUsuario = toLong(body.get("idUsuario"));
        return ResponseEntity.ok(confirmarTraspasoJob.ejecutar(idEmpresa, idUsuario));
    }

    // ── GenerarArchivoAGF ────────────────────────────────────────────────────

    @PostMapping("/generar-archivo-agf")
    public ResponseEntity<Map<String, Object>> generarArchivoAGF(@RequestBody Map<String, Object> body) {
        Long   jobId      = toLong(body.get("jobId"));
        Long   idEmpresa  = toLong(body.get("idEmpresa"));
        Long   idUsuario  = toLong(body.get("idUsuario"));
        String idModulo   = str(body, "idModulo");
        String fechaDesde = str(body, "fechaDesde");
        String fechaHasta = str(body, "fechaHasta");
        String idAmbiente = str(body, "idAmbiente");
        return ResponseEntity.ok(generarArchivoAGFJob.ejecutar(
                jobId, idEmpresa, idUsuario, idModulo, fechaDesde, fechaHasta, idAmbiente));
    }

    // ── GenerarArchivosFFMM ──────────────────────────────────────────────────

    @PostMapping("/generar-archivos-ffmm")
    public ResponseEntity<Map<String, Object>> generarArchivosFFMM(@RequestBody Map<String, Object> body) {
        Long   jobId                = toLong(body.get("jobId"));
        Long   idEmpresa            = toLong(body.get("idEmpresa"));
        Long   idUsuario            = toLong(body.get("idUsuario"));
        String fechaCurse           = str(body, "fechaCurse");
        String estadoMovimiento     = str(body, "estadoMovimiento");
        String idArchivoFondoMutuo  = str(body, "idArchivoFondoMutuo");
        String idMoneda             = str(body, "idMoneda");
        String fondoMutuo           = str(body, "fondoMutuo");
        String tipoSolicitud        = str(body, "tipoSolicitud");
        String idEmisor             = str(body, "idEmisor");
        return ResponseEntity.ok(generarArchivosFFMMJob.ejecutar(
                jobId, idEmpresa, idUsuario, fechaCurse, estadoMovimiento,
                idArchivoFondoMutuo, idMoneda, fondoMutuo, tipoSolicitud, idEmisor));
    }

    // ── GenerarArchivoSolicitudAporteRescate ─────────────────────────────────

    @PostMapping("/generar-archivo-solicitud-aporte-rescate")
    public ResponseEntity<Map<String, Object>> generarArchivoSolicitud(@RequestBody Map<String, Object> body) {
        Long   jobId      = toLong(body.get("jobId"));
        Long   idEmpresa  = toLong(body.get("idEmpresa"));
        Long   idUsuario  = toLong(body.get("idUsuario"));
        String idModulo   = str(body, "idModulo");
        String fechaDesde = str(body, "fechaDesde");
        String fechaHasta = str(body, "fechaHasta");
        @SuppressWarnings("unchecked")
        Map<String, Object> filtros = (Map<String, Object>) body.getOrDefault("filtros", Map.of());
        return ResponseEntity.ok(generarArchivoSolicitudAporteRescateJob.ejecutar(
                jobId, idEmpresa, idUsuario, idModulo, fechaDesde, fechaHasta, filtros));
    }

    // ── GenerarCertificado31FFMM ─────────────────────────────────────────────

    @PostMapping("/generar-certificado-31-ffmm")
    public ResponseEntity<Map<String, Object>> generarCertificado31(@RequestBody Map<String, Object> body) {
        Long   jobId              = toLong(body.get("jobId"));
        Long   idEmpresa          = toLong(body.get("idEmpresa"));
        Long   idUsuario          = toLong(body.get("idUsuario"));
        String idModulo           = str(body, "idModulo");
        String ciudadCertificado  = str(body, "ciudadCertificado");
        String fechaCertificado   = str(body, "fechaCertificado");
        String listaTraspasos     = str(body, "listaTraspasos");
        return ResponseEntity.ok(generarCertificado31FFMMJob.ejecutar(
                jobId, idEmpresa, idUsuario, idModulo, ciudadCertificado, fechaCertificado, listaTraspasos));
    }

    // ── GenerarInformeRentabilidadFFMMExtranjero ─────────────────────────────

    @PostMapping("/generar-informe-rentabilidad-ffmm-extranjero")
    public ResponseEntity<Map<String, Object>> generarInformeRentabilidad(@RequestBody Map<String, Object> body) {
        Long   jobId         = toLong(body.get("jobId"));
        Long   idEmpresa     = toLong(body.get("idEmpresa"));
        Long   idUsuario     = toLong(body.get("idUsuario"));
        String idModulo      = str(body, "idModulo");
        String agnoComercial = str(body, "agnoComercial");
        String esMasivo      = str(body, "esMasivo");
        Long   idCliente     = toLong(body.getOrDefault("idCliente", 0L));
        return ResponseEntity.ok(generarInformeRentabilidadJob.ejecutar(
                jobId, idEmpresa, idUsuario, idModulo, agnoComercial, esMasivo, idCliente));
    }

    // ── IngresarOrdenBonificacionFiscalReinversion ───────────────────────────

    @PostMapping("/ingresar-orden-bonificacion-fiscal-reinversion")
    public ResponseEntity<Void> ingresarOrdenBFReinversion(@RequestBody Map<String, Object> body) {
        Long jobId = toLong(body.get("jobId"));
        ingresarOrdenBFReinversionJob.ejecutar(jobId);
        return ResponseEntity.ok().build();
    }

    // ── NotificarTraspaso ────────────────────────────────────────────────────

    @PostMapping("/notificar-traspaso")
    public ResponseEntity<Void> notificarTraspaso(@RequestBody Map<String, Object> body) {
        Long idEmpresa = toLong(body.get("idEmpresa"));
        Long idUsuario = toLong(body.get("idUsuario"));
        notificarTraspasoJob.ejecutar(idEmpresa, idUsuario);
        return ResponseEntity.ok().build();
    }

    // ── ProcesarComisionesNN ─────────────────────────────────────────────────

    @PostMapping("/procesar-comisiones-nn")
    public ResponseEntity<Map<String, Object>> procesarComisionesNN(@RequestBody Map<String, Object> body) {
        Long   idEmpresa  = toLong(body.get("idEmpresa"));
        Long   idUsuario  = toLong(body.get("idUsuario"));
        String idModulo   = str(body, "idModulo");
        String fechaDesde = str(body, "fechaDesde");
        String fechaHasta = str(body, "fechaHasta");
        String comisiones = str(body, "comisiones");
        return ResponseEntity.ok(procesarComisionesNNJob.ejecutar(
                idEmpresa, idUsuario, idModulo, fechaDesde, fechaHasta, comisiones));
    }

    // ── TrxCFileManager ─────────────────────────────────────────────────────

    @PostMapping("/trx-c-file-manager")
    public ResponseEntity<Map<String, Object>> trxCFileManager(@RequestBody Map<String, Object> body) {
        Long idEmpresa = toLong(body.get("idEmpresa"));
        Long idUsuario = toLong(body.get("idUsuario"));
        return ResponseEntity.ok(trxCFileManagerJob.ejecutar(idEmpresa, idUsuario));
    }

    // ── TrxPFileManager ─────────────────────────────────────────────────────

    @PostMapping("/trx-p-file-manager")
    public ResponseEntity<Map<String, Object>> trxPFileManager(@RequestBody Map<String, Object> body) {
        Long idEmpresa = toLong(body.get("idEmpresa"));
        Long idUsuario = toLong(body.get("idUsuario"));
        return ResponseEntity.ok(trxPFileManagerJob.ejecutar(idEmpresa, idUsuario));
    }

    // ── ValorCuotaFileManager ────────────────────────────────────────────────

    @PostMapping("/valor-cuota-file-manager")
    public ResponseEntity<Map<String, Object>> valorCuotaFileManager(@RequestBody Map<String, Object> body) {
        Long idEmpresa = toLong(body.get("idEmpresa"));
        Long idUsuario = toLong(body.get("idUsuario"));
        return ResponseEntity.ok(valorCuotaFileManagerJob.ejecutar(idEmpresa, idUsuario));
    }

    // ── ValorCuotaInternacionalFileManager ───────────────────────────────────

    @PostMapping("/valor-cuota-internacional-file-manager")
    public ResponseEntity<Map<String, Object>> valorCuotaInternacionalFileManager(
            @RequestBody Map<String, Object> body) {
        Long idEmpresa = toLong(body.get("idEmpresa"));
        Long idUsuario = toLong(body.get("idUsuario"));
        return ResponseEntity.ok(valorCuotaInternacionalFileManagerJob.ejecutar(idEmpresa, idUsuario));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(value.toString());
    }

    private String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v != null ? v.toString() : "";
    }
}
