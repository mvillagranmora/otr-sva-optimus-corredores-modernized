SELECT 
	ccli."DocumentoIdentidad",
	ccli."IdCliente",
	por."IdPortafolio",
	por."CodigoPortafolio"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ccli
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" por ON (ccli."IdCliente" = por."IdCliente")
WHERE ccli."DocumentoIdentidad" in (:DocumentoIdentidad) 
AND por."IdEstado" NOT IN ('INA','BLO');