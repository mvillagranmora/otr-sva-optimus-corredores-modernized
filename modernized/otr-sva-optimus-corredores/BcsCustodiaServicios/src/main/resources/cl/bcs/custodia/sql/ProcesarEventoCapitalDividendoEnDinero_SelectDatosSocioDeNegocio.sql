SELECT 	"IdSocioDeNegocio",
		"IdPersona",
		"IdEmpresa",
		"IdCliente",
		"IdEmpleado",
		"IdOperador",
		"IdProveedor",
		"IdContraparte",
		"Borrado"
FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as SOC
WHERE "SOC"."Borrado"='N'
  AND SOC."IdCliente" IN (:IdCliente);