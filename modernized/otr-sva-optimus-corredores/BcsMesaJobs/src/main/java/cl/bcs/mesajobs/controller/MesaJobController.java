package cl.bcs.mesajobs.controller;

import cl.bcs.mesajobs.domain.JobParams;
import cl.bcs.mesajobs.port.out.MesaDataPort;
import cl.bcs.mesajobs.service.LiberaBloqueoService;
import cl.bcs.mesajobs.service.RowFormatterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * B-MES-01: Un endpoint por job de BcsMesaJobs.
 * Patrón: POST /api/jobs/{nombre}/ejecutar con body { params }.
 * Todos los endpoints que llaman a HANA retornan 503 (UnsupportedOperationException → GlobalExceptionHandler).
 */
@RestController
@RequestMapping("/api/jobs")
public class MesaJobController {

    // B-MES-06: Nombres exactos de SPs HANA (pinned)
    public static final String SP_ANEXO_IX         = "BSCL.Mesa.BD.Servicios::GenerarAnexoIX";
    public static final String SP_ANEXO_XIX        = "BSCL.Mesa.BD.Servicios::GenerarAnexoXIX";
    public static final String SP_CCCCMMDD        = "BSCL.Mesa.BD.Servicios::GenerarArchivoCCccmmdd";
    public static final String SP_DEPOSITOS        = "BSCL.Mesa.BD.Servicios::GenerarArchivoDepositosYAutodesembolsos";
    public static final String SP_CARTERA_PROPIA   = "BSCL.Mesa.BD.Servicios::GenerarArchivoInformeCarteraCuentaPropia";
    public static final String SP_OMA              = "BSCL.Mesa.BD.Servicios::GenerarArchivoOMA";
    public static final String SP_PACCMMDD        = "BSCL.Mesa.BD.Servicios::GenerarArchivoPAccmmdd";
    public static final String SP_SALDOS_CAMBIO    = "BSCL.Mesa.BD.Servicios::GenerarInformeSaldosCuentaCambio";
    public static final String SP_UTILIDAD_PERDIDA = "BSCL.Mesa.BD.Servicios::GenerarInformeUtilidadPerdida";
    public static final String SP_REPORTE_MENSUAL  = "BSCL.Mesa.BD.Servicios::GenerarReporteMensualOperaciones";
    public static final String SP_PUNTASDATATEC    = "BSCL.Mesa.BD.Servicios::PuntasDatatec";
    public static final String SP_CARGA_MASIVA     = "BSCL.Mesa.BD.Servicios::ProcesarCargaMasivaArchivoOperacionTermino";
    public static final String SP_FORWARD          = "BSCL.Mesa.BD.Servicios::ValorizacionForward";

    private final MesaDataPort dataPort;

    public MesaJobController(MesaDataPort dataPort) {
        this.dataPort = dataPort;
    }

    // ── Jobs generadores de archivo (mismo patrón, SP vía param "servicio") ──

    @PostMapping("/generar-archivo-anexo-ix/ejecutar")
    public ResponseEntity<Map<String, Object>> generarAnexoIX(@RequestBody Map<String, String> body) {
        return ejecutarJobArchivo(SP_ANEXO_IX, body);
    }

    @PostMapping("/generar-archivo-anexo-xix/ejecutar")
    public ResponseEntity<Map<String, Object>> generarAnexoXIX(@RequestBody Map<String, String> body) {
        return ejecutarJobArchivo(SP_ANEXO_XIX, body);
    }

    @PostMapping("/generar-archivo-ccccmmdd/ejecutar")
    public ResponseEntity<Map<String, Object>> generarCCccmmdd(@RequestBody Map<String, String> body) {
        return ejecutarJobArchivo(SP_CCCCMMDD, body);
    }

    @PostMapping("/generar-archivo-depositos-y-autodesembolsos/ejecutar")
    public ResponseEntity<Map<String, Object>> generarDepositos(@RequestBody Map<String, String> body) {
        return ejecutarJobArchivo(SP_DEPOSITOS, body);
    }

    @PostMapping("/generar-archivo-informe-cartera-cuenta-propia/ejecutar")
    public ResponseEntity<Map<String, Object>> generarCarteraPropia(@RequestBody Map<String, String> body) {
        return ejecutarJobArchivo(SP_CARTERA_PROPIA, body);
    }

    @PostMapping("/generar-archivo-oma/ejecutar")
    public ResponseEntity<Map<String, Object>> generarOMA(@RequestBody Map<String, String> body) {
        return ejecutarJobArchivo(SP_OMA, body);
    }

    @PostMapping("/generar-archivo-paccmmdd/ejecutar")
    public ResponseEntity<Map<String, Object>> generarPAccmmdd(@RequestBody Map<String, String> body) {
        return ejecutarJobArchivo(SP_PACCMMDD, body);
    }

