UPDATE "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle"
SET    "IdInstrumentoAnterior"	=	:p_IdInstrumento,
	   "IdInstrumento"			= 	:p_IdInstrumentoNuevo,
	   "NemotecnicoInstrumento"	= 	:p_NemoNuevo
WHERE  "IdInstrumento"			=   :p_IdInstrumento;