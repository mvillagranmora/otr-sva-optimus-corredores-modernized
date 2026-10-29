WITH FFMM as (
	SELECT (CASE WHEN SOL."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(SOL."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END) AS "FechaLiquidacion"
		,SOL."IdPortafolio"
		,SOL."IdMoneda"
		,TO_DECIMAL(0,26,8) as "Monto"
		,SOL."CostoMonedaLocal" AS "MontoAporte"
		,TO_DECIMAL(0,26,8) AS "MontoRescate"
		,'APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA' AS "ItemCalculo"
	/*FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCaja" AS SOL 
	INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarPorComprobanteOrigenLiquidado" INGRESO_EGRESO 
		ON (SOL."IdSolicitudAporteRescate" = INGRESO_EGRESO."IdComprobanteOrigen")
	WHERE SOL."IdPortafolio" IN (:IdPortafolio)
	AND SOL."IdMoneda" IN (:IdMoneda)
	AND SOL."EstadoSolicitud" = 'ING'
	AND SOL."TipoSolicitud" = 'APO'
	AND SOL."IdTipoMovimientoFondoMutuo" IN ('APONORM','APOOVER','APORESPRO')
	AND SOL."IdMedioDePago" != 'CIN'
	AND INGRESO_EGRESO."IdTipoComprobanteOrigen" = 'FMM'
	AND INGRESO_EGRESO."IdEstado" = 'LIQ'
	AND SOL."IdEmpresa" = :IdEmpresa
	AND SOL."FechaLiquidacion" <= :FechaHasta*/
	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCaja" AS SOL
		ON (SAL."IdPortafolio" = SOL."IdPortafolio" AND
			SAL."IdMoneda" = SOL."IdMoneda" AND
			SOL."EstadoSolicitud" IN ('ING','PEN') AND
			SOL."TipoSolicitud" = 'APO' AND
			SOL."IdTipoMovimientoFondoMutuo" IN ('APONORM','APOOVER','APORESPRO','APOREINV') AND
			SOL."IdMedioDePago" != 'CIN' AND
			SOL."IdEmpresa" = :IdEmpresa AND
			SOL."FechaLiquidacion" <= :FechaHasta)
	INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarPorComprobanteOrigenLiquidado" INGRESO_EGRESO 
		ON (SOL."IdSolicitudAporteRescate" = INGRESO_EGRESO."IdComprobanteOrigen" AND
			INGRESO_EGRESO."IdTipoComprobanteOrigen" = 'FMM' AND
			INGRESO_EGRESO."IdEstado" = 'LIQ')
	)
SELECT "IdPortafolio" || '_' || "IdMoneda" AS "Id","FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoAporte") as "MontoCompra",SUM("MontoRescate") as "MontoVenta","ItemCalculo"
,:TipoCalculoFlujoCaja AS "TipoCalculoFlujoCaja"
FROM FFMM GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";