SELECT		
		REL."IdRelacionPersona" AS "IdRelacion",
		REL."IdPersonaOrigen" AS "IdPersona",
		REL."IdPersonaDestino" AS "IdPersonaRelacionada",	
		REL."IdTipoRelacion",
		REL."IdEstado",
		PER."Tipo",
		CLI."TipoEnrolamiento",
		CLI."IdCliente"
FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona"  AS REL
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = REL."IdPersonaOrigen"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE  REL."IdRelacionPersona" IN (:IdRelacionPersona);