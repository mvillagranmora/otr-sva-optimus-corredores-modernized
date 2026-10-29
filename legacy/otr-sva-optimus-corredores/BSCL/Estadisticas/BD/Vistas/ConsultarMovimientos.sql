SET SCHEMA "BSCL";

DROP VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarMovimientos";

CREATE VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarMovimientos"
AS 
	SELECT 1 FROM DUMMY
	;
		 

GRANT SELECT ON "BSCL.Estadisticas.BD.Vistas::ConsultarMovimientos" TO BCS_DESARROLLADOR WITH GRANT OPTION;
