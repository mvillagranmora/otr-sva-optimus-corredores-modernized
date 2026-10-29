package cl.bcs.cuentainversion.jobs.runtime;

import java.util.Map;

/**
 * One legacy {@code BcsProcesoPauta} implementation (BcsPlataformaJobs {@code BcsProcesoPauta.java:11}): a step of the end-of-day
 * PAUTA. It is NOT a {@code BcsJob}: the PAUTA runner owns the connection and the commit, the step receives the connection and the
 * parameters ({@code IdEmpresa}, {@code IdUsuario} as {@link Long}) and answers a {@link SalidaPauta}; an exception aborts the step.
 * In-process the step joins the transaction of its caller.
 */
public interface ProcesoPauta {

    /** Legacy fully qualified class name, for example {@code cl.bcs.cuentaInversion.jobs.PautaGenerarReporteAcreedores}. */
    String clase();

    /** Legacy {@code transaction(con, parameters)}. */
    SalidaPauta transaction(Map<String, Object> parametros) throws Exception;
}
