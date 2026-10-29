package cl.bcs.plataforma.jobs.port.out;

/**
 * Output port for reading system configuration parameters from HANA.
 */
public interface ConfiguracionPort {

    String getParametroString(Long idEmpresa, String idModulo, String idParametro);

    Long getParametroLong(Long idEmpresa, String idModulo, String idParametro);

    String getRutaBaseArchivos(Long idEmpresa);
}
