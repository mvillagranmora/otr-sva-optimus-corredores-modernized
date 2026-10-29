SELECT 
	"IdOrden"
FROM "BSCL.Orden.BD::ORD.Orden"
WHERE "IdEstadoOrden" = 'OFE'
AND "IdOrden" IN (:IdsOrden)