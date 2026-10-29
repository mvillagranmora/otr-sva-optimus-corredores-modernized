package cl.bcs.fondomutuo.port.out.kit;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Kit port K: legacy {@code plataforma.pla.utils.ConfiguracionSistema} (parameters of {@code PLA.Configuracion} / {@code FMM.Parametro} / ...).
 * Reads on the connection of the unit of work, NOT cached across calls (the legacy cache was switched off: the day-end changes
 * {@code PLA.FechaSistema} while the services run). A missing parameter raises {@code ParametroNoEncontradoException}; an unknown module raises
 * {@code IllegalArgumentException("Configuracion modulo X no encontrada")}. Replaced by Plataforma's kit when it exists (DEV to register).
 */
public interface ConfiguracionSistemaPort {

    String texto(Long idEmpresa, String modulo, String nombreParametro);

    long entero(Long idEmpresa, String modulo, String nombreParametro);

    BigDecimal decimal(Long idEmpresa, String modulo, String nombreParametro);

    Set<String> conjunto(Long idEmpresa, String modulo, String nombreParametro, String delimitador);
}
