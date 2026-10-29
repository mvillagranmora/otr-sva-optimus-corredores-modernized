package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record AnulacionMandatoPACRequest(
        Long idEmpresa,
        Long idUsuario,
        String idModulo,
        String simulaOperacion,
        List<MandatoRef> mandatos
) {}
