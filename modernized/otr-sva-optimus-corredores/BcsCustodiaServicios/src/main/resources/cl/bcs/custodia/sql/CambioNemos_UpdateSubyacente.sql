UPDATE "BSCL.Custodia.BD::CUS.Subyacente"
SET    "IdInstrumentoAnterior"	=	:p_IdInstrumento,
	   "IdInstrumento"			= 	:p_IdInstrumentoNuevo,
	   "Nemotecnico" 			= 	:p_NemoNuevo,
	   "IdEmisor"				= 	:p_Emisor,
	   "ISIN"					=	COALESCE(:p_ISIN, ' ')
WHERE  "IdInstrumento"			=   :p_IdInstrumento AND "IdEstado" = 'VIG';