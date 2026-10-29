UPDATE "BSCL.APV.BD::REC.Anotacion" 
SET
	"IdAnotacion"				= :IdAnotacion,
	"IdMotivoAnotacion"			= :IdMotivo,
	"Titulo"					= :Titulo,
	"Detalle"					= :Detalle,
	"FechaModificacion"			= CURRENT_UTCTIMESTAMP,
	"IdRelevanciaAnotacion"		= :IdRelevancia,
	"IdAmbitoAnotacion"			= :IdAmbito,
	"ValidoDesde"				= :FechaDesde,
	"ValidoHasta"				= :FechaHasta
WHERE	"IdAnotacion" 	= :IdAnotacion;