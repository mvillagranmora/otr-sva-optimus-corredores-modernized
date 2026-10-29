package cl.bcs.cuentainversion.jobs.job.port;

/**
 * The platform configuration parameters the jobs read (legacy {@code ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa,
 * modulo, nombre)} and {@code Utils.getConfiguracion(con, schema, idEmpresa, codigo)}, which is the module {@code PLATAFORMA}).
 *
 * <p>A parameter that is not configured answers {@code null}; the job then fails with the legacy message
 * {@code "Parametro <nombre> en modulo <modulo> no encontrado"} (ConfiguracionSistema.java:70-71).
 */
@FunctionalInterface
public interface ConfiguracionJobs {

    /** @return the value, or {@code null} when the parameter is not configured for the company */
    String parametro(Long idEmpresa, String modulo, String nombre);
}
