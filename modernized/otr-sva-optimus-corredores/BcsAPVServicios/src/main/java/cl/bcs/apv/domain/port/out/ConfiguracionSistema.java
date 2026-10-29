package cl.bcs.apv.domain.port.out;

/** INTENDED API: legacy {@code ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, modulo, parametro)} (e.g. "PLATAFORMA", "PLA.FechaSistema"); null when not configured. */
public interface ConfiguracionSistema {
    String parametro(Long idEmpresa, String modulo, String nombre);
}
