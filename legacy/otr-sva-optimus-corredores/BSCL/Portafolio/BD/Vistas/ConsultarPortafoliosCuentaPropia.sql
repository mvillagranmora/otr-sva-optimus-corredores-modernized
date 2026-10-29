SET SCHEMA "BSCL";
GO
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia";
GO
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" AS 
SELECT  
	EMP."IdEmpresa",
	EMP."IdPais"   AS "IdPaisEmpresa",
	EMP."IdEstado" AS "IdEstadoEmpresa",
	SOC."IdSocioDeNegocio",
	SOC."IdPersona",	
	POR."IdCliente",
	CASE COALESCE(CLI."IdPortafolioDefecto",0) WHEN POR."IdPortafolio" THEN 'S' ELSE 'N' END AS "EsPortafolioDefecto",
	POR."IdPortafolio", 
	POR."CodigoPortafolio", 	
	POR."DescripcionPortafolio",
	POR."Proposito", 
	COALESCE(CLI."IdPortafolioDefecto",0) AS "IdPortafolioDefecto",
	PER."DocumentoIdentidad",
	PER."NombreCompleto" AS "Nombre"
FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC on (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio")
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR on (SOC."IdCliente" = POR."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER on (PER."IdPersona" = SOC."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI on (CLI."IdCliente" = SOC."IdCliente")
WHERE SOC."Borrado"='N'
WITH READ ONLY;
GO
		 
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;


/*
--EJEMPLOS:
-- 1) portafolio específico
select * from "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" 
where "IdEmpresa" = 1 and "IdPortafolio" = 153981;

-- 2) todos los portafolios del cliente
select * from "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" 
where "IdEmpresa" = 1 and "IdCliente" = 249212;
*/
