SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::APV.ConsultarRangoDiasTraspaso"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::APV.ConsultarRangoDiasTraspaso"  AS 

SELECT 	 'MAYOR20' AS "Codigo"
	 	,'Mayor a 20 Días' AS "Nombre"
FROM DUMMY
UNION ALL	
SELECT 	 'MENOR20' AS "Codigo"
	 	,'Menor a 20 Días' AS "Nombre"
FROM DUMMY;

GRANT SELECT ON "BSCL.APV.BD.Vistas::APV.ConsultarRangoDiasTraspaso" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 