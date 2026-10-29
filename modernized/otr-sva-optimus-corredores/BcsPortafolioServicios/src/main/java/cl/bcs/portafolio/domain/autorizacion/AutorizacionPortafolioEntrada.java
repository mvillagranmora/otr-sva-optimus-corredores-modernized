package cl.bcs.portafolio.domain.autorizacion;

import java.util.List;

public record AutorizacionPortafolioEntrada(
    Long idEvento,
    String idUsuario,
    String accion,
    List<String> campos
) {}
