SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::INV.ConsultarAnoProceso";
 
CREATE VIEW "BSCL.APV.BD.Vistas::INV.ConsultarAnoProceso"  AS 

SELECT 
"AnoProceso" 
FROM "BSCL.APV.BD.Vistas::INV.ConsultarBonificacion"
GROUP BY "AnoProceso";
	   
GRANT SELECT ON "BSCL.APV.BD.Vistas::INV.ConsultarAnoProceso" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 