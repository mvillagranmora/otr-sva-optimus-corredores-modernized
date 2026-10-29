WITH 
	Garantia AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,GAR."INGFechaOperacion" AS "FechaOperacion"
				,GAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,GAR."IdClasificacionTipoInstrumento"
				,GAR."IdTipoInstrumento"
				,GAR."IdInstrumento"
				,GAR."Nemotecnico"
				,CAR."Cantidad" AS "CantidadOriginal"
				,GAR."INGCantidad" AS "CantidadGarantia"
				,GAR."IdMonedaInstrumento"
				,CAR."IdEmisor"
				--------------------------------------------------------------------------------------------
				,CAR."INGPrecioPromedioCompra" AS "PrecioPromedioCompra"
				,GAR."MontoPrecioPromedioCompraOperacionOrigen" AS "MontoPromedioCompraMonedaSistema"
				,GAR."MontoPrecioPromedioMonedaAdicionalOperacionOrigen" AS "MontoPromedioCompraMonedaAdicional"				
				,GAR."INGTasaMercadoActual" AS "TasaMercado"
				,GAR."INGPrecioMercadoActual" as "PrecioMercado"
				,GAR."INGMontoATasaMercadoActual" AS "MontoMercadoMonedaSistema"
				,GAR."INGMontoMercadoActualMonedaAdicional" AS "MontoMercadoMonedaAdicional"
				,GAR."INGMontoATasaCompraActual" AS "MontoATasaCompra"
		FROM "BSCL.Custodia.BD::HIS.Garantia" AS GAR
		INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CAR ON CAR."FolioCartera" = GAR."FolioCarteraOrigen" AND CAR."FechaRespaldo" = GAR."FechaRespaldo"
		INNER JOIN (
					SELECT C."FolioCartera",C."IdCliente",C."IdPortafolio"
					FROM "BSCL.Custodia.BD::HIS.Cartera" C
					WHERE 
					C."IdPortafolio" IN (:IdPortafolio)
					AND C."FechaRespaldo" = :FechaHasta
					AND C."IdEstado" = 'LIQ'
					EXCEPT 
					SELECT "FolioCartera","IdCliente","IdPortafolio"
					FROM "BSCL.Custodia.BD::HIS.Cartera"
					WHERE "IdPortafolio" IN (:IdPortafolio)
					AND "FechaRespaldo" = :FechaHasta
					AND "IdClasificacionTipoInstrumento" = 'RV'
				  	AND "IdEmpresa" = :IdEmpresa
				  	AND "IdEstado" = 'VIG'
				   	AND "FechaOperacion" <= :FechaHasta
				  	AND "EGRCantidad" = 0
				 	) AS CEX ON CEX."FolioCartera" = CAR."FolioCartera"
		WHERE 
		  CAR."IdPortafolio" IN (:IdPortafolio)
		  AND GAR."FechaRespaldo" = :FechaHasta
		  AND GAR."IdClasificacionTipoInstrumento" = 'RV'
		  AND GAR."IdEstado" = 'VIG'
		  AND GAR."IdEmpresa" = :IdEmpresa
		  AND GAR."INGFechaOperacion" <= :FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal"
			,"PrecioPromedioCompra"
			,IFNULL(SUM("MontoPromedioCompraMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoPromedioCompraMonedaSistema"
			,IFNULL(SUM("MontoPromedioCompraMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoPromedioCompraMonedaAdicional"
			,"TasaMercado","PrecioMercado"
			,IFNULL(SUM("CantidadGarantia"),TO_DECIMAL(0,26,8)) AS "CantidadGarantia"
			,IFNULL(SUM("MontoMercadoMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaSistema"
			,IFNULL(SUM("MontoMercadoMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoMercadoMonedaAdicional"
			,IFNULL(SUM("MontoATasaCompra"),TO_DECIMAL(0,26,8)) AS "MontoATasaCompra"
	FROM Garantia
	GROUP BY "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","PrecioPromedioCompra"
			,"TasaMercado","PrecioMercado","IdEmisor";