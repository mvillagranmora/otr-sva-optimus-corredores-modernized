package cl.bcs.portafolio.domain.portafolio;

import java.util.List;

public record ModificarPortafolioEntrada(
    Long idPortafolio,
    String codigoPortafolio,
    String motivoCambio,
    String idUsuario,
    List<RegistroCambio> cambios
) {}
