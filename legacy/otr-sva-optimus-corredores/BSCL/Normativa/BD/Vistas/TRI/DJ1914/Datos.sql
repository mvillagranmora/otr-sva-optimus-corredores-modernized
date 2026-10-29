SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1914.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1914.Datos" (
    "ID"
	,"IdDocumento"
	,"VboUsuario"
	,"Observaciones"
	,"FuenteDatos"
	,"IdCliente"
	,"Numero"
	,"RutCliente"
	,"TipoInstrumento"
	,"NumeroIdentificadorInversion"
	,"FechaInversionOriginal"
	,"RetiroInversion54BISMontoNominal"
	,"RetiroInversion54BISInteresPositivo"
	,"RetiroInversion54BISInteresNegativo"
	,"NumeroCertificado"

) AS
SELECT
    a."ID"
	,a."IdDocumento"
	,a."VboUsuario"
	,COALESCE(VAL."CodigoValidacion", a."Observaciones") AS "Observaciones"
	,a."FuenteDatos"
	,a."IdCliente"
	,a."Numero"
	,a."RutCliente"
	,a."TipoInstrumento"
	,a."NumeroIdentificadorInversion"
	,a."FechaInversionOriginal"
	,a."RetiroInversion54BISMontoNominal"
	,a."RetiroInversion54BISInteresPositivo"
	,a."RetiroInversion54BISInteresNegativo"
	,a."NumeroCertificado"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1914" a
LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"ID"
			FROM (
			    SELECT 
			        R."CodigoValidacion", DT."IdDocumento", DT."ID", 
			        ROW_NUMBER() OVER (
			            PARTITION BY DT."ID" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1914" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."ID" = VAL."ID"
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1914.Datos" TO bcs_desarrollador WITH GRANT OPTION;
