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
				,GAR."INGMontoATasaCompraActual" AS "MontoATasaCompra"
		FROM "BSCL.Custodia.BD::GTA.Garantia" AS GAR
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON CAR."FolioCartera" = GAR."FolioCarteraOrigen"
		INNER JOIN (
					SELECT C."FolioCartera",C."IdCliente",C."IdPortafolio"
					FROM "BSCL.Custodia.BD::CUS.Cartera" C
					WHERE 
					C."IdPortafolio" IN (:IdPortafolio)
					AND C."IdEstado" = 'LIQ'
					EXCEPT 
					SELECT "FolioCartera","IdCliente","IdPortafolio"
					FROM "BSCL.Custodia.BD::CUS.Cartera"
					WHERE "IdPortafolio" IN (:IdPortafolio)
					AND "IdClasificacionTipoInstrumento" = 'RV'
				  	AND "IdEmpresa" = :IdEmpresa
				  	AND "IdEstado" = 'VIG'
				   	AND "FechaOperacion" <= :FechaHasta
				  	AND "EGRCantidad" = 0
				 	) AS CEX ON CEX."FolioCartera" = CAR."FolioCartera"
		WHERE 
		  CAR."IdPortafolio" IN (:IdPortafolio)
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
			,IFNULL(SUM("CantidadGarantia"),TO_DECIMAL(0,26,8)) AS "CantidadGarantia"
			,IFNULL(SUM("MontoATasaCompra"),TO_DECIMAL(0,26,8)) AS "MontoATasaCompra"
			,"IdEmisor"
	FROM Garantia
	GROUP BY "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","PrecioPromedioCompra","IdEmisor";