SELECT 
    "IdAutorizado",
    "IdPortafolio",
    "IdPersona",
    "IdTipoAutorizado",
    "IdEstado",
    "IdEstadoAutorizacion",
    "FechaIngreso",
    "FechaCreacion",
    "FechaModificacion",
    "IdUsuarioCreador",
    "NumeroRegistroPublico",
    "Asiento",
    "FichaPartida",
    "TipoFirma"
FROM "BSCL.Portafolio.BD::POR.Autorizado"
WHERE "IdAutorizado" = :IdAutorizado
