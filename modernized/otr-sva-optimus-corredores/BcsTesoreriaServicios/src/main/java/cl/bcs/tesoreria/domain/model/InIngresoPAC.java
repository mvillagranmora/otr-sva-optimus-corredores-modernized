package cl.bcs.tesoreria.domain.model;

public record InIngresoPAC(
        Long idCliente,
        Long idPortafolio,
        String folioCSC,
        String cuentaCorriente
) {}
