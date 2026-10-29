package cl.bcs.cuentainversion.domain;

import java.util.List;

public record CrearCargoAbonoSalida(
        List<CargoAbonoGenerado> cargosGenerados,
        List<MensajeSalida> informacion,
        List<MensajeSalida> advertencias,
        List<MensajeSalida> errores
) {}
