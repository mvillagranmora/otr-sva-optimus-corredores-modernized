SET SCHEMA "BSCL";

DROP VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarAsientos";

CREATE VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarAsientos"
AS 
	SELECT 1 FROM DUMMY
	;
		 

GRANT SELECT ON "BSCL.Estadisticas.BD.Vistas::ConsultarAsientos" TO BCS_DESARROLLADOR WITH GRANT OPTION;
