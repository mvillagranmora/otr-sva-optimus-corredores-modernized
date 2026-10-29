package cl.bcs.portafolio.domain.preferencia;

public record PreferenciaPagoEntrada(
    Long idPortafolio,
    String codigoPortafolio,
    String tipoPago,
    String banco,
    String numeroCuenta
) {}
