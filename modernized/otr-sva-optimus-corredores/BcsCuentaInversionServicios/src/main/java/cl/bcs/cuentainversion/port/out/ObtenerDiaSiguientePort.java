package cl.bcs.cuentainversion.port.out;

import java.time.LocalDate;

public interface ObtenerDiaSiguientePort {
    LocalDate obtenerDiaSiguiente(LocalDate fecha, int plazoEnDias, Long idEmpresa);
}
