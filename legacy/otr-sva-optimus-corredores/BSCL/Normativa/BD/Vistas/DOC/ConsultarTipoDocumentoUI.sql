

SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarTipoDocumentoUI"; 
 		
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarTipoDocumentoUI" AS

SELECT
	DOC."IdTipoDocumento",
 	DOC."Descripcion",
 	DOC."IdNormativa",
 	DUI."UI",
 	DUI."IdEstado"
FROM "BSCL.Normativa.BD::DOC.TipoDocumento" AS DOC 
INNER JOIN "BSCL.Normativa.BD::DOC.TipoDocumentoUI" AS DUI
ON (DOC."IdTipoDocumento"=DUI."IdTipoDocumento")
ORDER BY DOC."IdTipoDocumento";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarTipoDocumentoUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  
