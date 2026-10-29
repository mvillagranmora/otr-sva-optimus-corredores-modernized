
-- Actualiza carga masiva detalle validas y procesadas
UPDATE CMD
SET 
			CMD."IdEstado" = (CASE 
								  WHEN (COALESCE(:IdIngresoEgreso,0) <> 0) THEN 'PRO'
								  ELSE CMD."IdEstado" 
							  END),							  
			CMD."IdMovimientoComprobante" = COALESCE(:IdIngresoEgreso,0),  -- Id del movimiento generado
			CMD."ObservacionValidacion" = (CASE 
											  WHEN (PROC."CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP' AND :ExisteMovimiento = 'S') THEN 'Movimiento ya Existe con folio  ' || CAST(:IdMovimientoExistente AS NVARCHAR(50))
											  ELSE CMD."ObservacionValidacion" 
										   END)
			
FROM "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle" AS CMD
INNER JOIN "BSCL.Tesoreria.BD::GBL.ProcesarCargaMasiva_Proceso" AS PROC ON (PROC."IdCargaMasiva" = CMD."IdCargaMasiva")
WHERE PROC."IdCargaMasiva" 		= :IdCargaMasiva
AND CMD."IdCargaMasivaDetalle" 	= :IdCargaMasivaDetalle;
