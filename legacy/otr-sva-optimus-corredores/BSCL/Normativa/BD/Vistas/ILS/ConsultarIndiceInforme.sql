SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultarIndiceInforme";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultarIndiceInforme" 
AS

SELECT
	IND."IdIndice"
	,IND."Nombre"
	,IC."Orden"
	,IC."IdTipoInforme"
FROM "BSCL.Normativa.BD::ILS.Indice" AS IND
INNER JOIN "BSCL.Normativa.BD::ILS.InformeConfiguracion" AS IC
ON IND."IdIndice" = IC."IdIndice"
WHERE	IND."Estado" = 'VIG'
ORDER BY IC."Orden" ASC;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultarIndiceInforme" TO BCS_DESARROLLADOR WITH GRANT OPTION;