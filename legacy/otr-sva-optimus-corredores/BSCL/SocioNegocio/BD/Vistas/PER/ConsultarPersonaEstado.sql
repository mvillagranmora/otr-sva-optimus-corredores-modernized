SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaEstado"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaEstado"
AS
	SELECT
		"PER"."IdPersona",
		CASE WHEN "PER"."IdEstado" = 'ACT' THEN CASE WHEN "PER"."EsEnrolado"='P' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS "IdEstado"
	FROM   "BSCL.SocioNegocio.BD::PER.Persona" AS "PER"
WITH READ ONLY; 


GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaEstado" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
