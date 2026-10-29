SELECT 
SOC."IdPersona" AS "IdPersonaCtaPropia",
SOC."IdCliente" AS "IdClienteCtaPropia",
IDENT."DocumentoIdentidad" AS "DocumentoIdentidadCtaPropia",
POR."IdPortafolio" AS "IdPortafolioCtaPropia"
FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC on (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio")
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR on (SOC."IdCliente" = POR."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER on (SOC."IdPersona" = PER."IdPersona")
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
WHERE EMP."IdEmpresa" = :IdEmpresa
GROUP BY SOC."IdPersona",SOC."IdCliente",IDENT."DocumentoIdentidad",POR."IdPortafolio";
