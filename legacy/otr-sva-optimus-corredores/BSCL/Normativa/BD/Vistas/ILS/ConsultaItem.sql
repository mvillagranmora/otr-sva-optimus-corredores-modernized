SET SCHEMA "BSCL";
DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaItem";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaItem" AS
SELECT 	I."IdItem",
		I."IdTipoItem",
		I."Nombre" AS "NombreItem",
		I."IdItem" || ' - ' || I."Nombre" AS "IdItemNombre",
		I."NombreCorto" AS "NombreCortoItem", 		
		I."Estado" 
  FROM 	"BSCL.Normativa.BD::ILS.Item" AS I
ORDER BY "IdItem"  
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaItem" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
