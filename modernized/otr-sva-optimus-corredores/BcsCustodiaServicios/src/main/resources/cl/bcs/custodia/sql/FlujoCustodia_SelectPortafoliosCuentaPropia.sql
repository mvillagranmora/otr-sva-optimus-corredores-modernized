/********************************************************** 
Proposito	: Recuperar los Portafolios de Cuenta Propia
autor		: RFCS
fecha		: 12/06/2017
***********************************************************/
/*
SELECT "IdPortafolio"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" AS PCP
WHERE PCP."IdEmpresa" = :IdEmpresa;
*/
SELECT  
	POR."IdPortafolio"
FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC on (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio" AND SOC."Borrado"='N')
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR on (SOC."IdCliente" = POR."IdCliente")
--INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER
	on (PER."IdPersona" = SOC."IdPersona" AND PER."Borrado" = 'N')
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT
	ON (PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" AND IDENT."Borrado" = 'N')
WHERE EMP."IdEmpresa" = :IdEmpresa;