SELECT 
POR."IdCliente",
POR."IdPortafolio"
FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC on (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio")
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR on (SOC."IdCliente" = POR."IdCliente")
WHERE EMP."IdEmpresa" = :IdEmpresa;