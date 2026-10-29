SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarArea"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarArea" AS
SELECT
	AREA."Codigo",
	AREA."Descripcion",
	AREA."IdEstado"
FROM "BSCL.SocioNegocio.BD::CLI.Area" AS AREA;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarArea" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 