SELECT 
	DIREL."IdDireccionElectronica",
	DIREL."IdPersona",
	DIREL."Direccion",
	DIREL."CodigoProposito",
	DIREL."Comentario",
	DIREL."IdEstado",
	DIREL."IdEstadoAutorizacion",
	DIREL."Borrado",
	DIREL."Predeterminado",
	CLI."TipoEnrolamiento",
	CLI."IdCliente",
	PER."Tipo"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DIREL
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON DIREL."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE DIREL."IdDireccionElectronica" IN (:IdDireccionesElectronicas) AND DIREL."Borrado" = 'N';