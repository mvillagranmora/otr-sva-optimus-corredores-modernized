SELECT
    PER."IdPersona",
    DOC."IdTipoDocumentoIdentidad",
    DOC."DocumentoIdentidad",
    CLI."IdCliente",
    CLI."IdEstado" AS "IdEstadoCliente",
    POR."IdPortafolio",
    POR."CodigoPortafolio",
    POR."IdEstado" AS "IdEstadoPortafolio",
    CLI."IdTipoBloqueo" AS "IdTipoBloqueoCliente", 
    CLI."IdTipoDesbloqueo" AS "IdTipoDesbloqueoCliente",
    CLI."ComentarioBloqueo" AS "ComentarioBloqueoCliente",
    CLI."ComentarioDesbloqueo" AS "ComentarioDesbloqueoCliente",
    POR."IdTipoBloqueo" AS "IdTipoBloqueoPortafolio", 
    POR."IdTipoDesbloqueo" AS "IdTipoDesbloqueoPortafolio",
    POR."ComentarioBloqueo" AS "ComentarioBloqueoPortafolio",
    POR."ComentarioDesbloqueo" AS "ComentarioDesbloqueoPortafolio",
    CLI."FechaBloqueo" AS "FechaBloqueoCliente",
    CLI."FechaDesbloqueo" AS "FechaDesbloqueoCliente",
    POR."FechaBloqueo" AS "FechaBloqueoPortafolio",
    POR."FechaDesbloqueo" AS "FechaDesbloqueoPortafolio"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona"=DOC."IdPersona" 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdPersona"=PER."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente"=CLI."IdCliente"
WHERE 
	CLI."IdEmpresa" = :IdEmpresa AND 
	DOC."DocumentoIdentidad" IN (:ListaDocumentoIdentidad)
