SELECT 
CASE WHEN  "Nombre" LIKE '%RV%'  THEN 'RV' 
	 WHEN  "Nombre" LIKE '%RF%'  THEN 'RF' 
	 ELSE '' END AS "ClasificacionTipoInstrumento" 
FROM "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo"
WHERE "IdTipoCentralizacion" = :IdTipoCentralizacion;