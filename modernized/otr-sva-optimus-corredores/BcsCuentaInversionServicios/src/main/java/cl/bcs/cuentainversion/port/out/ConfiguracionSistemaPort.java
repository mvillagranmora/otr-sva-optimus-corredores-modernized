package cl.bcs.cuentainversion.port.out;

import java.time.LocalDate;

public interface ConfiguracionSistemaPort {
    String getConfiguracion(Long idEmpresa, String clave);
    boolean usaCargoAbonoJava(Long idEmpresa);
    boolean permiteAnularMovimiento(Long idEmpresa);
    boolean validaFlujoCajaEnCertificacion(Long idEmpresa);
    LocalDate getFechaGoLive(Long idEmpresa);
}
