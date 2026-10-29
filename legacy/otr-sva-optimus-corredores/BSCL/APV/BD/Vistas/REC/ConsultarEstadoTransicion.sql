SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::REC.ConsultarEstadoTransicion"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::REC.ConsultarEstadoTransicion"  AS 

SELECT 	"IdEstadoTransicion",
		"IdRegistro",
		"Nombre",
		"FechaTransicion" AS "Fecha",
		"Observacion"
	FROM "BSCL.APV.BD::APV.EstadoTransicion" AS "ET"
		INNER JOIN  "BSCL.APV.BD::APV.Estado"AS  "E"
			ON "ET"."EstadoNuevo" = "E"."Codigo"
				AND UPPER ("ET"."Tipo") = "E"."NombreEntidad";
	   
GRANT SELECT ON "BSCL.APV.BD.Vistas::REC.ConsultarEstadoTransicion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 