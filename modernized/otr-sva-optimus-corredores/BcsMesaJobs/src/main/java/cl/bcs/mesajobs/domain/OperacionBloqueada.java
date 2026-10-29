package cl.bcs.mesajobs.domain;

import java.time.LocalDateTime;

// B-MES-07: Operación con bloqueo de edición (LiberaBloqueoEdicionPactos)
public record OperacionBloqueada(
    long idOperacion,
    long folioOperacion,
    LocalDateTime fechaHoraInicioEdicion,
    String bloqueoPorEdicion,
    String codigoUsuarioBloqueoEdicion,
    String idTipoOperacion,
    Long idCliente
) {}
