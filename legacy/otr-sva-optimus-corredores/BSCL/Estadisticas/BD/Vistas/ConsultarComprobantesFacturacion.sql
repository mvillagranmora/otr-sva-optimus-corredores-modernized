SET SCHEMA "BSCL";

DROP VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarComprobantesFacturacion";

CREATE VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarComprobantesFacturacion"
AS 
	SELECT 1 FROM DUMMY
	;
		 

GRANT SELECT ON "BSCL.Estadisticas.BD.Vistas::ConsultarComprobantesFacturacion" TO BCS_DESARROLLADOR WITH GRANT OPTION;
