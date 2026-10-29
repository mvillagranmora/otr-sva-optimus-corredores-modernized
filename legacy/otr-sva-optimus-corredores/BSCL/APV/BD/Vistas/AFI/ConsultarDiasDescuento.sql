SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarDiasDescuento"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::AFI.ConsultarDiasDescuento"  AS 

SELECT 	"ValorParametro" AS "ValorDia"
	FROM  "BSCL.APV.BD::APV.Parametro" 
 		WHERE "NombreParametro" = 'DIA_DESCUENTO';

GRANT SELECT ON "BSCL.APV.BD.Vistas::AFI.ConsultarDiasDescuento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 