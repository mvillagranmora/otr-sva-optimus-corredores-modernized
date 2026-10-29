WITH SMES as (
	SELECT (CASE WHEN MES."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(MES."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END) AS "FechaLiquidacion"
		,MES."IdPortafolio"
		,MES."IdMonedaLiquidacion" AS "IdMoneda"
		,TO_DECIMAL(0,26,8) AS "Monto"
		,CASE WHEN instr(MES."IdTipoOperacion",'COM') = 1 THEN MES."MontoOperacion" ELSE TO_DECIMAL(0,26,8) END "MontoCompra"
		,CASE WHEN instr(MES."IdTipoOperacion",'VEN') = 1 THEN MES."MontoOperacion" ELSE TO_DECIMAL(0,26,8) END "MontoVenta"					
		,CASE WHEN MES."IdTipoOperacion" = 'COMPAC' THEN 'COMPRA_PACTO'
		  WHEN MES."IdTipoOperacion" = 'COMTER' THEN 'COMPRA_TERMINO'
		  WHEN MES."IdTipoOperacion" = 'COMFWD' THEN 'COMPRA_FORWARD'												  
		  WHEN MES."IdTipoOperacion" = 'VENPAC' THEN 'VENTA_PACTO'
		  WHEN MES."IdTipoOperacion" = 'VENTER' THEN 'VENTA_TERMINO'
		  WHEN MES."IdTipoOperacion" = 'VENFWD' THEN 'VENTA_FORWARD'
		  END AS "ItemCalculo"
	/*FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" AS MES 
	WHERE MES."IdPortafolio" IN (:IdPortafolio)
	AND MES."IdMonedaLiquidacion" IN (:IdMoneda)
	AND MES."IdEmpresa" = :IdEmpresa
	AND MES."IdEstado"  = 'PEN'
	AND MES."FechaLiquidacion" <= :FechaHasta*/
	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" AS MES
		ON (SAL."IdPortafolio" = MES."IdPortafolio" AND
			SAL."IdMoneda" = MES."IdMonedaLiquidacion" AND
			MES."IdEmpresa" = :IdEmpresa AND
			MES."IdEstado" = 'PEN' AND
			MES."FechaLiquidacion" <= :FechaHasta)
	)
SELECT "IdPortafolio" || '_' || "IdMoneda" AS "Id","FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta","ItemCalculo"
,:TipoCalculoFlujoCaja AS "TipoCalculoFlujoCaja"
FROM SMES GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";