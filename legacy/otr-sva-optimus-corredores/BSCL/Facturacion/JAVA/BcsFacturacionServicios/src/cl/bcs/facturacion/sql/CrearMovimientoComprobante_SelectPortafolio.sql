SELECT 
	"IdPortafolio" AS "IdPortafolio",
	"IdCliente" AS "IdCliente",
	"IdEstado" AS "EstadoPortafolio" 
		FROM "BSCL.Portafolio.BD::POR.Portafolio"	
		 WHERE "IdPortafolio" IN (:IdPortafolio)