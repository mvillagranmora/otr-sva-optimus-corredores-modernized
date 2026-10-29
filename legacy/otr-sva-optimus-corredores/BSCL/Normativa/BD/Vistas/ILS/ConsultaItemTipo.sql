SET SCHEMA "BSCL";
DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaItemTipo";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaItemTipo" AS
	SELECT	IT."IdTipoItem",
			IT."Nombre" AS "NombreItemTipo",
			IT."Orden" AS "Orden", 		
			IT."Estado"
	  FROM	"BSCL.Normativa.BD::ILS.ItemTipo" AS IT
  ORDER BY 	IT."Orden"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaItemTipo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;