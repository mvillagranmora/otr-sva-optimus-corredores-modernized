package cl.bcs.becb.domain;

public record EntradaGenerarInforme(
        Long idEmpresa,
        Long idUsuario,
        String idModulo,
        String fechaSistema,
        String ruta,
        String codigoInforme,
        String fechaInforme
) {}
