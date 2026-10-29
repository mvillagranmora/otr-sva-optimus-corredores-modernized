

SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarTipoDocumento"; 
 		
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarTipoDocumento" AS

SELECT
	DOC."IdTipoDocumento",
 	DOC."Descripcion",
 	DOC."IdNormativa",
 	DOC."UI_Visor",
 	DOC."UI_Emision",
 	DOC."Esquema"
FROM "BSCL.Normativa.BD::DOC.TipoDocumento" AS DOC 
ORDER BY DOC."IdTipoDocumento";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarTipoDocumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  
