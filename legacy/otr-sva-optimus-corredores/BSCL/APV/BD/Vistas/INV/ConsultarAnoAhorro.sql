SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::INV.ConsultarAnoAhorro";
 
CREATE VIEW "BSCL.APV.BD.Vistas::INV.ConsultarAnoAhorro"  AS 

SELECT 
"AnoAhorro" 
FROM "BSCL.APV.BD.Vistas::INV.ConsultarBonificacion"
GROUP BY "AnoAhorro";
	   
GRANT SELECT ON "BSCL.APV.BD.Vistas::INV.ConsultarAnoAhorro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 