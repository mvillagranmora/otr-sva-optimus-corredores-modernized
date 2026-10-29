SELECT 
TO_BIGINT(0) AS "id"
,'INFORMACION' AS "tipo"
,"Codigo" AS "codigo" 
,"Descripcion" AS "descripcion"
FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_InformacionDetalle"
UNION ALL
SELECT
TO_BIGINT(0) AS "id"
,CASE WHEN "RequiereAutorizacion" = 'S' THEN 'AUTORIZACION' ELSE 'ADVERTENCIA' END AS "tipo"
,"Codigo" AS "codigo" 
,"Descripcion" AS "descripcion"
FROM "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_AdvertenciasDetalle"
UNION ALL
SELECT 
TO_BIGINT(0) AS "id"
,'ERROR' AS "tipo"
,"Codigo" AS "codigo" 
,"Descripcion" AS "descripcion"
FROM "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_ErroresDetalle"