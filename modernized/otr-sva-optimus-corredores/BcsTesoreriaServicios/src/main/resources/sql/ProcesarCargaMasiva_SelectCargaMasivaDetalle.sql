
SELECT
	CMD."IdCargaMasiva",
	CMD."IdCargaMasivaDetalle",
	PROC."CodigoTipoCargaMasiva",	
	CMD."IdTipoMovimiento",
	CMD."DocumentoIdentidad",
	CMD."Monto",
	CMD."IdPersona",
	CMD."IdPortafolio",
	CMD."Glosa",	
	-- Datos para carga "CONCILIACION_RECAUDACION_BCP" (Peru)
	CMD."BCP_CON_FechaTransaccion",
	CMD."BCP_CON_NumeroOperacion",			
	(CASE WHEN COALESCE(IE."IdIngresoEgreso",0) = 0 THEN 'N' ELSE 'S' END) AS "ExisteMovimiento",
	COALESCE(IE."IdIngresoEgreso",0) AS "IdMovimientoExistente"

FROM "BSCL.Tesoreria.BD::GBL.ProcesarCargaMasiva_Proceso" AS PROC
INNER JOIN "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle" AS CMD ON (CMD."IdCargaMasiva" = PROC."IdCargaMasiva")
-- Para CONCILIACION_RECAUDACION_BCP chequea si el movimiento ya fue creado 
LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON 
(
		IE."IdComprobanteOrigen" = CMD."BCP_CON_NumeroOperacion"
	AND	COALESCE(IE."FechaComprobanteOrigen",'0001-01-01') = CAST(COALESCE(CMD."BCP_CON_FechaTransaccion",'0001-01-01') AS DATE)
	AND IE."IdConcepto" = 'RECBCP'
	AND IE."IdEstado" NOT IN ('ANU','BLO')
	AND PROC."CodigoTipoCargaMasiva" = 'CONCILIACION_RECAUDACION_BCP' -- Carga Peru
)		
WHERE PROC."IdCargaMasiva" IN (:IdCargaMasiva);
		
		
		