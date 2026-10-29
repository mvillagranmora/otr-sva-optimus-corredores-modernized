SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::PRT.UIConsultarClientesParaPrestamo";

CREATE VIEW  "BSCL.Custodia.BD.Vistas::PRT.UIConsultarClientesParaPrestamo" AS 

SELECT 
 	SOC."IdPersona",
 	SOC."IdCliente",
 	CLI."NombreCompleto",
 	CLI."DocumentoIdentidad",
 	POR."IdPortafolio",
 	POR."CodigoPortafolio",
 	POR."Proposito",
 	'S' as "Cliente",
 	CASE CNT."Corredora" WHEN 'S' THEN 'S' ELSE 'N' END as "Corredora",
 	CASE TSEG."NombreSegmentoCliente" WHEN 'INSTITUCIONAL' THEN 'S' ELSE 'N' END as "Institucional",
 	'S' as "Externo", -- Cliente que no tiene contrato custodia.
 	'S' as "Interno" -- Tiene contrato custodia.
FROM 
"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI 
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
	ON (SOC."IdCliente" = CLI."IdCliente" )
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR
	ON (POR."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoSegmento" TSEG
ON (CLI."IdSegmentoCliente" = TSEG."IdSegmentoCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" CNT
ON (CNT."IdContraparte" = SOC."IdContraparte");

GRANT SELECT ON  "BSCL.Custodia.BD.Vistas::PRT.UIConsultarClientesParaPrestamo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
