package cl.bcs.portafolio.domain.portafolio;

public record CrearPortafolioEntrada(
    String propositoPortafolio,
    String codigoPortafolio,
    String maximoCodigoPortafolio,
    String idPerfilCobros,
    String idPlantillaCobro,
    String idPlantillaProducto,
    String usaReglasNegocio,
    Long idPersonaBrokerExtranjero,
    String estado,
    String tipo,
    String docId,
    String razonSocial,
    String nombres,
    String apPat,
    String apMat,
    Long idPersona,
    Long idPerfilComercial,
    String monedaSistema,
    String crearCuentaInversionEUR
) {}
