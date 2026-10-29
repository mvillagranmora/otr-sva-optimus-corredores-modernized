--VISTA-FUNCION PARA OBTENER MATRIZ, NO MODIFICAR!
SET SCHEMA "BSCL";

DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatriz";
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatriz" AS 
SELECT "IdPortafolio",
	CASE WHEN COALESCE(PORT."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(PORT."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador",
	CASE WHEN COALESCE(PORT."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MAT."EsADC",'N') ELSE COALESCE(MAT_NOAPV."EsADC",'N') END AS "EsADC"
	FROM "BSCL.Portafolio.BD::POR.Portafolio" AS PORT
 	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MAT ON MAT."IdMatrizAgrupadorAPV" = PORT."IdMatrizAgrupadorAPV"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = PORT."CodigoPortafolio" AND  PORT."IdMatrizAgrupadorAPV" = 0;
 
		 
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatriz" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
