WITH FFMM as (
	SELECT (CASE WHEN SOL."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(SOL."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END) AS "FechaLiquidacion"
		,SOL."IdPortafolio"
		,SOL."IdMoneda"
		,TO_DECIMAL(0,26,8) as "Monto"
		,TO_DECIMAL(0,26,8) AS "MontoAporte"
		,SOL."CostoMonedaLocal" AS "MontoRescate"
		,'RESCATES_EN_TRANSITO' AS "ItemCalculo"
	/*FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCaja" AS SOL 	
	WHERE SOL."IdPortafolio" IN (:IdPortafolio)
	AND SOL."IdMoneda" IN (:IdMoneda)
	AND SOL."EstadoSolicitud" = 'CRT'
	AND SOL."TipoSolicitud" = 'RES'
	AND SOL."IdTipoMovimientoFondoMutuo" IN ('RESNORM', 'RESREINV')
	AND SOL."IdMedioDePago" = 'CIN'
	AND SOL."AdministracionLiquidez" = 'N'
	AND SOL."IdEmpresa" = :IdEmpresa
	AND SOL."FechaLiquidacion" > :FechaHasta*/
	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCaja" AS SOL
		ON (SAL."IdPortafolio" = SOL."IdPortafolio" AND
			SAL."IdMoneda" = SOL."IdMoneda" AND
			SOL."EstadoSolicitud" = 'CRT' AND
			SOL."TipoSolicitud" = 'RES' AND
			SOL."IdTipoMovimientoFondoMutuo" IN ('RESNORM', 'RESREINV') AND
			SOL."IdMedioDePago" = 'CIN' AND
			SOL."AdministracionLiquidez" = 'N' AND
			SOL."IdEmpresa" = :IdEmpresa AND
			SOL."FechaLiquidacion" > :FechaHasta)
	)
SELECT "IdPortafolio" || '_' || "IdMoneda" AS "Id","FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoAporte") as "MontoCompra",SUM("MontoRescate") as "MontoVenta","ItemCalculo"
,:TipoCalculoFlujoCaja AS "TipoCalculoFlujoCaja"
FROM FFMM GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";