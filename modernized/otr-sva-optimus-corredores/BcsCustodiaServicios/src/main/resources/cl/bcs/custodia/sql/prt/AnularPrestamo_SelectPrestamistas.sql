SELECT CAR."IdCartera", CAR."FolioCartera", CAR."FolioCarteraRelacionada", CAR."FolioSistemaExterno", CAR."IdCliente", PER."Nombres" , DOCUM."DocumentoIdentidad"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR, "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI,   
     "BSCL.SocioNegocio.BD::PER.Persona" AS PER, "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOCUM
	WHERE CAR."FolioCarteraRelacionada" IN (:FolioPrestamo) 
	AND   CAR."IdEmpresa"               = :IdEmpresa
	AND   CAR."IdTipoInstrumento"		= 'EMP'
	AND   CAR."IdEstado"				= 'VIG'
	AND   CAR."IdCliente"               = CLI."IdCliente"
	AND   CLI."IdPersona"               = DOCUM."IdPersona"
	AND   PER."IdDocumentoIdentidad"    = DOCUM."IdDocumentoIdentidad"