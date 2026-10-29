package cl.bcs.tesoreria.domain.model;

import java.util.List;

public record MandatoPACRequest(
        Long idEmpresa,
        Long idUsuario,
        String idModulo,
        String simulaOperacion,
        String usaReglasNegocio,
        List<InIngresoPAC> ingresoPAC
) {}
