SET SCHEMA "BSCL";

DROP VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarOrdenes";

CREATE VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarOrdenes"
AS 
	SELECT 1 FROM DUMMY
	;
		 

GRANT SELECT ON "BSCL.Estadisticas.BD.Vistas::ConsultarOrdenes" TO BCS_DESARROLLADOR WITH GRANT OPTION;
