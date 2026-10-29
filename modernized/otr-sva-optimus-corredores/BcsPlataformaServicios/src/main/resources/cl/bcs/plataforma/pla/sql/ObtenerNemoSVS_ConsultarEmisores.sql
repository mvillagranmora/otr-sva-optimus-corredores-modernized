SELECT 
	"IdEmisor",
	"IdEmisor" AS "NombreCorto",
	"CodigoEmisor",
	"CodigoRegulador",
	"Regulador",
	"EmiteIIF",
	"EmiteIRF",
	"EmiteFungible",
	"ValorClasificacionRiesgoIIF",
	"IdEntidadClasificadoraRiesgo",
	"IndicadorAGF",
	EMI."IdPersona",
	EMI."IdEstado",
	"FechaActualizacion",
	"TipoEntidad"
	
FROM "BSCL.Plataforma.BD::MER.Emisor" EMI
WHERE "IdEmisor" IN (:idsEmisor)
;