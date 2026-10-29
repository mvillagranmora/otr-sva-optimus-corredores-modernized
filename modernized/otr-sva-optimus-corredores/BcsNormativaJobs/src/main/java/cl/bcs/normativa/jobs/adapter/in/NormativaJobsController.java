package cl.bcs.normativa.jobs.adapter.in;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;
import cl.bcs.normativa.jobs.service.apv.ReporteApvService;
import cl.bcs.normativa.jobs.service.carga.CargaService;
import cl.bcs.normativa.jobs.service.costos.ProcesarCostosService;
import cl.bcs.normativa.jobs.service.cuadratura.CuadraturaService;
import cl.bcs.normativa.jobs.service.declaraciones.CertificadosTributariosService;
import cl.bcs.normativa.jobs.service.exportacion.ExportacionService;
import cl.bcs.normativa.jobs.service.generacion.GeneracionService;
import cl.bcs.normativa.jobs.service.serv.ProcesarServService;
import cl.bcs.normativa.jobs.service.validacion.ValidacionService;
import cl.bcs.normativa.jobs.service.valorizar.ValorizarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST adapter for triggering normativa jobs.
 *
 * All endpoints accept a JSON body with:
 *   - jobId: long — BAT.Programa.IdPrograma for this run
 *   - parameters: Map&lt;String, String&gt; — job parameters from BAT.ParametrosPrograma
 *
 * Pattern follows BcsJobManager HTTP job dispatch.
 */
@RestController
@RequestMapping("/api/normativa/jobs")
public class NormativaJobsController {

    private static final Logger log = LoggerFactory.getLogger(NormativaJobsController.class);

    private final GeneracionService generacionService;
    private final ValidacionService validacionService;
    private final CargaService cargaService;
    private final CuadraturaService cuadraturaService;
    private final ExportacionService exportacionService;
    private final ProcesarServService procesarServService;
    private final ValorizarService valorizarService;
    private final CertificadosTributariosService certificadosService;
    private final ReporteApvService reporteApvService;
    private final ProcesarCostosService costoService;

    public NormativaJobsController(
            GeneracionService generacionService,
            ValidacionService validacionService,
            CargaService cargaService,
            CuadraturaService cuadraturaService,
            ExportacionService exportacionService,
            ProcesarServService procesarServService,
            ValorizarService valorizarService,
            CertificadosTributariosService certificadosService,
            ReporteApvService reporteApvService,
            ProcesarCostosService costoService) {
        this.generacionService = generacionService;
        this.validacionService = validacionService;
        this.cargaService = cargaService;
        this.cuadraturaService = cuadraturaService;
        this.exportacionService = exportacionService;
        this.procesarServService = procesarServService;
        this.valorizarService = valorizarService;
        this.certificadosService = certificadosService;
        this.reporteApvService = reporteApvService;
        this.costoService = costoService;
    }

    @PostMapping("/generacion")
    public ResponseEntity<ResultadoJob> generacion(@RequestBody JobRequest request) {
        ParametrosJob params = request.toParametrosJob();
        return ResponseEntity.ok(generacionService.ejecutar(params));
    }

    @PostMapping("/validacion")
    public ResponseEntity<ResultadoJob> validacion(@RequestBody JobRequest request) {
        ParametrosJob params = request.toParametrosJob();
        return ResponseEntity.ok(validacionService.ejecutar(params));
    }

    @PostMapping("/carga")
    public ResponseEntity<ResultadoJob> carga(@RequestBody JobRequest request) {
        ParametrosJob params = request.toParametrosJob();
        return ResponseEntity.ok(cargaService.ejecutar(params));
    }

    @PostMapping("/cuadratura")
    public ResponseEntity<ResultadoJob> cuadratura(@RequestBody JobRequest request) {
        ParametrosJob params = request.toParametrosJob();
        return ResponseEntity.ok(cuadraturaService.ejecutar(params));
    }

    @PostMapping("/exportacion")
    public ResponseEntity<ResultadoJob> exportacion(@RequestBody JobRequest request) {
        ParametrosJob params = request.toParametrosJob();
        return ResponseEntity.ok(exportacionService.ejecutar(params));
    }

    @PostMapping("/exportacion-ncg380")
    public ResponseEntity<ResultadoJob> exportacionNcg380(@RequestBody JobRequest request) {
        ParametrosJob params = request.toParametrosJob();
        return ResponseEntity.ok(exportacionService.ejecutarServ(params));
    }

    @PostMapping("/procesar-serv")
    public ResponseEntity<ResultadoJob> procesarServ(@RequestBody JobRequest request) {
        ParametrosJob params = request.toParametrosJob();
        return ResponseEntity.ok(procesarServService.ejecutar(params));
    }

    @PostMapping("/valorizar-ncg499")
    public ResponseEntity<ResultadoJob> valorizarNcg499(@RequestBody JobRequest request) {
        ParametrosJob params = request.toParametrosJob();
        return ResponseEntity.ok(valorizarService.valorizarNCG499(params));
    }

    @PostMapping("/valorizar-declaracion")
    public ResponseEntity<ResultadoJob> valorizarDeclaracion(@RequestBody JobRequest request) {
        ParametrosJob params = request.toParametrosJob();
        return ResponseEntity.ok(valorizarService.valorizarDeclaracion(params));
    }

    @PostMapping("/certificados-tributarios")
    public ResponseEntity<ResultadoJob> certificadosTributarios(@RequestBody JobRequest request) {
        ParametrosJob params = request.toParametrosJob();
        return ResponseEntity.ok(certificadosService.generarCertificados(params));
    }

    @PostMapping("/reporte-apv")
    public ResponseEntity<ResultadoJob> reporteApv(@RequestBody JobRequest request) {
        ParametrosJob params = request.toParametrosJob();
        return ResponseEntity.ok(reporteApvService.generarReporte(params));
    }

    @PostMapping("/cargar-datos-apv")
    public ResponseEntity<ResultadoJob> cargarDatosApv(@RequestBody JobRequest request) {
        ParametrosJob params = request.toParametrosJob();
        return ResponseEntity.ok(reporteApvService.cargarDatos(params));
    }

    @PostMapping("/procesar-costos")
    public ResponseEntity<ResultadoJob> procesarCostos(@RequestBody JobRequest request) {
        ParametrosJob params = request.toParametrosJob();
        return ResponseEntity.ok(costoService.ejecutar(params));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "normativa-jobs"));
    }

    /** HTTP request body for job dispatch. */
    public record JobRequest(long jobId, Map<String, String> parameters) {
        ParametrosJob toParametrosJob() {
            return new ParametrosJob(jobId, parameters != null ? parameters : Map.of());
        }
    }
}
