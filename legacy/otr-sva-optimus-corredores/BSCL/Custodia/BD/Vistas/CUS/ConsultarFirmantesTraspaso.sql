SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarFirmantesTraspaso";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarFirmantesTraspaso"
AS 
SELECT
	CO."Descripcion"				as "IdTipoFirmante",
	CO."Valor"						as "IdFirmante",
	IFNULL(CL."NombreCompleto",'')	as "NombreFirmante",
	CO."Descripcion"				as "NombreAgente"
	

FROM "BSCL.Plataforma.BD::PLA.Configuracion" 			AS CO
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"	AS CL
ON (CO."Valor" = CL."IdCliente")
WHERE CO."CodigoParametro" in ('PrimerFirmante', 'SegundoFirmante', 'TercerFirmante','CuartoFirmante');

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarFirmantesTraspaso" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
