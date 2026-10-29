SELECT 
	DIRFI."IdDireccionFisica",
	DIRFI."IdPersona",
	DIRFI."Calle",
	DIRFI."Numero",
	DIRFI."Departamento",
	DIRFI."Sector",
	DIRFI."CodigoPostal",
	DIRFI."IdCiudad",
	DIRFI."CodigoProposito",
	DIRFI."Comentario",
	DIRFI."IdEstado",
	DIRFI."IdEstadoAutorizacion",
	DIRFI."Borrado",
	DIRFI."IdDivisionPais",
	DIRFI."Predeterminado",
	DIRFI."CodigoCompuesto",
	CLI."TipoEnrolamiento",
	CLI."IdCliente",
	PER."Tipo"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIRFI
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON DIRFI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE DIRFI."IdDireccionFisica" IN (:IdDireccionesFisicas) AND DIRFI."Borrado" = 'N';