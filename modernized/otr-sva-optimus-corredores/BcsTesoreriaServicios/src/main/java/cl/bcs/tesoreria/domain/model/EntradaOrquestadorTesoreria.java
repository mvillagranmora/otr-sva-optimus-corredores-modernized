package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record EntradaOrquestadorTesoreria(
        Long idEmpresa,
        Long idUsuario,
        String idModulo,
        String simulaOperacion,
        List<String> nombresServicios
) {}
