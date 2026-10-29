UPDATE "BSCL.Custodia.BD::CUS.CarteraNegativa"
SET    "IdInstrumentoAnterior"	=	:p_IdInstrumento,
	   "IdInstrumento"			= 	:p_IdInstrumentoNuevo,
	   "Nemotecnico" 			= 	:p_NemoNuevo,
	   "IdEmisor"				= 	:p_Emisor,
	   "NemoSVS"				= 	:p_NemoNuevo
WHERE  "IdInstrumento"			=   :p_IdInstrumento AND "IdEstado" = 'VIG';
