SELECT 
	"IdCliente"
FROM "BSCL.APV.BD::INV.Bonificacion"
WHERE "IdCliente" IN (:IdCliente)
AND "AnoAhorro" = :AnoAhorro
AND "AnoProceso" = :AnoProceso;