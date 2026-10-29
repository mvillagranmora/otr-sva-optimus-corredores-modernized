WITH FAC as (

	-- MOVIMIENTOS POR FACTURAR DE VENTAS Y COMISION DE SALIDA DE FFMM
		SELECT (
		CASE 
			WHEN (:TipoCalculoFlujoCajaSpot = 'LIQUIDACION' AND MC."IdTipoInstrumento" = 'SPT') THEN (CASE WHEN TO_DATE(MC."FechaLiquidacion") >= :FechaHasta THEN TO_DATE(MC."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END) 
			WHEN (:TipoCalculoFlujoCajaSpot = 'INGRESO' AND MC."IdTipoInstrumento" = 'SPT' AND SAL."IdMoneda" != MC."IdMonedaSpot") THEN (CASE WHEN MC."FechaSistemaSinHora" >= :FechaHasta THEN MC."FechaSistemaSinHora" ELSE TO_DATE(:FechaDesde) END)																											  
			ELSE (CASE WHEN TO_DATE(MC."FechaLiquidacion") >= :FechaHasta THEN TO_DATE(MC."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END)				
		END
		) AS "FechaLiquidacion"
		,MC."IdPortafolio"
		,SAL."IdMoneda" as "IdMoneda"
		,CASE 
			WHEN MC."AccionTipoOperacion" = 'RESCATE' THEN MC."MontoFinal" ELSE TO_DECIMAL(0,26,8)
		END AS "Monto"
		,CASE WHEN MC."IdTipoInstrumento" = 'SPT' THEN 
			CASE WHEN SAL."IdMoneda" = MC."IdMonedaSpot" THEN 
				CASE WHEN MC."AccionTipoOperacion" = 'COMPRA' THEN MC."Cantidad" ELSE TO_DECIMAL(0,26,8) END
			ELSE 
				CASE WHEN MC."AccionTipoOperacion" = 'VENTA' THEN MC."MontoFinal" ELSE TO_DECIMAL(0,26,8) END
			END
		 ELSE 
		 	CASE WHEN MC."AccionTipoOperacion" = 'COMPRA' THEN MC."MontoFinal" ELSE TO_DECIMAL(0,26,8) END 
		 END AS "MontoCompra"
		,CASE WHEN MC."IdTipoInstrumento" = 'SPT' THEN 
			CASE WHEN SAL."IdMoneda" = MC."IdMonedaSpot" THEN 
				CASE WHEN MC."AccionTipoOperacion" = 'VENTA' THEN MC."Cantidad" ELSE TO_DECIMAL(0,26,8) END
			ELSE 
				CASE WHEN MC."AccionTipoOperacion" = 'COMPRA' THEN MC."MontoFinal" ELSE TO_DECIMAL(0,26,8) END
			END
		 ELSE 
		 	CASE WHEN MC."AccionTipoOperacion" = 'VENTA' THEN MC."MontoFinal" ELSE TO_DECIMAL(0,26,8) END
		 END AS "MontoVenta"
		,CASE WHEN MC."IdTipoInstrumento" = 'SPT' AND  SAL."IdMoneda" != MC."IdMonedaSpot" THEN 
			CASE WHEN MC."AccionTipoOperacion" = 'VENTA' THEN 'MOVIMIENTOS_FACTURAR_COMPRAS'
			WHEN MC."AccionTipoOperacion" = 'COMPRA' THEN 'MOVIMIENTOS_FACTURAR_VENTAS'
			END
		ELSE
			CASE WHEN MC."AccionTipoOperacion" = 'COMPRA' THEN 'MOVIMIENTOS_FACTURAR_COMPRAS'
			WHEN MC."AccionTipoOperacion" = 'VENTA' THEN 'MOVIMIENTOS_FACTURAR_VENTAS'
			WHEN MC."AccionTipoOperacion" = 'RESCATE' THEN 'MOVIMIENTOS_FACTURAR_COMISION_SALIDA_FFM'
			END
		END	AS "ItemCalculo"
	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFlujoCaja" AS MC
		ON (SAL."IdPortafolio" = MC."IdPortafolio" AND
			(SAL."IdMoneda" = MC."IdMonedaLiquidacion" OR (MC."IdTipoInstrumento" = 'SPT' AND SAL."IdMoneda" = MC."IdMonedaSpot")) AND
			MC."IdEstadoFacturacion" = 'ING' AND
			MC."IdEmpresa" = :IdEmpresa AND			
			(CASE 
			  	WHEN (:TipoCalculoFlujoCajaSpot = 'LIQUIDACION' AND MC."IdTipoInstrumento" = 'SPT') THEN MC."FechaLiquidacion"
			  	WHEN (:TipoCalculoFlujoCajaSpot = 'INGRESO' AND MC."IdTipoInstrumento" = 'SPT' AND SAL."IdMoneda" != MC."IdMonedaSpot") THEN MC."FechaSistemaSinHora"
			  	ELSE MC."FechaLiquidacion"
			 END) <= :FechaHasta			)
	AND MC."AccionTipoOperacion" IN ('VENTA', 'RESCATE')
	
	
	UNION ALL
	
	-- MOVIMIENTOS POR FACTURAR DE COMPRAS
	SELECT 	
		(
		CASE
			WHEN (:TipoCalculoFlujoCajaSpot = 'LIQUIDACION' AND MC."IdTipoInstrumento" = 'SPT') THEN (CASE WHEN MC."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(MC."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END)
		  	WHEN (:TipoCalculoFlujoCajaSpot = 'INGRESO' AND MC."IdTipoInstrumento" = 'SPT' AND SAL."IdMoneda" = MC."IdMonedaSpot") THEN (CASE WHEN MC."FechaSistemaSinHora" >= :FechaHasta THEN MC."FechaSistemaSinHora" ELSE TO_DATE(:FechaDesde) END)
		  	WHEN (:TipoCalculoFlujoCajaSpot = 'INGRESO' AND MC."IdTipoInstrumento" = 'SPT' AND SAL."IdMoneda" != MC."IdMonedaSpot") THEN (CASE WHEN MC."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(MC."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END)
		  	WHEN (MC."IdTipoInstrumento" != 'SPT') THEN 
		  		(CASE
		  			WHEN :TipoCalculoFlujoCaja = 'LIQUIDACION' THEN (CASE WHEN MC."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(MC."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END)
		  			WHEN :TipoCalculoFlujoCaja = 'ADQUISICION' THEN (CASE WHEN MC."FechaSistemaSinHora" >= :FechaHasta THEN MC."FechaSistemaSinHora" ELSE TO_DATE(:FechaDesde) END)			  			
		  			ELSE (CASE WHEN MC."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(MC."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END)
	  			END)		  		
		  	ELSE (CASE WHEN MC."FechaLiquidacion" >= :FechaHasta THEN TO_DATE(MC."FechaLiquidacion") ELSE TO_DATE(:FechaDesde) END)
		 END
		) AS "FechaLiquidacion"
			 
		,MC."IdPortafolio"
		,SAL."IdMoneda" as "IdMoneda"
		,TO_DECIMAL(0,26,8) as "Monto"
		,CASE WHEN MC."IdTipoInstrumento" = 'SPT' THEN 
			CASE WHEN SAL."IdMoneda" = MC."IdMonedaSpot" THEN 
				CASE WHEN MC."AccionTipoOperacion" = 'COMPRA' THEN MC."Cantidad" ELSE TO_DECIMAL(0,26,8) END
			ELSE 
				CASE WHEN MC."AccionTipoOperacion" = 'VENTA' THEN MC."MontoFinal" ELSE TO_DECIMAL(0,26,8) END
			END
		 ELSE 
		 	CASE WHEN MC."AccionTipoOperacion" = 'COMPRA' THEN MC."MontoFinal" ELSE TO_DECIMAL(0,26,8) END
		 END AS "MontoCompra"
		,CASE WHEN MC."IdTipoInstrumento" = 'SPT' THEN 
			CASE WHEN SAL."IdMoneda" = MC."IdMonedaSpot" THEN 
				CASE WHEN MC."AccionTipoOperacion" = 'VENTA' THEN MC."Cantidad" ELSE TO_DECIMAL(0,26,8) END
			ELSE 
				CASE WHEN MC."AccionTipoOperacion" = 'COMPRA' THEN MC."MontoFinal" ELSE TO_DECIMAL(0,26,8) END
			END
		 ELSE 
		 	CASE WHEN MC."AccionTipoOperacion" = 'VENTA' THEN MC."MontoFinal" ELSE TO_DECIMAL(0,26,8) END
		 END AS "MontoVenta"
		,CASE WHEN MC."IdTipoInstrumento" = 'SPT' AND  SAL."IdMoneda" != MC."IdMonedaSpot" THEN 
			CASE WHEN MC."AccionTipoOperacion" = 'VENTA' THEN 'MOVIMIENTOS_FACTURAR_COMPRAS'
			WHEN MC."AccionTipoOperacion" = 'COMPRA' THEN 'MOVIMIENTOS_FACTURAR_VENTAS'
			END
		ELSE		
			CASE WHEN MC."AccionTipoOperacion" = 'COMPRA' THEN 'MOVIMIENTOS_FACTURAR_COMPRAS'
			WHEN MC."AccionTipoOperacion" = 'VENTA' THEN 'MOVIMIENTOS_FACTURAR_VENTAS'
			END
		END	AS "ItemCalculo"
	FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFlujoCaja" AS MC
		ON (SAL."IdPortafolio" = MC."IdPortafolio" AND
			(SAL."IdMoneda" = MC."IdMonedaLiquidacion" OR (MC."IdTipoInstrumento" = 'SPT' AND SAL."IdMoneda" = MC."IdMonedaSpot")) AND
			MC."IdEstadoFacturacion" = 'ING' AND
			MC."IdEmpresa" = :IdEmpresa AND			
			(CASE
				WHEN (:TipoCalculoFlujoCajaSpot = 'LIQUIDACION' AND MC."IdTipoInstrumento" = 'SPT') THEN MC."FechaLiquidacion"
			  	WHEN (:TipoCalculoFlujoCajaSpot = 'INGRESO' AND MC."IdTipoInstrumento" = 'SPT' AND SAL."IdMoneda" = MC."IdMonedaSpot") THEN MC."FechaSistemaSinHora"
			  	WHEN (:TipoCalculoFlujoCajaSpot = 'INGRESO' AND MC."IdTipoInstrumento" = 'SPT' AND SAL."IdMoneda" != MC."IdMonedaSpot") THEN MC."FechaLiquidacion"
			  	WHEN (MC."IdTipoInstrumento" != 'SPT') THEN 
			  		(CASE
			  			WHEN :TipoCalculoFlujoCaja = 'LIQUIDACION' THEN MC."FechaLiquidacion"
			  			WHEN :TipoCalculoFlujoCaja = 'ADQUISICION' THEN MC."FechaSistemaSinHora"			  			
			  			ELSE MC."FechaLiquidacion"
		  			END)		  		
			  	ELSE MC."FechaLiquidacion"
			 END) <= :FechaHasta		  -- Para Compras el calculo puede ser por fecha liquidacion o adquisicion, segun parametro PLA.TipoCalculoFlujoCaja
			)
	AND MC."AccionTipoOperacion" = 'COMPRA'
			
)
SELECT "IdPortafolio" || '_' || "IdMoneda" AS "Id","FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta","ItemCalculo"
	 ,:TipoCalculoFlujoCaja AS "TipoCalculoFlujoCaja"
FROM FAC GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";