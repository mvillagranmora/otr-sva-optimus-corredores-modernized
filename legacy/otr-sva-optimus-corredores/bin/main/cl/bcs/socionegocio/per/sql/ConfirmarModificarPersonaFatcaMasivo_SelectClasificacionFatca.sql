SELECT 
	"IdClasificacionFatca" AS "IdClasificacionFatca", 
	"Nombre" AS "Nombre" 
	FROM "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"
	WHERE "IdClasificacionFatca" IN (:IdClasificacionFatca);