UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET    "IdInstrumentoAnterior"	=	:p_IdInstrumento,
	   "IdInstrumento"			= 	:p_IdInstrumentoNuevo,
	   "Nemotecnico" 			= 	:p_NemoNuevo,
	   "IdEmisor"				= 	:p_Emisor,
	   "ISIN"					=	COALESCE(:p_ISIN, ' '),
	   "NemoSVS"				= 	:p_NemoNuevo
WHERE  "IdCartera"			IN (SELECT "IdCartera" FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
									INNER JOIN "BSCL.Custodia.BD::GTA.Garantia" AS GAR ON GAR."IdCarteraOrigen" = CAR."IdCartera"
									WHERE GAR."IdInstrumento" = :p_IdInstrumento AND GAR."IdEstado" = 'VIG');