SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarFondos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarFondos" (
	"IdFondo"
	,"Esquema"
	,"Version"
	,"PorcentajeAccionario"
	,"Observacion"
	,"IdEstado"
) AS
SELECT
	"IdFondo"
	,"Esquema"
	,"Version"
	,"PorcentajeAccionario"
	,"Observacion"
	,"IdEstado"
FROM "BSCL.Normativa.BD::TRI.Fondos"
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultarFondos" TO bcs_desarrollador WITH GRANT OPTION;