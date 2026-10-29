package cl.bcs.cuentainversion.port.in;

import cl.bcs.cuentainversion.domain.CrearCargoAbonoEntrada;
import cl.bcs.cuentainversion.domain.CrearCargoAbonoSalida;

public interface CrearCargoAbonoUseCase {
    CrearCargoAbonoSalida crear(CrearCargoAbonoEntrada entrada);
}
