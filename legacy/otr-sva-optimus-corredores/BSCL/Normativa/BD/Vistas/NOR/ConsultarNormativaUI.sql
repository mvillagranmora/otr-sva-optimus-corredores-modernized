SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarNormativaUI";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarNormativaUI" 
AS 
SELECT
	NOR."IdNormativa",
 	NOR."NombreNormativa",
 	DUI."UI",
 	DUI."IdEstado",
 	NOR."EntidadDestino"
FROM "BSCL.Normativa.BD::NOR.Normativa" AS NOR 
INNER JOIN "BSCL.Normativa.BD::DOC.TipoDocumento" AS DOC 
ON (NOR."IdNormativa"=DOC."IdNormativa")
INNER JOIN "BSCL.Normativa.BD::DOC.TipoDocumentoUI" AS DUI
ON (DOC."IdTipoDocumento"=DUI."IdTipoDocumento")
ORDER BY NOR."IdNormativa", NOR."EntidadDestino"; 

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarNormativaUI"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
