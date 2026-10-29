SET SCHEMA "BSCL";
DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaConcepto";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaConcepto" AS
SELECT 	C."IdConcepto",
		C."Nombre" 		AS "NombreConcepto",
		C."IdConcepto" || ' - ' || C."Nombre" AS "IdConceptoNombre",
		C."NombreCorto" AS "NombreCortoConcepto", 		
		C."Estado" 
  FROM 	"BSCL.Normativa.BD::ILS.Concepto" AS C
ORDER BY C."IdConcepto"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaConcepto" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
