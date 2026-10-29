SET SCHEMA "BSCL";

DROP VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarHitsWebService";

CREATE VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarHitsWebService"
AS 
	SELECT 1 FROM DUMMY
	;
		 

GRANT SELECT ON "BSCL.Estadisticas.BD.Vistas::ConsultarHitsWebService" TO BCS_DESARROLLADOR WITH GRANT OPTION;
