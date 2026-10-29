SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarControlProceso";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarControlProceso" (
	"Esquema"
	,"Version"
	,"EstadoCertificado"
	,"Carga"
	,"Validacion"
	,"PreDeclaracion"
	,"Certificacion"
) AS
SELECT
	"Esquema"
	,"Version"
	,"EstadoCertificado"
	,"Carga"
	,"Validacion"
	,"PreDeclaracion"
	,"Certificacion"
FROM "BSCL.Normativa.BD::TRI.ControlProceso"
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultarControlProceso" TO bcs_desarrollador WITH GRANT OPTION;
