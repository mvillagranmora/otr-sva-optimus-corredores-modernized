WITH ASIG as (

	-- ASIGNACIONES DE VENTAS
	SELECT (CASE WHEN ASIG."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(ASIG."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END) AS "FechaLiquidacion"
	  ,ASIG."IdPortafolio"
	  ,ASIG."IdMonedaLiquidacion" as "IdMoneda"
	  ,TO_DECIMAL(0,26,8) as "Monto"
	  ,TO_DECIMAL(0,26,8) AS "MontoCompra"
	  ,ASIG."MontoTotalAsignadoMonedaLiquidacion" AS "MontoVenta"
	  ,'ASIGNACIONES_VENTAS'  AS "ItemCalculo"	
	   
	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.Orden.BD.Vistas::Flujos.ConsultarAsignacionNoEnviadaFacturarFlujoCaja" ASIG
		ON (SAL."IdPortafolio" = ASIG."IdPortafolio" AND
			SAL."IdMoneda" = ASIG."IdMonedaLiquidacion" AND
			ASIG."FechaLiquidacion" <= :FechaHasta AND
			ASIG."IdEmpresa" = :IdEmpresa)
	WHERE ASIG."Accion" = 'VENTA'
	
	
	UNION ALL
	
	-- ASIGNACIONES DE COMPRA
	SELECT 	
	   (CASE 
	  	  WHEN :TipoCalculoFlujoCaja = 'LIQUIDACION' THEN (CASE WHEN ASIG."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(ASIG."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END)
	  	  ELSE (CASE WHEN ASIG."FechaAsignacionSinHora" >= :FechaHasta THEN ASIG."FechaAsignacionSinHora" ELSE TO_DATE(:FechaDesde) END)
	   END) AS "FechaLiquidacion"			  
			  
	  ,ASIG."IdPortafolio"
	  ,ASIG."IdMonedaLiquidacion" as "IdMoneda"
	  ,TO_DECIMAL(0,26,8) as "Monto"
	  ,ASIG."MontoTotalAsignadoMonedaLiquidacion" AS "MontoCompra"
	  ,TO_DECIMAL(0,26,8) AS "MontoVenta"
	  ,'ASIGNACIONES_COMPRAS' AS "ItemCalculo"	

	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.Orden.BD.Vistas::Flujos.ConsultarAsignacionNoEnviadaFacturarFlujoCaja" ASIG
		ON (SAL."IdPortafolio" = ASIG."IdPortafolio" AND
			SAL."IdMoneda" = ASIG."IdMonedaLiquidacion" AND			
			
			(CASE 
			  	WHEN :TipoCalculoFlujoCaja = 'LIQUIDACION' THEN ASIG."FechaLiquidacion"
			  	ELSE ASIG."FechaAsignacionSinHora"
			 END) <= :FechaHasta AND		  -- Para Compras el calculo puede ser por fecha liquidacion o adquisicion, segun parametro PLA.TipoCalculoFlujoCaja

			ASIG."IdEmpresa" = :IdEmpresa)
	WHERE ASIG."Accion" = 'COMPRA'
	
	)
SELECT "IdPortafolio" || '_' || "IdMoneda" AS "Id","FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta","ItemCalculo"
	 ,:TipoCalculoFlujoCaja AS "TipoCalculoFlujoCaja"
FROM ASIG GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";