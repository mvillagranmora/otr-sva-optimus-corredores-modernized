package cl.bcs.cuentainversion.domain;

import java.time.LocalDate;
import java.util.List;

public record CrearCargoAbonoEntrada(
        Long idEmpresa,
        Long idUsuario,
        String idModulo,
        String simularOperacion,
        String usaReglasNegocio,
        LocalDate fechaSistema,
        String esIngresoManual,
        List<CargoAbono> cargosAbonos
) {}
