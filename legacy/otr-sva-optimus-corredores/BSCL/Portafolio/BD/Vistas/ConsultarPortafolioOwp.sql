SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioOwp";
GO

CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioOwp" AS 
SELECT
	CLI."DocumentoIdentidad"
	,PORT."IdPortafolio"
	,PORT."CodigoPortafolio"
	,EST."IdEstado"
	,CLI."DocumentoIdentidadAdicional"
	,PORT."Proposito"
	,PORT."DescripcionPortafolio"
	,PORT."FechaIngreso"
	,CRT."NombrePlan"
	,CRT."Producto"
	,CRT."IdPlanTipoInversion" as "IdPlan"
	,CRT."IdProductoAgrupador" as "IdProducto"
	,CASE WHEN COALESCE(PORT."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(PORT."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
	,CASE WHEN COALESCE(PORT."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE("MAT"."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS PORT
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI on PORT."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON "EST"."IdEstado" = CASE
																			WHEN PORT."IdEstado" IN ('ACT','VIG') THEN 
																			CASE
																				WHEN PORT."IdEstadoAutorizacion" IN ('PEN','') THEN 'PEN'
																				ELSE PORT."IdEstado"
																				END
																			ELSE 'BLO'
																		END
LEFT JOIN "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" CRT ON CRT."IdMatrizAgrupadorAPV" = PORT."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = PORT."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = PORT."CodigoPortafolio" AND  "PORT"."IdMatrizAgrupadorAPV" = 0;
GO

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioOwp" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
