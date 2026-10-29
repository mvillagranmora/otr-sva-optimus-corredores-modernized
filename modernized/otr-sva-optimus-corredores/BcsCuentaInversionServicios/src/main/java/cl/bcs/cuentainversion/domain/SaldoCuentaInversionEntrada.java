package cl.bcs.cuentainversion.domain;

import java.time.LocalDate;
import java.util.List;

public record SaldoCuentaInversionEntrada(
        Long idEmpresa,
        Long idUsuario,
        String idModulo,
        List<Long> idsCuentaInversion,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        String incluyeCargosPendientes,
        String incluyeAbonosPendientes,
        String soloSaldoNegativo,
        String soloSaldoDisponible,
        String soloSaldoDisponibleNegativo
) {}
