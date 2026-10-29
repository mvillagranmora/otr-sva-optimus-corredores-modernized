package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record ConfirmarRechazarAnularRequest(
        Long idEmpresa,
        Long idUsuario,
        String idModulo,
        List<MovimientoRechazar> movimientos
) {}
