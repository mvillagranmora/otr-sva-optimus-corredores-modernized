package cl.bcs.jobmanager.service;

/**
 * What the dispatcher learned from one POST /internal/job-call. In NO case does the dispatcher change BAT.Programa:
 * the job module claims the program and writes TER/ERR itself. Only the log level differs.
 */
public enum JobCallDesenlace {
    /** 200: the job ran (COMMIT or ROLLBACK); the module wrote the final state. */
    HECHO,
    /** 409: already claimed or running in the module; skip. */
    YA_RECLAMADO,
    /** 404/422: the module found no such program, or class/parameters disagree with the row (data or configuration error). */
    INCONSISTENTE,
    /** 503: the module does not run that class (destination map points to the wrong module). */
    CLASE_NO_EJECUTADA,
    /** 500: the job failed; the module already wrote ERR. */
    ERROR_DEL_JOB,
    /** Read timeout: the job may still be running or finished; the module owns the state. NOT a failure. */
    SIN_RESPUESTA,
    /** Connection refused / connect timeout / IO error before the request was sent: nothing was claimed, stays PEN. */
    NO_ENVIADO,
    /** Any other status (400, 401, ...): configuration problem (token, URL); nothing changes. */
    RESPUESTA_INESPERADA,
    /** The program has no destination in jobmanager.destinos. Nothing was sent. */
    SIN_DESTINO
}
