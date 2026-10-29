package cl.bcs.normativa.jobs.port.out;

/**
 * Port for reading system configuration values from HANA.
 * Corresponds to legacy ConfiguracionSistema.getParametroConfiguracionString and Utils.getConfiguracion.
 */
public interface ConfiguracionPort {

    /** Returns the system date (PLA.FechaSistema) for the given company. */
    String getFechaSistema(Long idEmpresa);

    /** Returns the base file path (PLA.RutaBaseArchivos) for the given company. */
    String getRutaBaseArchivos(Long idEmpresa);

    /** Returns a generic configuration parameter value. */
    String getParametro(Long idEmpresa, String modulo, String codigoParametro);
}
