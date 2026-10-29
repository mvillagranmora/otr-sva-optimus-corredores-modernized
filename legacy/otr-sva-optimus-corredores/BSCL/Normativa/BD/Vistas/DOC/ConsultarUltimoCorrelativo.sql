SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarUltimoCorrelativo";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarUltimoCorrelativo"
AS

SELECT
	ResumenGeneracion."IdDocumento",
	ResumenGeneracion."UltimoCorrelativo"
FROM
(
	SELECT
	    "IdDocumento",
	    MAX("ID") AS "UltimoID"
	FROM
		"BSCL.Normativa.BD::TRI.ResumenGeneracion"
	GROUP BY
		"IdDocumento"
) AS Query
INNER JOIN
	"BSCL.Normativa.BD::TRI.ResumenGeneracion" AS ResumenGeneracion ON ResumenGeneracion."ID" = Query."UltimoID"
ORDER BY
	ResumenGeneracion."IdDocumento";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarUltimoCorrelativo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;