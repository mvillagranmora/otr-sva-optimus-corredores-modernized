SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.UIConsultarSolicitudParametros"; 
GO		
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.UIConsultarSolicitudParametros" AS

SELECT
	 PAR."IdSolicitudParametros",
	 PAR."IdSolicitudCircular",
	 PAR."TipoParametro",
	 PAR."Valor",
	 COALESCE(TPAR."Nombre",PAR."TipoParametro")  AS "Nombre",
	 COALESCE(TPAR."EsMasivo",'N')  AS "EsMasivo",
	 COALESCE(TPAR."EsInterno",'N')  AS "EsInterno"
FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" AS PAR 
LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.TipoParametros" TPAR
ON (TPAR."TipoParametro" =PAR."TipoParametro")
WHERE COALESCE(TPAR."EsMasivo",'N') ='N'  AND  PAR."Valor" <> ''
UNION ALL
SELECT
	 MAX(PAR."IdSolicitudParametros"),
	 PAR."IdSolicitudCircular",
	 PAR."TipoParametro",
	 '' AS "Valor",
	 MAX(COALESCE(TPAR."Nombre",PAR."TipoParametro"))  AS "Nombre",
	 MAX(COALESCE(TPAR."EsMasivo",'N'))  AS "EsMasivo",
	 MAX(COALESCE(TPAR."EsInterno",'N'))  AS "EsInterno"
FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarSolicitudParametros" AS PAR 
LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.TipoParametros" TPAR
ON (TPAR."TipoParametro" =PAR."TipoParametro")
WHERE COALESCE(TPAR."EsMasivo",'N') ='S' AND  PAR."Valor" <> ''
GROUP BY PAR."IdSolicitudCircular",PAR."TipoParametro"
ORDER BY PAR."IdSolicitudCircular";
GO

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.UIConsultarSolicitudParametros" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  




