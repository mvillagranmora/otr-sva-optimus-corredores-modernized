UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET    "IdInstrumentoAnterior"	=	:p_IdInstrumento,
	   "IdInstrumento"			= 	:p_IdInstrumentoNuevo,
	   "Nemotecnico" 			= 	:p_NemoNuevo,
	   "IdEmisor"				= 	:p_Emisor,
	   "ISIN"					=	COALESCE(:p_ISIN, ' '),
	   "NemoSVS"				= 	:p_NemoNuevo
WHERE  "IdInstrumento"			=   :p_IdInstrumento AND "IdEstado" = 'VIG';
