package cl.bcs.replica.controller;

import cl.bcs.replica.domain.EntradaIniciarReplica;
import cl.bcs.replica.domain.SalidaIniciarReplica;
import cl.bcs.replica.domain.Workstack;
import cl.bcs.replica.port.out.ReplicaDataPort;
import cl.bcs.replica.service.ReplicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Objects;

/**
 * REST controller for replica operations.
 *
 * POST /api/replica/full   — initiates Full replica (QueryReplica)
 * POST /api/replica/delta  — initiates Delta replica (QueryReplicaDelta)
 * GET  /api/replica/cod-per/{fechaSistema} — returns year-month portion
 * POST /api/replica/respaldo — triggers Sebra backup if config == "S"
 */
@RestController
@RequestMapping("/api/replica")
public class ReplicaController {

    private static final String TABLA_WORKSTACK   = "INICIAR_REPLICA_PAUTA";
    private static final String ACTION_FULL       = "F";
    private static final String QUERY_FULL        = "QueryReplica";
    private static final String QUERY_DELTA       = "QueryReplicaDelta";

    private final ReplicaDataPort dataPort;

    public ReplicaController(ReplicaDataPort dataPort) {
        this.dataPort = dataPort;
    }

    /**
     * B-REP-02: Full replica — uses "QueryReplica".
     */
    @PostMapping("/full")
    public ResponseEntity<SalidaIniciarReplica> iniciarFull(
            @RequestBody EntradaIniciarReplica entrada) {
        Objects.requireNonNull(entrada.idEmpresa(), "idEmpresa is required");
        Objects.requireNonNull(entrada.idUsuario(),  "idUsuario is required");

        String fecha      = dataPort.obtenerFechaSistema(entrada.idEmpresa());
        String parameters = ReplicaService.buildParametersString(QUERY_FULL, fecha);

        Workstack ws = new Workstack(
                null,
                "REPLICA_FULL",
                TABLA_WORKSTACK,
                ACTION_FULL,
                0,
                Instant.now(),
                parameters
        );
        dataPort.insertarWorkstack(ws);

        return ResponseEntity.ok(new SalidaIniciarReplica(0L, "Full replica iniciada"));
    }

    /**
     * B-REP-02: Delta replica — uses "QueryReplicaDelta".
     */
    @PostMapping("/delta")
    public ResponseEntity<SalidaIniciarReplica> iniciarDelta(
            @RequestBody EntradaIniciarReplica entrada) {
        Objects.requireNonNull(entrada.idEmpresa(), "idEmpresa is required");
        Objects.requireNonNull(entrada.idUsuario(),  "idUsuario is required");

        String fecha      = dataPort.obtenerFechaSistema(entrada.idEmpresa());
        String parameters = ReplicaService.buildParametersString(QUERY_DELTA, fecha);

        Workstack ws = new Workstack(
                null,
                "REPLICA_DELTA",
                TABLA_WORKSTACK,
                ACTION_FULL,
                0,
                Instant.now(),
                parameters
        );
        dataPort.insertarWorkstack(ws);

        return ResponseEntity.ok(new SalidaIniciarReplica(0L, "Delta replica iniciada"));
    }

    /**
     * B-REP-04: Returns COD_PER (year-month) from a fechaSistema path variable.
     */
    @GetMapping("/cod-per/{fechaSistema}")
    public ResponseEntity<String> obtenerCodPer(@PathVariable String fechaSistema) {
        Objects.requireNonNull(fechaSistema, "fechaSistema is required");
        return ResponseEntity.ok(ReplicaService.extractCodPer(fechaSistema));
    }

    /**
     * B-REP-03: Triggers Sebra backup only when config param == "S".
     * When active, delegates to adapter which throws UnsupportedOperationException
     * (HANA-specific path) — caught by GlobalExceptionHandler and returned as 503.
     */
    @PostMapping("/respaldo")
    public ResponseEntity<String> ejecutarRespaldo(
            @RequestBody EntradaIniciarReplica entrada) {
        Objects.requireNonNull(entrada.idEmpresa(), "idEmpresa is required");

        String configValue = dataPort.obtenerParametroRespaldoSebra(entrada.idEmpresa());
        if (ReplicaService.esRespaldoActivo(configValue)) {
            String fecha   = dataPort.obtenerFechaSistema(entrada.idEmpresa());
            String codPer  = ReplicaService.extractCodPer(fecha);
            dataPort.ejecutarRespaldoSebra(codPer);
        }
        return ResponseEntity.ok("Respaldo omitido — RespaldoReplicaTablasSebra != S");
    }
}
