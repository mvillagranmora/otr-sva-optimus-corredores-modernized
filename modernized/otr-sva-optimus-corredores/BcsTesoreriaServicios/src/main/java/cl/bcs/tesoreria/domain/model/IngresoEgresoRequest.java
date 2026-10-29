package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record IngresoEgresoRequest(
        Long idEmpresa,
        Long idUsuario,
        String idModulo,
        String simulaOperacion,
        String usaReglasNegocio,
        String idOrigenProceso,
        String esIngresoAutomatico,
        String esIngresoUI,
        String fechaMovimiento,
        List<MovimientoRequest> ingresoEgresoGenerar,
        List<FormaDePagoRequest> formaDePago
) {
    public static IngresoEgresoRequest withDefaults(Long idEmpresa, Long idUsuario, String idModulo) {
        return new IngresoEgresoRequest(
                idEmpresa, idUsuario, idModulo,
                "N",          // p_SimularOperacion
                "S",          // p_UsaReglasNegocio
                null,         // p_IdOrigenProceso
                "S",          // p_EsIngresoAutomatico
                "N",          // p_EsIngresoUI
                "0001-01-01", // p_FechaMovimiento
                List.of(),
                List.of()
        );
    }
}
