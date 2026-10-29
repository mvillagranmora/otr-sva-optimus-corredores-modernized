WITH ORD as (

	-- ORDENES EN TRANSITO DE COMPRA
	SELECT 
		 (CASE 
		  	WHEN :TipoCalculoFlujoCaja = 'LIQUIDACION' THEN CASE WHEN ORD."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(ORD."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END
		  	ELSE  CASE WHEN ORD."FechaOperacionSinHora" >= :FechaHasta THEN ORD."FechaOperacionSinHora" ELSE TO_DATE(:FechaDesde) END
		  END) AS "FechaLiquidacion"		  

		  ,ORD."IdPortafolio"
		  ,ORD."IdMonedaLiquidacion" as "IdMoneda"
		  ,ORD."Accion"
		  ,TO_DECIMAL(0,26,8) as "Monto"					  
		  ,ORD."MontoTotalOrdenMonedaLiquidacion" AS "MontoCompra"
		  ,TO_DECIMAL(0,26,8) as "MontoVenta"
	  
	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenIngresadaFlujoCaja" ORD
		ON (	SAL."IdPortafolio" = ORD."IdPortafolio" 
			AND SAL."IdMoneda" = ORD."IdMonedaLiquidacion" 

		  	AND (CASE 
		  		WHEN :TipoCalculoFlujoCaja = 'LIQUIDACION' THEN ORD."FechaLiquidacion"
		  		ELSE  ORD."FechaOperacionSinHora"
		  	END) <= :FechaHasta 	-- Para Compras el calculo puede ser por fecha liquidacion o adquisicion, segun parametro PLA.TipoCalculoFlujoCaja
		  
		  	AND ORD."IdEmpresa" = :IdEmpresa)
	WHERE ORD."Accion" = 'COMPRA'	
			
	-- ORDENES EN TRANSITO DE VENTA
	UNION ALL

	SELECT 			
		   CASE 
				WHEN ORD."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(ORD."FechaLiquidacion") 
				ELSE TO_DATE(:FechaDesde) 
		   END AS "FechaLiquidacion"

		  ,ORD."IdPortafolio"
		  ,ORD."IdMonedaLiquidacion" as "IdMoneda"
		  ,ORD."Accion"
		  ,TO_DECIMAL(0,26,8) as "Monto"					  
		  ,TO_DECIMAL(0,26,8) as "MontoCompra"
		  ,ORD."MontoTotalOrdenMonedaLiquidacion" as "MontoVenta"
	  
	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenIngresadaFlujoCaja" ORD
		ON (SAL."IdPortafolio" = ORD."IdPortafolio" AND
			SAL."IdMoneda" = ORD."IdMonedaLiquidacion" AND
			ORD."FechaLiquidacion" <= :FechaHasta AND		  
			ORD."IdEmpresa" = :IdEmpresa)
	WHERE ORD."Accion" = 'VENTA'		

	
	
	
	-- ORDENES EN TRANSITO DE COMPRA (Pendientes de Certificar)
	UNION ALL

	
	SELECT 	
			(CASE 
			  	WHEN :TipoCalculoFlujoCaja = 'LIQUIDACION' THEN (CASE WHEN ORD."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(ORD."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END)
			  	ELSE (CASE WHEN ORD."FechaOperacionSinHora" >= :FechaHasta THEN ORD."FechaOperacionSinHora" ELSE TO_DATE(:FechaDesde) END)
			 END) AS "FechaLiquidacion" 
			  
		  ,ORD."IdPortafolio"
		  ,ORD."IdMonedaLiquidacion" as "IdMoneda"
		  ,ORD."Accion"
		  ,TO_DECIMAL(0,26,8)  as "Monto"
		  ,ORD."MontoTotalOrdenMonedaLiquidacion"  as "MontoCompra"
		  ,TO_DECIMAL(0,26,8)  as "MontoVenta"
		  
	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenPendienteAutorizacionFlujoCaja" ORD
		ON (SAL."IdPortafolio" = ORD."IdPortafolio" AND
			SAL."IdMoneda" = ORD."IdMonedaLiquidacion" AND
			
			 (CASE 
			  	WHEN :TipoCalculoFlujoCaja = 'LIQUIDACION' THEN ORD."FechaLiquidacion"
			  	ELSE ORD."FechaOperacionSinHora"
			  END) <= :FechaHasta AND		  -- Para Compras el calculo puede ser por fecha liquidacion o adquisicion, segun parametro PLA.TipoCalculoFlujoCaja
		  
			ORD."IdEmpresa" = :IdEmpresa)
	WHERE ORD."Accion" = 'COMPRA'
	
	-- ORDENES EN TRANSITO DE VENTA (Pendientes de Certificar)
	UNION ALL
	
		SELECT	
			   (CASE WHEN ORD."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(ORD."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END) AS "FechaLiquidacion"	
			  ,ORD."IdPortafolio"
			  ,ORD."IdMonedaLiquidacion" as "IdMoneda"
			  ,ORD."Accion"
			  ,TO_DECIMAL(0,26,8)  as "Monto"
			  ,TO_DECIMAL(0,26,8) as "MontoCompra"
			  ,ORD."MontoTotalOrdenMonedaLiquidacion" as "MontoVenta"
			  
		FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
		INNER JOIN "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenPendienteAutorizacionFlujoCaja" ORD
			ON (SAL."IdPortafolio" = ORD."IdPortafolio" AND
				SAL."IdMoneda" = ORD."IdMonedaLiquidacion" AND
				ORD."FechaLiquidacion" <= :FechaHasta AND		  
				ORD."IdEmpresa" = :IdEmpresa)
		WHERE ORD."Accion" = 'VENTA'
	
)
	
	
	
SELECT "IdPortafolio" || '_' || "IdMoneda" AS "Id","FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta",
		CASE WHEN "Accion" = 'COMPRA' THEN 'ORDENES_EN_TRANSITO_COMPRAS'
			 WHEN "Accion" = 'VENTA' THEN 'ORDENES_EN_TRANSITO_VENTAS'
		 END "ItemCalculo"
		,:TipoCalculoFlujoCaja AS "TipoCalculoFlujoCaja"

FROM ORD
GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","Accion";