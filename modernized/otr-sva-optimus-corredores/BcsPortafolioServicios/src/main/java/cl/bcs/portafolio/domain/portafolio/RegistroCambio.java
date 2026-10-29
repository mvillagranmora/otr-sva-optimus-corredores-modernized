package cl.bcs.portafolio.domain.portafolio;

public record RegistroCambio(
    String campo,
    String valorAnterior,
    String valorNuevo
) {}
