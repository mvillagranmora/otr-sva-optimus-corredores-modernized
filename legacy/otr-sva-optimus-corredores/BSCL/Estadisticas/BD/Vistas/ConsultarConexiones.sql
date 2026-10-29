SET SCHEMA "BSCL";

DROP VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarConexiones";

CREATE VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarConexiones"
AS 
	SELECT 1 FROM DUMMY
	;
		 

GRANT SELECT ON "BSCL.Estadisticas.BD.Vistas::ConsultarConexiones" TO BCS_DESARROLLADOR WITH GRANT OPTION;
