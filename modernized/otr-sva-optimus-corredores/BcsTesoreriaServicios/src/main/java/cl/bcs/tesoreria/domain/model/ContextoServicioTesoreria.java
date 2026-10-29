package cl.bcs.tesoreria.domain.model;

public record ContextoServicioTesoreria(
        Long idEmpresa,
        Long idUsuario,
        String idModulo,
        String simulaOperacion
) {}
