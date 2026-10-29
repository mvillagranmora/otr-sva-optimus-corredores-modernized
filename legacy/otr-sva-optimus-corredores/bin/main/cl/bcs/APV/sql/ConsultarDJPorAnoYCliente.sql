SELECT 
	"ID" as "ID"
	,"IdCliente"
	,"RutCliente" as "DocumentoIdentidad"
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1871.Datos"
WHERE "IdCliente" IN (:IdCliente)
AND "AnoAhorro" = :AnoAhorro