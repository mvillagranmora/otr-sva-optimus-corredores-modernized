package cl.bcs.contabilidad.jobs.web;

import cl.bcs.contabilidad.jobs.programa.EjecutorPrograma;
import cl.bcs.contabilidad.jobs.programa.ProgramaIncoherenteException;
import cl.bcs.contabilidad.jobs.programa.ProgramaJdbcException;
import cl.bcs.contabilidad.jobs.programa.ProgramaNoEncontradoException;
import cl.bcs.contabilidad.jobs.programa.ProgramaNoReclamableException;
import cl.bcs.contabilidad.jobs.programa.ProgramaRequiereRevisionException;
import cl.bcs.contabilidad.jobs.runtime.JobEnCursoException;
import cl.bcs.contabilidad.jobs.runtime.JobNoMigradoException;
import cl.bcs.contabilidad.jobs.runtime.JobResultado;
import cl.bcs.contabilidad.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.contabilidad.jobs.runtime.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@code POST /internal/job-call}: runs one migrated legacy job in-process (CONT-J-90..96).
 *
 * <p>The legacy Job Manager instantiated the {@code BcsJob} subclass by name and ran it (BcsJob.java:126-160); it now
 * posts {@code {"clase", "idPrograma", "parametros"}} here instead. The transaction rule is the BcsJob one
 * ({@code transaction()} result or exception), not the JavaCall one of BcsContabilidadServicios
 * ({@code P_OUT_ERRORESCONTADOR}, CONT-J-93). Responses: 200 for COMMIT and ROLLBACK (a rollback is not an error,
 * BcsJob.java:151), 500 for ERROR with the exception message, 503 for a class this module does not run (the legacy
 * Job Manager keeps it), 409 when the same program or job class is already running (review HIGH-3), 400 for a
 * malformed request. Parameter values are passed as text; JSON nulls are dropped.
 * Internal endpoint: it is not published outside the container network.
 */
@RestController
public class JobCallController {

    /** Request body. */
    public record JobCall(String clase, Long idPrograma, Map<String, Object> parametros) {
    }

    /** Outcome of a run. {@code mensaje} is "" unless {@code error}. */
    public record JobCallResultado(String clase, long idPrograma, String resultado, boolean error, String mensaje) {
    }

    /** A request that was not run. */
    public record JobCallRechazo(boolean error, String clase, String mensaje) {
    }

    private static final Logger log = LoggerFactory.getLogger(JobCallController.class);

    private final EjecutorPrograma ejecutor;

    /** Caller mode (the contract the golden and runner tests pin): class and parameters come from the request. */
    public JobCallController(JobRunner runner) {
        this(EjecutorPrograma.parametrosDelLlamador(Objects.requireNonNull(runner, "runner")));
    }

    /** Job pattern H4: in mode {@code programa} only {@code idPrograma} is required; see {@link EjecutorPrograma}. */
    @Autowired
    public JobCallController(EjecutorPrograma ejecutor) {
        this.ejecutor = Objects.requireNonNull(ejecutor, "ejecutor");
    }

    @PostMapping(path = "/internal/job-call", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobCallResultado> ejecutar(@RequestBody JobCall peticion) {
        if (ejecutor.exigeClase() && (peticion.clase() == null || peticion.clase().isBlank())) {
            throw new PeticionInvalidaException("'clase' is required");
        }
        if (peticion.idPrograma() == null) {
            throw new PeticionInvalidaException("'idPrograma' is required");
        }
        JobResultado r = ejecutor.ejecutar(peticion.idPrograma(), peticion.clase(), parametros(peticion.parametros()));
        boolean error = r.desenlace() == Desenlace.ERROR;
        return ResponseEntity.status(error ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK)
                .body(new JobCallResultado(r.clase(), r.idPrograma(), r.desenlace().name(), error, r.mensaje()));
    }

    /** JSON scalars become their text; nulls are dropped; objects and arrays are rejected. */
    private static Map<String, String> parametros(Map<String, Object> json) {
        Map<String, String> parametros = new HashMap<>();
        if (json == null) {
            return parametros;
        }
        json.forEach((nombre, valor) -> {
            if (valor instanceof Map<?, ?> || valor instanceof List<?>) {
                throw new PeticionInvalidaException("parameter '" + nombre + "' must be a text, number or boolean");
            }
            if (valor != null) {
                parametros.put(nombre, String.valueOf(valor));
            }
        });
        return parametros;
    }

    @ExceptionHandler(JobNoMigradoException.class)
    ResponseEntity<JobCallRechazo> noMigrado(JobNoMigradoException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new JobCallRechazo(true, e.clase(), e.getMessage()));
    }

    /** Review HIGH-3: a run of the same program or job class is in progress; the caller must not retry blindly. */
    @ExceptionHandler(JobEnCursoException.class)
    ResponseEntity<JobCallRechazo> enCurso(JobEnCursoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new JobCallRechazo(true, e.clase(), e.getMessage()));
    }

    /** Job pattern H4: no such program in {@code BAT.Programa}. */
    @ExceptionHandler(ProgramaNoEncontradoException.class)
    ResponseEntity<JobCallRechazo> programaNoEncontrado(ProgramaNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JobCallRechazo(true, null, e.getMessage()));
    }

    /** Job pattern H4: the atomic claim matched 0 rows, another instance or run has the program. */
    @ExceptionHandler(ProgramaNoReclamableException.class)
    ResponseEntity<JobCallRechazo> programaNoReclamable(ProgramaNoReclamableException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new JobCallRechazo(true, null, e.getMessage()));
    }

    /**
     * Step 6: earlier TomEE writes of this program (guard marks) forbid a re-run; the program is already "requires review". 423, not the 409 of
     * a claim: the caller must neither retry nor mark the program ERR again (a human decides).
     */
    @ExceptionHandler(ProgramaRequiereRevisionException.class)
    ResponseEntity<JobCallRechazo> programaRequiereRevision(ProgramaRequiereRevisionException e) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(new JobCallRechazo(true, null, e.getMessage()));
    }

    /** Job pattern H4: the class or the parameters sent do not match the program row. */
    @ExceptionHandler(ProgramaIncoherenteException.class)
    ResponseEntity<JobCallRechazo> programaIncoherente(ProgramaIncoherenteException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new JobCallRechazo(true, null, e.getMessage()));
    }

    /** Job pattern H4: BAT.Programa could not be read or claimed; nothing ran. Stable body, no database text. */
    @ExceptionHandler(ProgramaJdbcException.class)
    ResponseEntity<JobCallRechazo> programaJdbc(ProgramaJdbcException e) {
        log.error("BAT.Programa access failed in /internal/job-call", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new JobCallRechazo(true, null, "INTERNAL_ERROR"));
    }

    @ExceptionHandler(PeticionInvalidaException.class)
    ResponseEntity<JobCallRechazo> peticionInvalida(PeticionInvalidaException e) {
        return ResponseEntity.badRequest().body(new JobCallRechazo(true, null, e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<JobCallRechazo> cuerpoIlegible(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new JobCallRechazo(true, null,
                "The body must be {\"clase\": text, \"idPrograma\": number, \"parametros\": object}"));
    }

    /** The request is malformed (400). */
    static final class PeticionInvalidaException extends RuntimeException {
        PeticionInvalidaException(String mensaje) {
            super(mensaje);
        }
    }
}
