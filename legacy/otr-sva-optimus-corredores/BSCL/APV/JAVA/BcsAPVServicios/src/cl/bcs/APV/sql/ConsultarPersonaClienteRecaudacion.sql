SELECT CLI."IdPersona" as "IdPersonaCliente"
	   ,DetalleCliente."IdRecaudacionCliente"
FROM "BSCL.APV.BD::REC.Cliente" DetalleCliente
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI 
ON CLI."IdCliente" = DetalleCliente."IdCliente"
WHERE DetalleCliente."IdRecaudacionCliente" IN  (:IdsRecaudacionCliente)