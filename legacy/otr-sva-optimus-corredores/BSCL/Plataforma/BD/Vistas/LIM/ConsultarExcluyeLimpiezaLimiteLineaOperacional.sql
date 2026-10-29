SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarExcluyeLimpiezaLimiteLineaOperacional";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarExcluyeLimpiezaLimiteLineaOperacional" AS
SELECT
	POR."IdPortafolio" AS "IdPortafolio"
	,POR."CodigoPortafolio" AS "CodigoPortafolio"
	,POR."IdCliente" AS "IdCliente"
	,CLI."DocumentoIdentidad" AS "DocumentoIdentidad"
	,CLI."NombreCompleto" AS "NombreCompleto"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CLI" ON "CLI"."IdCliente" = "POR"."IdCliente"
WHERE POR."ExcluyeLimpiezaLineaOperacional" = 'S';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarExcluyeLimpiezaLimiteLineaOperacional" TO BCS_DESARROLLADOR WITH GRANT OPTION;