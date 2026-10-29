UPDATE "BSCL.Plataforma.BD::GTA.CoberturaGarantia"
SET
	"IdEntidadDestinoGarantia" 			= COALESCE(:IdEntidadDestinoGarantia,0),
    "IdTipoInstrumentoExigeGarantia"	= COALESCE(:IdTipoInstrumentoExigeGarantia,''),
    "IdInstrumentoExigeGarantia" 		= COALESCE(:IdInstrumentoExigeGarantia,0),
	"IdTipoInstrumentoComoGarantia" 	= COALESCE(:IdTipoInstrumentoComoGarantia,''),
    "IdTipoClasificacionRiesgo" 		= COALESCE(:IdTipoClasificacionRiesgo,''),
	"PlazoTope" 						= COALESCE(:PlazoTope,0),
	"UnidadPlazo" 						= COALESCE(:UnidadPlazo,''),
	"CoberturaEntidad" 					= COALESCE(:CoberturaEntidad,0),
	"CoberturaCorredora" 				= COALESCE(:CoberturaCorredora,0),
    "IdEstado" 							= COALESCE(:IdEstado,''),
    "IdEntidadClasificadoraRiesgo"      = COALESCE(:IdEntidadClasificadoraRiesgo,''),
    "ClasificacionRiesgo"               = COALESCE(:ClasificacionRiesgo,'') 		    
WHERE "IdCoberturaGarantia" 			= :IdCoberturaGarantia;