    @PostMapping("/generar-informe-saldos-cuenta-cambio/ejecutar")
    public ResponseEntity<Map<String, Object>> generarSaldosCambio(@RequestBody Map<String, String> body) {
        return ejecutarJobArchivo(SP_SALDOS_CAMBIO, body);
    }

    @PostMapping("/generar-informe-utilidad-perdida/ejecutar")
    public ResponseEntity<Map<String, Object>> generarUtilidadPerdida(@RequestBody Map<String, String> body) {
        return ejecutarJobArchivo(SP_UTILIDAD_PERDIDA, body);
    }

    @PostMapping("/generar-reporte-mensual-operaciones/ejecutar")
    public ResponseEntity<Map<String, Object>> generarReporteMensual(@RequestBody Map<String, String> body) {
        return ejecutarJobArchivo(SP_REPORTE_MENSUAL, body);
    }

    @PostMapping("/puntasdatatec/ejecutar")
    public ResponseEntity<Map<String, Object>> puntasDatatec(@RequestBody Map<String, String> body) {
        return ejecutarJobArchivo(SP_PUNTASDATATEC, body);
    }

    @PostMapping("/procesar-carga-masiva-archivo-operacion-termino/ejecutar")
    public ResponseEntity<Map<String, Object>> procesarCargaMasiva(@RequestBody Map<String, String> body) {
        return ejecutarJobArchivo(SP_CARGA_MASIVA, body);
    }

    @PostMapping("/valorizacion-forward/ejecutar")
    public ResponseEntity<Map<String, Object>> valorizacionForward(@RequestBody Map<String, String> body) {
        return ejecutarJobArchivo(SP_FORWARD, body);
    }

    // ── LiberaBloqueoEdicionPactos — lógica mixta (pura + HANA) ──

    @PostMapping("/libera-bloqueo-edicion-pactos/ejecutar")
    public ResponseEntity<Map<String, Object>> liberaBloqueo(@RequestBody Map<String, String> body) {
        String idEmpresaStr = Objects.requireNonNull(body.get("idEmpresa"), "idEmpresa requerido");
        long idEmpresa = Long.parseLong(idEmpresaStr);

        // B-MES-07: guard puro antes de llamar a HANA
        String codigoUsuario = body.get("codigoUsuario");
        long idUsuario = LiberaBloqueoService.esCodigoUsuarioVacio(codigoUsuario)
            ? 0L
            : dataPort.buscarIdUsuarioPorCodigo(idEmpresa, codigoUsuario); // → 503 si HANA no disponible

        // Requiere HANA para obtener y liberar operaciones → 503
        var operaciones = dataPort.buscarOperacionesBloqueadas(idEmpresa);
        int liberadas = dataPort.liberarOperaciones(idEmpresa,
            operaciones.stream().map(o -> o.idOperacion()).toList());

        return ResponseEntity.ok(Map.of(
            "operacionesLiberadas", liberadas,
            "idUsuario", idUsuario
        ));
    }

    // ── Helper interno ──

    private ResponseEntity<Map<String, Object>> ejecutarJobArchivo(String sp, Map<String, String> body) {
        // B-MES-02: validar params requeridos
        long idEmpresa    = Long.parseLong(Objects.requireNonNull(body.get("idEmpresa"), "idEmpresa requerido"));
        String fechaDesde  = Objects.requireNonNull(body.get("fechaDesde"), "fechaDesde requerido");
        String fechaHasta  = Objects.requireNonNull(body.get("fechaHasta"), "fechaHasta requerido");
        String ruta        = Objects.requireNonNull(body.get("rutaArchivo"), "rutaArchivo requerido");
        String nombre      = Objects.requireNonNull(body.get("nombreArchivo"), "nombreArchivo requerido");
        String formato     = Objects.requireNonNull(body.get("formatoArchivo"), "formatoArchivo requerido");

        if (fechaDesde.isBlank()) throw new IllegalArgumentException("fechaDesde no puede ser vacío");
        if (fechaHasta.isBlank()) throw new IllegalArgumentException("fechaHasta no puede ser vacío");
        if (ruta.isBlank())       throw new IllegalArgumentException("rutaArchivo no puede ser vacío");
        if (nombre.isBlank())     throw new IllegalArgumentException("nombreArchivo no puede ser vacío");

        var params = new JobParams(idEmpresa, fechaDesde, fechaHasta, ruta, nombre, formato, sp);

        // B-MES-04/05: formato determina cómo se construye el archivo
        char sep = RowFormatterService.esCsv(formato) ? RowFormatterService.separadorCsv() : '\0';

        // Requiere HANA → 503
        List<List<String>> filas = dataPort.ejecutarSpJob(sp, params);

        return ResponseEntity.ok(Map.of(
            "servicio", sp,
            "formato", formato,
            "filas", filas.size()
        ));
    }
}
