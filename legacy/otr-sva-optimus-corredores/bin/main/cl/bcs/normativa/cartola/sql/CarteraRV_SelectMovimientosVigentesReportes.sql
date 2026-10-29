WITH 
	Reporte AS (
		SELECT 	 CAR."IdCliente"
				,CAR."IdPortafolio"
				,CAR."FolioCartera"
				,REP."INGFechaOperacion" AS "FechaOperacion"
				,CAR."INGFechaLiquidacion" as "FechaLiquidacion"
				,CAR."IdBoveda"
				,REP."IdClasificacionTipoInstrumento"
				,REP."IdTipoInstrumento"
				,REP."IdInstrumento"
				,REP."Nemotecnico"
				,CAR."INGCantidad" AS "CantidadOriginal"
				,REP."Cantidad" AS "CantidadReporte"
				,REP."IdMonedaInstrumento"
				--------------------------------------------------------------------------------------------
				,CAR."INGPrecioPromedioCompra" AS "PrecioPromedioCompra"
				,REP."MontoPrecioPromedioCompraOperacionRelacionada" AS "MontoPromedioCompraMonedaSistema"
				,REP."MontoPrecioPromedioMonedaAdicionalOperacionRelacionada" AS "MontoPromedioCompraMonedaAdicional"
				,REP."INGMontoATasaCompraActual" AS "MontoATasaCompra"
				,CAR."IdEmisor"
		FROM "BSCL.Custodia.BD::CUS.Subyacente" REP
		INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."FolioCartera" = REP."FolioCarteraRelacionada"
		INNER JOIN (
					SELECT C."IdCartera",C."IdCliente",C."IdPortafolio"
					FROM "BSCL.Custodia.BD::CUS.Cartera" C
					WHERE 
					  C."IdPortafolio" IN (:IdPortafolio)
					  AND C."IdEmpresa" = :IdEmpresa
					  AND C."IdTipoInstrumento" IN ('RRV','RRF')
					  AND C."IdEstado" = 'LIQ'
					EXCEPT 
					SELECT "IdCartera","IdCliente","IdPortafolio"
					FROM "BSCL.Custodia.BD::CUS.Cartera"
					WHERE
					"IdPortafolio" IN (:IdPortafolio)
					AND "IdClasificacionTipoInstrumento" = 'RV'
				  	AND "IdEmpresa" = :IdEmpresa
				  	AND "IdEstado" = 'VIG'
				   	AND "FechaOperacion" <= :FechaHasta
				  	AND "EGRCantidad" = 0
					AND "IdTipoInstrumento" IN ('RRV','RRF')
				 	) AS CEX ON CEX."IdCartera" = CAR."IdCartera"
		WHERE 
		  CAR."IdPortafolio" IN (:IdPortafolio)
		  AND REP."IdClasificacionTipoInstrumento" = 'RV'
		  AND REP."IdEstado" = 'VIG'
		  AND REP."INGFechaOperacion" <= :FechaHasta
	)
	SELECT   "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal"
			,"PrecioPromedioCompra"
			,IFNULL(SUM("MontoPromedioCompraMonedaSistema"),TO_DECIMAL(0,26,8)) AS "MontoPromedioCompraMonedaSistema"
			,IFNULL(SUM("MontoPromedioCompraMonedaAdicional"),TO_DECIMAL(0,26,8)) AS "MontoPromedioCompraMonedaAdicional"
			,IFNULL(SUM("CantidadReporte"),TO_DECIMAL(0,26,8)) AS "CantidadReporte"
			,IFNULL(SUM("MontoATasaCompra"),TO_DECIMAL(0,26,8)) AS "MontoATasaCompra"
	FROM Reporte
	GROUP BY "IdCliente","IdPortafolio","IdBoveda","IdClasificacionTipoInstrumento"
			,"IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMonedaInstrumento","CantidadOriginal","PrecioPromedioCompra","IdEmisor";