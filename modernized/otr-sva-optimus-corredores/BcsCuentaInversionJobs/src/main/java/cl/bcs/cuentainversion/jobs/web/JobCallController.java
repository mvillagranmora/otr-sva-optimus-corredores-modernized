package cl.bcs.cuentainversion.jobs.web;

import cl.bcs.cuentainversion.jobs.programa.EjecutorPrograma;
import cl.bcs.cuentainversion.jobs.programa.ProgramaIncoherenteException;
import cl.bcs.cuentainversion.jobs.programa.ProgramaNoEncontradoException;
import cl.bcs.cuentainversion.jobs.programa.ProgramaNoReclamableException;
import cl.bcs.cuentainversion.jobs.programa.ProgramaRequiereRevisionException;
import cl.bcs.cuentainversion.jobs.runtime.JobEnCursoException;
import cl.bcs.cuentainversion.jobs.runtime.JobNoMigradoException;
import cl.bcs.cuentainversion.jobs.runtime.JobResultado;
import cl.bcs.cuentainversion.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.cuentainversion.jobs.runtime.JobRunner;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * {@code POST /internal/job-call}: runs one migrated legacy job in-process.
 *
 * <p>The legacy Job Manager instantiated the {@code BcsJob} subclass by name and ran it (BcsJob.java:126-160); it now
 * posts {@code {"clase", "idPrograma", "parametros"}} here instead. The transaction rule is the BcsJob one
 * ({@code transaction()} result or exception), not the JavaCall one of BcsCuentaInversionServicios
 * (service outputs). Responses: 200 for COMMIT and ROLLBACK (a rollback is not an error,
 * BcsJob.java:151), 500 for ERROR with the exception message, 503 for a class this module does not run (the legacy
 * Job Manager keeps it), 409 when the same program or job class is already running (review HIGH-3), 400 for a
 * malformed request. Parameter values are passed as text; JSON nulls are dropped.
 * Internal endpoint: it is not published outside the container network.
 */
@RestController
public class JobCallController {

    /** Request body. */
    public record JobCall(String clase, Long idPrograma,
                          @JsonDeserialize(using = ParametrosTexto.class) Map<String, String> parametros) {
    }

    /**
     * Review M3: JSON scalars become their literal text, so 1.50 stays "1.50" and 100000000.0 stays "100000000.0"
     * (binding to Double gave "1.5" and "1.0E8"). Nulls are dropped; objects and arrays are rejected (400).
     */
    static final class ParametrosTexto extends ValueDeserializer<Map<String, String>> {
        @Override
        public Map<String, String> deserialize(JsonParser p, DeserializationContext ctxt) {
            if (p.currentToken() != JsonToken.START_OBJECT) {
                return (Map<String, String>) ctxt.handleUnexpectedToken(Map.class, p);
            }
            Map<String, String> salida = new HashMap<>();
            while (p.nextToken() == JsonToken.PROPERTY_NAME) {
                String nombre = p.currentName();
                JsonToken valor = p.nextToken();
                if (valor == JsonToken.START_OBJECT || valor == JsonToken.START_ARRAY) {
                    return ctxt.reportInputMismatch(Map.class, "parameter '%s' must be a text, number or boolean", nombre);
                }
                if (valor != JsonToken.VALUE_NULL) {
                    salida.put(nombre, p.getString());
                }
            }
            return salida;
        }
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
        Map<String, String> parametros = peticion.parametros() == null ? new HashMap<>() : peticion.parametros();
        JobResultado r = ejecutor.ejecutar(peticion.idPrograma(), peticion.clase(), parametros);
        boolean error = r.desenlace() == Desenlace.ERROR;
        return ResponseEntity.status(error ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK)
                .body(new JobCallResultado(r.clase(), r.idPrograma(), r.desenlace().name(), error, mensaje(r)));
    }

    /**
     * Review M2: the message of an infrastructure failure (database, HTTP call, I/O) is not echoed; the body gets a
     * stable code and the class name. The text of job-level errors is pinned by the legacy contract and kept.
     */
    static String mensaje(JobResultado r) {
        if (r.desenlace() != Desenlace.ERROR) {
            return r.mensaje();
        }
        for (Throwable t = r.error(); t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof SQLException || t instanceof DataAccessException || t instanceof IOException
                    || t instanceof RestClientResponseException) {
                return "INFRASTRUCTURE_ERROR: " + t.getClass().getSimpleName();
            }
        }
        return r.mensaje();
    }

    /** Review M8: anything unexpected gives a stable body without the exception message. */
    @ExceptionHandler(Exception.class)
    ResponseEntity<JobCallRechazo> inesperado(Exception e) {
        if (e instanceof ErrorResponse er) { // Spring's own 4xx (405, 415, ...) keep their status
            return ResponseEntity.status(er.getStatusCode()).body(new JobCallRechazo(true, null, "Request not accepted"));
        }
        log.error("Unexpected error in /internal/job-call", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new JobCallRechazo(true, null, "INTERNAL_ERROR"));
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
