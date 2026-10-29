SELECT "IdCliente",
	   "IdEstado" as "IdEstadoCliente"
FROM 	"BSCL.SocioNegocio.BD::CLI.Cliente"
WHERE 	"IdCliente" IN (:IdsCliente)