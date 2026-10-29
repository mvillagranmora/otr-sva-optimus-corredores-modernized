package cl.bcs.plataforma.domain;

/**
 * Result of a successful session + service validation via ACC.Sesion / PLA.Servicio.
 * Source: ProcesaLlamadaServletCall.SalidaProcesaLlamadaServletCall
 */
public record SessionContext(
    long   idEmpresa,
    long   idUsuario,
    String codigoUsuario,
    String tipoUsuario,
    String rutaServicio,
    String tipoServicio,   // P | Q | J | O
    String tablasTempIn,
    String tablasTempOut,
    String extras
) {}
