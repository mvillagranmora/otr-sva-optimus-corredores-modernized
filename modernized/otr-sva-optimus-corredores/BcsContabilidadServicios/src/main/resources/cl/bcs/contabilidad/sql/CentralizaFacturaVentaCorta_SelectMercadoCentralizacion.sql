SELECT CASE WHEN  "Nemo" = 'FactVCRV' THEN 'RV' 
	 WHEN  "Nemo" = 'FactVCRF' THEN 'RF' 
	 ELSE '' END AS "ClasificacionTipoInstrumento" 
FROM "BSCL.Contabilidad.BD::ITZ.CentralizacionTipo"
WHERE "IdTipoCentralizacion" = :IdTipoCentralizacion;